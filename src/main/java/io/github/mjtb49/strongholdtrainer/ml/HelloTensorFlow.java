package io.github.mjtb49.strongholdtrainer.ml;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;

import java.io.IOException;


public class HelloTensorFlow {

    public static void main( String[] args ) throws IOException
    {
        // good idea to print the version number, 1.2.0 as of this writing
        String simpleMlp = new ClassPathResource("model102.keras").getFile().getPath();
        try {
            MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);
            // make a random sample
            int inputs = 103;
// get the prediction
            INDArray prediction = model.output(Nd4j.createFromArray(
                    new int[][] {{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}})
            );
            System.out.println(prediction.getDouble(0));
            System.out.println(prediction.getDouble(1));
            System.out.println(prediction.getDouble(2));
            System.out.println(prediction.getDouble(3));
            System.out.println(prediction.getDouble(4));
            System.out.println("OMG");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.err.println("LOL");
        }
    }
}
