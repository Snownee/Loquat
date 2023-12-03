package snownee.loquat.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;

public class ClearCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("clear")
				.then(Commands.literal("events")
						.executes(ctx -> {
							AreaManager.of(ctx.getSource().getLevel()).clearEvents();
							ctx.getSource().sendSuccess(() -> Component.translatable("loquat.command.clear.events.success"), true);
							return 1;
						})
				)
				.then(Commands.literal("restrictions")
						.executes(ctx -> {
							AreaManager.of(ctx.getSource().getLevel()).clearRestrictions();
							ctx.getSource().sendSuccess(() -> Component.translatable("loquat.command.clear.restrictions.success"), true);
							return 1;
						})
				);
	}
}
