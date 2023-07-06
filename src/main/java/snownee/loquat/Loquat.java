package snownee.loquat;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.SharedConstants;
import snownee.kiwi.loader.Platform;

public class Loquat {

	public static final String ID = "loquat";
	public static final String NAME = "Loquat";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static boolean hasLychee;

	public static void init() {
		if (LoquatConfig.debug && !Platform.isProduction()) {
			// print command exceptions
			SharedConstants.IS_RUNNING_IN_IDE = true;
		}
		hasLychee = Platform.isModLoaded("lychee");
		LoquatRegistries.init();
	}

}
