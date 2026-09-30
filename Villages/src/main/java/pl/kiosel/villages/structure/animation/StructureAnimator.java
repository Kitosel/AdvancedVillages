package pl.kiosel.villages.structure.animation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class StructureAnimator {

	private static final Comparator<StructureBlock> BUILD_ORDER = Comparator
			.comparingInt(StructureBlock::y)
			.thenComparingInt(StructureBlock::x)
			.thenComparingInt(StructureBlock::z);

	private final Plugin plugin;
	private final Set<Animation> animations = ConcurrentHashMap.newKeySet();

	public StructureAnimator(Plugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}

	public Animation animate(Location origin, Collection<StructureBlock> blocks) {
		return animate(origin, blocks, AnimationSettings.defaults(), () -> {});
	}

	public Animation animate(Location origin, Collection<StructureBlock> blocks,
	                         AnimationSettings settings, Runnable completion) {
		if (!Bukkit.isPrimaryThread())
			throw new IllegalStateException("Structure animations must be started on the server thread");

		Location start = Objects.requireNonNull(origin, "origin").clone();
		Objects.requireNonNull(start.getWorld(), "origin world");
		List<StructureBlock> ordered = new ArrayList<>(Objects.requireNonNull(blocks, "blocks"));
		ordered.replaceAll(StructureBlock::copy);
		ordered.sort(BUILD_ORDER);

		Animation animation = new Animation(
				start, List.copyOf(ordered), Objects.requireNonNull(settings, "settings"),
				Objects.requireNonNull(completion, "completion")
		);
		this.animations.add(animation);
		animation.start();
		return animation;
	}

	public int getActiveCount() {
		return this.animations.size();
	}

	public void cancelAll() {
		for (Animation animation : List.copyOf(this.animations)) animation.cancel();
	}

	public final class Animation implements Runnable {

		private final Location origin;
		private final List<StructureBlock> blocks;
		private final AnimationSettings settings;
		private final Runnable completion;
		private BukkitTask task;
		private int cursor;
		private boolean finished;

		private Animation(Location origin, List<StructureBlock> blocks,
		                  AnimationSettings settings, Runnable completion) {
			this.origin = origin;
			this.blocks = blocks;
			this.settings = settings;
			this.completion = completion;
		}

		private void start() {
			if (this.blocks.isEmpty()) {
				finish(true);
				return;
			}
			this.task = Bukkit.getScheduler().runTaskTimer(
					plugin, this, 0L, this.settings.periodTicks());
		}

		@Override
		public void run() {
			if (this.finished) return;
			int layerY = this.blocks.get(this.cursor).y();
			int placedThisTick = 0;
			while (this.cursor < this.blocks.size() && placedThisTick < this.settings.blocksPerTick()) {
				StructureBlock placement = this.blocks.get(this.cursor);
				if (placement.y() != layerY) break;
				placement.place(this.origin, this.settings.applyPhysics());
				this.cursor++;
				placedThisTick++;
			}
			if (this.cursor >= this.blocks.size()) finish(true);
		}

		public void cancel() {
			finish(false);
		}

		public boolean isRunning() {
			return !this.finished;
		}

		public int getPlacedBlocks() {
			return this.cursor;
		}

		public int getTotalBlocks() {
			return this.blocks.size();
		}

		private void finish(boolean completed) {
			if (this.finished) return;
			this.finished = true;
			if (this.task != null) {
				this.task.cancel();
				this.task = null;
			}
			animations.remove(this);
			if (completed && this.settings.refreshAtEnd()) refreshBlocks();
			if (completed) this.completion.run();
		}

		private void refreshBlocks() {
			World world = this.origin.getWorld();
			if (world == null) return;
			for (StructureBlock placement : this.blocks) {
				Block block = world.getBlockAt(
						this.origin.getBlockX() + placement.x(),
						this.origin.getBlockY() + placement.y(),
						this.origin.getBlockZ() + placement.z());
				block.setBlockData(block.getBlockData(), true);
			}
		}
	}

	public record AnimationSettings(long periodTicks, int blocksPerTick,
	                                boolean applyPhysics, boolean refreshAtEnd) {

		public AnimationSettings {
			if (periodTicks < 1L) throw new IllegalArgumentException("periodTicks must be at least 1");
			if (blocksPerTick < 1) throw new IllegalArgumentException("blocksPerTick must be at least 1");
		}

		public static AnimationSettings defaults() {
			return new AnimationSettings(2L, 32, false, true);
		}
	}

	public record StructureBlock(int x, int y, int z, BlockData data) {

		public StructureBlock {
			data = Objects.requireNonNull(data, "data").clone();
		}

		public static StructureBlock of(Material material, int x, int y, int z) {
			return new StructureBlock(x, y, z,
					Objects.requireNonNull(material, "material").createBlockData());
		}

		@Override
		public BlockData data() {
			return this.data.clone();
		}

		private StructureBlock copy() {
			return new StructureBlock(this.x, this.y, this.z, this.data);
		}

		private void place(Location origin, boolean applyPhysics) {
			World world = Objects.requireNonNull(origin.getWorld(), "origin world");
			world.getBlockAt(origin.getBlockX() + this.x, origin.getBlockY() + this.y,
					origin.getBlockZ() + this.z).setBlockData(this.data, applyPhysics);
		}
	}
}
