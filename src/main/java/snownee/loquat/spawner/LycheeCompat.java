package snownee.loquat.spawner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.loquat.Loquat;
import snownee.loquat.spawner.difficulty.DifficultyLoader;
import snownee.loquat.util.CommonProxy;
import snownee.loquat.util.LoquatDataLoader;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.PostActionTypes;
import snownee.lychee.mixin.LootContextParamSetsAccess;

public class LycheeCompat {
	public static final ResourceLocation DIFFICULTY_ID = Loquat.id("difficulty");
	public static final ResourceLocation SPAWNER_ID = Loquat.id("spawner");
	public static final LootContextParamSet LOOT_CONTEXT_PARAM_SET = LootContextParamSetsAccess.callRegister(SPAWNER_ID.toString(), $ -> {
		$.required(LootContextParams.ORIGIN);
	});
	public static final LoquatDataLoader<Spawner> SPAWNERS = new LoquatDataLoader<>(SPAWNER_ID, "loquat_spawners", $ -> {
		return LoquatDataLoader.GSON.fromJson($, Spawner.class);
	});
	public static final LoquatDataLoader<Difficulty> DIFFICULTIES = new LoquatDataLoader<>(DIFFICULTY_ID, "loquat_difficulties", $ -> {
		return LoquatDataLoader.GSON.fromJson($, Difficulty.class);
	});

	public static void init() {
		SPAWNERS.supportsFragment = true;
		DIFFICULTIES.supportsFragment = true;
		CommonProxy.registerReloadListener(SPAWNERS);
		CommonProxy.registerReloadListener(DIFFICULTIES);
		PostActionTypes.register("loquat:spawn", SpawnMobAction.TYPE);
	}

}
