package pl.kiosel.common.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WorldEditUtils {

	public static void pasteSchematic(File file, Location loc) {
		if (!file.exists()) return;

		try (FileInputStream fis = new FileInputStream(file)) {
			ClipboardFormat format = ClipboardFormats.findByFile(file);
			if (format == null) return;

			try (ClipboardReader reader = format.getReader(fis)) {
				Clipboard clipboard = reader.read();

				World world = loc.getWorld();
				if (world == null) return;
				com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(world);
				BlockVector3 pastePos = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

				try (EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld)) {
					Operation operation = new ClipboardHolder(clipboard)
							.createPaste(editSession)
							.to(pastePos)
							.ignoreAirBlocks(false)
							.build();
					Operations.complete(operation);
				}
			}
		} catch (IOException | WorldEditException e) {
			e.printStackTrace();
		}
	}
}