package io.github.mjtb49.strongholdtrainer.ml;

import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;

import java.io.File;
import java.util.Arrays;

public class StrongholdRoomClassifier {
    private static SavedModelBundle bundle;


    public static void init(String modelPath) {
        try {
            File modelFolder = new File(Thread.currentThread().getContextClassLoader().getResource(modelPath).getPath());
            bundle = SavedModelBundle.load(modelFolder.getPath(), "serve");
        } catch (Exception e) {
            //TODO better exception handling here
            System.err.println(e.getMessage());
            System.err.println("Unable to load model " + modelPath);
        }
    }


    public static double[] getPredictions(StrongholdGenerator.Start start, StrongholdGenerator.Piece piece) {
        //hack fix since the model hasn't been trained on rooms where the portal room is adjacent
        // TODO: Make sure the model works properly when converted
        int index = 0;
        for (StructurePiece piece1 : ((StrongholdTreeAccessor) start).getTree().get(piece)) {
            if (piece1 instanceof StrongholdGenerator.PortalRoom) {
                double[] output = new double[5];
                output[index] = 1.0;
                return output;
            }
            index++;
        }
        double[] predictions = new double[5];
        Arrays.fill(predictions,0.0d);

        Session session = bundle.session();
        try(Tensor input = RoomHelper.getMLInputFromRoom(start, piece)) {
            try(Tensor out = session.runner()
                    .feed("serving_default_input_1:0", input)
                    .fetch("StatefulPartitionedCall:0")
                    .run().get(0)){
                System.out.println(out.shape());
                if(out instanceof TFloat32 && out.shape().isCompatibleWith(Shape.of(1,5))){
                    for(int i = 0; i < 5; ++i){
                        predictions[i] = ((TFloat32) out).getFloat(0,i);
                    }
                } else {
                    System.out.println("Output from model is not formatted correctly. The output needs to be a [1,5] float32 tensor.");
                }
            }
        }
        return predictions;
    }
}
