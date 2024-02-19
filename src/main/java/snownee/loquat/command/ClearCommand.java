package snownee.loquat.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import snownee.loquat.command.argument.AreaArgument;
import snownee.loquat.core.AreaManager;

public class ClearCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("clear")
				.then(Commands.argument("areas", AreaArgument.areas())
						.then(Commands.literal("events")
								.executes(ctx -> {
									int count = AreaManager.of(ctx.getSource().getLevel()).clearEvents(AreaArgument.getAreas(ctx, "areas"));
									return LoquatCommand.countedSuccess(ctx, "clear.events", count);
								})
						)
						.then(Commands.literal("restrictions")
								.executes(ctx -> {
									int count = AreaManager.of(ctx.getSource().getLevel()).clearRestrictions(AreaArgument.getAreas(ctx, "areas"));
									return LoquatCommand.countedSuccess(ctx, "clear.restrictions", count);
								})
						)
				);
	}
}
