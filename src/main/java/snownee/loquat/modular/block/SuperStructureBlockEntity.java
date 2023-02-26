package snownee.loquat.modular.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import snownee.loquat.mixin.BlockEntityAccess;
import snownee.loquat.mixin.StructureBlockEntityAccess;
import snownee.loquat.modular.ModularModule;

public class SuperStructureBlockEntity extends StructureBlockEntity {
	public SuperStructureBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(blockPos, blockState);
		((BlockEntityAccess) this).setType(ModularModule.SUPER_STRUCTURE_BLOCK_ENTITY.get());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
	}

	@Override
	public void setMode(StructureMode mode) {
		((StructureBlockEntityAccess) this).loquat$setMode(mode);
		BlockState blockState = this.level.getBlockState(this.getBlockPos());
		if (ModularModule.SUPER_STRUCTURE_BLOCK.is(blockState)) {
			this.level.setBlock(this.getBlockPos(), blockState.setValue(StructureBlock.MODE, mode), 2);
		}
	}
}
