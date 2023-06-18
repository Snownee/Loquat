package snownee.loquat;

import java.util.Optional;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import snownee.kiwi.config.KiwiConfig;

@KiwiConfig(type = KiwiConfig.ConfigType.CLIENT)
public class LoquatClientConfig {
	@KiwiConfig.Path("restrict.notification")
	public static boolean restrictNotification = true;
}
