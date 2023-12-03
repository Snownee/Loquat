package snownee.loquat.util;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;
import snownee.loquat.AreaEventTypes;
import snownee.loquat.AreaTypes;
import snownee.loquat.Loquat;
import snownee.loquat.LoquatEvents;
import snownee.loquat.LoquatRegistries;
import snownee.loquat.PlaceProgramTypes;
import snownee.loquat.client.LoquatClient;
import snownee.loquat.command.LoquatCommand;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.RestrictInstance;
import snownee.loquat.core.area.Area;
import snownee.loquat.core.select.SelectionManager;
import snownee.loquat.spawner.LycheeCompat;

@Mod(Loquat.ID)
public class CommonProxy {

	public static final List<LoquatEvents.PlayerEnterArea> playerEnterAreaListeners = ObjectArrayList.of();
	public static final List<LoquatEvents.PlayerLeaveArea> playerLeaveAreaListeners = ObjectArrayList.of();
	private static final ConcurrentLinkedQueue<Consumer<Entity>> entityDeathListeners = new ConcurrentLinkedQueue<>();
	private static final ConcurrentLinkedQueue<BiConsumer<Entity, Entity>> entitySuccessiveSpawnListeners = new ConcurrentLinkedQueue<>();
	private static final List<PreparableReloadListener> pendingReloadListeners = ObjectArrayList.of();

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
		for (LoquatEvents.PlayerEnterArea listener : playerEnterAreaListeners) {
			listener.enterArea(player, area);
		}
	}

	public static void postPlayerLeaveArea(ServerPlayer player, Area area) {
		for (LoquatEvents.PlayerLeaveArea listener : playerLeaveAreaListeners) {
			listener.leaveArea(player, area);
		}
	}

	public static void registerPlayerEnterAreaListener(LoquatEvents.PlayerEnterArea listener) {
		playerEnterAreaListeners.add(listener);
	}

	public static void registerPlayerLeaveAreaListener(LoquatEvents.PlayerLeaveArea listener) {
		playerLeaveAreaListeners.add(listener);
	}

	public static void notifyRestriction(Player entity, RestrictInstance.RestrictBehavior value) {
		if (entity.isLocalPlayer()) {
			LoquatClient.get().notifyRestriction(value);
		}
	}

	public CommonProxy() {
		Loquat.init();
		if (Loquat.hasLychee) {
			LycheeCompat.init();
		}
		FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonProxy::registerThings);
		MinecraftForge.EVENT_BUS.addListener(CommonProxy::registerReloadListeners);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RegisterCommandsEvent.class, event -> {
			LoquatCommand.register(event.getDispatcher());
		});
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerInteractEvent.RightClickItem.class, event -> {
			Player player = event.getEntity();
			InteractionHand hand = event.getHand();
			Level world = event.getLevel();
			if (!world.isClientSide && hand == InteractionHand.MAIN_HAND && SelectionManager.of(player).rightClickItem((ServerLevel) world, player.pick(5, 0, false), (ServerPlayer) player)) {
				event.setCancellationResult(InteractionResult.sidedSuccess(world.isClientSide));
				event.setCanceled(true);
			}
		});
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingDeathEvent.class, event -> {
			Entity entity = event.getEntity();
			entityDeathListeners.forEach(consumer -> consumer.accept(entity));
		});
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, LivingConversionEvent.Post.class, event -> {
			Entity entity = event.getEntity();
			onSuccessiveSpawn(entity, event.getOutcome());
			entityDeathListeners.forEach(consumer -> consumer.accept(entity));
		});
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerChangedDimensionEvent.class, event -> {
			if (!(event.getEntity() instanceof ServerPlayer))
				return;
			ServerPlayer player = (ServerPlayer) event.getEntity();
			ServerLevel destination = player.getServer().getLevel(event.getTo());
			ServerLevel origin = player.getServer().getLevel(event.getFrom());
			AreaManager.of(destination).playerChangedWorld(player, origin);
		});
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.Clone.class, event -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				AreaManager.of(player.serverLevel()).playerLoaded(player);
			}
		});
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, false, PlayerInteractEvent.LeftClickBlock.class, event -> {
			Player player = event.getEntity();
			BlockPos pos = event.getPos();
			if (RestrictInstance.of(player).isRestricted(pos, RestrictInstance.RestrictBehavior.DESTROY)) {
				CommonProxy.notifyRestriction(player, RestrictInstance.RestrictBehavior.DESTROY);
				event.setCanceled(true);
			}
		});
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, false, PlayerEvent.BreakSpeed.class, event -> {
			Player player = event.getEntity();
			Optional<BlockPos> pos = event.getPosition();
			if (pos.isEmpty())
				return;
			if (RestrictInstance.of(player).isRestricted(pos.get(), RestrictInstance.RestrictBehavior.DESTROY)) {
				CommonProxy.notifyRestriction(player, RestrictInstance.RestrictBehavior.DESTROY);
				event.setCanceled(true);
			}
		});
		if (FMLEnvironment.dist.isClient()) {
			ClientProxy.initClient();
		}
	}

	public static void registerThings(RegisterEvent event) {
		event.register(LoquatRegistries.AREA.getRegistryKey(), $ -> AreaTypes.init());
		event.register(LoquatRegistries.AREA_EVENT.getRegistryKey(), $ -> AreaEventTypes.init());
		event.register(LoquatRegistries.PLACE_PROGRAM.getRegistryKey(), $ -> PlaceProgramTypes.init());
	}

	public static void registerReloadListeners(AddReloadListenerEvent event) {
		pendingReloadListeners.forEach(event::addListener);
	}

	public static void registerReloadListener(PreparableReloadListener instance) {
		pendingReloadListeners.add(instance);
	}
}
