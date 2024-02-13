package snownee.loquat;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import snownee.loquat.core.AreaEvent;
import snownee.loquat.core.area.Area;
import snownee.loquat.program.PlaceProgram;
import snownee.loquat.util.RegistryBridge;
import snownee.lychee.Lychee;

public final class LoquatRegistries {

	public static RegistryBridge<Area.Type<?>> AREA;
	public static RegistryBridge<AreaEvent.Type<?>> AREA_EVENT;
	public static RegistryBridge<PlaceProgram.Type<?>> PLACE_PROGRAM;

	public static void init() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(LoquatRegistries::newRegistries);
	}

	public static void newRegistries(NewRegistryEvent event) {
		event.<Area.Type<?>>create(register("area"), v -> AREA = new RegistryBridge<>(v));
		event.<AreaEvent.Type<?>>create(register("area_event"), v -> AREA_EVENT = new RegistryBridge<>(v));
		event.<PlaceProgram.Type<?>>create(register("place_program"), v -> PLACE_PROGRAM = new RegistryBridge<>(v));
	}

	private static <T> RegistryBuilder<T> register(String name) {
		return new RegistryBuilder<T>().setName(new ResourceLocation(Lychee.ID, name));
	}

}
