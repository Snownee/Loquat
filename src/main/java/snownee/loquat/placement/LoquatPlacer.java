package snownee.loquat.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface LoquatPlacer {
	boolean accept(ResourceLocation structureId);

	Structure.GenerationStub place(ResourceLocation structureId, RandomState randomState, BlockPos defaultStartPos, VoxelShape defaultValidSpace, int range, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, LevelHeightAccessor levelHeightAccessor, WorldgenRandom worldgenRandom, Registry<StructureTemplatePool> poolRegistry, PoolElementStructurePiece defaultStartPiece);
}
