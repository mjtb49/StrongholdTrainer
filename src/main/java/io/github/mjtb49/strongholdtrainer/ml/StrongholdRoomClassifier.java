package io.github.mjtb49.strongholdtrainer.ml;

import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.io.ClassPathResource;

public class StrongholdRoomClassifier {
    private static MultiLayerNetwork model;

    public static void init(String modelPath) {
        try {
            String simpleMlp = new ClassPathResource(modelPath).getFile().getPath();
            model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);
        } catch (Exception e) {
            //TODO better exception handling here
            System.err.println(e.getMessage());
            System.err.println("LOL");
        }
    }


    public static double[] getPredictions(StrongholdGenerator.Start start, StrongholdGenerator.Piece piece) {
        //hack fix since the model hasn't been trained on rooms where the portal room is adjacent
        int index = 0;
        for (StructurePiece piece1 : ((StrongholdTreeAccessor) start).getTree().get(piece)) {
            if (piece1 instanceof StrongholdGenerator.PortalRoom) {
                double[] output = new double[5];
                output[index] = 1.0;
                return output;
            }
            index++;
        }

        return model.output(RoomHelper.getMLInputFromRoom(start,piece)).toDoubleVector();
    }
}
