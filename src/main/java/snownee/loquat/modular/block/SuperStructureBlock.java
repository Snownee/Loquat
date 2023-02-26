package snownee.loquat.modular.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SuperStructureBlock extends StructureBlock {

	public SuperStructureBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SuperStructureBlockEntity(pos, state);
	}

}
