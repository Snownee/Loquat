package snownee.loquat.command;

import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.network.SSyncSelectionPacket;

// A temporary solution before the area selector is implemented
@Deprecated
public class SelectTagCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("select_tag")
				.then(Commands.argument("tag", StringArgumentType.string())
						.executes(ctx -> {
							var source = ctx.getSource();
							String tag = StringArgumentType.getString(ctx, "tag");
							AreaManager manager = AreaManager.of(source.getLevel());
							List<UUID> selectedAreas = SelectionManager.of(source.getPlayerOrException()).getSelectedAreas();
							manager.areas().forEach(area -> {
								if (area.getTags().contains(tag)) {
									selectedAreas.add(area.getUuid());
								}
							});
							SSyncSelectionPacket.sync(source.getPlayerOrException());
							return 1;
						})
				);
	}

}
