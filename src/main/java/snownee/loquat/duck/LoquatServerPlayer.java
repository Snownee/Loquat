package snownee.loquat.duck;

import java.util.Set;

import snownee.loquat.core.RestrictInstance;
import snownee.loquat.core.area.Area;

public interface LoquatServerPlayer {

	Set<Area> loquat$getAreasIn();

	RestrictInstance loquat$getRestrictionInstance();

	void loquat$reset();
}
