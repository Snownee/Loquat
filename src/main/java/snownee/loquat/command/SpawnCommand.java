package snownee.loquat.command;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;
import snownee.loquat.spawner.SpawnerLoader;

public class SpawnCommand extends LoquatCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("spawn")
				.then(Commands.argument("spawner", ResourceLocationArgument.id())
						.executes(ctx -> spawn(ctx, getOnlySelectedArea(ctx.getSource()), null))
						.then(Commands.argument("area", UuidArgument.uuid())
								.executes(ctx -> spawn(ctx, null, null))
								.then(Commands.argument("difficulty", StringArgumentType.string())
										.executes(ctx -> spawn(ctx, null, StringArgumentType.getString(ctx, "difficulty")))
								)
						)
				);
	}

	private static int spawn(CommandContext<CommandSourceStack> ctx, @Nullable Area area, @Nullable String difficultyId) {
		if (area == null) {
			area = AreaManager.of(ctx.getSource().getLevel()).get(UuidArgument.getUuid(ctx, "area"));
			if (area == null) {
				ctx.getSource().sendFailure(Component.translatable("loquat.command.areaNotFound"));
				return 0;
			}
		}
		ResourceLocation spawnerId = ResourceLocationArgument.getId(ctx, "spawner");
		var source = ctx.getSource();
		try {
			SpawnerLoader.INSTANCE.spawn(spawnerId, difficultyId, source.getLevel(), area);
		} catch (NullPointerException e) {
			source.sendFailure(Component.translatable("loquat.command.spawnerNotExists"));
			return 0;
		}
		source.sendSuccess(() -> Component.translatable("loquat.command.spawn.success", spawnerId), true);
		return 1;
	}

}
