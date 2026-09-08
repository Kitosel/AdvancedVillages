package pl.kiosel.villages.api.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRole;

public final class VillageMemberRoleChangeEvent extends VillageEvent implements Cancellable {

	@Getter
	private final User member;
	@Getter
	private final VillageRole previousRole;
	@Getter
	private final VillageRole newRole;
	private boolean cancelled;

	public VillageMemberRoleChangeEvent(Village village, Player actor, User member,
	                                    VillageRole previousRole, VillageRole newRole) {
		super(village, actor);
		this.member = member;
		this.previousRole = previousRole;
		this.newRole = newRole;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
