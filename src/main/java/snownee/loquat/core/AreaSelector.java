package snownee.loquat.core;

import java.util.function.Predicate;

import snownee.loquat.core.area.Area;

public class AreaSelector implements Predicate<Area> {

	public static AreaSelector fromString(String string) {
		return new AreaSelector();
	}

	@Override
	public boolean test(Area area) {
		return false;
	}
}
