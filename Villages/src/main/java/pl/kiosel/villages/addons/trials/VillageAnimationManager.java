package pl.kiosel.villages.addons.trials;

import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.trials.animation.CentralBlockAnimator;
import pl.kiosel.villages.addons.trials.animation.CreationAnimator;
import pl.kiosel.villages.addons.trials.animation.RemovalAnimator;
import pl.kiosel.villages.addons.trials.animation.VillageAnimationSettings;
import pl.kiosel.villages.data.village.Village;

public final class VillageAnimationManager {

    private final AdvancedVillages plugin;
    private final RosaConfig config;

    private VillageAnimationSettings settings;
    private CentralBlockAnimator centralAnimator;
    private CreationAnimator creationAnimator;
    private RemovalAnimator removalAnimator;
    private boolean started;

    public VillageAnimationManager(AdvancedVillages plugin) {
        this.plugin = plugin;
        this.config = plugin.getAnimationFile();
        loadComponents();
    }

    public void reload() {
        boolean restart = started;
        shutdown();
        loadComponents();
        if (restart || plugin.getDataloader() != null) {
            start();
        }
    }

    public void start() {
        if (started || !isEnabled() || !settings.getCentral().isEnabled()
                || plugin.getVillageManager() == null) {
            return;
        }
        started = true;
        centralAnimator.start();
    }

    public void shutdown() {
        started = false;
        if (centralAnimator != null) {
            centralAnimator.stop();
        }
    }

    public boolean isEnabled() {
        return settings.isEnabled();
    }

    public boolean isEnabledFor(Village village) {
        return canAnimate(village);
    }

    public void playCreation(Village village) {
        if (settings.getCreation().isEnabled() && hasLocation(village) && canAnimate(village)) {
            creationAnimator.play(village);
        }
    }

    public void playLevelUpgrade(Village village) {
        if (settings.getCentral().isEnabled() && hasLocation(village) && canAnimate(village)) {
            centralAnimator.playUpgrade(village);
        }
    }

    public long playRemoval(Village village, boolean tntAnimation) {
        if (!tntAnimation || !settings.getRemoval().isEnabled()
                || !hasLocation(village) || !canAnimate(village)) {
            return 0L;
        }
        return removalAnimator.play(village);
    }

    private void loadComponents() {
        this.settings = VillageAnimationSettings.load(config);
        this.centralAnimator = new CentralBlockAnimator(plugin, settings.getCentral(), settings.getUpgraded(), this::canAnimate);
        this.creationAnimator = new CreationAnimator(plugin, settings.getCreation(), this::canAnimate);
        this.removalAnimator = new RemovalAnimator(plugin, settings.getRemoval());
    }

    private boolean canAnimate(Village village) {
        return village != null && isEnabled() && village.isAnimationsEnabled();
    }

    private boolean hasLocation(Village village) {
        return village != null && village.getLocation().isPresent();
    }
}
