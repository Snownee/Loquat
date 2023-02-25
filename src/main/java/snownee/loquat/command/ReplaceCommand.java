package snownee.loquat.command;

import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import snownee.loquat.AreaTypes;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.network.SSyncSelectionPacket;
import snownee.loquat.util.AABBSerializer;

public class ReplaceCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("replace")
				.executes(ctx -> {
					var source = ctx.getSource();
					SelectionManager selectionManager = SelectionManager.of(source.getPlayerOrException());
					var selections = selectionManager.getSelections();
					var selectedAreas = selectionManager.getSelectedAreas();
					if (selections.isEmpty() || selectedAreas.isEmpty()) {
						source.sendFailure(Component.translatable("loquat.command.emptySelection"));
						return 0;
					}
					if (selections.size() > 1 || selectedAreas.size() > 1) {
						source.sendFailure(Component.translatable("loquat.command.tooManySelections"));
						return 0;
					}
					var areaManager = AreaManager.of(source.getLevel());
					var area = areaManager.get(selectedAreas.get(0));
					if (area == null || area.getType() != AreaTypes.BOX) {
						source.sendFailure(Component.translatable("loquat.command.areaMustBeBox"));
						return 0;
					}
					var selection = selections.get(0);
					var tag = AreaManager.saveAreas(List.of(area), false);
					tag.getCompound(0).put("AABB", AABBSerializer.write(selection.toAABB()));
					areaManager.add(AreaManager.loadAreas(tag).get(0));
					selections.clear();
					SSyncSelectionPacket.sync(source.getPlayerOrException());
					source.sendSuccess(Component.translatable("loquat.command.replace.success"), true);
					return 1;
				});
	}

}
