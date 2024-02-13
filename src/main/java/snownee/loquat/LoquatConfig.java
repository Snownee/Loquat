package snownee.loquat;

import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import snownee.kiwi.config.KiwiConfig;

@KiwiConfig
public class LoquatConfig {
	@KiwiConfig.Path("debug.enable")
	public static boolean debug;

	@KiwiConfig.Path("general.nearbyRange")
	@KiwiConfig.Range(min = 1)
	public static int nearbyRange = 100;

	@KiwiConfig.Path("general.selectionItem")
	public static String selectionItemId = "minecraft:spectral_arrow";

	public static Item selectionItem = Items.SPECTRAL_ARROW;

	@KiwiConfig.Path("restrict.positionCheckInterval")
	@KiwiConfig.Range(min = 1)
	public static int positionCheckInterval = 20;

	@KiwiConfig.Listen("general.selectionItem")
	public static void selectionItemChanged(String path) {
		selectionItem = Optional.ofNullable(ResourceLocation.tryParse(selectionItemId)).map(BuiltInRegistries.ITEM::get).orElse(Items.AIR);
	}
}
