package io.github.mjtb49.strongholdtrainer.ml;

import net.minecraft.structure.StrongholdGenerator;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.io.ClassPathResource;

public class StrongholdRoomClassifier {
    private static MultiLayerNetwork model;
    static {
        try {
            String simpleMlp = new ClassPathResource("model102.keras").getFile().getPath();
            model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);
        } catch (Exception e) {
            //TODO better exception handling here
            System.err.println(e.getMessage());
            System.err.println("LOL");
        }
    }

    public static double[] getPredictions(StrongholdGenerator.Start start, StrongholdGenerator.Piece piece) {
        return model.output(RoomHelper.getMLInputFromRoom(start,piece)).toDoubleVector();
    }
}
