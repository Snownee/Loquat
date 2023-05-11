package snownee.loquat.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import snownee.loquat.Loquat;
import snownee.loquat.LoquatEvents;
import snownee.loquat.command.LoquatCommand;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.spawner.LycheeCompat;
import snownee.loquat.spawner.SpawnMobAction;
import snownee.loquat.spawner.SpawnerLoader;
import snownee.loquat.spawner.difficulty.DifficultyLoader;
import snownee.lychee.PostActionTypes;

public class CommonProxy implements ModInitializer {

	public static final Event<LoquatEvents.PlayerEnterArea> PLAYER_ENTER_AREA = EventFactory.createArrayBacked(LoquatEvents.PlayerEnterArea.class, listeners -> (player, area) -> {
		for (LoquatEvents.PlayerEnterArea listener : listeners) {
			listener.enterArea(player, area);
		}
	});

	public static final Event<LoquatEvents.PlayerLeaveArea> PLAYER_LEAVE_AREA = EventFactory.createArrayBacked(LoquatEvents.PlayerLeaveArea.class, listeners -> (player, area) -> {
		for (LoquatEvents.PlayerLeaveArea listener : listeners) {
			listener.leaveArea(player, area);
		}
	});

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

	public static void postPlayerEnterArea(ServerPlayer player, Area area) {
		PLAYER_ENTER_AREA.invoker().enterArea(player, area);
	}

	public static void postPlayerLeaveArea(ServerPlayer player, Area area) {
		PLAYER_LEAVE_AREA.invoker().leaveArea(player, area);
	}

	public static void registerPlayerEnterAreaListener(LoquatEvents.PlayerEnterArea listener) {
		PLAYER_ENTER_AREA.register(listener);
	}

	public static void registerPlayerLeaveAreaListener(LoquatEvents.PlayerLeaveArea listener) {
		PLAYER_LEAVE_AREA.register(listener);
	}

	@Override
	public void onInitialize() {
		Loquat.init();
		if (Loquat.hasLychee) {
			ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener((IdentifiableResourceReloadListener) DifficultyLoader.INSTANCE);
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
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			AreaManager.of(destination).playerChangedWorld(player, origin);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			AreaManager.of(handler.player.getLevel()).playerJoined(handler.player);
		});
	}
}
