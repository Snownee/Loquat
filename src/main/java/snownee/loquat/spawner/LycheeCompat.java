package snownee.loquat.spawner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import snownee.loquat.Loquat;
import snownee.loquat.spawner.difficulty.DifficultyLoader;
import snownee.loquat.util.CommonProxy;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.mixin.LootContextParamSetsAccess;

public class LycheeCompat {
	public static final ResourceLocation DIFFICULTY_ID = new ResourceLocation(Loquat.ID, "difficulty");
	public static final ResourceLocation SPAWNER_ID = new ResourceLocation(Loquat.ID, "spawner");
	public static final LootContextParamSet LOOT_CONTEXT_PARAM_SET = LootContextParamSetsAccess.callRegister(SPAWNER_ID.toString(), $ -> {
		$.required(LootContextParams.ORIGIN);
	});

	public static void init() {
		CommonProxy.registerReloadListener(DifficultyLoader.INSTANCE);
		CommonProxy.registerReloadListener(SpawnerLoader.INSTANCE);
		FMLJavaModLoadingContext.get().getModEventBus().addListener((RegisterEvent event) -> {
			event.register(LycheeRegistries.POST_ACTION.key(), new ResourceLocation("loquat:spawn"), () -> SpawnMobAction.TYPE);
		});
	}

}
