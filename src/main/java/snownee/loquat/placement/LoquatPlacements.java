package snownee.loquat.placement;

import java.util.List;

import com.google.common.base.Preconditions;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;

public class LoquatPlacements {
	public static final List<Pair<ResourceLocation, LoquatPlacer>> PLACERS = ObjectArrayList.of();

	public static LoquatPlacer getPlacerFor(ResourceLocation structureId) {
		return PLACERS.stream().map(Pair::right).filter($ -> $.accept(structureId)).findFirst().orElse(null);
	}

	public static void register(ResourceLocation id, LoquatPlacer placer) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(placer);
		PLACERS.removeIf($ -> $.left().equals(id));
		PLACERS.add(Pair.of(id, placer));
	}
}
