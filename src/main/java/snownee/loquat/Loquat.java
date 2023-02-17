package snownee.loquat;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class Loquat implements ModInitializer {

	public static final String MOD_ID = "loquat";
	public static final String MOD_NAME = "Loquat";

	@Override
	public void onInitialize() {
		initClient();
	}

	public void initClient() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal(MOD_ID)
					.then(registerCreate(Commands.literal("create")))
					.then(Commands.literal("delete")
							.then(Commands.argument("uuid", UuidArgument.uuid())
									.executes(context -> {
										return 0;
									}))));
		});
	}

	public LiteralArgumentBuilder<CommandSourceStack> registerCreate(LiteralArgumentBuilder<CommandSourceStack> builder) {
		return builder.then(Commands.argument("name", StringArgumentType.string())
				.then(Commands.literal("boxes")
						.then(Commands.argument("begin", Vec3Argument.vec3())
								.then(Commands.argument("end", Vec3Argument.vec3()).executes(context -> {
											var boxName = context.getArgument("name", String.class);
											var begin = Vec3Argument.getVec3(context,"begin");
											var end = Vec3Argument.getVec3(context,"end");
											var volume = AABBVolume.of(begin, end);


											var source = context.getSource();

											var manager = WorldVolume.getVolumeManager(source.getLevel().dimension());
											if (manager.contains(volume)) {
												source.sendFailure(Component.literal("can't add an already exist volume:"));
												return 1;
											}
											manager.appendVolume(volume);
											source.sendSuccess(Component.literal("add volume success"), true);

											return 0;
										})
								))));
	}

}
