package snownee.loquat;

import snownee.kiwi.Mod;
import snownee.kiwi.loader.Platform;

@Mod(Loquat.ID)
public class Loquat {

	public static final String ID = "loquat";
	public static final String NAME = "Loquat";

	public static boolean hasLychee;

	public static void init() {
		hasLychee = Platform.isModLoaded("lychee");
		LoquatRegistries.init();
		AreaTypes.init();
		AreaEventTypes.init();
	}

}
