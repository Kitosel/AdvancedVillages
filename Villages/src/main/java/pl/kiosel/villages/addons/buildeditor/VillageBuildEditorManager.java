package pl.kiosel.villages.addons.buildeditor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.rosacore.compatibility.ZMaterial;
import pl.kiosel.rosacore.compatibility.ZParticle;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.hook.worldedit.WorldEditHook;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRegion;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.data.village.level.LevelManager;

import java.io.File;
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

    public static final int MAX_LEVEL = LevelManager.MAX_LEVEL;
    private static final DateTimeFormatter BACKUP_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final AdvancedVillages plugin;
    private final RosaConfig config;
    private final Map<UUID, BuildEditorSession> sessions = new HashMap<>();
    private final Map<UUID, LevelSetupConversation> conversations = new ConcurrentHashMap<>();
    private BukkitTask boundaryTask;

    public VillageBuildEditorManager(AdvancedVillages plugin) {
        this.plugin = plugin;
        this.config = plugin.getBuildEditorFile();
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
			WorldEditHook.Schematic schematic = plugin.getHookManager().getWorldEdit().loadSchematic(schematicFile);
			RelativeBounds relative = RelativeBounds.fromSchematic(schematic);
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
				plugin.getHookManager().getWorldEdit().pasteSchematic(schematic, origin, false, false);
				origin.getBlock().setType(Material.NOTE_BLOCK, false);
			} catch (IOException exception) {
				clearArea(cleanupBounds);
				throw exception;
			}

            beginSession(player, level, origin, bounds, cleanupBounds, false);
		} catch (IOException exception) {
            plugin.getRosaLogger().severe("Failed to start editing level " + level + ": " + exception.getMessage());
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
        ZMaterial baseMaterial = ZMaterial.match(config.getString("base-material", "STONE")).orElse(ZMaterial.STONE);
        if (!Objects.requireNonNull(baseMaterial.getMaterial().orElse(Material.STONE)).isBlock()) {
            baseMaterial = ZMaterial.STONE;
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
			writeSchematic(session, temp);
            backupAndReplace(target.toPath(), temp.toPath());
            boolean newLevel = session.isNewLevel();
            finishSession(session, player, true);
            sendLocalized(player, Lang.BUILD_EDITOR_SAVED, "level", session.getLevel());
            if (newLevel) {
                beginCreationWizard(player, session.getLevel());
            } else {
                sendLocalized(player, Lang.BUILD_EDITOR_SAVED_HELP);
            }
		} catch (IOException exception) {
            plugin.getRosaLogger().severe("Failed to save editing level " + session.getLevel() + ": " + exception.getMessage());
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
        Map<ZMaterial, Integer> parsed = new LinkedHashMap<>();
        if (!input.equalsIgnoreCase("none")
                && !input.equalsIgnoreCase("brak")
                && !input.equalsIgnoreCase("keine")
                && !input.equals("0")) {
            for (String entry : input.split("[,;]")) {
                String[] parts = entry.trim().split(":", 2);
                if (parts.length != 2) {
                    return false;
                }
                ZMaterial material = ZMaterial.match(parts[0].trim()).orElse(null);
                int amount;
                try {
                    amount = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ignored) {
                    return false;
                }
                if (material == null || material.resolveForItem().isEmpty() || amount < 1) {
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
        RosaConfig levels = plugin.getLevelsFile();
        String path = "Level-" + level;
        List<String> materials = draft.getMaterials().entrySet().stream()
                .map(entry -> entry.getKey().name() + ":" + entry.getValue())
                .collect(Collectors.toList());
        levels.set(path + ".Cost-item", materials);
        levels.set(path + ".Cost-xp", draft.getExperience());
        levels.set(path + ".Cost-eco", draft.getEconomy());
        levels.set(path + ".Size", draft.getSize());
        backupLevelsFile(levels.getFile().toPath());
		if (!levels.save().isSuccess()) {
            plugin.getRosaLogger().severe("Could not save level " + level + " to levels.yml");
            return false;
        }
        return plugin.getLevelManager().reloadLevels() && plugin.getLevelManager().isLevel(level);
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
            plugin.getRosaLogger().warning("Could not create levels.yml backup: " + exception.getMessage());
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

    RosaConfig getConfig() {
        return config;
    }

    File getSchematicFile(int level) {
        return new File(plugin.getDataFolder(), "schematics/Turret" + level + ".schem");
    }

    private boolean checkRequirements(Player player) {
        if (!config.getBoolean("enabled", true)) {
            sendLocalized(player, Lang.ADDON_DISABLED, "addon", "Build Editor");
            return true;
        }
		if (!plugin.getHookManager().getWorldEdit().isEnabled()) {
            sendLocalized(player, Lang.BUILD_EDITOR_WORLD_EDIT_REQUIRED);
            return true;
        }
        if (!Settings.WORLDEDIT.getBoolean()) {
            sendLocalized(player, Lang.BUILD_EDITOR_SETTING_REQUIRED);
            return true;
        }
        return false;
    }

	private void writeSchematic(BuildEditorSession session, File file) throws IOException {
		BuildEditorBounds bounds = session.getBounds();
		World world = bounds.getWorld();
		plugin.getHookManager().getWorldEdit().saveSchematic(
				file,
				new Location(world, bounds.getMinX(), bounds.getMinY(), bounds.getMinZ()),
				new Location(world, bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ()),
				session.getOrigin(),
				false,
				false
		);
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
		if (intersectsVillageRegion(bounds)) {
			return false;
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

	private boolean intersectsVillageRegion(BuildEditorBounds bounds) {
		if (plugin.getVillageManager() == null) {
			return false;
		}
		for (Village village : plugin.getVillageManager().getVillagesView()) {
			VillageRegion region = village.getRegion().orElse(null);
			if (region == null || region.getWorld() == null || !region.getWorld().equals(bounds.getWorld())) {
				continue;
			}
			if (bounds.getMinX() <= region.getUpperX() && bounds.getMaxX() >= region.getLowerX()
					&& bounds.getMinZ() <= region.getUpperZ() && bounds.getMaxZ() >= region.getLowerZ()) {
				return true;
			}
		}
		return false;
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
            plugin.getRosaLogger().log(java.util.logging.Level.SEVERE, "Failed to clear build editor area for " + session.getPlayerId(), exception);
        }
    }

    void sendLocalized(Player player, Lang message) {
        plugin.getVillageMessages().get(message).sendPrefixed(player);
    }

    private void sendLocalized(Player player, Lang message, String placeholder, Object value) {
        plugin.getVillageMessages().get(message)
                .with(placeholder, String.valueOf(value))
                .sendPrefixed(player);
    }

    private void createStoneBase(BuildEditorBounds bounds, Location origin, ZMaterial material) {
        int y = bounds.getMinY();
        for (int x = bounds.getMinX(); x <= bounds.getMaxX(); x++) {
            for (int z = bounds.getMinZ(); z <= bounds.getMaxZ(); z++) {
                bounds.getWorld().getBlockAt(x, y, z).setType(material.getMaterial().orElse(Material.STONE), false);
            }
        }
        origin.getBlock().setType(ZMaterial.NOTE_BLOCK.getMaterial().orElse(Material.NOTE_BLOCK), false);
    }

    private void clearArea(BuildEditorBounds bounds) {
        for (int x = bounds.getMinX(); x <= bounds.getMaxX(); x++)
            for (int y = bounds.getMinY(); y <= bounds.getMaxY(); y++)
                for (int z = bounds.getMinZ(); z <= bounds.getMaxZ(); z++) {
                    Block block = bounds.getWorld().getBlockAt(x, y, z);
                    if (!block.getType().isAir())
                        block.setType(ZMaterial.AIR.getMaterial().orElse(Material.AIR), false);
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
        ZParticle particle = parseParticle(config.getString("boundary.particle", "END_ROD"));
        double step = Math.max(0.5, config.getDouble("boundary.step", 1.0));
        for (BuildEditorSession session : sessions.values()) {
            Player player = Bukkit.getPlayer(session.getPlayerId());
            if (player == null || !player.isOnline() || !session.getBounds().getWorld().equals(player.getWorld())) {
                continue;
            }
            drawBounds(player, session.getBounds(), particle, step);
        }
    }

    private void drawBounds(Player player, BuildEditorBounds bounds, ZParticle particle, double step) {
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

    private void particle(Player player, ZParticle particle, double x, double y, double z) {
        particle.spawn(player, new Location(player.getWorld(), x, y, z), 1, 0, 0, 0, 0);
    }

    private ZParticle parseParticle(String name) {
        return ZParticle.match(name)
                .filter(ZParticle::isSupported)
                .orElse(ZParticle.END_ROD);
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

		static RelativeBounds fromSchematic(WorldEditHook.Schematic schematic) {
			return new RelativeBounds(
					schematic.getMinimumX(), schematic.getMinimumY(), schematic.getMinimumZ(),
					schematic.getMaximumX(), schematic.getMaximumY(), schematic.getMaximumZ()
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
