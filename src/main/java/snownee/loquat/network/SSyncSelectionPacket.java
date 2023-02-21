package snownee.loquat.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;
import snownee.loquat.core.select.PosSelection;
import snownee.loquat.core.select.SelectionManager;

@KiwiPacket(value = "sync_selection", dir = KiwiPacket.Direction.PLAY_TO_CLIENT)
public class SSyncSelectionPacket extends PacketHandler {
	public static SSyncSelectionPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		var player = Minecraft.getInstance().player;
		if (player == null)
			return null;
		var selections = SelectionManager.of(player).getSelections();
		selections.clear();
		int size = buf.readVarInt();
		for (int i = 0; i < size; i++) {
			selections.add(new PosSelection(buf.readBlockPos(), buf.readBlockPos()));
		}
		return null;
	}

	public static void sync(ServerPlayer player) {
		I.send(player, buf -> {
			var selections = SelectionManager.of(player).getSelections();
			buf.writeVarInt(selections.size());
			for (var selection : selections) {
				buf.writeBlockPos(selection.pos1);
				buf.writeBlockPos(selection.pos2);
			}
		});
	}
}
