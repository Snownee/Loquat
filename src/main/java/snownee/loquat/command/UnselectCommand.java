package snownee.loquat.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import snownee.loquat.command.argument.AreaArgument;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.network.SSyncSelectionPacket;

public class UnselectCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("unselect")
				.requires(CommandSourceStack::isPlayer)
				.then(Commands.argument("areas", AreaArgument.areas())
						.executes(ctx -> {
							var source = ctx.getSource();
							ServerPlayer player = source.getPlayerOrException();
							var selectedAreas = SelectionManager.of(player).getSelectedAreas();
							int count = 0;
							for (Area area : AreaArgument.getAreas(ctx, "areas")) {
								if (selectedAreas.contains(area.getUuid())) {
									selectedAreas.remove(area.getUuid());
									count++;
								}
							}
							SSyncSelectionPacket.sync(source.getPlayerOrException());
							return LoquatCommand.countedSuccess(ctx, "unselect", count);
						})
				);
	}

}
