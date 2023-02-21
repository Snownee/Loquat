package snownee.loquat.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.AABBArea;

import java.util.UUID;

public class CreateCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("create")
				.then(Commands.literal("box")
						.then(Commands.argument("begin", Vec3Argument.vec3(false))
								.then(Commands.argument("end", Vec3Argument.vec3(false))
										.executes(ctx -> {
											var begin = Vec3Argument.getVec3(ctx, "begin");
											var end = Vec3Argument.getVec3(ctx, "end");
											var area = AABBArea.of(begin, end);
											area.setUuid(UUID.randomUUID());
											var source = ctx.getSource();
											var manager = AreaManager.of(source.getLevel());
											if (manager.contains(area)) {
												source.sendFailure(Component.translatable("loquat.command.areaAlreadyExists"));
												return 0;
											}
											manager.add(area);
											source.sendSuccess(Component.translatable("loquat.command.create.success"), true);
											return 1;
										})
								)
						)
				);
	}

}
