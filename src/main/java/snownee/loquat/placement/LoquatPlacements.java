package snownee.loquat.placement;

import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import snownee.loquat.Loquat;

public class LoquatPlacements {
	public static final List<Pair<ResourceLocation, LoquatPlacer>> PLACERS = ObjectArrayList.of();
	public static final TicketType<ChunkPos> TICKET_TYPE = TicketType.create(Loquat.ID, Comparator.comparingLong(ChunkPos::toLong));

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
