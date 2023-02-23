package snownee.loquat.util;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import snownee.loquat.Loquat;
import snownee.loquat.command.LoquatCommand;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.spawner.SpawnerLoader;
import snownee.loquat.spawner.lychee.SpawnMobAction;
import snownee.lychee.PostActionTypes;

public class CommonProxy implements ModInitializer {
	public static final ResourceLocation SPAWNER_LOADER_ID = new ResourceLocation(Loquat.ID, "spawners");

	@Override
	public void onInitialize() {
		Loquat.init();
		if (Loquat.hasLychee) {
			ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener((IdentifiableResourceReloadListener) SpawnerLoader.INSTANCE);
			PostActionTypes.register("loquat:spawn", SpawnMobAction.TYPE);
		}
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LoquatCommand.register(dispatcher);
		});
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (!world.isClientSide && hand == InteractionHand.MAIN_HAND &&
					SelectionManager.of(player).leftClickBlock((ServerLevel) world, pos, (ServerPlayer) player)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!world.isClientSide && hand == InteractionHand.MAIN_HAND &&
					SelectionManager.of(player).rightClickItem((ServerLevel) world, player.blockPosition(), (ServerPlayer) player)) {
				return InteractionResultHolder.success(stack);
			}
			return InteractionResultHolder.pass(stack);
		});
	}
}
