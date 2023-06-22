package snownee.loquat;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Streams;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.RestrictInstance;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.duck.LoquatServerPlayer;
import snownee.loquat.duck.LoquatStructurePiece;
import snownee.loquat.network.CRequestOutlinesPacket;
import snownee.loquat.network.CSelectAreaPacket;
import snownee.loquat.placement.LoquatPlacements;
import snownee.loquat.util.CommonProxy;
import snownee.loquat.util.TransformUtil;

public interface Hooks {
	static boolean handleComponentClicked(String value) {
		String[] s = StringUtils.split(value, " ", 5);
		if (s.length != 5 || !s[0].equals("@loquat")) {
			return false;
		}
		LocalPlayer player = Minecraft.getInstance().player;
		ResourceLocation dimension = ResourceLocation.tryParse(s[2]);
		if (player == null || dimension == null) {
			return true;
		}
		UUID uuid = UUID.fromString(s[3]);
		if (s[1].equals("highlight")) {
			if (!player.level.dimension().location().equals(dimension)) {
				player.displayClientMessage(Component.translatable("loquat.command.wrongDimension"), false);
				return true;
			}
			CRequestOutlinesPacket.request(60, List.of(uuid));
		} else if (s[1].equals("info")) {
			if (Screen.hasControlDown()) {
				Minecraft.getInstance().keyboardHandler.setClipboard(s[4]);
				Minecraft.getInstance().getChatListener().handleSystemMessage(Component.translatable("loquat.msg.copied"), false);
			} else {
				Minecraft.getInstance().keyboardHandler.setClipboard(uuid.toString());
				Minecraft.getInstance().getChatListener().handleSystemMessage(Component.translatable("loquat.msg.copied.uuid"), false);
			}
		} else if (s[1].equals("select")) {
			SelectionManager manager = SelectionManager.of(player);
			CSelectAreaPacket.send(!manager.getSelectedAreas().contains(uuid), uuid);
		}
		return true;
	}

	static void fillFromWorld(AreaManager manager, BlockPos pos, Vec3i size, List<Area> areas) {
		AABB aabb = new AABB(pos, pos.offset(size));
		var settings = new StructurePlaceSettings();
		for (Area area : manager.areas()) {
			if (area.inside(aabb)) {
				area = TransformUtil.transform(settings, pos.multiply(-1), area);
				area.setUuid(null);
				areas.add(area);
			}
		}
	}

	static void placeInWorld(AreaManager manager, BlockPos pos, BlockPos blockPos, List<Area> areas, StructurePlaceSettings settings, Vec3i size) {
		manager.removeAllInside(TransformUtil.transform(settings, pos, new AABB(pos, pos.offset(size))));
		for (Area area : areas) {
			area = TransformUtil.transform(settings, pos, area);
			if (settings.getBoundingBox() != null && !AABB.of(settings.getBoundingBox()).contains(area.getOrigin())) {
				continue;
			}
			LoquatStructurePiece piece = LoquatStructurePiece.current();
			if (piece != null && piece.loquat$getAttachedData() != null) {
				CompoundTag data = piece.loquat$getAttachedData();
				if (data.contains("Tags")) {
					for (Tag tag : data.getList("Tags", Tag.TAG_STRING)) {
						area.getTags().add(tag.getAsString());
					}
				}
				if (data.contains("Data")) {
					area.getOrCreateAttachedData().merge(data.getCompound("Data"));
				}
			}
			// still not sure the difference between pos and blockPos...
			manager.add(area);
		}
	}

	static void tickServerPlayer(ServerPlayer player, LoquatServerPlayer loquatPlayer) {
		AreaManager manager = AreaManager.of(player.getLevel());
		long chunkPos = ChunkPos.asLong(player.blockPosition());
		Set<Area> areasIn = loquatPlayer.loquat$getAreasIn();
		Streams.concat(manager.byChunk(chunkPos), areasIn.stream()).distinct().toList().forEach(area -> {
			boolean inside = area.contains(player.getBoundingBox());
			if (inside && areasIn.add(area)) {
				CommonProxy.postPlayerEnterArea(player, area);
			} else if (!inside && areasIn.remove(area)) {
				CommonProxy.postPlayerLeaveArea(player, area);
			}
		});
	}

	static void prePlaceStructure(ServerLevel serverLevel, ChunkPos chunkPos, ChunkPos chunkPos2) {
		ChunkPos.rangeClosed(chunkPos, chunkPos2).forEach(chunkPos3 -> {
			serverLevel.getChunkSource().addRegionTicket(LoquatPlacements.TICKET_TYPE, chunkPos3, 1, chunkPos3);
		});
		serverLevel.getChunkSource().tick(() -> true, false);
	}

