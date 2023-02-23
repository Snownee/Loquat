package snownee.loquat.core;

import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import snownee.loquat.LoquatRegistries;
import snownee.loquat.core.area.Area;

public abstract class AreaEvent {

	public int ticksExisted;
	@Getter
	protected boolean isFinished;
	@Getter
	protected final Area area;

	protected AreaEvent(Area area) {
		this.area = area;
	}

	public abstract void tick(ServerLevel world);

	public abstract Type<?> getType();

	public static AreaEvent deserialize(AreaManager manager, CompoundTag data) {
		var area = manager.get(data.getUUID("Area"));
		if (area == null) {
			return null;
		}
		AreaEvent.Type<?> type = LoquatRegistries.AREA_EVENT.get(new ResourceLocation(data.getString("Type")));
		AreaEvent event = type.deserialize(area, data);
		event.ticksExisted = data.getInt("Ticks");
		return event;
	}

	@SuppressWarnings("rawtypes")
	public final CompoundTag serialize(CompoundTag data) {
		data.putString("Type", LoquatRegistries.AREA_EVENT.getKey(getType()).toString());
		((Type) getType()).serialize(data, this);
		data.putUUID("Area", area.getUuid());
		data.putInt("Ticks", ticksExisted);
		return data;
	}

	public static abstract class Type<T extends AreaEvent> {
		public abstract T deserialize(Area area, CompoundTag data);

		public abstract CompoundTag serialize(CompoundTag data, T event);
	}
}
