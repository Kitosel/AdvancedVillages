package pl.kiosel.villages.addons.firststeps;

import pl.kiosel.rosacore.config.ConfigSaveResult;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.VillageConfigFile;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public final class TutorialSession {

	private final AdvancedVillages plugin;
	private final Map<TutorialSetting, Object> initial = new EnumMap<>(TutorialSetting.class);
	private final Map<TutorialSetting, Object> selected = new EnumMap<>(TutorialSetting.class);
	private final boolean onJoin;

	TutorialSession(AdvancedVillages plugin, boolean onJoin) {
		this.plugin = plugin;
		this.onJoin = onJoin;
		for (TutorialSetting setting : TutorialSetting.values()) {
			Object value = setting.read(plugin);
			this.initial.put(setting, value);
			this.selected.put(setting, value);
		}
	}

	public Object get(TutorialSetting setting) {
		return this.selected.get(setting);
	}

	public boolean isEnabled(TutorialSetting setting) {
		return Boolean.TRUE.equals(this.selected.get(setting));
	}

	public void toggle(TutorialSetting setting) {
		if (setting.getValueType() != TutorialSetting.ValueType.BOOLEAN) {
			throw new IllegalArgumentException("Only boolean can be toggled");
		}
		this.selected.put(setting, !this.isEnabled(setting));
	}

	public void set(TutorialSetting setting, Object value) {
		this.selected.put(setting, Objects.requireNonNull(value, "value"));
	}

	public boolean isDirty() {
		return !this.initial.equals(this.selected);
	}

	public boolean save() {
		EnumSet<TutorialSetting> changed = EnumSet.noneOf(TutorialSetting.class);
		EnumSet<VillageConfigFile> touchedFiles = EnumSet.noneOf(VillageConfigFile.class);
		for (TutorialSetting setting : TutorialSetting.values()) {
			if (Objects.equals(this.initial.get(setting), this.selected.get(setting))) continue;
			changed.add(setting);
			setting.write(this.plugin, this.selected.get(setting), touchedFiles);
		}
		if (changed.isEmpty()) return true;

		for (VillageConfigFile file : touchedFiles) {
			RosaConfig config = this.plugin.getConfigurationManager().get(file);
			ConfigSaveResult result = config.save();
			if (result.isSuccess()) continue;

			this.plugin.getRosaLogger().warning("Could not save tutorial setting file " + file.getPath() + ": " + result.getProblems());
			if (result.getCause() != null)
				this.plugin.getRosaLogger().log(Level.WARNING, "Tutorial configuration save failure", result.getCause());

			this.restore(changed, touchedFiles);
			return false;
		}

		if (this.onJoin) {
			this.plugin.getCoreConfig().set("first-steps-completed", true);
			this.plugin.getCoreConfig().save();
		}
		this.plugin.reloadConfig();
		if (!this.plugin.wasLastConfigReloadSuccessful()) return false;
		this.initial.clear();
		this.initial.putAll(this.selected);
		return true;
	}

	private void restore(EnumSet<TutorialSetting> changed, EnumSet<VillageConfigFile> touchedFiles) {
		for (TutorialSetting setting : changed)
			setting.write(this.plugin, this.initial.get(setting), touchedFiles);
		for (VillageConfigFile file : touchedFiles) {
			ConfigSaveResult result = this.plugin.getConfigurationManager().get(file).save();
			if (!result.isSuccess())
				this.plugin.getRosaLogger().severe("Could not restore " + file.getPath() + " after tutorial save failure");
		}
	}
}
