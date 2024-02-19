package snownee.loquat;

import net.minecraft.core.Registry;
import snownee.loquat.core.AreaEvent;
import snownee.loquat.spawner.SpawnMobAreaEvent;

public class AreaEventTypes {

	public static final SpawnMobAreaEvent.Type SPAWN_MOBS = register("spawn_mobs", new SpawnMobAreaEvent.Type());

	public static void init() {
	}

	public static <T extends AreaEvent.Type<?>> T register(String name, T t) {
		Registry.register(LoquatRegistries.AREA_EVENT, name, t);
		return t;
	}
}
