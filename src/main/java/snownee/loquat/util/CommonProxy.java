package snownee.loquat.util;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import snownee.loquat.Loquat;
import snownee.loquat.command.LoquatCommand;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.placement.LoquatPlacements;
import snownee.loquat.placement.tree.TreeNode;
import snownee.loquat.placement.tree.TreeNodePlacer;
import snownee.loquat.spawner.LycheeCompat;
import snownee.loquat.spawner.SpawnMobAction;
import snownee.loquat.spawner.SpawnerLoader;
import snownee.lychee.PostActionTypes;

public class CommonProxy implements ModInitializer {

	private static final ConcurrentLinkedQueue<Consumer<Entity>> entityDeathListeners = new ConcurrentLinkedQueue<>();
	private static final ConcurrentLinkedQueue<BiConsumer<Entity, Entity>> entitySuccessiveSpawnListeners = new ConcurrentLinkedQueue<>();

	public static void registerDeathListener(Consumer<Entity> listener) {
		entityDeathListeners.add(listener);
	}

	public static void unregisterDeathListener(Consumer<Entity> listener) {
		entityDeathListeners.remove(listener);
	}

	public static void registerSuccessiveSpawnListener(BiConsumer<Entity, Entity> listener) {
		entitySuccessiveSpawnListeners.add(listener);
	}

	public static void unregisterSuccessiveSpawnListener(BiConsumer<Entity, Entity> listener) {
		entitySuccessiveSpawnListeners.remove(listener);
	}

	public static void onSuccessiveSpawn(Entity entity, Entity newEntity) {
		entitySuccessiveSpawnListeners.forEach(consumer -> consumer.accept(entity, newEntity));
	}

	@Override
	public void onInitialize() {
		Loquat.init();
		if (Loquat.hasLychee) {
			ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener((IdentifiableResourceReloadListener) SpawnerLoader.INSTANCE);
			PostActionTypes.register("loquat:spawn", SpawnMobAction.TYPE);
			LycheeCompat.init();
		}
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LoquatCommand.register(dispatcher);
		});
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!world.isClientSide && hand == InteractionHand.MAIN_HAND &&
					SelectionManager.of(player).rightClickItem((ServerLevel) world, player.pick(5, 0, false), (ServerPlayer) player)) {
				return InteractionResultHolder.success(stack);
			}
			return InteractionResultHolder.pass(stack);
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, world) -> {
			entityDeathListeners.forEach(consumer -> consumer.accept(entity));
		});

		LoquatPlacements.register(new ResourceLocation("test"), new TreeNodePlacer("loquat:test", (random, root) -> {
			List<TreeNode> rooms = Lists.newArrayList();
			TreeNode lastNode = root;
			for (int i = 0; i < 4; i++) {
				rooms.add(lastNode = lastNode.addChild("door", "loquat:room"));
			}
			rooms.forEach($ -> {
				$.setUniqueGroup("room");
				$.setMinEdgeDistance(8);
			});
			rooms.get(random.nextInt(rooms.size())).addChild("door", "loquat:treasure");
			lastNode.addChild("door", "loquat:lobby");
			return root;
		}));
	}
}
