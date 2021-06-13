package io.github.mjtb49.strongholdtrainer.ml.model;

import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import io.github.mjtb49.strongholdtrainer.ml.RoomHelper;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import org.jetbrains.annotations.Nullable;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.exceptions.TensorFlowException;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.proto.framework.SignatureDef;
import org.tensorflow.proto.framework.TensorInfo;
import org.tensorflow.proto.framework.TensorShapeProto;
import org.tensorflow.types.TFloat32;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// TODO: Refactor and optimize
public class StrongholdModel {

    // Metadata
    private String identifier;
    private String creator;
    private String path;
    private boolean isInternal;
    // Model Metadata
    private Shape inputShape; // TODO: Shape detection
    private Shape outputShape; // TODO: Shape detection
    private String inputName;
    private String outputName;
    private SavedModelBundle modelBundle;

    /**
     * Create a new external StrongholdModel
     * @param identifier The identifier for this model. Must be unique to be registered in a ModelRegistry.
     * @param path Either the name of the classpath resource or the <b>system path</b>.
     * @param creator The name of the creator(s).
     */
    public StrongholdModel(String identifier, String path, @Nullable String creator, boolean internal) {
        this.identifier = identifier;
        this.path = path;
        this.creator = creator;
        this.isInternal = internal;
        if(!internal){
            try {
                this.modelBundle = new SavedModelLoader(this.path).loadModel();
            } catch (TensorFlowException ioException){
                System.err.println("Failed to create model " + identifier + " because of " + ioException.toString());
                ioException.printStackTrace();
                return;
            }
        } else {
            if(!path.endsWith(".zip")){
                throw new IllegalArgumentException("Internal model must be a ZIP file!");
            }
            try {
                this.modelBundle = new InternalModelLoader(path, identifier).loadModel();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        validateAndInitializeModel();
    }

    private void validateAndInitializeModel(){
        SignatureDef signatureMap = this.modelBundle.metaGraphDef().getSignatureDefMap().get("serving_default");
        Map<String, TensorInfo> inputMap = signatureMap.getInputsMap();
        Map<String, TensorInfo> outputMap = signatureMap.getOutputsMap();
        // Force models to have one input and output
        if(inputMap.size() == 1){
            TensorInfo info = inputMap.values().toArray()[0] instanceof TensorInfo ? ((TensorInfo)inputMap.values().toArray()[0]) : null;
            if(info != null){
                this.inputShape = tensorShapeProtoToShape(info.getTensorShape());
                this.inputName = info.getName();
            }
        } else {
            throw new IllegalArgumentException("Models must have only one input!");
        }
        if(outputMap.size() == 1){
            TensorInfo info = outputMap.values().toArray()[0] instanceof TensorInfo ? ((TensorInfo)outputMap.values().toArray()[0]) : null;
            if(info != null){
                this.outputShape = tensorShapeProtoToShape(info.getTensorShape());
                this.outputName = info.getName();
            }
        } else {
            throw new IllegalArgumentException("Models must have only one output!");
        }
    }

    public SavedModelBundle getModelBundle() {
        return modelBundle;
    }

    protected static Shape tensorShapeProtoToShape(TensorShapeProto tensorShapeProto){
        int numberOfDimensions = tensorShapeProto.getDimCount();
        List<TensorShapeProto.Dim> dimensionList = tensorShapeProto.getDimList();
        long[] dimensionSizes = new long[numberOfDimensions];
        for(int i = 0; i < numberOfDimensions; ++i){
            dimensionSizes[i] = dimensionList.get(i).getSize();
        }
        return Shape.of(dimensionSizes);
    }

    public double[] getPredictions(StrongholdGenerator.Start start, StrongholdGenerator.Piece piece) {
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

        Session session = this.modelBundle.session();
        try(Tensor input = RoomHelper.getMLInputFromRoom(start, piece)) {
//            System.out.println(input.shape().toString());
            try(Tensor out = session.runner()
                    .feed(this.inputName, input)
                    .fetch(this.outputName)
                    .run().get(0)){
                if(out instanceof TFloat32 && out.shape().isCompatibleWith(Shape.of(1,5))){
                    for(int i = 0; i < 5; ++i){
                        predictions[i] = ((TFloat32) out).getFloat(0,i);
                    }
                } else {
                    System.out.println("Output from model " + this.identifier + "is not formatted correctly. The output needs to be a [1,5] float32 tensor.");
                }
            }
        }
        return predictions;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getCreator() {
        return creator;
    }

    public boolean isInternal() {
        return isInternal;
    }
}
