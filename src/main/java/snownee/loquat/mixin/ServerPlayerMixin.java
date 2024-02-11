package snownee.loquat.mixin;

import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Sets;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import snownee.loquat.Hooks;
import snownee.loquat.LoquatConfig;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.RestrictInstance;
import snownee.loquat.core.area.Area;
import snownee.loquat.duck.LoquatServerPlayer;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements LoquatServerPlayer {

	@Unique
	private final Set<Area> loquat$areasIn = Sets.newHashSet();
	@Unique
	private RestrictInstance loquat$restriction;
	@Unique
	private Vec3 loquat$lastGoodPos;


	@Inject(method = "doTick", at = @At("HEAD"))
	private void loquat$doTick(CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		if (loquat$lastGoodPos != null && Hooks.checkServerPlayerRestriction(player, this)) {
			if (player.isPassenger()) {
				player.stopRiding();
			}
			player.teleportTo(loquat$lastGoodPos.x, loquat$lastGoodPos.y, loquat$lastGoodPos.z);
		} else {
			loquat$lastGoodPos = player.position();
		}
		if (player.tickCount % LoquatConfig.positionCheckInterval != 0) {
			return;
		}
		Hooks.tickServerPlayer(player, this);
	}

	@Inject(method = "teleportTo(DDD)V", at = @At("HEAD"), cancellable = true)
	private void loquat$teleportTo(double x, double y, double z, CallbackInfo ci) {
		if (Hooks.teleportServerPlayer((ServerPlayer) (Object) this, this, x, y, z)) {
			ci.cancel();
		}
	}

	@Override
	public Set<Area> loquat$getAreasIn() {
		return loquat$areasIn;
	}

	@Override
	public RestrictInstance loquat$getRestrictionInstance() {
		if (loquat$restriction == null) {
			ServerPlayer player = (ServerPlayer) (Object) this;
			loquat$restriction = AreaManager.of(player.serverLevel()).getOrCreateRestrictInstance(player.getScoreboardName());
		}
		return loquat$restriction;
	}

	@Override
	public void loquat$setLastGoodPos(Vec3 pos) {
		loquat$lastGoodPos = pos;
	}

	@Nullable
	@Override
	public Vec3 loquat$getLastGoodPos() {
		return loquat$lastGoodPos;
	}

	@Override
	public void loquat$reset() {
		loquat$areasIn.clear();
		loquat$restriction = null;
		loquat$lastGoodPos = null;
	}

}
