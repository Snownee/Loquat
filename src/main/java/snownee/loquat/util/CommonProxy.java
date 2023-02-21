package snownee.loquat.util;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import snownee.loquat.Loquat;
import snownee.loquat.command.LoquatCommand;
import snownee.loquat.core.select.SelectionManager;

public class CommonProxy implements ModInitializer {
	@Override
	public void onInitialize() {
		Loquat.init();
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
