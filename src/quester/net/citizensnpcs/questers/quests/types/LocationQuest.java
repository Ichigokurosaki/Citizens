package net.citizensnpcs.questers.quests.types;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.citizensnpcs.questers.quests.Objective;
import net.citizensnpcs.questers.quests.progress.ObjectiveProgress;
import net.citizensnpcs.questers.quests.progress.QuestUpdater;
import net.citizensnpcs.utils.LocationUtils;
import net.citizensnpcs.utils.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerMoveEvent;

import com.google.common.collect.Maps;

public class LocationQuest implements QuestUpdater {
	private static final Type[] EVENTS = new Type[] { Type.PLAYER_MOVE };
	private static final Map<Player, Long> reachTimes = Maps.newHashMap();

	@Override
	public boolean update(Event event, ObjectiveProgress progress) {
		if (event instanceof PlayerMoveEvent) {
			PlayerMoveEvent ev = (PlayerMoveEvent) event;
			Objective objective = progress.getObjective();
			if (LocationUtils.withinRange(ev.getTo(), objective.getLocation(),
					objective.getAmount())) {
				if (!objective.hasParameter("time"))
					return true;
				return updateTime(objective.getParameter("time").getInt(),
						progress.getPlayer());
			} else
				reachTimes.remove(progress.getPlayer());
		}
		return false;
	}

	private boolean updateTime(int ticks, Player player) {
		if (ticks <= 0)
			return true;
		if (!reachTimes.containsKey(player)) {
			reachTimes.put(player, System.currentTimeMillis());
			return false;
		}
		long diff = TimeUnit.SECONDS.convert(System.currentTimeMillis()
				- reachTimes.get(player), TimeUnit.MILLISECONDS);
		if (0 > ticks - diff) {
			reachTimes.remove(player);
		}
		return !reachTimes.containsKey(player);
	}

	@Override
	public Type[] getEventTypes() {
		return EVENTS;
	}

	@Override
	public String getStatus(ObjectiveProgress progress) {
		int amount = progress.getObjective().getAmount();
		return ChatColor.GREEN
				+ "Moving to "
				+ StringUtils.format(progress.getObjective().getLocation())
				+ " "
				+ StringUtils.bracketize(StringUtils.wrap(amount)
						+ StringUtils.formatter(" block").plural(amount)
						+ " leeway", true);
	}
}