package snownee.loquat.placement.tree;

import net.minecraft.util.RandomSource;

@FunctionalInterface
public interface BuildTreeFunction {
	TreeNode buildTree(RandomSource random, TreeNode root);
}
