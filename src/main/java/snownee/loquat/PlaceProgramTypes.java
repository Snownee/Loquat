package snownee.loquat;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoadingContext;
import snownee.loquat.program.PlaceProgram;
import snownee.loquat.program.SeaLevelPlaceProgram;

public class PlaceProgramTypes {

	public static final SeaLevelPlaceProgram.Type SEA_LEVEL = register("sea_level", new SeaLevelPlaceProgram.Type());

	public static void init() {
	}

	public static <T extends PlaceProgram.Type<?>> T register(String name, T t) {
		ModLoadingContext.get().setActiveContainer(null); // bypass Forge warning
		LoquatRegistries.PLACE_PROGRAM.register(new ResourceLocation(name), t);
		return t;
	}
}
