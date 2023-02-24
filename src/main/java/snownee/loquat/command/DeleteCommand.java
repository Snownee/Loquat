package snownee.loquat.command;

import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.network.SSyncSelectionPacket;

public class DeleteCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("delete")
				.then(Commands.argument("uuid", UuidArgument.uuid())
						.executes(ctx -> {
							var uuid = UuidArgument.getUuid(ctx, "uuid");
							var source = ctx.getSource();
							var manager = AreaManager.of(source.getLevel());
							if (manager.remove(uuid)) {
								source.sendSuccess(Component.translatable("loquat.command.delete.success"), true);
								return 1;
							} else {
								source.sendFailure(Component.translatable("loquat.command.areaNotFound"));
								return 0;
							}
						})
				)
				.then(Commands.literal("selection")
						.executes(ctx -> {
							var source = ctx.getSource();
							var manager = AreaManager.of(source.getLevel());
							List<UUID> selectedAreas = SelectionManager.of(source.getPlayerOrException()).getSelectedAreas();
							int count = 0;
							for (UUID uuid : selectedAreas) {
								if (manager.remove(uuid)) {
									count++;
								}
							}
							if (count == 0) {
								source.sendFailure(Component.translatable("loquat.command.emptySelection"));
								return 0;
							}
							selectedAreas.clear();
							SSyncSelectionPacket.sync(source.getPlayerOrException());
							source.sendSuccess(Component.translatable("loquat.command.delete.success"), true);
							return count;
						})
				);
	}

}
