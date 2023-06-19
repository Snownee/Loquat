package snownee.loquat.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface LoquatPlacer {
	boolean accept(ResourceLocation structureId);

	Structure.GenerationStub place(ResourceLocation structureId, Structure.GenerationContext generationContext, BlockPos defaultStartPos, VoxelShape defaultValidSpace, int range, Registry<StructureTemplatePool> poolRegistry, PoolElementStructurePiece defaultStartPiece);
}
