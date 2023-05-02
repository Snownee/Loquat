package snownee.loquat;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import snownee.loquat.core.AreaEvent;
import snownee.loquat.core.area.Area;
import snownee.loquat.program.PlaceProgram;

public final class LoquatRegistries {

	public static final MappedRegistry<Area.Type<?>> AREA = register("area", Area.Type.class);
	public static final MappedRegistry<AreaEvent.Type<?>> AREA_EVENT = register("area_event", AreaEvent.Type.class);
	public static final MappedRegistry<PlaceProgram.Type<?>> PLACE_PROGRAM = register("place_program", PlaceProgram.Type.class);

	public static void init() {
	}

	private static <T> MappedRegistry<T> register(String name, Class<?> clazz) {
		return FabricRegistryBuilder.createSimple((Class<T>) clazz, new ResourceLocation(Loquat.ID, name)).attribute(RegistryAttribute.SYNCED).buildAndRegister();
	}

}
