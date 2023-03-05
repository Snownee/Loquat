/*package snownee.loquat.event;

import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import snownee.loquat.AreaEventTypes;
import snownee.loquat.core.AreaEvent;
import snownee.loquat.core.area.Area;

import java.util.List;
import java.util.UUID;

public class PlayerEnterExitAreaEvent extends AreaEvent {

	private final List<ServerPlayer> players = ObjectArrayList.of();
	private final List<UUID> pendingPlayers = ObjectArrayList.of();

	public PlayerEnterExitAreaEvent(Area area) {
		super(area);
	}

	@Override
	public void tick(ServerLevel world) {
		if (!pendingPlayers.isEmpty()) {
			for (UUID uuid : pendingPlayers) {
				if (world.getPlayerByUUID(uuid) instanceof ServerPlayer player) {
					players.add(player);
				}
			}
			pendingPlayers.clear();
		}
		if (ticksExisted % 20 != 0) {
			return;
		}
		List<ServerPlayer> list = Streams.concat(players.stream(), world.players().stream()).distinct().toList();
		for (ServerPlayer player : list) {
			boolean inside = area.contains(player.getBoundingBox());
			if (inside && !players.contains(player)) {
				players.add(player);
				area.onPlayerEnter(player);
			} else if (!inside && players.contains(player)) {
				players.remove(player);
				area.onPlayerExit(player);
			}
		}
	}

	@Override
	public AreaEvent.Type<?> getType() {
		return AreaEventTypes.PLAYER_ENTER_EXIT;
	}

	public static class Type extends AreaEvent.Type<PlayerEnterExitAreaEvent> {
		@Override
		public PlayerEnterExitAreaEvent deserialize(Area area, CompoundTag data) {
			PlayerEnterExitAreaEvent event = new PlayerEnterExitAreaEvent(area);
			for (Tag tag : data.getList("Players", Tag.TAG_COMPOUND)) {
				event.pendingPlayers.add(((CompoundTag) tag).getUUID("UUID"));
			}
			return event;
		}

		@Override
		public CompoundTag serialize(CompoundTag data, PlayerEnterExitAreaEvent event) {
			ListTag list = new ListTag();
			Streams.concat(event.players.stream().map(Entity::getUUID), event.pendingPlayers.stream()).forEach(uuid -> {
				CompoundTag tag = new CompoundTag();
				tag.putUUID("UUID", uuid);
				list.add(tag);
			});
			data.put("Players", list);
			return data;
		}
	}

}
*/