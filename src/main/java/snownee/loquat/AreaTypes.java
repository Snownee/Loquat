package snownee.loquat;

import snownee.kiwi.util.Util;
import snownee.loquat.core.area.AABBArea;
import snownee.loquat.core.area.Area;

public class AreaTypes {

	public static void init() {
	}

	public static final AABBArea.Type BOX = register("box", new AABBArea.Type());

	public static <T extends Area.Type<?>> T register(String name, T t) {
		LoquatRegistries.AREA.register(Util.RL(name), t);
		return t;
	}
}
