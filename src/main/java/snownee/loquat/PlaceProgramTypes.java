package snownee.loquat;

import net.minecraft.core.Registry;
import snownee.loquat.program.PlaceProgram;
import snownee.loquat.program.SeaLevelPlaceProgram;

public class PlaceProgramTypes {

	public static final SeaLevelPlaceProgram.Type SEA_LEVEL = register("sea_level", new SeaLevelPlaceProgram.Type());

	public static void init() {
	}

	public static <T extends PlaceProgram.Type<?>> T register(String name, T t) {
		Registry.register(LoquatRegistries.PLACE_PROGRAM, name, t);
		return t;
	}
}
