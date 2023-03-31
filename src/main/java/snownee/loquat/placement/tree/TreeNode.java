package snownee.loquat.placement.tree;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class TreeNode {

	public final List<String> tags = Lists.newArrayList();
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
	@Getter
	@Setter
	private Function<String, TreeNode> fallbackNodeProvider = jointName -> null;
	@Getter
	@Setter
	private CompoundTag data;

	public TreeNode(ResourceLocation pool) {
		this(pool, null);
	}

	public TreeNode(ResourceLocation pool, @Nullable String parentEdge) {
		this.pool = pool;
		this.parentEdge = parentEdge == null ? null : new ResourceLocation(parentEdge).toString();
	}

	public TreeNode addChild(String edgeName, String childPool) {
		TreeNode child = new TreeNode(new ResourceLocation(childPool), edgeName);
		children.add(child);
		return child;
	}

	public void walk(Consumer<TreeNode> consumer) {
		consumer.accept(this);
		for (TreeNode child : children) {
			child.walk(consumer);
		}
	}

}
