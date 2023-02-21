package snownee.loquat.network;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;
import snownee.loquat.client.LoquatClient;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;

@KiwiPacket(value = "highlight", dir = KiwiPacket.Direction.PLAY_TO_CLIENT)
public class SHighlightPacket extends PacketHandler {
	public static SHighlightPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		long expire = buf.readVarLong();
		CompoundTag tag = buf.readNbt();
		for (Area area : AreaManager.loadAreas(tag.getList("0", Tag.TAG_COMPOUND))) {
			if (expire == Long.MIN_VALUE) {
				LoquatClient.renderDebugAreas.remove(area.getUuid());
				continue;
			}
			var debugData = LoquatClient.renderDebugAreas.get(area.getUuid());
			if (debugData != null && debugData.expire() > expire) {
				continue;
			}
			debugData = new LoquatClient.RenderDebugData(area, 0xFFFFFFFF, expire);
			LoquatClient.renderDebugAreas.put(area.getUuid(), debugData);
		}
		return null;
	}

	// expire == Long.MIN_VALUE means clear
	public static void highlight(ServerPlayer player, long expire, Collection<Area> areas) {
		I.send(player, buf -> {
			buf.writeVarLong(expire);
			CompoundTag tag = new CompoundTag();
			tag.put("0", AreaManager.saveAreas(areas));
			buf.writeNbt(tag);
		});
	}
}
