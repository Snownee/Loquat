package snownee.loquat.spawner;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import snownee.loquat.AreaEventTypes;
import snownee.loquat.core.AreaEvent;
import snownee.loquat.core.area.Area;

public class SpawnMobAreaEvent extends AreaEvent {
	private final String spawnerId;

	public SpawnMobAreaEvent(Area area, String spawnerId) {
		super(area);
		this.spawnerId = spawnerId;
	}

	@Override
	public void tick(ServerLevel world) {

	}

	@Override
	public AreaEvent.Type<?> getType() {
		return AreaEventTypes.SPAWN_MOB;
	}

	public static class Type extends AreaEvent.Type<SpawnMobAreaEvent> {
		@Override
		public SpawnMobAreaEvent deserialize(Area area, CompoundTag data) {
			String spawnerId = data.getString("Spawner");
			return new SpawnMobAreaEvent(area, spawnerId);
		}

		@Override
		public CompoundTag serialize(CompoundTag data, SpawnMobAreaEvent event) {
			data.putString("Spawner", event.spawnerId);
			return data;
		}
	}
}
