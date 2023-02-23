package snownee.loquat;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import snownee.loquat.core.AreaEvent;
import snownee.loquat.core.area.Area;

public final class LoquatRegistries {

	public static void init() {
	}

	public static final MappedRegistry<Area.Type<?>> AREA = register("area", Area.Type.class);
	public static final MappedRegistry<AreaEvent.Type<?>> AREA_EVENT = register("area_event", AreaEvent.Type.class);

	private static <T> MappedRegistry<T> register(String name, Class<?> clazz) {
		return FabricRegistryBuilder.createSimple((Class<T>) clazz, new ResourceLocation(Loquat.ID, name)).attribute(RegistryAttribute.SYNCED).buildAndRegister();
	}

}
