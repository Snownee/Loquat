package snownee.loquat;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.Util;
import snownee.loquat.command.argument.AreaSelectorOptions;
import snownee.loquat.program.PlaceProgram;
import snownee.loquat.util.CommonProxy;

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
		AreaSelectorOptions.bootstrap();
		CommonProxy.registerReloadListener(PlaceProgram.LOADER);
	}

	public static ResourceLocation id(String s) {
		return Util.RL(s, ID);
	}
}
