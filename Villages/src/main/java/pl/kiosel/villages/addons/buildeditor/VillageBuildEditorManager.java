package pl.kiosel.villages.addons.buildeditor;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.core.compatibility.CompatibleMaterial;
import pl.kiosel.core.configuration.Config;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.settings.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class VillageBuildEditorManager {

    public static final int MAX_LEVEL = pl.kiosel.villages.data.village.level.LevelManager.MAX_LEVEL;
    private static final DateTimeFormatter BACKUP_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final AdvancedVillages plugin;
    private final Config config;
    private final Map<UUID, BuildEditorSession> sessions = new HashMap<>();
    private final Map<UUID, LevelSetupConversation> conversations = new ConcurrentHashMap<>();
    private BukkitTask boundaryTask;

    public VillageBuildEditorManager(AdvancedVillages plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        restartBoundaryTask();
    }

    public void openLevelMenu(Player player) {
        if (checkRequirements(player)) {
            return;
        }
        if (hasSession(player)) {
            sendLocalized(player, Lang.BUILD_EDITOR_SESSION_ACTIVE);
            return;
        }
        LevelSetupConversation conversation = conversations.get(player.getUniqueId());
        if (conversation != null) {
            sendLocalized(player, Lang.BUILD_EDITOR_SETUP_ACTIVE);
            prompt(player, conversation.getCurrentField());
            return;
        }
        plugin.getGuiManager().showGUI(player, new EditorMenu(plugin, player, this));
    }

    void openLevelSettings(Player player, int level) {
        if (checkRequirements(player)) {
            return;
        }
        if (!plugin.getLevelManager().isLevel(level)) {
            sendLocalized(player, Lang.BUILD_EDITOR_LEVEL_MISSING, "level", level);
            return;
        }
        plugin.getGuiManager().showGUI(player, new LevelSettingsMenu(plugin, player, this, level));
    }

    int getNextAvailableLevel() {
        for (int level = 1; level <= MAX_LEVEL; level++) {
            if (!plugin.getLevelManager().isLevel(level)) {
                return level;
            }
        }
        return MAX_LEVEL + 1;
    }

    public void startSession(Player player, int level) {
        if (checkRequirements(player)) {
            return;
        }
        if (level < 1 || level > MAX_LEVEL || !plugin.getLevelManager().isLevel(level)) {
            sendLocalized(player, Lang.BUILD_EDITOR_LEVEL_MISSING, "level", level);
            return;
        }
        if (hasSession(player) || hasConversation(player)) {
            sendLocalized(player, Lang.BUILD_EDITOR_SESSION_ACTIVE);
            return;
        }

        File schematicFile = getSchematicFile(level);
        if (!schematicFile.isFile()) {
            sendLocalized(player, Lang.BUILD_EDITOR_SCHEMATIC_MISSING, "schematic", schematicFile.getName());
            return;
        }

        try {
            Clipboard clipboard = readClipboard(schematicFile);
            RelativeBounds relative = RelativeBounds.fromClipboard(clipboard);
            long maxVolume = Math.max(1L, config.getLong("search.max-volume", 50_000L));
            if (relative.volume() > maxVolume) {
                sendLocalized(player, Lang.BUILD_EDITOR_SCHEMATIC_TOO_LARGE, "blocks", relative.volume());
                return;
            }

            Location origin = findSafeOrigin(player, relative);
            if (origin == null) {
                sendLocalized(player, Lang.BUILD_EDITOR_SAFE_LOCATION_NOT_FOUND);
                return;
            }

            BuildEditorBounds bounds = relative.at(origin);
            int margin = Math.max(0, config.getInt("search.empty-margin", 8));
            BuildEditorBounds cleanupBounds = bounds.expand(margin);
            try {
                pasteClipboard(clipboard, origin);
                origin.getBlock().setType(Material.NOTE_BLOCK, false);
            } catch (WorldEditException exception) {
                clearArea(cleanupBounds);
                throw exception;
            }

            beginSession(player, level, origin, bounds, cleanupBounds, false);
        } catch (IOException | WorldEditException exception) {
            plugin.getLogger().severe("Failed to start editing level " + level + ": " + exception.getMessage());
            sendLocalized(player, Lang.BUILD_EDITOR_LOAD_FAILED);
        }
    }

    public void startNewLevel(Player player, int level) {
        if (checkRequirements(player)) {
            return;
        }
        if (level < 1 || level > MAX_LEVEL || level != getNextAvailableLevel()) {
            sendLocalized(player, Lang.BUILD_EDITOR_MAX_LEVEL);
            return;
        }
        if (hasSession(player) || hasConversation(player)) {
            sendLocalized(player, Lang.BUILD_EDITOR_SESSION_ACTIVE);
            return;
        }

        File schematic = getSchematicFile(level);
        if (schematic.isFile()) {
            beginCreationWizard(player, level);
            sendLocalized(player, Lang.BUILD_EDITOR_SETUP_RESUMED, "level", level);
            return;
        }

        RelativeBounds relative = RelativeBounds.forNewLevel();
        long maxVolume = Math.max(1L, config.getLong("search.max-volume", 50_000L));
        if (relative.volume() > maxVolume) {
            sendLocalized(player, Lang.BUILD_EDITOR_SCHEMATIC_TOO_LARGE, "blocks", relative.volume());
            return;
        }

        Location origin = findSafeOrigin(player, relative);
        if (origin == null) {
            sendLocalized(player, Lang.BUILD_EDITOR_SAFE_LOCATION_NOT_FOUND);
            return;
        }

        BuildEditorBounds bounds = relative.at(origin);
        int margin = Math.max(0, config.getInt("search.empty-margin", 8));
        BuildEditorBounds cleanupBounds = bounds.expand(margin);
        Material baseMaterial = Material.matchMaterial(config.getString("new-level.base-material", "STONE"));
        if (baseMaterial == null || !baseMaterial.isBlock()) {
            baseMaterial = Material.STONE;
        }
        createStoneBase(bounds, origin.add(0, 3, 0), baseMaterial);
        beginSession(player, level, origin, bounds, cleanupBounds, true);
        sendLocalized(player, Lang.BUILD_EDITOR_NEW_LEVEL_STARTED, "level", level);
    }

    private void beginSession(Player player, int level, Location origin, BuildEditorBounds bounds,
                              BuildEditorBounds cleanupBounds, boolean newLevel) {
        player.closeInventory();
        BuildEditorPlayerState playerState = BuildEditorPlayerState.capture(player);
        BuildEditorSession session = new BuildEditorSession(
                player.getUniqueId(), level, origin, player.getLocation(),
                bounds, cleanupBounds, playerState, newLevel
        );
        playerState.prepareForEditing(player);
        sessions.put(player.getUniqueId(), session);

        Location teleport = new Location(
                origin.getWorld(), origin.getX() + 0.5, bounds.getMaxY() + 2.5, origin.getZ() + 0.5,
                player.getLocation().getYaw(), player.getLocation().getPitch()
        );
        player.teleport(teleport);
        sendLocalized(player, Lang.BUILD_EDITOR_STARTED, "level", level);
        sendLocalized(player, Lang.BUILD_EDITOR_BOUNDS_HELP);
        sendLocalized(player, Lang.BUILD_EDITOR_COMMANDS_HELP);
    }

    public void saveSession(Player player) {
        BuildEditorSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            sendLocalized(player, Lang.BUILD_EDITOR_NO_SESSION);
            return;
        }

        session.getOrigin().getBlock().setType(Material.NOTE_BLOCK, false);
        File target = getSchematicFile(session.getLevel());
        File temp = new File(target.getParentFile(), target.getName() + ".tmp");

        try {
            writeClipboard(session, temp);
            backupAndReplace(target.toPath(), temp.toPath());
            boolean newLevel = session.isNewLevel();
            finishSession(session, player, true);
            sendLocalized(player, Lang.BUILD_EDITOR_SAVED, "level", session.getLevel());
            if (newLevel) {
                beginCreationWizard(player, session.getLevel());
            } else {
                sendLocalized(player, Lang.BUILD_EDITOR_SAVED_HELP);
            }
        } catch (IOException | WorldEditException exception) {
            plugin.getLogger().severe("Failed to save editing level " + session.getLevel() + ": " + exception.getMessage());
            sendLocalized(player, Lang.BUILD_EDITOR_SAVE_FAILED);
        }
    }

    public void cancelSession(Player player) {
        BuildEditorSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            if (conversations.remove(player.getUniqueId()) != null) {
                sendLocalized(player, Lang.BUILD_EDITOR_SETUP_CANCELED);
                return;
            }
            sendLocalized(player, Lang.BUILD_EDITOR_NO_SESSION);
            return;
        }
        finishSession(session, player, true);
        sendLocalized(player, Lang.BUILD_EDITOR_CANCELED);
    }

    void cancelOnQuit(Player player) {
        conversations.remove(player.getUniqueId());
        BuildEditorSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            finishSession(session, player, false);
        }
    }

    public boolean hasSession(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    boolean hasConversation(Player player) {
        return conversations.containsKey(player.getUniqueId());
    }

    void beginFieldEdit(Player player, int level, LevelEditorField field) {
        Level current = plugin.getLevelManager().getLevel(level);
        if (current == null) {
            sendLocalized(player, Lang.BUILD_EDITOR_LEVEL_MISSING, "level", level);
            return;
        }
        conversations.put(player.getUniqueId(), LevelSetupConversation.edit(level, LevelDraft.from(current), field));
        prompt(player, field);
    }

    void handleChatInput(Player player, String rawInput) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> handleChatInput(player, rawInput));
            return;
        }
        LevelSetupConversation conversation = conversations.get(player.getUniqueId());
        if (conversation == null) {
            return;
        }
        String input = rawInput == null ? "" : rawInput.trim();
        if (input.equalsIgnoreCase("cancel")
                || input.equalsIgnoreCase("anuluj")
                || input.equalsIgnoreCase("abbrechen")) {
            conversations.remove(player.getUniqueId());
            sendLocalized(player, Lang.BUILD_EDITOR_SETUP_CANCELED);
            return;
        }

        LevelEditorField field = conversation.getCurrentField();
        if (!applyInput(conversation.getDraft(), field, input)) {
            sendLocalized(player, Lang.BUILD_EDITOR_INVALID_INPUT);
            prompt(player, field);
            return;
        }

        if (conversation.advance()) {
            prompt(player, conversation.getCurrentField());
            return;
        }

        if (!saveLevel(conversation.getLevel(), conversation.getDraft())) {
            sendLocalized(player, Lang.BUILD_EDITOR_LEVEL_CONFIG_SAVE_FAILED);
            return;
        }
        conversations.remove(player.getUniqueId());
        Lang completedMessage = conversation.isCreation()
                ? Lang.BUILD_EDITOR_SETUP_COMPLETE
                : Lang.BUILD_EDITOR_LEVEL_UPDATED;
        sendLocalized(player, completedMessage, "level", conversation.getLevel());
        if (conversation.isCreation()) {
            openLevelMenu(player);
        } else {
            openLevelSettings(player, conversation.getLevel());
        }
    }

    private void beginCreationWizard(Player player, int level) {
        conversations.put(player.getUniqueId(), LevelSetupConversation.creation(level));
        sendLocalized(player, Lang.BUILD_EDITOR_SETUP_STARTED, "level", level);
        prompt(player, LevelEditorField.ITEMS);
    }

    private void prompt(Player player, LevelEditorField field) {
        switch (field) {
            case ITEMS:
                sendLocalized(player, Lang.BUILD_EDITOR_PROMPT_ITEMS);
                break;
            case EXPERIENCE:
                sendLocalized(player, Lang.BUILD_EDITOR_PROMPT_EXPERIENCE);
                break;
            case ECONOMY:
                sendLocalized(player, Lang.BUILD_EDITOR_PROMPT_ECONOMY);
                break;
            case SIZE:
                sendLocalized(player, Lang.BUILD_EDITOR_PROMPT_SIZE);
                break;
        }
        sendLocalized(player, Lang.BUILD_EDITOR_PROMPT_CANCEL);
    }

    private boolean applyInput(LevelDraft draft, LevelEditorField field, String input) {
        if (field == LevelEditorField.ITEMS) {
            return applyMaterials(draft, input);
        }

        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException ignored) {
            return false;
        }
        if (value < (field == LevelEditorField.SIZE ? 1 : 0)) {
            return false;
        }
        switch (field) {
            case EXPERIENCE:
                draft.setExperience(value);
                return true;
            case ECONOMY:
                draft.setEconomy(value);
                return true;
            case SIZE:
                draft.setSize(value);
                return true;
            default:
                return false;
        }
    }

    private boolean applyMaterials(LevelDraft draft, String input) {
        Map<XMaterial, Integer> parsed = new LinkedHashMap<>();
        if (!input.equalsIgnoreCase("none")
                && !input.equalsIgnoreCase("brak")
                && !input.equalsIgnoreCase("keine")
                && !input.equals("0")) {
            for (String entry : input.split("[,;]")) {
                String[] parts = entry.trim().split(":", 2);
                if (parts.length != 2) {
                    return false;
                }
                XMaterial material = CompatibleMaterial.getMaterial(parts[0].trim()).orElse(null);
                int amount;
                try {
                    amount = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ignored) {
                    return false;
                }
                if (material == null || material.get() == null || !material.get().isItem() || amount < 1) {
                    return false;
                }
                parsed.merge(material, amount, Integer::sum);
            }
        }
        draft.getMaterials().clear();
        draft.getMaterials().putAll(parsed);
        return true;
    }

    private boolean saveLevel(int level, LevelDraft draft) {
        Config levels = plugin.getLevelsFile();
        String path = "Level-" + level;
        List<String> materials = draft.getMaterials().entrySet().stream()
                .map(entry -> entry.getKey().name() + ":" + entry.getValue())
                .collect(Collectors.toList());
        levels.set(path + ".Cost-item", materials);
        levels.set(path + ".Cost-xp", draft.getExperience());
        levels.set(path + ".Cost-eco", draft.getEconomy());
        levels.set(path + ".Size", draft.getSize());
        backupLevelsFile(levels.getFile().toPath());
        if (!levels.save()) {
            plugin.getLogger().severe("Could not save level " + level + " to levels.yml");
            return false;
        }
        plugin.reloadLevels();
        return plugin.getLevelManager().isLevel(level);
    }

    private void backupLevelsFile(Path source) {
        if (!Files.isRegularFile(source)) {
            return;
        }
        try {
            Path backupDirectory = source.getParent().resolve("backups").resolve("levels");
            Files.createDirectories(backupDirectory);
            Path backup = backupDirectory.resolve("levels-" + BACKUP_DATE.format(LocalDateTime.now()) + ".yml");
            Files.copy(source, backup, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not create levels.yml backup: " + exception.getMessage());
        }
    }

    BuildEditorSession getSession(UUID playerId) {
        return sessions.get(playerId);
    }

    BuildEditorSession findSessionAt(Location location) {
        for (BuildEditorSession session : sessions.values()) {
            if (session.getCleanupBounds().contains(location)) {
                return session;
            }
        }
        return null;
    }

    public void reload() {
        restartBoundaryTask();
    }

    public void shutdown() {
        if (boundaryTask != null) {
            boundaryTask.cancel();
            boundaryTask = null;
        }
        for (BuildEditorSession session : new ArrayList<>(sessions.values())) {
            Player player = Bukkit.getPlayer(session.getPlayerId());
            finishSession(session, player, player != null && player.isOnline());
        }
        sessions.clear();
        conversations.clear();
    }

    Config getConfig() {
        return config;
    }

    File getSchematicFile(int level) {
        return new File(plugin.getDataFolder(), "schematics/Turret" + level + ".schem");
    }

    private boolean checkRequirements(Player player) {
        if (!config.getBoolean("enabled", true)) {
            sendLocalized(player, Lang.BUILD_EDITOR_DISABLED);
            return true;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            sendLocalized(player, Lang.BUILD_EDITOR_WORLD_EDIT_REQUIRED);
            return true;
        }
        if (!Settings.WORLDEDIT.getBoolean()) {
            sendLocalized(player, Lang.BUILD_EDITOR_SETTING_REQUIRED);
            return true;
        }
        return false;
    }

    private Clipboard readClipboard(File file) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            throw new IOException("Unknown format: " + file.getName());
        }
        try (FileInputStream input = new FileInputStream(file);
             ClipboardReader reader = format.getReader(input)) {
            return reader.read();
        }
    }

    private void pasteClipboard(Clipboard clipboard, Location origin) throws WorldEditException {
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Objects.requireNonNull(origin.getWorld()));
        BlockVector3 position = BlockVector3.at(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(position)
                    .ignoreAirBlocks(false)
                    .copyEntities(false)
                    .build();
            Operations.complete(operation);
        }
    }

    private void writeClipboard(BuildEditorSession session, File file) throws IOException, WorldEditException {
        BuildEditorBounds bounds = session.getBounds();
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(bounds.getWorld());
        BlockVector3 min = BlockVector3.at(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
        BlockVector3 max = BlockVector3.at(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
        CuboidRegion region = new CuboidRegion(world, min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, min);
            copy.setCopyingEntities(false);
            copy.setCopyingBiomes(false);
            Operations.complete(copy);
        }
        Location origin = session.getOrigin();
        clipboard.setOrigin(BlockVector3.at(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ()));

        Files.createDirectories(file.toPath().getParent());
        try (FileOutputStream output = new FileOutputStream(file);
             ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(output)) {
            writer.write(clipboard);
        }
    }

    private void backupAndReplace(Path target, Path temp) throws IOException {
        Files.createDirectories(target.getParent());
        if (Files.exists(target)) {
            Path backupDirectory = target.getParent().resolve("backups");
            Files.createDirectories(backupDirectory);
            String name = target.getFileName().toString().replace(".schem", "-")
                    + BACKUP_DATE.format(LocalDateTime.now()) + ".schem";
            Files.copy(target, backupDirectory.resolve(name), StandardCopyOption.COPY_ATTRIBUTES);
        }
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Location findSafeOrigin(Player player, RelativeBounds relative) {
        World world = player.getWorld();
        int heightOffset = Math.max(16, config.getInt("search.height-offset", 48));
        int verticalStep = Math.max(4, config.getInt("search.vertical-step", 12));
        int horizontalStep = Math.max(16, config.getInt("search.horizontal-step", 32));
        int horizontalRadius = Math.max(0, config.getInt("search.horizontal-radius", 96));
        int margin = Math.max(0, config.getInt("search.empty-margin", 8));

        int minOriginY = world.getMinHeight() - relative.minY + margin;
        int maxOriginY = world.getMaxHeight() - 1 - relative.maxY - margin;
        if (minOriginY > maxOriginY) {
            return null;
        }

        for (int[] offset : createSearchOffsets(horizontalStep, horizontalRadius)) {
            int x = player.getLocation().getBlockX() + offset[0];
            int z = player.getLocation().getBlockZ() + offset[1];
            int highest = world.getHighestBlockYAt(x, z);
            int preferred = Math.max(player.getLocation().getBlockY() + heightOffset, highest + heightOffset);
            int start = Math.max(minOriginY, Math.min(maxOriginY, preferred));

            for (int y = start; y <= maxOriginY; y += verticalStep) {
                Location origin = new Location(world, x, y, z);
                BuildEditorBounds candidate = relative.at(origin).expand(margin);
                if (isSafeAndEmpty(candidate)) {
                    return origin;
                }
            }
            for (int y = start - verticalStep; y >= minOriginY; y -= verticalStep) {
                Location origin = new Location(world, x, y, z);
                BuildEditorBounds candidate = relative.at(origin).expand(margin);
                if (isSafeAndEmpty(candidate)) {
                    return origin;
                }
            }
        }
        return null;
    }

    private Collection<int[]> createSearchOffsets(int step, int radius) {
        List<int[]> offsets = new ArrayList<>();
        offsets.add(new int[]{0, 0});
        for (int current = step; current <= radius; current += step) {
            for (int x = -current; x <= current; x += step) {
                offsets.add(new int[]{x, -current});
                offsets.add(new int[]{x, current});
            }
            for (int z = -current + step; z <= current - step; z += step) {
                offsets.add(new int[]{-current, z});
                offsets.add(new int[]{current, z});
            }
        }
        return offsets;
    }

    private boolean isSafeAndEmpty(BuildEditorBounds bounds) {
        Location firstCorner = new Location(bounds.getWorld(), bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
        Location secondCorner = new Location(bounds.getWorld(), bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
        if (!bounds.getWorld().getWorldBorder().isInside(firstCorner)
                || !bounds.getWorld().getWorldBorder().isInside(secondCorner)) {
            return false;
        }
        for (BuildEditorSession session : sessions.values()) {
            if (bounds.intersects(session.getCleanupBounds())) {
                return false;
            }
        }
        double centerX = (bounds.getMinX() + bounds.getMaxX() + 1) / 2.0;
        double centerY = (bounds.getMinY() + bounds.getMaxY() + 1) / 2.0;
        double centerZ = (bounds.getMinZ() + bounds.getMaxZ() + 1) / 2.0;
        Location center = new Location(bounds.getWorld(), centerX, centerY, centerZ);
        double radiusX = (bounds.getMaxX() - bounds.getMinX() + 1) / 2.0;
        double radiusY = (bounds.getMaxY() - bounds.getMinY() + 1) / 2.0;
        double radiusZ = (bounds.getMaxZ() - bounds.getMinZ() + 1) / 2.0;
        if (!bounds.getWorld().getNearbyEntities(center, radiusX, radiusY, radiusZ).isEmpty()) {
            return false;
        }
        for (int x = bounds.getMinX(); x <= bounds.getMaxX(); x++) {
            for (int y = bounds.getMinY(); y <= bounds.getMaxY(); y++) {
                for (int z = bounds.getMinZ(); z <= bounds.getMaxZ(); z++) {
                    if (!bounds.getWorld().getBlockAt(x, y, z).getType().isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void finishSession(BuildEditorSession session, Player player, boolean teleportBack) {
        sessions.remove(session.getPlayerId());

        if (player != null) {
            try {
                if (teleportBack && session.getReturnLocation().getWorld() != null) {
                    player.teleport(session.getReturnLocation());
                }
            } finally {
                session.getPlayerState().restore(player);
            }
        }

        try {
            clearArea(session.getCleanupBounds());
        } catch (RuntimeException exception) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to clear build editor area for " + session.getPlayerId(), exception);
        }
    }

    void sendLocalized(Player player, Lang message) {
        plugin.getLocale().getMessage(message.getPath()).sendPrefixedMessage(player);
    }

    private void sendLocalized(Player player, Lang message, String placeholder, Object value) {
        plugin.getLocale().getMessage(message.getPath())
                .processPlaceholder(placeholder, String.valueOf(value))
                .sendPrefixedMessage(player);
    }

    private void createStoneBase(BuildEditorBounds bounds, Location origin, Material material) {
        int y = bounds.getMinY();
        for (int x = bounds.getMinX(); x <= bounds.getMaxX(); x++) {
            for (int z = bounds.getMinZ(); z <= bounds.getMaxZ(); z++) {
                bounds.getWorld().getBlockAt(x, y, z).setType(material, false);
            }
        }
        origin.getBlock().setType(Material.NOTE_BLOCK, false);
    }

    private void clearArea(BuildEditorBounds bounds) {
        for (int x = bounds.getMinX(); x <= bounds.getMaxX(); x++)
            for (int y = bounds.getMinY(); y <= bounds.getMaxY(); y++)
                for (int z = bounds.getMinZ(); z <= bounds.getMaxZ(); z++) {
                    Block block = bounds.getWorld().getBlockAt(x, y, z);
                    if (!block.getType().isAir())
                        block.setType(Material.AIR, false);
                }
    }

    private void restartBoundaryTask() {
        if (boundaryTask != null) {
            boundaryTask.cancel();
            boundaryTask = null;
        }
        if (!config.getBoolean("enabled", true)) {
            return;
        }
        long interval = Math.max(1L, config.getLong("boundary.interval-ticks", 5L));
        boundaryTask = Bukkit.getScheduler().runTaskTimer(plugin, this::showBoundaries, interval, interval);
    }

    private void showBoundaries() {
        Particle particle = parseParticle(config.getString("boundary.particle", "END_ROD"));
        double step = Math.max(0.5, config.getDouble("boundary.step", 1.0));
        for (BuildEditorSession session : sessions.values()) {
            Player player = Bukkit.getPlayer(session.getPlayerId());
            if (player == null || !player.isOnline() || !session.getBounds().getWorld().equals(player.getWorld())) {
                continue;
            }
            drawBounds(player, session.getBounds(), particle, step);
        }
    }

    private void drawBounds(Player player, BuildEditorBounds bounds, Particle particle, double step) {
        double minX = bounds.getMinX();
        double minY = bounds.getMinY();
        double minZ = bounds.getMinZ();
        double maxX = bounds.getMaxX() + 1.0;
        double maxY = bounds.getMaxY() + 1.0;
        double maxZ = bounds.getMaxZ() + 1.0;

        for (double x = minX; x <= maxX; x += step) {
            particle(player, particle, x, minY, minZ);
            particle(player, particle, x, minY, maxZ);
            particle(player, particle, x, maxY, minZ);
            particle(player, particle, x, maxY, maxZ);
        }
        for (double y = minY; y <= maxY; y += step) {
            particle(player, particle, minX, y, minZ);
            particle(player, particle, minX, y, maxZ);
            particle(player, particle, maxX, y, minZ);
            particle(player, particle, maxX, y, maxZ);
        }
        for (double z = minZ; z <= maxZ; z += step) {
            particle(player, particle, minX, minY, z);
            particle(player, particle, minX, maxY, z);
            particle(player, particle, maxX, minY, z);
            particle(player, particle, maxX, maxY, z);
        }
    }

    private void particle(Player player, Particle particle, double x, double y, double z) {
        player.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
    }

    private Particle parseParticle(String name) {
        try {
            return Particle.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return Particle.END_ROD;
        }
    }

    private static final class RelativeBounds {
        private final int minX;
        private final int minY;
        private final int minZ;
        private final int maxX;
        private final int maxY;
        private final int maxZ;

        private RelativeBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        static RelativeBounds fromClipboard(Clipboard clipboard) {
            BlockVector3 origin = clipboard.getOrigin();
            BlockVector3 min = clipboard.getMinimumPoint().subtract(origin);
            BlockVector3 max = clipboard.getMaximumPoint().subtract(origin);
            return new RelativeBounds(
                    min.getBlockX(), min.getBlockY(), min.getBlockZ(),
                    max.getBlockX(), max.getBlockY(), max.getBlockZ()
            );
        }

        static RelativeBounds forNewLevel() {
            return new RelativeBounds(-2, -1, -2, 2, 8, 2);
        }

        long volume() {
            return (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        }

        BuildEditorBounds at(Location origin) {
            return new BuildEditorBounds(
                    origin.getWorld(),
                    origin.getBlockX() + minX,
                    origin.getBlockY() + minY,
                    origin.getBlockZ() + minZ,
                    origin.getBlockX() + maxX,
                    origin.getBlockY() + maxY,
                    origin.getBlockZ() + maxZ
            );
        }
    }
}
