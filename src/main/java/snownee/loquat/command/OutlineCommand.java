package snownee.loquat.command;

import java.util.Set;
import java.util.UUID;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import snownee.loquat.core.AreaManager;
import snownee.loquat.network.SOutlinesPacket;

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
						SOutlinesPacket.outlines(player, Long.MAX_VALUE, false, false, manager.areas());
						showOutlinePlayers.add(source.getEntityOrException().getUUID());
					} else {
						SOutlinesPacket.outlines(player, Long.MIN_VALUE, false, false, manager.areas());
						showOutlinePlayers.remove(source.getEntityOrException().getUUID());
					}
					source.sendSuccess(Component.translatable("loquat.command.outline.success", show), true);
					return show ? 1 : 0;
				});
	}

}
