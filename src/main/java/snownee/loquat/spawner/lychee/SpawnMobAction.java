package snownee.loquat.spawner.lychee;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class SpawnMobAction extends PostAction {
	public static final Type TYPE = new Type();

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {

	}

	@Override
	public PostActionType<?> getType() {
		return TYPE;
	}

	public static class Type extends PostActionType<SpawnMobAction> {
		@Override
		public SpawnMobAction fromJson(JsonObject jsonObject) {
			return null;
		}

		@Override
		public void toJson(SpawnMobAction spawnMobAction, JsonObject jsonObject) {

		}

		@Override
		public SpawnMobAction fromNetwork(FriendlyByteBuf friendlyByteBuf) {
			return null;
		}

		@Override
		public void toNetwork(SpawnMobAction spawnMobAction, FriendlyByteBuf friendlyByteBuf) {

		}
	}
}
