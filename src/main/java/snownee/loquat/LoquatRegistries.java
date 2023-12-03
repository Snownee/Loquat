package snownee.loquat;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import snownee.loquat.core.AreaEvent;
import snownee.loquat.core.area.Area;
import snownee.loquat.program.PlaceProgram;

public final class LoquatRegistries {

	public static final MappedRegistry<Area.Type<?>> AREA = register("area");
	public static final MappedRegistry<AreaEvent.Type<?>> AREA_EVENT = register("area_event");
	public static final MappedRegistry<PlaceProgram.Type<?>> PLACE_PROGRAM = register("place_program");

	public static void init() {
	}

	private static <T> MappedRegistry<T> register(String name) {
		return FabricRegistryBuilder.createSimple(ResourceKey.<T>createRegistryKey(new ResourceLocation(Loquat.ID, name))).attribute(RegistryAttribute.SYNCED).buildAndRegister();
	}

}
