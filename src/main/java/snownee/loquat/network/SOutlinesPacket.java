package snownee.loquat.network;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;
import snownee.loquat.client.ClientHooks;
import snownee.loquat.client.LoquatClient;
import snownee.loquat.core.AreaManager;
import snownee.loquat.core.area.Area;

@KiwiPacket(value = "outlines", dir = KiwiPacket.Direction.PLAY_TO_CLIENT)
public class SOutlinesPacket extends PacketHandler {
	public static SOutlinesPacket I;

	// expire == Long.MIN_VALUE means clear
	public static void outlines(ServerPlayer player, long expire, boolean clear, boolean highlight, Collection<Area> areas) {
		I.send(player, buf -> {
			buf.writeVarLong(expire);
			buf.writeBoolean(clear);
			buf.writeBoolean(highlight);
			CompoundTag tag = new CompoundTag();
			tag.put("0", AreaManager.saveAreas(areas));
			buf.writeNbt(tag);
		});
	}

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(
			Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor,
			FriendlyByteBuf buf,
			ServerPlayer sender) {
		Level level = ClientHooks.getLevel();
		if (level == null) {
			return null;
		}
		long expire = buf.readVarLong();
		if (expire == Long.MIN_VALUE) {
			expire = level.getGameTime() + 10;
		}
		LoquatClient client = LoquatClient.get();
		boolean clear = buf.readBoolean();
		if (clear) {
			client.clearDebugAreas();
		}
		boolean highlight = buf.readBoolean();
		var outlines = highlight ? client.highlightOutlines : client.normalOutlines;
		CompoundTag tag = buf.readNbt();
		for (Area area : AreaManager.loadAreas(tag.getList("0", Tag.TAG_COMPOUND))) {
			var debugData = outlines.get(area.getUuid());
			debugData = new LoquatClient.RenderDebugData(
					area,
					highlight ? LoquatClient.DebugAreaType.HIGHLIGHT : LoquatClient.DebugAreaType.NORMAL,
					expire);
			outlines.put(area.getUuid(), debugData);
		}
		return null;
	}
}
