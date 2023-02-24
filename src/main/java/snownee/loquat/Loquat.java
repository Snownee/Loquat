package snownee.loquat;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import snownee.kiwi.Mod;
import snownee.kiwi.loader.Platform;

@Mod(Loquat.ID)
public class Loquat {

	public static final String ID = "loquat";
	public static final String NAME = "Loquat";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static boolean hasLychee;

	public static void init() {
		hasLychee = Platform.isModLoaded("lychee");
		LoquatRegistries.init();
		AreaTypes.init();
		AreaEventTypes.init();
	}

}
