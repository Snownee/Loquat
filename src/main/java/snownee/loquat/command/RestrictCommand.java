package snownee.loquat.command;

import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import snownee.loquat.command.argument.AreaArgument;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.RestrictInstance;
import snownee.loquat.core.area.Area;
import snownee.loquat.network.SSyncRestrictionPacket;

public class RestrictCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		var builder = Commands.argument("players", ScoreHolderArgument.scoreHolders());
		builder.then(register("*"));
		for (var behavior : RestrictInstance.RestrictBehavior.values()) {
			builder.then(register(behavior.name));
		}
		return Commands.literal("restrict")
				.then(Commands.argument("areas", AreaArgument.areas())
						.then(builder));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> register(String behavior) {
		return Commands.literal(behavior)
				.then(Commands.literal("true")
						.executes(ctx -> execute(ctx, behavior, false))
				)
				.then(Commands.literal("false")
						.executes(ctx -> execute(ctx, behavior, true))
				);
	}

	private static int execute(CommandContext<CommandSourceStack> ctx, String behavior, boolean value) throws CommandSyntaxException {
		StringRange playersRange = ctx.getNodes().get(2).getRange();
		if (playersRange.get(ctx.getInput()).startsWith("@e")) {
			ctx.getSource().sendFailure(Component.translatable("loquat.command.entitySelectorNotAllowed"));
			return 0;
		}
		var source = ctx.getSource();
		var names = ScoreHolderArgument.getNames(ctx, "players", () -> List.of("*"));
		var areas = AreaArgument.getAreas(ctx, "areas");
		int count = 0;
		for (Area area : areas) {
			for (var player : names) {
				var restrictions = RestrictInstance.of(source.getLevel(), player);
				for (RestrictInstance.RestrictBehavior restrictBehavior : RestrictInstance.RestrictBehavior.VALUES) {
					if (behavior.equals("*") || behavior.equals(restrictBehavior.name)) {
						restrictions.restrict(area, restrictBehavior, value);
						count++;
					}
				}
			}
		}
		if (count > 0) {
			AreaManager.of(source.getLevel()).setDirty();
			List<ServerPlayer> players = source.getLevel().players();
			if (!names.contains("*")) {
				players = players.stream().filter($ -> names.contains($.getScoreboardName())).toList();
			}
			for (ServerPlayer player : players) {
				SSyncRestrictionPacket.sync(player);
			}
		}
		return LoquatCommand.countedSuccess(ctx, "restrict", count);
	}

}
