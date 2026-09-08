package pl.kiosel.villages.addons.firststeps;

import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.AnvilGui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.gui.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class TutorialValueGUI extends AnvilGui {

	private final AdvancedVillages plugin;
	private final TutorialSession session;
	private final TutorialSetting setting;
	private final TutorialSettingsGUI parent;

	TutorialValueGUI(AdvancedVillages plugin, Player player, TutorialSession session,
					 TutorialSetting setting, TutorialSettingsGUI parent) {
		super(player, parent);
		this.plugin = plugin;
		this.session = session;
		this.setting = setting;
		this.parent = parent;

		String value = setting.display(session.get(setting));
		setTitle(replace(plugin.getGuiSettings().text(
				"guis.tutorial.editor.title", "&8Edit %setting%"), value));
		setInput(Item.create(setting.getMaterial(), value));
		setInputText(value);
		setTextChangeHandler(this::refreshPrompt);
		setAction(event -> this.submit(event.getPlayer()));
		playSoundOnClick(true);
		refreshPrompt(value);
	}

	private void submit(Player player) {
		Optional<Object> parsed = this.setting.parse(getInputText());
		if (!parsed.isPresent()) {
			this.refreshPrompt(getInputText());
			return;
		}

		this.session.set(this.setting, parsed.get());
		this.parent.refresh(this.setting);
		this.plugin.getGuiManager().showGUI(player, this.parent);
	}

	private void refreshPrompt(String input) {
		boolean valid = this.setting.parse(input).isPresent();
		String path = "guis.tutorial.editor." + (valid ? "valid" : "invalid");
		List<String> fallback = valid
				? java.util.Arrays.asList("&aClick to save", "&7Type: &f%type%")
				: java.util.Arrays.asList("&cInvalid value", "&7Expected: &f%type%");
		List<String> configured = this.plugin.getGuiSettings().list(path, fallback);
		List<String> prompt = new ArrayList<>(configured.size());
		for (String line : configured) prompt.add(replace(line, input));
		setOutputPrompt(prompt);
	}

	private String replace(String text, String value) {
		return text.replace("%setting%", this.setting.getId())
				.replace("%type%", this.setting.getValueType().getId())
				.replace("%value%", value == null ? "" : value);
	}
}
