package snownee.loquat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;

@Mixin(StructureBlockEntity.class)
public interface StructureBlockEntityAccess {

	@Accessor("mode")
	void loquat$setMode(StructureMode mode);

}
