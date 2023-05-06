package snownee.loquat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import snownee.loquat.core.AreaEvent;
import snownee.loquat.duck.LoquatMob;

//TODO: inject to PathfinderMob#getWalkTargetValue?
@Mixin(Mob.class)
public class MobMixin implements LoquatMob {

	private AreaEvent loquat$restriction;
	@Shadow
	private BlockPos restrictCenter;
	@Shadow
	private float restrictRadius;

	@Override
	public void loquat$setRestriction(AreaEvent event) {
		loquat$restriction = event;
	}

	@Inject(method = "isWithinRestriction(Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
	private void loquat$isWithinRestriction(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
		if (loquat$restriction != null) {
			info.setReturnValue(loquat$restriction.getArea().contains(pos));
		}
	}

	@Inject(method = "hasRestriction", at = @At("HEAD"), cancellable = true)
	private void loquat$hasRestriction(CallbackInfoReturnable<Boolean> info) {
		if (loquat$restriction != null) {
			info.setReturnValue(true);
		}
	}

	@Inject(method = "clearRestriction", at = @At("HEAD"))
	private void loquat$clearRestriction(CallbackInfo ci) {
		loquat$restriction = null;
	}

	@Inject(method = "getRestrictCenter", at = @At("HEAD"))
	private void loquat$getRestrictCenter(CallbackInfoReturnable<BlockPos> info) {
		if (loquat$restriction != null && restrictCenter == BlockPos.ZERO) {
			restrictCenter = new BlockPos(loquat$restriction.getArea().getCenter());
		}
	}

	@Inject(method = "getRestrictRadius", at = @At("HEAD"))
	private void loquat$getRestrictRadius(CallbackInfoReturnable<Float> info) {
		if (loquat$restriction != null && restrictRadius == -1.0f) {
			AABB aabb = loquat$restriction.getArea().getRoughAABB();
			restrictRadius = (float) Math.max(aabb.getXsize(), aabb.getZsize()) / 2;
		}
	}
}
