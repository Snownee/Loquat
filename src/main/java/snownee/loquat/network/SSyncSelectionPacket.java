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

	public static void sync(ServerPlayer player) {
		I.send(player, buf -> {
			var manager = SelectionManager.of(player);
			buf.writeVarInt(manager.getSelections().size());
			for (var selection : manager.getSelections()) {
				buf.writeBlockPos(selection.pos1);
				buf.writeBlockPos(selection.pos2);
			}
			buf.writeVarInt(manager.getSelectedAreas().size());
			for (var uuid : manager.getSelectedAreas()) {
				buf.writeUUID(uuid);
			}
		});
	}

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		var player = Minecraft.getInstance().player;
		if (player == null)
			return null;
		var manager = SelectionManager.of(player);
		var selections = manager.getSelections();
		var selectedAreas = manager.getSelectedAreas();
		selections.clear();
		selectedAreas.clear();
		int size = buf.readVarInt();
		for (int i = 0; i < size; i++) {
			selections.add(new PosSelection(buf.readBlockPos(), buf.readBlockPos()));
		}
		size = buf.readVarInt();
		for (int i = 0; i < size; i++) {
			selectedAreas.add(buf.readUUID());
		}
		return null;
	}
}
