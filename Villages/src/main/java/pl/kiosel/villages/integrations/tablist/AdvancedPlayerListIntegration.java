package pl.kiosel.villages.integrations.tablist;

import org.bukkit.entity.Player;
import pl.kiosel.playerlist.api.*;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.integrations.TabListIntegration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class AdvancedPlayerListIntegration implements TabListIntegration {

	public static final String PROFILE_ID = "advancedvillages";
	private static final String PLACEHOLDER_ID = "villages";

	private final AdvancedVillages plugin;
	private boolean placeholderRegistered;

	public AdvancedPlayerListIntegration(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@Override
	public void enable() {
		AdvancedPlayerListAPI api = AdvancedPlayerListAPI.get();
		if (api.isRegistered(PLACEHOLDER_ID)) {
			throw new IllegalStateException("AdvancedPlayerList placeholder is already registered: "
					+ PLACEHOLDER_ID);
		}
		api.register(plugin, new VillagePlaceholder());
		placeholderRegistered = true;
	}

	@Override
	public void disable() {
		if (!placeholderRegistered) return;
		try {
			AdvancedPlayerListAPI.get().unregister(plugin, PLACEHOLDER_ID);
		} catch (IllegalStateException ignored) {
		}
		placeholderRegistered = false;
	}

	@Override
	public void installProfile() {
		TablistProfile profile = new TablistProfile(
				PROFILE_ID,
				"AdvancedVillages",
				readResource("integrations/advancedplayerlist/global.yml"),
				readResource("integrations/advancedplayerlist/handler.yml")
		);
		AdvancedPlayerListAPI.get().installProfile(plugin, profile, true);
		reloadLegacyTablist();
	}

	@Override
	public String restorePreviousProfile() {
		String restored = AdvancedPlayerListAPI.get().restorePreviousProfile(plugin);
		reloadLegacyTablist();
		return restored;
	}

	@Override
	public boolean isProfileInstalled() {
		for (TablistProfileInfo profile : AdvancedPlayerListAPI.get().getProfiles()) {
			if (PROFILE_ID.equals(profile.getId()) && plugin.getName().equalsIgnoreCase(profile.getOwner())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isProfileActive() {
		return PROFILE_ID.equals(AdvancedPlayerListAPI.get().getActiveProfile());
	}

	@Override
	public String getActiveProfile() {
		return AdvancedPlayerListAPI.get().getActiveProfile();
	}

	private String readResource(String path) {
		try (InputStream input = plugin.getResource(path)) {
			if (input == null) {
				throw new IllegalStateException("Missing bundled profile resource: " + path);
			}
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read bundled profile resource: " + path, exception);
		}
	}

	private void reloadLegacyTablist() {
		if (plugin.getTablistManager() != null) {
			plugin.getTablistManager().reload();
		}
	}

	private final class VillagePlaceholder implements AdvancedTabPlaceholder {

		@Override
		public String getIdentifier() {
			return PLACEHOLDER_ID;
		}

		@Override
		public String onRequest(PlaceholderContext context, String parameter) {
			if (parameter == null || parameter.trim().isEmpty()) return "";
			Player player = context.getPlayer();
			if (player == null) player = context.getViewingPlayer();
			if (player == null || plugin.getUserManager() == null || plugin.getTablistPlaceholdersService() == null) {
				return "";
			}
			User user = plugin.getUserManager().findByPlayer(player).orElse(null);
			return user == null ? "" : plugin.getTablistPlaceholdersService()
					.format("%" + parameter + "%", user, player);
		}
	}
}
