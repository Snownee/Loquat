package snownee.loquat.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.network.SSyncSelectionPacket;

public class UnselectCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("unselect")
				.then(Commands.literal("all")
						.executes(ctx -> {
							var source = ctx.getSource();
							SelectionManager.of(source.getPlayerOrException()).getSelectedAreas().clear();
							SSyncSelectionPacket.sync(source.getPlayerOrException());
							return 1;
						})
				);
	}

}
