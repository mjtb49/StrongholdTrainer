package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.mjtb49.strongholdtrainer.api.MinecraftServerAccessor;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdMachineLearning;
import io.github.mjtb49.strongholdtrainer.ml.model.StrongholdModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModelCommand {
    public static void register(CommandDispatcher<ServerCommandSource> sourceCommandDispatcher){
        LiteralArgumentBuilder<ServerCommandSource> builder =  literal("load");
        for(String key: StrongholdMachineLearning.MODEL_REGISTRY.getRegisteredIdentifiers()){
            builder = builder.then(literal(key).executes(context -> {
                try{
                    StrongholdMachineLearning.MODEL_REGISTRY.setActiveModel(key);
                    ((MinecraftServerAccessor) context.getSource().getMinecraftServer()).refreshRooms();
                    return 1;
                } catch (Exception e){
                    return -1;
                }
            }));

        }
        sourceCommandDispatcher.register(
                literal("model").then(
                        literal("list").executes(c -> {
                            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                            if(playerEntity == null){
                                return -1;
                            }
                            Set<String> registeredModels = StrongholdMachineLearning.MODEL_REGISTRY.getRegisteredIdentifiers();
                            registeredModels = registeredModels.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
                            registeredModels.forEach(s -> {
                                StrongholdModel model = StrongholdMachineLearning.MODEL_REGISTRY.getModel(s);
                                String entry = "• \""  + s + "\" | by: " + model.getCreator() + " | external: " + !model.isInternal();
                                playerEntity.sendMessage(new LiteralText(entry).formatted(StrongholdMachineLearning.MODEL_REGISTRY.isActiveModel(s) ?
                                        Formatting.ITALIC : Formatting.RESET), false);
                            });
                            return 0;
                        })
                ).then(
                        builder
                ).then(
                        literal("debug").then(
                                argument("identifier", StringArgumentType.string()).executes(context -> {
                                    PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                                    if(playerEntity == null){
                                        return -1;
                                    }
                                    try{
                                        StrongholdMachineLearning.MODEL_REGISTRY.getModel(StringArgumentType.getString(context, "identifier"));
                                        playerEntity.sendMessage(new LiteralText(StrongholdMachineLearning.MODEL_REGISTRY.getModel(StringArgumentType.getString(context, "identifier")).getSignatureDefDebug().toString()), false);
                                        return 1;
                                    } catch (Exception e){
                                        return -1;
                                    }
                                })
                        )
                ).then(
                        literal("verbose").executes(e -> {
                            StrongholdMachineLearning.verboseOutput = !StrongholdMachineLearning.verboseOutput;
                            return 1;
                        })
                )
                );

    }
}
