package snownee.loquat.placement.tree;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;

public class TreeNode {

	@Getter
	private final ResourceLocation pool;
	@Getter
	@Nullable
	private final String parentEdge;
	@Getter
	private final List<TreeNode> children = ObjectArrayList.of();
	@Getter
	@Setter
	private String uniqueGroup;
	@Getter
	@Setter
	private int minEdgeDistance;

	public TreeNode(ResourceLocation pool, @Nullable String parentEdge) {
		this.pool = pool;
		this.parentEdge = parentEdge == null ? null : new ResourceLocation(parentEdge).toString();
	}

	public TreeNode addChild(String edgeName, String childPool) {
		TreeNode child = new TreeNode(new ResourceLocation(childPool), edgeName);
		children.add(child);
		return child;
	}

}
