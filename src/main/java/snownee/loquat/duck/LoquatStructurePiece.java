package snownee.loquat.duck;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;

public interface LoquatStructurePiece {

	ThreadLocal<Pair<LoquatStructurePiece, RegistryAccess>> CURRENT = new ThreadLocal<>();

	static LoquatStructurePiece current() {
		Pair<LoquatStructurePiece, RegistryAccess> pair = CURRENT.get();
		return pair == null ? null : pair.getLeft();
	}

	void loquat$setAttachedData(CompoundTag tag);

	CompoundTag loquat$getAttachedData();

}
