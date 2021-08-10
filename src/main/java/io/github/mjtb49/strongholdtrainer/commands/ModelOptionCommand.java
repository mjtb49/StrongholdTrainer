package io.github.mjtb49.strongholdtrainer.commands;

import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.mjtb49.strongholdtrainer.api.MinecraftServerAccessor;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdMachineLearning;
import io.github.mjtb49.strongholdtrainer.ml.model.StrongholdModel;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModelOptionCommand extends OptionCommand {

    public ModelOptionCommand() {
        super(OptionTracker.Option.MODEL);
        try {
            StrongholdMachineLearning.MODEL_REGISTRY.setActiveModel(OptionTracker.getString(optionID));
        } catch(Exception ignored){}
    }

    public void register(CommandDispatcher<ServerCommandSource> sourceCommandDispatcher){
        LiteralArgumentBuilder<ServerCommandSource> builder =  literal("load");
        for(String key: StrongholdMachineLearning.MODEL_REGISTRY.getRegisteredIdentifiers()){
            builder = builder.then(literal(key).executes(context -> {
                PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                if(playerEntity != null){
                    playerEntity.sendMessage(new LiteralText("loading model: ").append(new LiteralText("\"" + key + "\"").formatted(Formatting.LIGHT_PURPLE)), false);
                }
                try{
                    StrongholdMachineLearning.MODEL_REGISTRY.setActiveModel(key);
                    ((MinecraftServerAccessor) context.getSource().getMinecraftServer()).refreshRooms();
                    setOption(key);
                    return 1;
                } catch (Exception e){
                    return -1;
                }
            }));

        }
        sourceCommandDispatcher.register(
                literal("model").then(
                        builder
                ).then(
                        literal("list").executes(c -> {
                            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                            if (playerEntity == null) {
                                return -1;
                            }
                            playerEntity.sendMessage(new LiteralText("---------------- models ----------------"), false);

                            List<String> registeredModels = StrongholdMachineLearning.MODEL_REGISTRY.getRegisteredIdentifiers();
                            registeredModels = registeredModels.stream().sorted().collect(Collectors.toList());
                            registeredModels.forEach(s -> {
                                boolean isActive = StrongholdMachineLearning.MODEL_REGISTRY.isActiveModel(s);
                                StrongholdModel model = StrongholdMachineLearning.MODEL_REGISTRY.getModel(s);

                                StringBuilder stringBuilder = new StringBuilder();
                                if (isActive) {
                                    stringBuilder.append(" *");
                                } else {
                                    stringBuilder.append("•");
                                }
                                stringBuilder.append(" \"").append(s).append("\" | by: ").append(model.getCreator()).append(" | external: ").append(!model.isInternal());
                                playerEntity.sendMessage(new LiteralText(stringBuilder.toString()).formatted(isActive ?
                                        Formatting.ITALIC : Formatting.RESET).formatted(isActive? Formatting.LIGHT_PURPLE : Formatting.RESET).styled((style) -> style
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/model load " + s))
                                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("load \"" + s + "\"")))), false);
                            });
                            return 0;
                        })
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

    private void setOption(String key) {
        this.setOption(new JsonPrimitive(key));
    }
}
