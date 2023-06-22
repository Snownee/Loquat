package snownee.loquat.duck;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.phys.Vec3;
import snownee.loquat.core.RestrictInstance;
import snownee.loquat.core.area.Area;

public interface LoquatServerPlayer {

	Set<Area> loquat$getAreasIn();

	RestrictInstance loquat$getRestrictionInstance();

	void loquat$setLastGoodPos(Vec3 pos);

	@Nullable
	Vec3 loquat$getLastGoodPos();

	void loquat$reset();
}
