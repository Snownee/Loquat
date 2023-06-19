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
	public Structure.GenerationStub place(ResourceLocation structureId, Structure.GenerationContext generationContext, BlockPos defaultStartPos, VoxelShape defaultValidSpace, int range, Registry<StructureTemplatePool> pools, PoolElementStructurePiece defaultStartPiece) {
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
					if (step.node.tags.size() > 0 || step.node.getData() != null) {
						CompoundTag data = new CompoundTag();
						if (step.node.tags.size() > 0) {
							ListTag tags = new ListTag();
							for (var tag : step.node.tags) {
								tags.add(StringTag.valueOf(tag));
							}
							data.put("Tags", tags);
						}
						if (step.node.getData() != null) {
							data.put("Data", step.node.getData());
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

	private boolean doPlace(TreeNode node, StepStack steps, StructureTemplateManager structureTemplateManager, RandomSource random, Registry<StructureTemplatePool> pools) {
		Step step = steps.peek();
		StructurePoolElement element = step.piece.getElement();
		BlockPos blockPos = step.piece.getPosition();
		Rotation rotation = step.piece.getRotation();
		List<StructureTemplate.StructureBlockInfo> jigsawBlocks = element.getShuffledJigsawBlocks(structureTemplateManager, BlockPos.ZERO, rotation, random);
		ListMultimap<String, StructureTemplate.StructureBlockInfo> byJointName = ArrayListMultimap.create();
		Set<StructureTemplate.StructureBlockInfo> resolvedJigsaws = Sets.newHashSet();
		for (var jigsawBlock : jigsawBlocks) {
			if (step.jointPos.equals(jigsawBlock.pos.offset(blockPos))) {
				resolvedJigsaws.add(jigsawBlock);
				continue;
			}
			String jointName = jigsawBlock.nbt.getString("name");
			byJointName.put(jointName, jigsawBlock);
		}
		for (TreeNode child : node.getChildren()) {
			String jointName = child.getParentEdge();
			StructureTemplate.StructureBlockInfo selectedJigsaw = null;
			joints:
			for (var jigsawBlock : byJointName.get(jointName)) {
				int minEdgeDistance = node.getMinEdgeDistance();
				BlockPos jointPos = blockPos.offset(jigsawBlock.pos);
				Direction direction = JigsawBlock.getFrontFacing(jigsawBlock.state);
				BlockPos thatJointPos = jointPos.relative(direction);
				double dist = step.jointPos.distSqr(jointPos);
				if (dist <= minEdgeDistance * minEdgeDistance) {
					continue;
				}
				StructureTemplatePool pool = pools.getOptional(child.getPool()).orElseThrow();
				for (StructurePoolElement template : pool.getShuffledTemplates(random)) {
					if (steps.hasDuplicateElement(child.getUniqueGroup(), template)) {
						continue;
					}
					for (Rotation rotation2 : Rotation.getShuffled(random)) {
						List<StructureTemplate.StructureBlockInfo> jigsawBlocks1 = template.getShuffledJigsawBlocks(structureTemplateManager, BlockPos.ZERO, rotation2, random);
						for (StructureTemplate.StructureBlockInfo jigsawBlock1 : jigsawBlocks1) {
							if (!JigsawBlock.canAttach(jigsawBlock, jigsawBlock1)) {
								continue;
							}
							BlockPos thatPiecePos = thatJointPos.offset(jigsawBlock1.pos.multiply(-1));
							BoundingBox boundingBox = template.getBoundingBox(structureTemplateManager, thatPiecePos, rotation2);
							PoolElementStructurePiece piece = new PoolElementStructurePiece(structureTemplateManager, template, thatPiecePos, 0, rotation2, boundingBox);
							VoxelShape validSpace = steps.peek().validSpace;
							if (Shapes.joinIsNotEmpty(validSpace, Shapes.create(AABB.of(boundingBox).deflate(0.25)), BooleanOp.ONLY_SECOND)) {
								continue;
							}
							VoxelShape shape = Shapes.joinUnoptimized(validSpace, Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST);
							steps.push(new Step(piece, shape, thatJointPos, child));
							if (doPlace(child, steps, structureTemplateManager, random, pools)) {
								resolvedJigsaws.add(jigsawBlock);
								selectedJigsaw = jigsawBlock;
								break joints;
							}
						}
					}
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
		joints:
		for (var jigsawBlock : jigsawBlocks) {
			if (resolvedJigsaws.contains(jigsawBlock)) {
				continue;
			}
			String jointName = jigsawBlock.nbt.getString("name");
			TreeNode fallbackNode = node.getFallbackNodeProvider().apply(jointName);
			if (fallbackNode == null) {
				continue;
			}
			BlockPos jointPos = blockPos.offset(jigsawBlock.pos);
			StructureTemplatePool pool = pools.getOptional(fallbackNode.getPool()).orElseThrow();
			for (StructurePoolElement template : pool.getShuffledTemplates(random)) {
				if (steps.hasDuplicateElement(fallbackNode.getUniqueGroup(), template)) {
					continue;
				}
				for (Rotation rotation2 : Rotation.getShuffled(random)) {
					List<StructureTemplate.StructureBlockInfo> jigsawBlocks1 = template.getShuffledJigsawBlocks(structureTemplateManager, BlockPos.ZERO, rotation2, random);
					for (StructureTemplate.StructureBlockInfo jigsawBlock1 : jigsawBlocks1) {
						if (!JigsawBlock.canAttach(jigsawBlock, jigsawBlock1)) {
							continue;
						}
						BlockPos thatPiecePos = jointPos.offset(jigsawBlock1.pos.multiply(-1));
						BoundingBox boundingBox = template.getBoundingBox(structureTemplateManager, thatPiecePos, rotation2);
						PoolElementStructurePiece piece = new PoolElementStructurePiece(structureTemplateManager, template, thatPiecePos, 0, rotation2, boundingBox);
						VoxelShape shape = Shapes.joinUnoptimized(steps.peek().validSpace, Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST);
						steps.push(new Step(piece, shape, jointPos, fallbackNode));
						continue joints;
					}
				}
			}
		}
		return true;
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
			return step;
		}

	}

}
