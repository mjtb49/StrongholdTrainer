package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdRoomClassifier;
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
        sourceCommandDispatcher.register(
                literal("model").then(
                        literal("list").executes(c -> {
                            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                            if(playerEntity == null){
                                return -1;
                            }
                            Set<String> registeredModels = StrongholdRoomClassifier.STRONGHOLD_MODEL_REGISTRY.getRegisteredIdentifiers();
                            registeredModels = registeredModels.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
                            registeredModels.forEach(s -> {
                                StrongholdModel model = StrongholdRoomClassifier.STRONGHOLD_MODEL_REGISTRY.getModel(s);
                                String entry = "• \""  + s + "\" | by: " + model.getCreator() + " | external: " + !model.isInternal();
                                playerEntity.sendMessage(new LiteralText(entry).formatted(StrongholdRoomClassifier.STRONGHOLD_MODEL_REGISTRY.isActiveMode(s) ?
                                        Formatting.ITALIC : Formatting.RESET), false);
                            });
                            return 0;
                        })
                ).then(
                        literal("load").then(
                            argument("identifier", StringArgumentType.string()).executes(context -> {
                                try{
                                    StrongholdRoomClassifier.STRONGHOLD_MODEL_REGISTRY.setActiveModel(StringArgumentType.getString(context, "identifier"));
                                    return 1;
                                } catch (Exception e){
                                    return -1;
                                }
                            }))
                ).then(
                        literal("debug").then(
                                argument("identifier", StringArgumentType.string()).executes(context -> {
                                    PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                                    if(playerEntity == null){
                                        return -1;
                                    }
                                    try{
                                        StrongholdRoomClassifier.STRONGHOLD_MODEL_REGISTRY.getModel(StringArgumentType.getString(context, "identifier"));
                                        playerEntity.sendMessage(new LiteralText(StrongholdRoomClassifier.STRONGHOLD_MODEL_REGISTRY.getModel(StringArgumentType.getString(context, "identifier")).getSignatureDefDebug().toString()), false);
                                        return 1;
                                    } catch (Exception e){
                                        return -1;
                                    }
                                })
                        )
                ).then(
                        literal("verbose").executes(e -> {
                            StrongholdRoomClassifier.verboseOutput = !StrongholdRoomClassifier.verboseOutput;
                            return 1;
                        })
                )
                );

    }
}
