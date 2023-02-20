package snownee.loquat;

import snownee.kiwi.Mod;

@Mod(Loquat.ID)
public class Loquat {

	public static final String ID = "loquat";
	public static final String NAME = "Loquat";

	public static void init() {
		LoquatRegistries.init();
		AreaTypes.init();
	}

}
