package snownee.loquat;

import java.util.Optional;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import snownee.kiwi.config.KiwiConfig;

@KiwiConfig
public class LoquatConfig {
	@KiwiConfig.Path("debug.enable")
	public static boolean debug;

	@KiwiConfig.Path("general.selectionItem")
	public static String selectionItemId = "minecraft:spectral_arrow";

	public static Item selectionItem = Items.SPECTRAL_ARROW;

	@KiwiConfig.Path("restrict.postionCheckInterval")
	@KiwiConfig.Range(min = 1)
	public static int postionCheckInterval = 20;

	public static void onChanged(String path) {
		if ("general.selectionItem".equals(path)) {
			selectionItem = Optional.ofNullable(ResourceLocation.tryParse(selectionItemId)).map(Registry.ITEM::get).orElse(Items.AIR);
		}
	}
}
