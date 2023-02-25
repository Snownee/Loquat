package snownee.loquat.spawner;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class SpawnMobAction extends PostAction {
	public static final Type TYPE = new Type();

	@Getter
	private final MobEntry mob;
	@Getter
	private final int count;
	@Getter
	private final String zone;

	public SpawnMobAction(MobEntry mob, int count, String zone) {
		this.mob = mob;
		this.count = count;
		this.zone = zone;
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Preconditions.checkArgument(recipe instanceof ActiveWave);
		ActiveWave wave = (ActiveWave) recipe;
		for (int i = 0; i < count; i++) {
			wave.addPendingMob(this);
		}
	}

	@Override
	public PostActionType<?> getType() {
		return TYPE;
	}

	public static class Type extends PostActionType<SpawnMobAction> {
		@Override
		public SpawnMobAction fromJson(JsonObject jsonObject) {
			MobEntry mob = MobEntry.load(jsonObject.get("mob"));
			int count = GsonHelper.getAsInt(jsonObject, "count", 1);
			String zone = GsonHelper.getAsString(jsonObject, "zone", "0");
			return new SpawnMobAction(mob, count, zone);
		}

		@Override
		public void toJson(SpawnMobAction spawnMobAction, JsonObject jsonObject) {
			jsonObject.add("mob", spawnMobAction.mob.save());
			jsonObject.addProperty("count", spawnMobAction.count);
			jsonObject.addProperty("zone", spawnMobAction.zone);
		}

		@Override
		public SpawnMobAction fromNetwork(FriendlyByteBuf friendlyByteBuf) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void toNetwork(SpawnMobAction spawnMobAction, FriendlyByteBuf friendlyByteBuf) {
			throw new UnsupportedOperationException();
		}
	}
}
