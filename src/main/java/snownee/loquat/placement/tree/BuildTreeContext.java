package snownee.loquat.placement.tree;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class BuildTreeContext {
	public TreeNode root;
	public final RandomSource random;
	public final StructureTemplateManager structureTemplateManager;
	public final Registry<StructureTemplatePool> pools;
	public final List<String> globalTags = Lists.newArrayList();

	public BuildTreeContext(TreeNode root, RandomSource random, StructureTemplateManager structureTemplateManager, Registry<StructureTemplatePool> pools) {
		this.root = root;
		this.random = random;
		this.structureTemplateManager = structureTemplateManager;
		this.pools = pools;
	}
}
