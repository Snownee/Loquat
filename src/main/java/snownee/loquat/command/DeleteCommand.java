package snownee.loquat.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.network.SSyncSelectionPacket;

public class DeleteCommand extends LoquatCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("delete")
				.then(Commands.argument("uuid", UuidArgument.uuid())
						.executes(ctx -> {
							var uuid = UuidArgument.getUuid(ctx, "uuid");
							var source = ctx.getSource();
							var manager = AreaManager.of(source.getLevel());
							if (!manager.remove(uuid)) {
								source.sendFailure(Component.translatable("loquat.command.areaNotFound"));
								return 0;
							}
							source.sendSuccess(() -> Component.translatable("loquat.command.delete.success"), true);
							return 1;
						})
				)
				.then(Commands.literal("selection")
						.executes(ctx -> {
							var source = ctx.getSource();
							int count = forEachSelected(source, (area, manager) -> manager.remove(area.getUuid()));
							SelectionManager.of(source.getPlayerOrException()).getSelectedAreas().clear();
							SSyncSelectionPacket.sync(source.getPlayerOrException());
							source.sendSuccess(() -> Component.translatable("loquat.command.delete.success"), true);
							return count;
						})
				);
	}

}
