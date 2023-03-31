package snownee.loquat.duck;

import net.minecraft.nbt.CompoundTag;

public interface LoquatStructurePiece {

	ThreadLocal<LoquatStructurePiece> CURRENT = new ThreadLocal<>();

	void loquat$setAttachedData(CompoundTag tag);

	CompoundTag loquat$getAttachedData();

}
