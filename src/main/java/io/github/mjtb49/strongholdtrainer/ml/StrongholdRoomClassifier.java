package io.github.mjtb49.strongholdtrainer.ml;

import io.github.mjtb49.strongholdtrainer.ml.model.StrongholdModelRegistry;
import net.minecraft.structure.StrongholdGenerator;

public class StrongholdRoomClassifier {
    public static final StrongholdModelRegistry STRONGHOLD_MODEL_REGISTRY = new StrongholdModelRegistry();

    public static void init(String zippedModelPath, String modelPath) {
        // Register default included models.
        STRONGHOLD_MODEL_REGISTRY.createAndRegisterInternal("model-version-1-nobacktracking", zippedModelPath, "Geosquare, XeroOl, Matthew Bolan");
        STRONGHOLD_MODEL_REGISTRY.createAndRegisterInternal("model-version-2-nobacktracking", zippedModelPath, "Geosquare, XeroOl, Matthew Bolan");
        STRONGHOLD_MODEL_REGISTRY.setActiveModel("model-version-1-nobacktracking");
    }


    public static double[] getPredictions(StrongholdGenerator.Start start, StrongholdGenerator.Piece piece) {
        //hack fix since the model hasn't been trained on rooms where the portal room is adjacent
        if(STRONGHOLD_MODEL_REGISTRY.getActiveModel() != null){
            return STRONGHOLD_MODEL_REGISTRY.getActiveModel().getPredictions(start, piece);
        } else {
            return new double[]{0xffD, 0xffD, 0xffD, 0xffD, 0xffD};
        }
    }


}
