package io.github.mjtb49.strongholdtrainer.ml.model;

import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.Direction;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.exceptions.TensorFlowException;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.LongNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.proto.framework.SignatureDef;
import org.tensorflow.proto.framework.TensorInfo;
import org.tensorflow.proto.framework.TensorShapeProto;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TInt64;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.github.mjtb49.strongholdtrainer.ml.model.InternalModelLoader.CONFIG_DIRECTORY;

// TODO: Refactor and optimize
public class StrongholdModel {


    private final Map<Direction, int[]> directionMap = new HashMap<Direction, int[]>() {{
        put(Direction.EAST, new int[]{1, 0, 0, 0});
        put(Direction.WEST, new int[]{0, 1, 0, 0});
        put(Direction.NORTH, new int[]{0, 0, 1, 0});
        put(Direction.SOUTH, new int[]{0, 0, 0, 1});
    }};
    // Metadata
    private String identifier;
    private String creator;
    private String path;
    private final boolean isInternal;
    private boolean verboseOutput = false;
    // Model Metadata
    private Shape inputShape; // TODO: Shape detection
    private Shape outputShape; // TODO: Shape detection
    private String inputName;
    private String outputName;
    private SavedModelBundle modelBundle;
    private Map<Class<? extends StructurePiece>, int[]> roomVectorMap = new HashMap<Class<? extends StructurePiece>, int[]>() {{
        put(StrongholdGenerator.Corridor.class, new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        put(StrongholdGenerator.PrisonHall.class, new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        put(StrongholdGenerator.LeftTurn.class, new int[]{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        put(StrongholdGenerator.RightTurn.class, new int[]{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        put(StrongholdGenerator.SquareRoom.class, new int[]{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        put(StrongholdGenerator.Stairs.class, new int[]{0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0});
        put(StrongholdGenerator.SpiralStaircase.class, new int[]{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0});
        put(StrongholdGenerator.FiveWayCrossing.class, new int[]{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0});
        put(StrongholdGenerator.ChestCorridor.class, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0});
        put(StrongholdGenerator.Library.class, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0});
        put(StrongholdGenerator.PortalRoom.class, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0});
        put(StrongholdGenerator.SmallCorridor.class, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0});
        put(StrongholdGenerator.Start.class, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0});
        put(null, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1});
    }};
    private final HashMap<String, Class<? extends StructurePiece>> pieceNotationToClassMap = new HashMap<String, Class<? extends StructurePiece>>() {{
        put("COR", StrongholdGenerator.Corridor.class);
        put("PRI", StrongholdGenerator.PrisonHall.class);
        put("LEF", StrongholdGenerator.LeftTurn.class);
        put("RIG", StrongholdGenerator.RightTurn.class);
        put("SQU", StrongholdGenerator.SquareRoom.class);
        put("STA", StrongholdGenerator.Stairs.class);
        put("SPI", StrongholdGenerator.SpiralStaircase.class);
        put("FIV", StrongholdGenerator.FiveWayCrossing.class);
        put("CHE", StrongholdGenerator.ChestCorridor.class);
        put("LIB", StrongholdGenerator.Library.class);
        put("POR", StrongholdGenerator.PortalRoom.class);
        put("SMA", StrongholdGenerator.SmallCorridor.class);
        put("SPI*", StrongholdGenerator.Start.class);
        put("NUL", null);
    }};
    private List<RoomData> inputOrder;
    private List<RoomData> outputOrder;


    /**
     * Create a new external StrongholdModel
     *
     * @param path    The <b>system path</b>.
     * @param creator The name of the creator(s).
     */
    public StrongholdModel(String path, @Nullable String creator, boolean isInternal) {
        this.path = path;
        this.creator = creator;
        this.isInternal = isInternal;
        if(this.isInternal){
            this.creator = creator;
            try {
                this.path = unzipAndGetPath((UUID.randomUUID()).toString(), path).toString();
                this.modelBundle = new SavedModelLoader(this.path).loadModel();
            } catch (TensorFlowException | IOException ioException) {
                System.err.println("Failed to create model " + identifier + " because of " + ioException.toString());
                ioException.printStackTrace();
                return;
            }

        } else {
            try {
                this.modelBundle = new SavedModelLoader(this.path).loadModel();
            } catch (TensorFlowException ioException) {
                System.err.println("Failed to create model " + identifier + " because of " + ioException.toString());
                ioException.printStackTrace();
                return;
            }

        }

        try {
            parseSTMeta();
            System.out.println("Successfully parsed STMETA for model!");
        } catch (Exception e) {
            System.out.println("Failed to parse STMETA file, using default initializer.");
            e.printStackTrace();
            initializeToDefaultClassifierEncoding();
        }

        SignatureDef signatureMap = this.modelBundle.metaGraphDef().getSignatureDefMap().get("serving_default");
        Map<String, TensorInfo> inputMap = signatureMap.getInputsMap();
        Map<String, TensorInfo> outputMap = signatureMap.getOutputsMap();

        // Force models to have one input and output
        if (inputMap.size() == 1) {
            TensorInfo info = inputMap.values().toArray()[0] instanceof TensorInfo ? ((TensorInfo) inputMap.values().toArray()[0]) : null;
            if (info != null) {
                this.inputName = info.getName();
            }
        } else {
            throw new IllegalArgumentException("Models must have only one input!");
        }
        if (outputMap.size() == 1) {
            TensorInfo info = outputMap.values().toArray()[0] instanceof TensorInfo ? ((TensorInfo) outputMap.values().toArray()[0]) : null;
            if (info != null) {
                this.outputName = info.getName();
            }
        } else {
            throw new IllegalArgumentException("Models must have only one output!");
        }
    }

    @Deprecated // Reading shape from .stmeta
    protected static Shape tensorShapeProtoToShape(TensorShapeProto tensorShapeProto) {
        int numberOfDimensions = tensorShapeProto.getDimCount();
        List<TensorShapeProto.Dim> dimensionList = tensorShapeProto.getDimList();
        long[] dimensionSizes = new long[numberOfDimensions];
        for (int i = 0; i < numberOfDimensions; ++i) {
            dimensionSizes[i] = dimensionList.get(i).getSize() < 0 ? 1 : dimensionList.get(i).getSize();
        }
        return Shape.of(dimensionSizes);
    }

    static protected Tensor intArrayToInputTensor(int[][] data) {
        LongNdArray input = NdArrays.ofLongs(Shape.of(1, data[0].length));
        long[][] toInt64 = new long[1][data[0].length];
        for (int i = 0; i < toInt64[0].length; ++i) {
            toInt64[0][i] = data[0][i];
        }
        input.set(NdArrays.vectorOf(toInt64[0]), 0);
        return TInt64.tensorOf(input);
    }

    // HACK FIX: to get neoprene's model loaded, for some reason the model's input is shaped [-1,-1,89]. also hacks the dtype
    static protected Tensor int3ArrayToInputTensor(int[][][] data) {
        FloatNdArray input = NdArrays.ofFloats(Shape.of(1, 1, data[0][0].length));
        float[][][] toInt64 = new float[1][1][data[0][0].length];
        for (int i = 0; i < toInt64[0].length; ++i) {
            toInt64[0][0][i] = data[0][0][i];
        }
        input.set(NdArrays.vectorOf(toInt64[0][0]), 0, 0);
        return TFloat32.tensorOf(input);
    }


    private static HashMap<Class<? extends StructurePiece>, int[]> orderToFullMap(LinkedList<Class<? extends StructurePiece>> list) {
        HashMap<Class<? extends StructurePiece>, int[]> hashMap = new HashMap<>();
        int length = list.size();
        int[] oneHotEncoding = new int[length];
        for (int i = 0; i < length; ++i) {
            Arrays.fill(oneHotEncoding, 0);
            oneHotEncoding[i] = 1;
            hashMap.put(list.get(i), Arrays.copyOf(oneHotEncoding, length));
        }
        if(!hashMap.containsKey(null)){
            Arrays.fill(oneHotEncoding, 0);
            hashMap.put(null, Arrays.copyOf(oneHotEncoding, length));
        }
        return hashMap;
    }


    private static Path unzipAndGetPath(String modelIdentifier, String zippedPath) throws IOException {
        File modelFolder;
        modelFolder = new File(CONFIG_DIRECTORY.toString());
        FileUtils.deleteDirectory(modelFolder);
        if (!modelFolder.exists()) {
            modelFolder.mkdirs();
        }
        if (modelFolder.isDirectory()) {
            URLConnection connection = Thread.currentThread().getContextClassLoader().getResource(zippedPath).openConnection();
            unzipModel(new ZipInputStream(connection.getInputStream()), modelIdentifier);
        }
        return CONFIG_DIRECTORY.resolve(modelIdentifier).resolve("model/").toAbsolutePath();
    }

    private static void unzipModel(ZipInputStream f, String targetInternal) throws IOException {
        Path targetDirectory = CONFIG_DIRECTORY.resolve(targetInternal + "/");
        if ((new File(targetDirectory.toString())).exists()) {
            System.out.println("Model folder " + targetInternal + " already exists!");
            return;
        }
        System.out.println("Unzipping model to " + targetDirectory);
        ZipEntry e;
        while ((e = f.getNextEntry()) != null) {
            if (e.isDirectory()) {
                Files.createDirectories(targetDirectory.resolve(e.getName()));
            } else {
                Files.copy(f, targetDirectory.resolve(e.getName()));
            }
        }
    }

    private void initializeToDefaultClassifierEncoding() {

        this.inputOrder = new LinkedList<RoomData>() {{
            add(RoomData.DEPTH); // 1
            add(RoomData.PREV); // 14
            add(RoomData.CURRENT); // 14
            add(RoomData.EXIT_1); // 14
            add(RoomData.EXIT_2);// 14
            add(RoomData.EXIT_3); // 14
            add(RoomData.EXIT_4); // 14
            add(RoomData.EXIT_5); // 14
            add(RoomData.DIRECTION); // 4
        }};
        this.outputOrder = new LinkedList<RoomData>() {{
            add(RoomData.EXIT_1);
            add(RoomData.EXIT_2);
            add(RoomData.EXIT_3);
            add(RoomData.EXIT_4);
            add(RoomData.EXIT_5);
        }};
    }

    public SavedModelBundle getModelBundle() {
        return modelBundle;
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
        Arrays.fill(predictions, 0.0d);

        Session session = this.modelBundle.session();
        try (Tensor input = this.getMLInputFromRoom(start, piece)) {
//            System.out.println(input.shape().toString());
            try (Tensor out = session.runner()
                    .feed(this.inputName, input)
                    .fetch(this.outputName)
                    .run().get(0)) {
                predictions = processOutput(out);
            }
        }
        return predictions;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Tensor getMLInputFromRoom(StrongholdGenerator.Start start, StrongholdGenerator.Piece piece) {
        //TODO looks like start is null here sometimes
        int[][] input = new int[][]{{}};
//        this.roomVectorMap.forEach((key, value) -> System.out.println(key + Arrays.toString(value)));
        StrongholdTreeAccessor castStart = (StrongholdTreeAccessor) start;
        for (RoomData type : this.inputOrder) {
            if(type.roomDataType == RoomData.RoomDataType.INT_SCALAR){
                input[0] = ArrayUtils.add(input[0], (int) type.roomDataFunction.apply(castStart, piece));
            } else if(type.roomDataType == RoomData.RoomDataType.STRUCTURE_PIECE_VECTOR){
                StructurePiece result = (StructurePiece) type.roomDataFunction.apply(castStart, piece);
                if(result == null){
                    input[0] = ArrayUtils.addAll(input[0], roomVectorMap.get(null));
                } else {
                    input[0] = ArrayUtils.addAll(input[0], roomVectorMap.get(result.getClass()));
                }
            } else if(type.roomDataType == RoomData.RoomDataType.DIRECTION_VECTOR){
                Direction result = (Direction) type.roomDataFunction.apply(castStart, piece);
                input[0] = ArrayUtils.addAll(input[0], directionMap.get(result));
            }
        }
//        System.out.println("NEW "+Arrays.deepToString(input));
        Tensor inputTensor;
        if (this.inputShape.numDimensions() == 3) {
            int[][][] boxedInput = new int[1][1][((int) this.inputShape.size(2))];
            inputTensor = int3ArrayToInputTensor(boxedInput);
        } else {
            inputTensor = intArrayToInputTensor(input);
        }
        if (!inputTensor.shape().isCompatibleWith(this.inputShape)) {
            inputTensor.close();
            throw new IllegalArgumentException("Input " + inputTensor.shape() + " is not compatible with shape " + this.inputShape);
        } else {
            return inputTensor;
        }
    }

    public double[] processOutput(Tensor output) {
        if (!output.shape().isCompatibleWith(this.outputShape)) {
            output.close();
            throw new IllegalArgumentException("Output tensor doesn't match shape");
        } else {
            double[] predictions = new double[(int) this.outputShape.size(1)];
            if (output instanceof TFloat32) {
                for (int i = 0; i < predictions.length; ++i) {
                    predictions[i] = ((TFloat32) output).getFloat(0, i);
                }
            }
            return predictions;
        }
    }

    public String getCreator() {
        return creator;
    }

    public boolean isInternal() {
        return isInternal;
    }

    private void parseSTMeta() throws IOException {
        File metadataFile = new File(this.path + "/model.stmeta");
        if (!metadataFile.exists()) {
            throw new FileNotFoundException("No stmeta file at " + metadataFile.getPath() + ", using Geo classifier default init.");
        }
        BufferedReader fileReader = new BufferedReader(new FileReader(metadataFile));
        String current = fileReader.readLine();
        String[] args;
        int line = 1;
        /*byte flags = 0;*/ // All 8 things need to be defined
        try {
            if (current.contains("stmeta")) {
                while ((current = fileReader.readLine()) != null) {
                    ++line;
//                    System.out.println(line);
                    if (current.isEmpty()) {
                        // Consume empty lines
                        ;
                    } else {
                        args = current.split("=");
                        if (current.equals("eof")) {
                            break;
                        }
                        if (args.length < 2) {
                            throw new STMetaSyntaxException(line, "Illegal statement. All stmeta statements must be two arguments separated by an '='");
                        }
                        if (args[0].equals("creator")) {
                            this.creator = parseStringLiteral(args[1], line);
                        } else if (args[0].equals("id")) {
                            String candidateID = parseStringLiteral(args[1], line);
                            if (candidateID.isEmpty()) {
                                throw new STMetaSyntaxException(line, "Invalid identifier: Cannot be blank!");
                            }
                            this.identifier = candidateID;
                        } else if (args[0].equals("input_shape")) {
                            this.inputShape = parseShapeDefinition(args[1], line);
                        } else if (args[0].equals("redefine(ROOM_TO_VECTOR)")) {
                            LinkedList<Class<? extends StructurePiece>> order = new LinkedList<>();
                            String[] redef = args[1].replaceAll(" ", "").split(",");
                            for (String roomCode : redef) {
                                Class<? extends StructurePiece> piece = pieceNotationToClassMap.get(roomCode);
                                if (!pieceNotationToClassMap.containsKey(roomCode)) {
                                    throw new STMetaSyntaxException(line, "Invalid ROOM->VEC redefine: Invalid room token " + roomCode);
                                }
                                order.add(piece);
                            }
                            this.roomVectorMap = orderToFullMap(order);
                        } else if (args[0].equals("redefine(DIR_TO_VECTOR)")) {
                            // TODO We aren't allowing this to be defined currently.
                        } else if (args[0].equals("input_vec_order")) {
                            this.inputOrder = parseVecOrder(args[1], line);
                        } else if (args[0].equals("output_shape")) {
                            this.outputShape = parseShapeDefinition(args[1], line);
                        } else if (args[0].equals("output_vec_order")) {
                            // TODO We're not handling this rn.
                            this.outputOrder = parseVecOrder(args[1], line);
                        }
                    }
                }
            } else {
                throw new STMetaSyntaxException(1, "Invalid file: no stmeta statement");
            }
        } catch (Exception e) {
            fileReader.close();
            throw e;
        }
        fileReader.close();

    }

    private List<RoomData> parseVecOrder(String commaDelimitedVecOrder, int line) throws STMetaSyntaxException {
        List<RoomData> list = new LinkedList<>();
        String[] delimited = commaDelimitedVecOrder.split(",");
        if (delimited.length < 2) {
            throw new STMetaSyntaxException(line, "Illegal data order definition: not enough or no room data types specified.");
        }
        RoomData dataType;
        for (String entry : delimited) {
            try {
                dataType = RoomData.valueOf(entry);
            } catch (IllegalArgumentException e) {
                throw new STMetaSyntaxException(line, "Illegal data order definition: room data token");
            }
            list.add(dataType);
        }
        return list;
    }

    private String parseStringLiteral(String literal, int line) throws STMetaSyntaxException {
        char[] chars = literal.toCharArray();
        if (chars.length < 2) {
            throw new STMetaSyntaxException(line, "Illegal string literal: too few characters!");
        }
        if ((chars[0] == '"') && (chars[chars.length - 1] == '"')) {
            if (chars.length == 2) {
                return "";
            } else {
                return literal.replaceAll("\"", "");
            }
        } else {
            throw new STMetaSyntaxException(line, "Illegal string literal: does not start and end with '\"'!");
        }
    }

    private Shape parseShapeDefinition(String input, int line) throws STMetaSyntaxException {
        if (input.length() < 2) {
            throw new STMetaSyntaxException(line, "Invalid shape declaration: too few characters!");
        } else if (!(input.charAt(0) == '[') || !(input.charAt(input.length() - 1) == ']')) {
            throw new IllegalArgumentException("Invalid shape definition: isn't enclosed with []");
        }
        String shape = input.replaceAll("\\[", "").replaceAll("]", "");
        String[] shapeDef = shape.split(",");
        if (shapeDef.length > 1) {
            long[] dimSizes = new long[shapeDef.length];
            for (int i = 0; i < dimSizes.length; ++i) {
                try {
                    System.out.println("WARN: Hard converting unknown size to 1");
                    dimSizes[i] = Long.parseLong(shapeDef[i]) > -1 ? Long.parseLong(shapeDef[i]) : 1;
                } catch (NumberFormatException e) {
                    throw new STMetaSyntaxException(line, "Illegal shape definition: invalid integer literal!");
                }
            }
            return Shape.of(dimSizes);
        } else {
            throw new STMetaSyntaxException(line, "Invalid shape definition: stmeta shape must have more than 1 dimension");
        }
    }

    public Shape getInputShape() {
        return inputShape;
    }

    public Shape getOutputShape() {
        return outputShape;
    }

    public Map<String, SignatureDef> getSignatureDefDebug(){
        return this.modelBundle.metaGraphDef().getSignatureDefMap();
    }

    public void toggleVerboseOutput(){
        this.verboseOutput = !this.verboseOutput;
    }

    public void setVerboseOutput(boolean verboseOutput1){
        this.verboseOutput =  verboseOutput1;
    }
}
