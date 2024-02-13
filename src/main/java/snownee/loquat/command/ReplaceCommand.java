package snownee.loquat.command;

import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import snownee.loquat.AreaTypes;
import snownee.loquat.command.argument.AreaArgument;
import snownee.loquat.core.AreaManager;
import snownee.loquat.network.SSyncSelectionPacket;
import snownee.loquat.util.AABBSerializer;

public class ReplaceCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("replace")
				.requires(CommandSourceStack::isPlayer)
				.then(Commands.argument("area", AreaArgument.area())
						.executes(ctx -> {
							var area = AreaArgument.getArea(ctx, "area");
							if (area.getType() != AreaTypes.BOX) {
								throw LoquatCommand.AREA_MUST_BE_BOX.create();
							}
							var selection = LoquatCommand.getSingleSelectionAndClear(ctx);
							var tag = AreaManager.saveAreas(List.of(area));
							tag.getCompound(0).put("AABB", AABBSerializer.write(selection.toAABB()));
							var source = ctx.getSource();
							var areaManager = AreaManager.of(source.getLevel());
							areaManager.remove(area.getUuid());
							areaManager.add(AreaManager.loadAreas(tag).get(0));
							SSyncSelectionPacket.sync(source.getPlayerOrException());
							source.sendSuccess(() -> Component.translatable("loquat.command.replace.success"), true);
							return 1;
						})
				);
	}

}
