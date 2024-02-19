package snownee.loquat.spawner;

import java.lang.reflect.Type;
import java.util.function.ToIntFunction;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import snownee.loquat.util.LoquatUtil;

public class Difficulty {
	public static final ResourceLocation DEFAULT_ID = new ResourceLocation("default");

	public Provider provider;
	public DifficultyLevel[] levels;

	public DifficultyLevel getLevel(ServerLevel world) {
		int level = provider.applyAsInt(world);
		return levels[Mth.clamp(level, 1, levels.length) - 1];
	}

	public interface Provider extends ToIntFunction<ServerLevel> {
	}

	public static class DifficultyLevel {
		public float amount = 1;
		public float hp = 1;

		public LivingEntity apply(LivingEntity entity) {
			float hpRatio = entity.getHealth() / entity.getMaxHealth();
			entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(entity.getMaxHealth() * hp);
			entity.setHealth(entity.getMaxHealth() * hpRatio);
			return entity;
		}
	}

	public static class DifficultyProviderSerializer implements JsonDeserializer<Provider> {
		@Override
		public Provider deserialize(
				JsonElement jsonElement,
				Type type,
				JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			if (jsonElement.isJsonPrimitive()) {
				var primitive = jsonElement.getAsJsonPrimitive();
				if (primitive.isNumber()) {
					return level -> primitive.getAsInt();
				} else if (primitive.isString()) {
					var cmd = primitive.getAsString();
					return level -> LoquatUtil.runCommandSilently(level, cmd);
				}
			}
			throw new JsonParseException("Invalid difficulty provider");
		}
	}

}
