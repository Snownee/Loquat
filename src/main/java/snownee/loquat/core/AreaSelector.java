package snownee.loquat.core;

import snownee.loquat.core.area.Area;

import java.util.function.Predicate;

public class AreaSelector implements Predicate<Area> {

	public static AreaSelector fromString(String string) {
		return new AreaSelector();
	}

	@Override
	public boolean test(Area area) {
		return false;
	}
}
