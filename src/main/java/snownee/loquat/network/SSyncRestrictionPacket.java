package snownee.loquat.network;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;
import snownee.loquat.LoquatRegistries;
import snownee.loquat.client.LoquatClient;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.RestrictInstance;
import snownee.loquat.core.area.Area;

@KiwiPacket(value = "sync_restriction", dir = KiwiPacket.Direction.PLAY_TO_CLIENT)
public class SSyncRestrictionPacket extends PacketHandler {
	public static SSyncRestrictionPacket I;

	public static void sync(ServerPlayer player) {
		I.send(player, buf -> {
			var manager = RestrictInstance.of(player);
			ListTag listTag = new ListTag();
			manager.forEachRules((area, flags) -> {
				CompoundTag areaTag = new CompoundTag();
				areaTag.putString("Type", LoquatRegistries.AREA.getKey(area.getType()).toString());
				((Area.Type) area.getType()).serialize(areaTag, area);
				areaTag.putInt("Flags", flags);
				listTag.add(areaTag);
			});
			var tag = new CompoundTag();
			tag.put("0", listTag);
			buf.writeNbt(tag);
		});
	}

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		var manager = LoquatClient.get().restrictInstance;
		manager.resetForClient();
		var tag = Objects.requireNonNull(buf.readNbt());
		Object2IntMap<Area> rules = Objects.requireNonNull(manager.getRules());
		AreaManager.loadAreas(tag.getList("0", Tag.TAG_COMPOUND), (area, areaTag) -> {
			rules.put(area, areaTag.getInt("Flags"));
		});
		return null;
	}
}
