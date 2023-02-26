package snownee.loquat.modular;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.loquat.modular.block.SuperStructureBlock;
import snownee.loquat.modular.block.SuperStructureBlockEntity;

@KiwiModule("modular")
public class ModularModule extends AbstractModule {

	public static final KiwiGO<SuperStructureBlock> SUPER_STRUCTURE_BLOCK = go(() -> new SuperStructureBlock(blockProp(Blocks.STRUCTURE_BLOCK)));

	@KiwiModule.Name("super_structure_block")
	public static final KiwiGO<BlockEntityType<SuperStructureBlockEntity>> SUPER_STRUCTURE_BLOCK_ENTITY = blockEntity(SuperStructureBlockEntity::new, null, SUPER_STRUCTURE_BLOCK);

}
