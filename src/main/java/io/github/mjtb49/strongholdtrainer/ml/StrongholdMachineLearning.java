package io.github.mjtb49.strongholdtrainer.ml;

import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import io.github.mjtb49.strongholdtrainer.ml.model.StrongholdModelRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.Arrays;

// TODO: rename better
public class StrongholdMachineLearning {
    public static final StrongholdModelRegistry MODEL_REGISTRY = new StrongholdModelRegistry();
    public static boolean verboseOutput = false;

    public static void init(String... internalModels) {
        if(!StrongholdTrainer.ML_DISABLED){
            for(String path : internalModels){
                MODEL_REGISTRY.createAndRegisterInternal(path, null);
            }
            MODEL_REGISTRY.setActiveModel("basic-classifier-nobacktracking");
        }
        // Register default included models.

    }

    public static double[] getPredictions(StrongholdPath path) {
        //hack fix since the model hasn't been trained on rooms where the portal room is adjacent
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
            return new double[]{0xffD, 0xffD, 0xffD, 0xffD, 0xffD};
        }
    }



}
