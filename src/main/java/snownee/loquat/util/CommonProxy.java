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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.Mod;
import snownee.loquat.AreaEventTypes;
import snownee.loquat.AreaTypes;
import snownee.loquat.Loquat;
import snownee.loquat.LoquatEvents;
import snownee.loquat.LoquatRegistries;
import snownee.loquat.PlaceProgramTypes;
import snownee.loquat.client.LoquatClient;
import snownee.loquat.command.LoquatCommand;
import snownee.loquat.command.argument.AreaArgument;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.RestrictInstance;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.spawner.LycheeCompat;

@Mod(Loquat.ID)
public class CommonProxy implements ModInitializer {

	public static final Event<LoquatEvents.PlayerEnterArea> PLAYER_ENTER_AREA = EventFactory.createArrayBacked(
			LoquatEvents.PlayerEnterArea.class,
			listeners -> (player, area) -> {
				for (LoquatEvents.PlayerEnterArea listener : listeners) {
					listener.enterArea(player, area);
				}
			});

	public static final Event<LoquatEvents.PlayerLeaveArea> PLAYER_LEAVE_AREA = EventFactory.createArrayBacked(
			LoquatEvents.PlayerLeaveArea.class,
			listeners -> (player, area) -> {
				for (LoquatEvents.PlayerLeaveArea listener : listeners) {
					listener.leaveArea(player, area);
				}
			});

	private static final ConcurrentLinkedQueue<Consumer<Entity>> entityDeathListeners = new ConcurrentLinkedQueue<>();
	private static final ConcurrentLinkedQueue<BiConsumer<Entity, Entity>> entitySuccessiveSpawnListeners =
			new ConcurrentLinkedQueue<>();

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

	public static void notifyRestriction(Player entity, RestrictInstance.RestrictBehavior value) {
		if (entity.isLocalPlayer()) {
			LoquatClient.get().notifyRestriction(value);
		}
	}

	@Override
	public void onInitialize() {
		Loquat.init();
		AreaTypes.init();
		AreaEventTypes.init();
		PlaceProgramTypes.init();
		if (Loquat.hasLychee) {
			LycheeCompat.init();
		}
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LoquatCommand.register(dispatcher);
		});
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!world.isClientSide && hand == InteractionHand.MAIN_HAND &&
				SelectionManager.of(player).rightClickItem((ServerLevel) world,
														   player.pick(5, 0, false),
														   (ServerPlayer) player)) {
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
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof ServerPlayer player) {
				AreaManager.of(world).playerLoaded(player);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.StartTracking.class, event -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				AreaManager.of(player.serverLevel()).startTrackingPlayer(player);
			}
		});
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (RestrictInstance.of(player).isRestricted(pos, RestrictInstance.RestrictBehavior.DESTROY)) {
				CommonProxy.notifyRestriction(player, RestrictInstance.RestrictBehavior.DESTROY);
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (RestrictInstance.of(player).isRestricted(pos, RestrictInstance.RestrictBehavior.DESTROY)) {
				CommonProxy.notifyRestriction(player, RestrictInstance.RestrictBehavior.DESTROY);
				return false;
			}
			return true;
		});

	}

	private static void registerThings(RegisterEvent event) {
		event.register(LoquatRegistries.AREA.getRegistryKey(), $ -> AreaTypes.init());
		event.register(LoquatRegistries.AREA_EVENT.getRegistryKey(), $ -> AreaEventTypes.init());
		event.register(LoquatRegistries.PLACE_PROGRAM.getRegistryKey(), $ -> PlaceProgramTypes.init());
		event.register(Registries.COMMAND_ARGUMENT_TYPE, $ -> {
			var info = ArgumentTypeInfos.registerByClass(AreaArgument.class, new AreaArgument.Info());
			$.register(Loquat.id("area"), info);
		});
	}

	private static void registerReloadListeners(AddReloadListenerEvent event) {
		pendingReloadListeners.forEach(event::addListener);
	}

	public static void registerReloadListener(PreparableReloadListener instance) {
		ResourceManagerHelper.get(PackType.SERVER_DATA)
							 .registerReloadListener((IdentifiableResourceReloadListener) instance);
	}
}
