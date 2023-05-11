package snownee.loquat.command;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.RestrictInstance;
import snownee.loquat.network.SSyncRestrictionPacket;

public class RestrictCommand extends LoquatCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		var builder = Commands.argument("players", ScoreHolderArgument.scoreHolders());
		builder.then(register("*"));
		for (var behavior : RestrictBehavior.values()) {
			builder.then(register(behavior.name));
		}
		return Commands.literal("restrict").then(builder);
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
		var changed = new MutableInt();
		forEachSelected(source, (area, manager) -> {
			for (var player : names) {
				var restrictions = RestrictInstance.of(source.getLevel(), player);
				for (RestrictBehavior restrictBehavior : RestrictBehavior.VALUES) {
					if (behavior.equals("*") || behavior.equals(restrictBehavior.name)) {
						restrictions.restrict(area, restrictBehavior, value);
						changed.increment();
					}
				}
			}
			return true;
		});
		if (changed.intValue() == 0) {
			ctx.getSource().sendFailure(Component.translatable("loquat.command.nothingChanged"));
			return 0;
		}
		AreaManager.of(source.getLevel()).setDirty();
		List<ServerPlayer> players = source.getLevel().players();
		if (!names.contains("*")) {
			players = players.stream().filter($ -> names.contains($.getScoreboardName())).toList();
		}
		for (ServerPlayer player : players) {
			SSyncRestrictionPacket.sync(player);
		}
		ctx.getSource().sendSuccess(Component.translatable("loquat.command.restrict.success", changed.intValue()), true);
		return players.size();
	}

	public enum RestrictBehavior {
		ENTER("enter"),
		EXIT("exit"),
		BREAK("break"),
		PLACE("place"),
		;

		public static final RestrictBehavior[] VALUES = values();
		private final String name;

		RestrictBehavior(String name) {
			this.name = name;
		}

		public MutableComponent getDisplayName() {
			return Component.translatable("loquat.restrict.behavior." + name);
		}
	}

}
