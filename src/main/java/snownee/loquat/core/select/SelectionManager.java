package snownee.loquat.core.select;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import snownee.loquat.LoquatCommonConfig;
import snownee.loquat.network.SSyncSelectionPacket;

public class SelectionManager {

	public static SelectionManager of(Player player) {
		return ((LoquatPlayer) player).loquat$getSelectionManager();
	}

	@Getter
	private final List<PosSelection> selections = Lists.newArrayList();
	@Getter
	private boolean lastOneIncomplete;

	public boolean leftClickBlock(ServerLevel world, BlockPos pos, ServerPlayer player) {
		if (!isHoldingTool(player))
			return false;
		if (lastOneIncomplete) {
			selections.get(selections.size() - 1).pos2 = pos;
		} else {
			selections.add(new PosSelection(pos));
		}
		lastOneIncomplete = !lastOneIncomplete;
		SSyncSelectionPacket.sync(player);
		return true;
	}

	public boolean rightClickItem(ServerLevel world, BlockPos pos, ServerPlayer player) {
		if (!isHoldingTool(player) || !player.isShiftKeyDown())
			return false;
		selections.clear();
		lastOneIncomplete = false;
		SSyncSelectionPacket.sync(player);
		return true;
	}

	public static boolean isHoldingTool(Player player) {
		return player.hasPermissions(2) && player.getMainHandItem().is(LoquatCommonConfig.selectionItem);
	}

}
