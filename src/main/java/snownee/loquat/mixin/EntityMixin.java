package snownee.loquat.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.loquat.Hooks;
import snownee.loquat.core.RestrictInstance;

@Mixin(Entity.class)
public class EntityMixin {

	@Inject(
			method = "collideBoundingBox", at = @At(
			value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"
	), locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void loquat$collideBoundingBox(@Nullable Entity entity, Vec3 vec, AABB collisionBox, Level level, List<VoxelShape> potentialHits, CallbackInfoReturnable<Vec3> cir, ImmutableList.Builder<VoxelShape> builder) {
		if (entity == null || entity.getType() != EntityType.PLAYER) {
			return;
		}
		Hooks.collideWithLoquatAreas(RestrictInstance.of((Player) entity), collisionBox, vec, builder);
	}

}
