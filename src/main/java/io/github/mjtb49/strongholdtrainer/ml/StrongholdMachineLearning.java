package io.github.mjtb49.strongholdtrainer.ml;

import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import io.github.mjtb49.strongholdtrainer.ml.model.StrongholdModelRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

// TODO: rename better
public class StrongholdMachineLearning {
    public static final StrongholdModelRegistry MODEL_REGISTRY = new StrongholdModelRegistry();
    public static boolean verboseOutput = false;
    private static final Text ERROR_MSG = new LiteralText("An error occurred querying the ML. See logs for more").formatted(Formatting.BOLD, Formatting.RED);
    public static void init(String... internalModels) {
        if(!StrongholdTrainer.ML_DISABLED){
            for(String path : internalModels){
                MODEL_REGISTRY.createAndRegisterInternal(path);
            }
            MODEL_REGISTRY.setActiveModel("basic-classifier-nobacktracking");
        }
        // Register default included models.
    }
    private static final double[] ERROR_POLICY = new double[]{0xFFD, 0xFFD, 0xFFD, 0xFFD, 0xFFD, 0xFFD};

    public static double[] getPredictions(StrongholdPath path) {
        //hack fix since the model hasn't been trained on rooms where the portal room is adjacent
        try{
            if((!StrongholdTrainer.ML_DISABLED) || (MODEL_REGISTRY.getActiveModel() != null)){
                double[] predictions = MODEL_REGISTRY.getActiveModel().getPredictions(path);
                if(verboseOutput){
                    PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                    if(playerEntity != null){
                        playerEntity.sendMessage(new LiteralText(Arrays.toString(predictions)), false);
                    }
                }
                return MODEL_REGISTRY.getActiveModel().getPredictions(path);
            } else {
                return ERROR_POLICY;
            }
        } catch (Exception e){
            if(MinecraftClient.getInstance().player != null){
                MinecraftClient.getInstance().player.sendMessage(ERROR_MSG, false);
            }
            return ERROR_POLICY;
        }

    }



}
