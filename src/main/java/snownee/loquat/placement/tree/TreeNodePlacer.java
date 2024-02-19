package snownee.loquat.placement.tree;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.loquat.Loquat;
import snownee.loquat.LoquatConfig;
import snownee.loquat.duck.LoquatStructurePiece;
import snownee.loquat.mixin.SinglePoolElementAccess;
import snownee.loquat.placement.LoquatPlacer;

public class TreeNodePlacer implements LoquatPlacer {

	private final String structureIdPattern;
	private final Consumer<BuildTreeContext> buildTreeFunction;

	public TreeNodePlacer(String structureIdPattern, Consumer<BuildTreeContext> buildTreeFunction) {
		this.structureIdPattern = structureIdPattern;
		this.buildTreeFunction = buildTreeFunction;
	}

	public static Optional<ResourceLocation> getStructureId(StructurePoolElement element) {
		if (element instanceof SinglePoolElementAccess) {
			return ((SinglePoolElementAccess) element).getTemplate().left();
		}
		return Optional.empty();
	}

	@Override
	public boolean accept(ResourceLocation structureId) {
		return structureId.toString().matches(structureIdPattern);
	}

	@Override
	public Structure.GenerationStub place(
			ResourceLocation structureId,
			Structure.GenerationContext generationContext,
			BlockPos defaultStartPos,
			VoxelShape defaultValidSpace,
			int range,
			Registry<StructureTemplatePool> pools,
			PoolElementStructurePiece defaultStartPiece) {
		return new Structure.GenerationStub(defaultStartPos, structurePiecesBuilder -> {
			try {
				TreeNode root = new TreeNode(new ResourceLocation("start"), null);
				RandomSource random = RandomSource.create();
				BuildTreeContext ctx = new BuildTreeContext(root, random, generationContext, pools);
				buildTreeFunction.accept(ctx);
				root = ctx.root;
				Preconditions.checkState(root.getUniqueGroup() == null, "Root node must not have unique group");
				StepStack steps = new StepStack();
				steps.push(new Step(defaultStartPiece, defaultValidSpace, defaultStartPos, root));
				doPlace(root, steps, generationContext.structureTemplateManager(), random, pools);
				for (var step : steps) {
					step.node.tags.addAll(ctx.globalTags);
					boolean hasTags = !step.node.tags.isEmpty();
					if (hasTags || step.node.getData() != null) {
						CompoundTag data = new CompoundTag();
						if (hasTags) {
							ListTag tags = new ListTag();
							for (var tag : step.node.tags) {
								tags.add(StringTag.valueOf(tag));
							}
							data.put("Tags", tags);
						}
						if (step.node.getData() != null) {
							data.put("Data", step.node.getData());
						}
						if (!step.node.getLowPriorityProcessors().isEmpty()) {
							ListTag processors = new ListTag();
							step.node.getLowPriorityProcessors()
									.stream()
									.map(Object::toString)
									.map(StringTag::valueOf)
									.forEach(processors::add);
							data.put("LowPriorityProcessors", processors);
						}
						if (!step.node.getHighPriorityProcessors().isEmpty()) {
							ListTag processors = new ListTag();
							step.node.getHighPriorityProcessors()
									.stream()
									.map(Object::toString)
									.map(StringTag::valueOf)
									.forEach(processors::add);
							data.put("HighPriorityProcessors", processors);
						}
						((LoquatStructurePiece) step.piece).loquat$setAttachedData(data);
					}
					structurePiecesBuilder.addPiece(step.piece);
				}
			} catch (Throwable e) {
				Loquat.LOGGER.error("", e);
			}
		});
	}

	private boolean doPlace(
			TreeNode node,
			StepStack steps,
			StructureTemplateManager structureTemplateManager,
			RandomSource random,
			Registry<StructureTemplatePool> pools) {
		Step step = steps.peek();
		StructurePoolElement element = step.piece.getElement();
		BlockPos blockPos = step.piece.getPosition();
		Rotation rotation = step.piece.getRotation();
		List<StructureTemplate.StructureBlockInfo> jigsawBlocks = element.getShuffledJigsawBlocks(
				structureTemplateManager,
				BlockPos.ZERO,
				rotation,
				random);
		ListMultimap<String, StructureTemplate.StructureBlockInfo> byJointName = ArrayListMultimap.create();
		Set<StructureTemplate.StructureBlockInfo> resolvedJigsaws = Sets.newHashSet();
		for (var jigsawBlock : jigsawBlocks) {
			if (step.jointPos.equals(jigsawBlock.pos().offset(blockPos))) {
				resolvedJigsaws.add(jigsawBlock);
				continue;
			}
			String jointName = jigsawBlock.nbt().getString("name");
			byJointName.put(jointName, jigsawBlock);
		}
		StructureTemplate.StructureBlockInfo selectedJigsaw = null;
		for (TreeNode child : node.getChildren()) {
			String jointName = child.getParentEdge();
			for (var jigsawBlock : byJointName.get(jointName)) {
				selectedJigsaw = tryPlaceNode(node, steps, structureTemplateManager, random, pools, child, jigsawBlock, step, false);
				if (selectedJigsaw != null) {
					if (doPlace(child, steps, structureTemplateManager, random, pools)) {
						resolvedJigsaws.add(jigsawBlock);
						break;
					}
					selectedJigsaw = null;
				}
			}
			if (selectedJigsaw == null) {
				while (steps.peek() != step) {
					steps.pop();
				}
				steps.pop();
				return false;
			}
		}
		// Find fallbacks
		for (var jigsawBlock : jigsawBlocks) {
			if (resolvedJigsaws.contains(jigsawBlock)) {
				continue;
			}
			String jointName = jigsawBlock.nbt().getString("name");
			TreeNode fallbackNode = node.getFallbackNodeProvider().apply(jointName);
			if (fallbackNode == null) {
				continue;
			}
			tryPlaceNode(node, steps, structureTemplateManager, random, pools, fallbackNode, jigsawBlock, step, true);
		}
		return true;
	}

