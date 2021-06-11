package io.github.mjtb49.strongholdtrainer.ml;

import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class StrongholdRoomClassifier {
    private static SavedModelBundle bundle;


    public static void init(String modelPath) {
        try {
            // Experimental - This code extracts the model.zip containing the model to the config directory reserved for the mode
            /* TODO(critical): Write extensive validation code to prevent zipslip and other possible zip-based attacks (https://snyk.io/research/zip-slip-vulnerability). */
            File modelFolder;
            if(/*FabricLoader.getInstance().isDevelopmentEnvironment()*/ false){
                System.out.println("Detected a development environment, using ClassLoader");
                modelFolder = new File(Thread.currentThread().getContextClassLoader().getResource(modelPath).getPath());
            } else {
                // This is dependent on the model being stored there
                // TODO: Add initialization if the directory is empty

                modelFolder = new File(String.valueOf(FabricLoader.getInstance().getConfigDir().resolve("stronghold-trainer")));
                System.out.println("Detected a production environment, extracting model to " + modelFolder.getPath());

                if(!modelFolder.mkdirs()){
                    System.out.println("Unable to initialize config directory.");
                }
                if(modelFolder.isDirectory() && modelFolder.listFiles().length == 0){
                    URLConnection connection = Thread.currentThread().getContextClassLoader().getResource("model.zip").openConnection();
                    unzipModel(new ZipInputStream(connection.getInputStream()));
                }
            }
            bundle = SavedModelBundle.load(modelFolder.toPath().resolve("model/").toAbsolutePath().toString(), "serve");
        } catch (Exception e) {
            //TODO better exception handling here
            e.printStackTrace();
            System.err.println( e.getMessage());
            System.err.println("Unable to load model " + modelPath);
        }
    }


    public static double[] getPredictions(StrongholdGenerator.Start start, StrongholdGenerator.Piece piece) {
        //hack fix since the model hasn't been trained on rooms where the portal room is adjacent
        // TODO: Make sure the model works properly when converted
//        System.out.println("getPredictions[D: inputs are" + start + " " + piece);
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
            System.out.println(input.shape().toString());
            try(Tensor out = session.runner()
                    .feed("serving_default_input_1:0", input)
                    .fetch("StatefulPartitionedCall:0")
                    .run().get(0)){
//                System.out.println(out.shape());
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

    /**
     * Unzip our model to the config directory
     */
    public static void unzipModel(ZipFile f) throws IOException {
        // TODO: Is config the best palce to put this?
        Path targetDirectory = Paths.get(FabricLoader.getInstance().getConfigDir() + "/stronghold-trainer/");
        System.out.println("Unzipping model to " + targetDirectory);
        Enumeration<? extends ZipEntry> entries = f.entries();
        while(entries.hasMoreElements()){
            ZipEntry currentEntry = entries.nextElement();
            if(currentEntry.isDirectory()){
                Files.createDirectories(targetDirectory.resolve(currentEntry.getName()));
            } else{
                Files.copy(f.getInputStream(currentEntry), targetDirectory.resolve(currentEntry.getName()));
            }
        }
    }

    public static void unzipModel(ZipInputStream f) throws IOException {
        // TODO: Is config the best palce to put this?
        Path targetDirectory = Paths.get(FabricLoader.getInstance().getConfigDir() + "/stronghold-trainer/");
        System.out.println("Unzipping model to " + targetDirectory);
        ZipEntry e;
        while((e = f.getNextEntry()) != null){
            if(e.isDirectory()){
                Files.createDirectories(targetDirectory.resolve(e.getName()));
            } else{
                Files.copy(f, targetDirectory.resolve(e.getName()));
            }
        }
    }
}
