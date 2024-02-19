package snownee.loquat.command.argument;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.SelectionManager;

public record AreaSelector(
		int maxResults,
		Predicate<Area> predicate,
		MinMaxBounds.Doubles range,
		java.util.function.UnaryOperator<Vec3> position, @Nullable AABB aabb,
		BiConsumer<Vec3, List<? extends Area>> order,
		boolean selectedAreas,
		@Nullable UUID uuid
) {
	public static final BiConsumer<Vec3, List<? extends Area>> ORDER_ARBITRARY = (origin, areas) -> {
	};
	public static final BiConsumer<Vec3, List<? extends Area>> ORDER_NEAREST = (origin, areas) -> {
		areas.sort((a, b) -> {
			return Doubles.compare(a.distanceToSqr(origin), b.distanceToSqr(origin));
		});
	};
	public static final BiConsumer<Vec3, List<? extends Area>> ORDER_FURTHEST = (origin, areas) -> {
		areas.sort((a, b) -> {
			return Doubles.compare(b.distanceToSqr(origin), a.distanceToSqr(origin));
		});
	};
	public static final BiConsumer<Vec3, List<? extends Area>> ORDER_RANDOM = (origin, areas) -> {
		Collections.shuffle(areas);
	};

	private void checkPermissions(CommandSourceStack source) throws CommandSyntaxException {
		//NO-OP
	}

	public Area findSingleArea(CommandSourceStack source) throws CommandSyntaxException {
		this.checkPermissions(source);
		List<? extends Area> list = this.findAreas(source);
		if (list.isEmpty()) {
			throw AreaArgument.NO_AREAS_FOUND.create();
		} else if (list.size() > 1) {
			throw AreaArgument.ERROR_NOT_SINGLE_AREA.create();
		} else {
			return list.get(0);
		}
	}

	public List<? extends Area> findAreas(CommandSourceStack source) throws CommandSyntaxException {
		return findAreasRaw(source);
	}

	private List<? extends Area> findAreasRaw(CommandSourceStack source) throws CommandSyntaxException {
		this.checkPermissions(source);
		AreaManager areaManager = AreaManager.of(source.getLevel());
		if (this.uuid != null) {
			Area area = areaManager.get(this.uuid);
			if (area == null) {
				return List.of();
			}
			return Lists.newArrayList(area);
		}
		Vec3 vec3 = position.apply(source.getPosition());
		Predicate<Area> predicate = this.getPredicate(vec3);
		List<Area> list = Lists.newArrayList();
		if (this.selectedAreas) {
			ServerPlayer player = source.getPlayer();
			SelectionManager selectionManager;
			if (player != null) {
				selectionManager = SelectionManager.of(player);
			} else {
				//TODO: implement this
				selectionManager = null;
				return List.of();
			}
			addAreas(list, selectionManager.getSelectedAreas(source.getLevel()), predicate);
		} else {
			addAreas(list, areaManager.areas().stream(), predicate);
		}
		return this.sortAndLimit(vec3, list);
	}

	private void addAreas(List<Area> list, Stream<Area> areas, Predicate<Area> predicate) {
		int i = this.getResultLimit();
		if (list.size() < i) {
			if (this.aabb != null) {
				//TODO: optimize this
				areas.filter(predicate).forEach(list::add);
			} else {
				areas.filter(predicate).forEach(list::add);
			}
		}
	}

	private int getResultLimit() {
		return this.order == ORDER_ARBITRARY ? this.maxResults : Integer.MAX_VALUE;
	}

	private Predicate<Area> getPredicate(Vec3 vec3) {
		Predicate<Area> predicate = this.predicate;
		if (this.aabb != null) {
			AABB aabb = this.aabb.move(vec3);
			predicate = predicate.and(area -> {
				return area.intersects(aabb);
			});
		}

		if (!this.range.isAny()) {
			predicate = predicate.and(area -> {
				return this.range.matchesSqr(area.distanceToSqr(vec3));
			});
		}

		return predicate;
	}

	private <T extends Area> List<T> sortAndLimit(Vec3 vec3, List<T> areas) {
		if (areas.size() > 1) {
			this.order.accept(vec3, areas);
		}
		return areas.subList(0, Math.min(this.maxResults, areas.size()));
	}
}
