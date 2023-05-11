package snownee.loquat.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import snownee.loquat.Hooks;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;

@Mixin(value = StructureTemplate.class, priority = 800)
public class StructureTemplateMixin {

	private final List<Area> loquat$areas = Lists.newArrayList();
	@Shadow
	private Vec3i size;

	@Inject(method = "load", at = @At("HEAD"))
	private void loquat$load(CompoundTag tag, CallbackInfo ci) {
		loquat$areas.clear();
		if (tag.contains("Loquat", Tag.TAG_COMPOUND)) {
			CompoundTag loquat = tag.getCompound("Loquat");
			loquat$areas.addAll(AreaManager.loadAreas(loquat.getList("Areas", Tag.TAG_COMPOUND)));
		}
	}

	@Inject(method = "save", at = @At("HEAD"))
	private void loquat$save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (loquat$areas.isEmpty())
			return;
		CompoundTag loquat = new CompoundTag();
		loquat.put("Areas", AreaManager.saveAreas(loquat$areas));
		tag.put("Loquat", loquat);
	}

	@Inject(method = "placeInWorld", at = @At("HEAD"))
	private void loquat$placeInWorld(ServerLevelAccessor serverLevel, BlockPos pos, BlockPos blockPos, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
		if (loquat$areas.isEmpty()) {
			return;
		}
		AreaManager manager = AreaManager.of(serverLevel.getLevel());
		Hooks.placeInWorld(manager, pos, blockPos, loquat$areas, settings, size);
	}

	@Inject(method = "fillFromWorld", at = @At("HEAD"))
	private void loquat$fillFromWorld(Level level, BlockPos pos, Vec3i size, boolean withEntities, Block toIgnore, CallbackInfo ci) {
		loquat$areas.clear();
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		AreaManager manager = AreaManager.of(serverLevel);
		Hooks.fillFromWorld(manager, pos, size, loquat$areas);
	}

}
