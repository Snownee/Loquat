package snownee.loquat;

import net.minecraft.core.Registry;
import snownee.loquat.core.area.AABBArea;
import snownee.loquat.core.area.Area;

public class AreaTypes {

	public static void init() {
	}

	public static final AABBArea.Type BOX = register("box", new AABBArea.Type());

	public static <T extends Area.Type<?>> T register(String name, T t) {
		Registry.register(LoquatRegistries.AREA, name, t);
		return t;
	}
}