	static void postPlaceStructure(ServerLevel serverLevel, ChunkPos chunkPos, ChunkPos chunkPos2) {
		ChunkPos.rangeClosed(chunkPos, chunkPos2).forEach(chunkPos3 -> {
			serverLevel.getChunkSource().removeRegionTicket(LoquatPlacements.TICKET_TYPE, chunkPos3, 1, chunkPos3);
		});
	}

	static void collideWithLoquatAreas(@Nullable Entity entity, AABB expanded, Consumer<VoxelShape> consumer) {
		if (entity == null || entity.getType() != EntityType.PLAYER) {
			return;
		}
		RestrictInstance instance = RestrictInstance.of((Player) entity);
		if (instance.isEmpty()) {
			return;
		}
		MutableObject<RestrictInstance.RestrictBehavior> behavior = new MutableObject<>();
		MutableObject<Area> areaIn = new MutableObject<>();
		instance.areaStream().filter(area -> {
			return instance.isRestricted(area, RestrictInstance.RestrictBehavior.EXIT) && area.contains(entity.getBoundingBox());
		}).findFirst().filter(area -> {
			areaIn.setValue(area);
			if (!area.contains(entity.getBoundingBox().inflate(0.1))) {
				behavior.setValue(RestrictInstance.RestrictBehavior.EXIT);
			}
			return true;
		}).flatMap(Area::getVoxelShape).ifPresent(shape -> {
			shape = Shapes.join(shape, Shapes.INFINITY, BooleanOp.ONLY_SECOND);
			consumer.accept(shape);
		});
		instance.areaStream().filter(area -> {
			return !area.equals(areaIn.getValue()) && instance.isRestricted(area, RestrictInstance.RestrictBehavior.ENTER) && area.intersects(expanded);
		}).forEach(area -> {
			area.getVoxelShape().ifPresent(consumer);
			behavior.setValue(RestrictInstance.RestrictBehavior.ENTER);
		});
		if (behavior.getValue() != null) {
			CommonProxy.notifyRestriction((Player) entity, behavior.getValue());
		}
	}

	static boolean teleportServerPlayer(ServerPlayer player, LoquatServerPlayer loquatServerPlayer, double x, double y, double z) {
		RestrictInstance restrictInstance = RestrictInstance.of(player);
		if (restrictInstance.isEmpty()) {
			return false;
		}
		Vec3 pos = player.position();
		AABB expected = player.getBoundingBox().move(x - pos.x, y - pos.y, z - pos.z);
		if (restrictInstance.areaStream().filter(area -> {
			return restrictInstance.isRestricted(area, RestrictInstance.RestrictBehavior.EXIT) && area.contains(player.getBoundingBox());
		}).findFirst().map(area -> !area.contains(expected)).orElse(false)) {
			return true;
		}
		return restrictInstance.areaStream().anyMatch(area -> {
			return restrictInstance.isRestricted(area, RestrictInstance.RestrictBehavior.ENTER) && !loquatServerPlayer.loquat$getAreasIn().contains(area) && area.intersects(expected);
		});
	}

	static void addDynamicProcessors(StructurePlaceSettings settings, RegistryAccess registryAccess, CompoundTag data, String key) {
		if (!data.contains(key)) {
			return;
		}
		ListTag list = data.getList(key, Tag.TAG_STRING);
		for (int i = 0; i < list.size(); i++) {
			String s = list.getString(i);
			StructureProcessorList processorList = registryAccess.registryOrThrow(Registry.PROCESSOR_LIST_REGISTRY).getOptional(ResourceLocation.tryParse(s)).orElseThrow(() -> {
				return new IllegalStateException("Unknown processor list: " + s);
			});
			processorList.list().forEach(settings::addProcessor);
		}
	}

	static boolean checkServerPlayerRestriction(ServerPlayer player, LoquatServerPlayer loquatServerPlayer) {
		if (player.isSpectator()) {
			return false;
		}
		RestrictInstance restrictInstance = RestrictInstance.of(player);
		if (restrictInstance.isEmpty()) {
			return false;
		}
		Vec3 pos = player.position();
		Vec3 lastPos = Objects.requireNonNull(loquatServerPlayer.loquat$getLastGoodPos());
		AABB lastBox = player.getBoundingBox().move(lastPos.x - pos.x, lastPos.y - pos.y, lastPos.z - pos.z);
		if (restrictInstance.areaStream().filter(area -> {
			return restrictInstance.isRestricted(area, RestrictInstance.RestrictBehavior.EXIT) && area.contains(lastBox);
		}).findFirst().map(area -> !area.contains(player.getBoundingBox())).orElse(false)) {
			return true;
		}
		return restrictInstance.areaStream().anyMatch(area -> {
			return restrictInstance.isRestricted(area, RestrictInstance.RestrictBehavior.ENTER) && !loquatServerPlayer.loquat$getAreasIn().contains(area) && area.intersects(player.getBoundingBox());
		});
	}
}
