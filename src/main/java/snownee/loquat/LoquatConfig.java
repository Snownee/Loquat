package snownee.loquat;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import snownee.kiwi.config.KiwiConfig;

@KiwiConfig
public class LoquatConfig {
	@KiwiConfig.Path("debug.enable")
	public static boolean debug = true;

	public static Item selectionItem = Items.SPECTRAL_ARROW;
}
