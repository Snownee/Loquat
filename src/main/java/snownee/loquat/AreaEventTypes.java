package snownee.loquat;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoadingContext;
import snownee.loquat.core.AreaEvent;
import snownee.loquat.spawner.SpawnMobAreaEvent;

public class AreaEventTypes {

	public static final SpawnMobAreaEvent.Type SPAWN_MOBS = register("spawn_mobs", new SpawnMobAreaEvent.Type());

	public static void init() {
	}

	public static <T extends AreaEvent.Type<?>> T register(String name, T t) {
		ModLoadingContext.get().setActiveContainer(null); // bypass Forge warning
		LoquatRegistries.AREA_EVENT.register(new ResourceLocation(name), t);
		return t;
	}
}