	private StructureTemplate.StructureBlockInfo tryPlaceNode(
			TreeNode node,
			StepStack steps,
			StructureTemplateManager structureTemplateManager,
			RandomSource random,
			Registry<StructureTemplatePool> pools,
			TreeNode child,
			StructureTemplate.StructureBlockInfo jigsawBlock,
			Step parentStep,
			boolean fallback) {
		BlockPos jointPos = parentStep.piece.getPosition().offset(jigsawBlock.pos());
		Direction direction = JigsawBlock.getFrontFacing(jigsawBlock.state());
		BlockPos thatJointPos = child.isOffsetTowardsJigsawFront() ? jointPos.relative(direction) : jointPos;
		if (!fallback) {
			double dist = parentStep.jointPos.distSqr(jointPos);
			int minEdgeDistance = node.getMinEdgeDistance();
			if (dist <= minEdgeDistance * minEdgeDistance) {
				return null;
			}
		}
		StructureTemplatePool pool = pools.getOptional(child.getPool()).orElseThrow();
		boolean hasAnyTarget = false;
		for (StructurePoolElement template : pool.getShuffledTemplates(random)) {
			if (steps.hasDuplicateElement(child.getUniqueGroup(), template)) {
				continue;
			}
			for (Rotation rotation2 : Rotation.getShuffled(random)) {
				List<StructureTemplate.StructureBlockInfo> jigsawBlocks1 = template.getShuffledJigsawBlocks(
						structureTemplateManager,
						BlockPos.ZERO,
						rotation2,
						random);
				for (StructureTemplate.StructureBlockInfo jigsawBlock1 : jigsawBlocks1) {
					if (!JigsawBlock.canAttach(jigsawBlock, jigsawBlock1)) {
						continue;
					}
					hasAnyTarget = true;
					BlockPos thatPiecePos = thatJointPos.offset(jigsawBlock1.pos().multiply(-1));
					BoundingBox boundingBox = template.getBoundingBox(structureTemplateManager, thatPiecePos, rotation2);
					PoolElementStructurePiece piece = new PoolElementStructurePiece(
							structureTemplateManager,
							template,
							thatPiecePos,
							0,
							rotation2,
							boundingBox);
					VoxelShape validSpace = steps.peek().validSpace;
					if (child.isCheckForCollisions() && Shapes.joinIsNotEmpty(
							validSpace,
							Shapes.create(AABB.of(boundingBox).deflate(0.25)),
							BooleanOp.ONLY_SECOND)) {
						continue;
					}
					VoxelShape shape = Shapes.joinUnoptimized(validSpace, Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST);
					steps.push(new Step(piece, shape, thatJointPos, child));
					return jigsawBlock;
				}
			}
		}
		Preconditions.checkState(hasAnyTarget, "No valid targets found for joint %s", child.getParentEdge());
		return null;
	}

	private record Step(PoolElementStructurePiece piece, VoxelShape validSpace, BlockPos jointPos, TreeNode node) {

		Optional<ResourceLocation> structureId() {
			return getStructureId(piece.getElement());
		}

	}

	private static class StepStack extends Stack<Step> {

		private final SetMultimap<String, ResourceLocation> uniqueGroups = HashMultimap.create();

		public boolean hasDuplicateElement(String group, StructurePoolElement element) {
			if (group == null) {
				return false;
			}
			return getStructureId(element).map($ -> uniqueGroups.containsEntry(group, $)).orElse(false);
		}

		@Override
		public Step push(Step step) {
			if (LoquatConfig.debug) {
				Loquat.LOGGER.info("Push " + step.structureId());
			}
			String group = step.node.getUniqueGroup();
			if (group != null) {
				step.structureId().ifPresent(structureId -> uniqueGroups.put(group, structureId));
			}
			return super.push(step);
		}

		@Override
		public synchronized Step pop() {
			Step step = super.pop();
			String group = step.node.getUniqueGroup();
			if (group != null) {
				step.structureId().ifPresent(structureId -> uniqueGroups.remove(group, structureId));
			}
			if (LoquatConfig.debug) {
				Loquat.LOGGER.info("Pop " + step.structureId());
			}
			return step;
		}

	}

}
