package snownee.loquat.core.select;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import snownee.loquat.LoquatConfig;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;
import snownee.loquat.duck.LoquatPlayer;
import snownee.loquat.network.SSyncSelectionPacket;
import snownee.loquat.util.LoquatUtil;
import snownee.loquat.util.TransformUtil;

public class SelectionManager {

	@Getter
	private final List<PosSelection> selections;
	@Getter
	private final List<UUID> selectedAreas;
	@Getter
	private boolean lastOneIncomplete;

	public SelectionManager(boolean isClientSide) {
		selections = isClientSide ? Collections.synchronizedList(Lists.newArrayList()) : Lists.newArrayList();
		selectedAreas = isClientSide ? Collections.synchronizedList(Lists.newArrayList()) : Lists.newArrayList();
	}

	public static SelectionManager of(Player player) {
		return ((LoquatPlayer) player).loquat$getSelectionManager();
	}

	public static boolean isHoldingTool(Player player) {
		return LoquatConfig.selectionItem != Items.AIR && player.hasPermissions(2) && player.isCreative() && player.getMainHandItem().is(LoquatConfig.selectionItem);
	}

	public boolean leftClickBlock(ServerLevel world, BlockPos pos, ServerPlayer player) {
		if (!isHoldingTool(player))
			return false;
		if (world.getBlockEntity(pos) instanceof StructureBlockEntity be) {
			if (player.isShiftKeyDown()) {
				selectedAreas.clear();
				Vec3i size = be.getStructureSize();
				if (size.getX() > 0 && size.getY() > 0 && size.getZ() > 0) {
					AABB aabb = TransformUtil.getAABB(be);
					AreaManager.of(world).areas().forEach(area -> {
						if (area.inside(aabb)) {
							selectedAreas.add(area.getUuid());
						}
					});
				}
				player.displayClientMessage(Component.translatable("loquat.msg.shiftClickStructureBlock"), false);
			} else {
				if (selections.size() == 1) {
					AABB aabb = selections.get(0).toAABB();
					be.setStructurePos(new BlockPos((int) aabb.minX, (int) aabb.minY, (int) aabb.minZ).subtract(pos));
					if (be.getMode() != StructureMode.LOAD)
						be.setStructureSize(new BlockPos((int) aabb.getXsize(), (int) aabb.getYsize(), (int) aabb.getZsize()));
				}
			}
			be.setShowBoundingBox(true);
			be.setChanged();
			BlockState blockState = world.getBlockState(pos);
			world.sendBlockUpdated(pos, blockState, blockState, 3);
		} else {
			if (player.isShiftKeyDown()) {
				AreaManager manager = AreaManager.of(world);
				for (Area area : manager.areas()) {
					if (!area.contains(pos))
						continue;
					if (selectedAreas.contains(area.getUuid())) {
						selectedAreas.remove(area.getUuid());
					} else {
						selectedAreas.add(area.getUuid());
					}
				}
			} else {
				if (selections.isEmpty()) {
					lastOneIncomplete = false;
				}
				if (lastOneIncomplete) {
					selections.get(selections.size() - 1).pos2 = pos;
				} else {
					selections.add(new PosSelection(pos));
				}
				lastOneIncomplete = !lastOneIncomplete;
			}
		}
		SSyncSelectionPacket.sync(player);
		return true;
	}

	public boolean rightClickItem(ServerLevel world, HitResult hit, ServerPlayer player) {
		if (!isHoldingTool(player) || !player.isShiftKeyDown())
			return false;
		if (hit instanceof BlockHitResult blockHit && world.getBlockEntity(blockHit.getBlockPos()) instanceof StructureBlockEntity be && be.getMode() == StructureMode.LOAD) {
			Vec3i size = be.getStructureSize();
			if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) {
				return false;
			}
			AABB aabb = TransformUtil.getAABB(be);
			LoquatUtil.emptyBlocks(world, () -> BlockPos.betweenClosedStream(aabb));
			//FIXME 无法清除与AABB同样大小的区域
			AreaManager.of(world).removeAllInside(aabb);
			player.displayClientMessage(Component.translatable("loquat.msg.shiftUseStructureBlock"), false);
		} else {
			reset(player);
			return true;
		}
		SSyncSelectionPacket.sync(player);
		return true;
	}

	public boolean isSelected(Area area) {
		return selectedAreas.contains(area.getUuid());
	}

	public void reset(ServerPlayer player) {
		selections.clear();
		selectedAreas.clear();
		lastOneIncomplete = false;
		SSyncSelectionPacket.sync(player);
	}
}
