package snownee.loquat.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;
import snownee.loquat.spawner.SpawnerLoader;

public class SpawnCommand extends LoquatCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("spawn")
				.then(Commands.argument("spawner", StringArgumentType.string())
						.executes(ctx -> spawn(ctx, getOnlySelectedArea(ctx.getSource())))
						.then(Commands.argument("area", UuidArgument.uuid())
								.executes(ctx -> {
									Area area = AreaManager.of(ctx.getSource().getLevel()).get(UuidArgument.getUuid(ctx, "area"));
									if (area == null) {
										ctx.getSource().sendFailure(Component.translatable("loquat.command.areaNotFound"));
										return 0;
									}
									return spawn(ctx, area);
								})
						)
				);
	}

	private static int spawn(CommandContext<CommandSourceStack> ctx, Area area) {
		String spawnerId = StringArgumentType.getString(ctx, "spawner");
		var source = ctx.getSource();
		try {
			SpawnerLoader.INSTANCE.spawn(spawnerId, source.getLevel(), area, source.getLevel().getRandom());
		} catch (NullPointerException e) {
			source.sendFailure(Component.translatable("loquat.command.spawnerNotExists"));
			return 0;
		}
		source.sendSuccess(Component.translatable("loquat.command.spawn.success"), true);
		return 1;
	}

}
