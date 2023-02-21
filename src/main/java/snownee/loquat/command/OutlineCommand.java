package snownee.loquat.command;

import java.util.Set;
import java.util.UUID;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import snownee.loquat.core.AreaManager;
import snownee.loquat.network.SHighlightPacket;

public class OutlineCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("outline")
				.executes(ctx -> {
					var source = ctx.getSource();
					var manager = AreaManager.of(source.getLevel());
					Set<UUID> showOutlinePlayers = manager.getShowOutlinePlayers();
					ServerPlayer player = source.getPlayerOrException();
					boolean show = !showOutlinePlayers.contains(player.getUUID());
					if (show) {
						SHighlightPacket.highlight(player, Long.MAX_VALUE, manager.areas());
						showOutlinePlayers.add(source.getEntityOrException().getUUID());
					} else {
						SHighlightPacket.highlight(player, Long.MIN_VALUE, manager.areas());
						showOutlinePlayers.remove(source.getEntityOrException().getUUID());
					}
					source.sendSuccess(Component.translatable("loquat.command.outline.success", show), true);
					return show ? 1 : 0;
				});
	}

}
