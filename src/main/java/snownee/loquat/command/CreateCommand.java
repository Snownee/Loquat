package snownee.loquat.command;

import java.util.UUID;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.AABBArea;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.network.SSyncSelectionPacket;

public class CreateCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("create")
				.executes(ctx -> {
					var source = ctx.getSource();
					var selections = SelectionManager.of(source.getPlayerOrException()).getSelections();
					if (selections.isEmpty()) {
						source.sendFailure(Component.translatable("loquat.command.emptySelection"));
						return 0;
					}
					if (selections.size() > 1) {
						source.sendFailure(Component.translatable("loquat.command.tooManySelections"));
						return 0;
					}
					var selection = selections.get(0);
					selections.clear();
					SSyncSelectionPacket.sync(source.getPlayerOrException());
					var area = new AABBArea(selection.toAABB());
					return addArea(source, area);
				})
				.then(Commands.literal("box")
						.then(Commands.argument("begin", Vec3Argument.vec3(false))
								.then(Commands.argument("end", Vec3Argument.vec3(false))
										.executes(ctx -> {
											var begin = Vec3Argument.getVec3(ctx, "begin");
											var end = Vec3Argument.getVec3(ctx, "end");
											var area = AABBArea.of(begin, end);
											return addArea(ctx.getSource(), area);
										})
								)
						)
				);
	}

	private static int addArea(CommandSourceStack source, Area area) {
		area.setUuid(UUID.randomUUID());
		var manager = AreaManager.of(source.getLevel());
		if (manager.contains(area)) {
			source.sendFailure(Component.translatable("loquat.command.areaAlreadyExists"));
			return 0;
		}
		manager.add(area);
		source.sendSuccess(() -> Component.translatable("loquat.command.create.success"), true);
		return 1;
	}

}
