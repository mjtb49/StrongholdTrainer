package io.github.mjtb49.strongholdtrainer.ml.model;

import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import net.fabricmc.loader.api.FabricLoader;
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

public class StrongholdModel {

    public static Path CONFIG_DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("stronghold-trainer");

    private Map<Direction, int[]> directionMap = new HashMap<Direction, int[]>() {{
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
    private static final Map<Integer, int[]> indexVectorMap = new HashMap<Integer, int[]>() {{
        put(0, new int[]{1,0,0,0,0,0});
        put(1, new int[]{0,1,0,0,0,0});
        put(2, new int[]{0,0,1,0,0,0});
        put(3, new int[]{0,0,0,1,0,0});
        put(4, new int[]{0,0,0,0,1,0});
        put(5, new int[]{0,0,0,0,0,1});
    }};
    private final static HashMap<String, Class<? extends StructurePiece>> pieceNotationToClassMap = new HashMap<String, Class<? extends StructurePiece>>() {{
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
    private final static HashMap<String, Direction> directionNotationToDirectionMap = new HashMap<String, Direction>() {{
        put("E", Direction.EAST);
        put("W", Direction.WEST);
        put("N", Direction.NORTH);
        put("S", Direction.SOUTH);
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
            } catch (TensorFlowException | IOException ioException) {
                System.err.println("Failed to create model at "+ this.path + " because of " + ioException.toString());
                ioException.printStackTrace();
                return;
            }
        }

        this.modelBundle = SavedModelBundle.load(this.path);

        try {
            parseSTMeta();
            System.out.println("Successfully parsed STMETA for model!");
        } catch (Exception e) {
            System.out.println("Failed to parse STMETA file, using default initializer.");
            e.printStackTrace();
            initializeDefault();
        }

        SignatureDef signatureMap = this.modelBundle.metaGraphDef().getSignatureDefMap().get("serving_default");
        Map<String, TensorInfo> inputMap = signatureMap.getInputsMap();
        Map<String, TensorInfo> outputMap = signatureMap.getOutputsMap();

        // Force models to have one input and output
        if (inputMap.size() == 1) {
            TensorInfo info = inputMap.values().toArray()[0] instanceof TensorInfo ? ((TensorInfo) inputMap.values().toArray()[0]) : null;
            if (info != null) {
                this.inputName = info.getName();
//                System.out.println(info.getTensorShape());
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
        System.out.println(this.outputName);
        System.out.println(this.outputShape);
        System.out.println(this.inputName);
        System.out.println(this.inputShape);
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


    static protected Tensor int3ArrayToInputTensor(int[][] data) {
        FloatNdArray input = NdArrays.ofFloats(Shape.of(1, 1, data[0].length));
        float[][][] toInt64 = new float[1][1][data[0].length];
        for (int i = 0; i < data[0].length; ++i) {
            toInt64[0][0][i] = data[0][i];
        }
        input.set(NdArrays.vectorOf(toInt64[0][0]), 0, 0);
        return TFloat32.tensorOf(input);
    }


    private static <T> HashMap<T, int[]> encodingOrderToFullMap(LinkedList<T> list) {
        HashMap<T, int[]> hashMap = new HashMap<>();
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

    private void initializeDefault() {

        this.inputOrder = new LinkedList<RoomData>() {{
            add(RoomData.DEPTH); // 1
            add(RoomData.PREVIOUS_ROOM); // 14
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

    public double[] getPredictions(StrongholdGenerator.Start start, StrongholdGenerator.Piece piece, StrongholdGenerator.Piece prev) {
        int index = 0;

        for (StructurePiece piece1 : ((StrongholdTreeAccessor) start).getTree().get(piece)) {
            if (piece1 instanceof StrongholdGenerator.PortalRoom) {
                double[] output = new double[5];
                output[index] = 1.0;
                return output;
            }
            index++;
        }

        double[] predictions = new double[this.outputShape.size(1) == 5 ? 5 : 6];
        Arrays.fill(predictions, 0.0d);

        Session session = this.modelBundle.session();
        try (Tensor input = this.getMLInputFromRoom(start, piece, prev)) {
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

    public Tensor getMLInputFromRoom(StrongholdGenerator.Start start, StrongholdGenerator.Piece piece, StrongholdGenerator.Piece previous) {
        //TODO looks like start is null here sometimes
        int[][] input = new int[][]{{}};
//        this.roomVectorMap.forEach((key, value) -> System.out.println(key + Arrays.toString(value)));
        StrongholdTreeAccessor castStart = (StrongholdTreeAccessor) start;
        for (RoomData type : this.inputOrder) {
//            System.out.println(type.name() + ": " + type.roomDataFunction.apply(castStart, piece, previous));
            if(type.roomDataType == RoomData.RoomDataType.INT_SCALAR){
                System.out.println(type.name() + ": " + type.roomDataFunction.apply(castStart, piece, previous));
                input[0] = ArrayUtils.add(input[0], (Integer) type.roomDataFunction.apply(castStart, piece, previous));
            } else if(type.roomDataType == RoomData.RoomDataType.STRUCTURE_PIECE_VECTOR){
                StructurePiece result = (StructurePiece) type.roomDataFunction.apply(castStart, piece, previous);
                if(result == null){
                    System.out.println(type.name() + ": " + type.roomDataFunction.apply(castStart, piece, previous) + "->" + Arrays.toString(roomVectorMap.get(null)));
                    input[0] = ArrayUtils.addAll(input[0], roomVectorMap.get(null));
                } else {
                    System.out.println(type.name() + ": " + type.roomDataFunction.apply(castStart, piece, previous) + "->" + Arrays.toString(roomVectorMap.get(result.getClass())));

                    input[0] = ArrayUtils.addAll(input[0], roomVectorMap.get(result.getClass()));
                }
            } else if(type.roomDataType == RoomData.RoomDataType.DIRECTION_VECTOR){
                Direction result = (Direction) type.roomDataFunction.apply(castStart, piece, previous);
                System.out.println(type.name() + ": " + type.roomDataFunction.apply(castStart, piece, previous) + "->" + Arrays.toString(directionMap.get(result)));
                input[0] = ArrayUtils.addAll(input[0], directionMap.get(result));
            } else if(type.roomDataType == RoomData.RoomDataType.INDEX_VECTOR){
                System.out.println(type.name() + ": " + type.roomDataFunction.apply(castStart, piece, previous) + "->" + Arrays.toString(indexVectorMap.get(type.roomDataFunction.apply(castStart,piece,previous))));

                input[0] = ArrayUtils.addAll(input[0], indexVectorMap.get(type.roomDataFunction.apply(castStart, piece, previous)));
            }
        }
//        System.out.println("NEW "+Arrays.deepToString(input));
        Tensor inputTensor;
        if (this.inputShape.numDimensions() == 3) {

            inputTensor = int3ArrayToInputTensor(input);
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
                for(int j = 0; j < output.shape().size(1); ++j){
                    System.out.println(((TFloat32) output).getFloat(0, j));
                }

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
            throw new FileNotFoundException("No stmeta file at " + metadataFile.getPath() + ", using default init.");
        }
        BufferedReader fileReader = new BufferedReader(new FileReader(metadataFile));
        String current = fileReader.readLine();
        String[] args;
        int line = 1;
        byte flags = 0;
        try {
            if (current.contains("stmeta")) {
                while ((current = fileReader.readLine()) != null) {
                    ++line;
                    if (!current.isEmpty()) {
                        args = current.split("=");
                        if (current.equals("eof")) {
                            break;
                        }
                        if (args.length < 2) {
                            throw new STMetaSyntaxException(line, "Illegal statement. All stmeta statements must be two arguments separated by an '='");
                        }
                        if (args[0].equals("creator")) {
                            this.creator = parseStringLiteral(args[1], line);
                            flags |= 1;
                        } else if (args[0].equals("id")) {
                            String candidateID = parseStringLiteral(args[1], line);
                            if (candidateID.isEmpty()) {
                                throw new STMetaSyntaxException(line, "Invalid identifier: Cannot be blank!");
                            }
                            this.identifier = candidateID;
                            flags |= 1 << 1;
                        } else if (args[0].equals("input_shape")) {
                            this.inputShape = parseShapeDefinition(args[1], line);
                            flags |= 1 << 2;
                        } else if (args[0].startsWith("redefine")) {
                            String target = args[0].replace("redefine", "");
                            if(target.equals("(ROOM_TO_VECTOR)")){
                                try{
                                    LinkedList<Class<? extends StructurePiece>> order = parseEncodingRedefinition(args[1], pieceNotationToClassMap);
                                    this.roomVectorMap = encodingOrderToFullMap(order);
                                } catch (Exception e){
                                    throw new STMetaSyntaxException(line, "Invalid ROOM->VEC redefine: " + e.getMessage());
                                }
                            } else if(target.equals("(DIR_TO_VECTOR)")){
                                try{
                                    LinkedList<Direction> order = parseEncodingRedefinition(args[1], directionNotationToDirectionMap);
                                    this.directionMap = encodingOrderToFullMap(order);
                                } catch (Exception e){
                                    throw new STMetaSyntaxException(line, "Invalid DIR->VEC redefine: " + e.getMessage());
                                }
                            }
                        } else if (args[0].equals("input_vec_order")) {
                            this.inputOrder = parseVecOrder(args[1], line);
                            flags |= 1 << 3;
                        } else if (args[0].equals("output_shape")) {
                            this.outputShape = parseShapeDefinition(args[1], line);
                            flags |= 1 << 4;
                        } else if (args[0].equals("output_vec_order")) {
                            this.outputOrder = parseVecOrder(args[1], line);
                            flags |= 1 << 5;
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
        giveParserWarnings(flags);
        fileReader.close();

    }

    private void giveParserWarnings(byte flags){
//        System.out.println(Integer.toBinaryString(flags));
        if(((flags) & 1) != 1){
            warn("Creator not defined!", 0);
            this.creator = "";
        }
        if(((flags >> 1) & 1) != 1){
            warn("Identifier not defined!", 0);
            this.identifier = UUID.randomUUID().toString();
        }
        if(((flags >> 2) & 1) != 1){
            warn("Input shape not defined!", 0);
            this.identifier = UUID.randomUUID().toString();
        }
        if(((flags >> 3) & 1) != 1){
            warn("Input vector order not defined, using default!", 0);
        }
        if(((flags >> 4) & 1) != 1){
            warn("Output shape not defined, using default!", 0);
        }
        if(((flags >> 5) & 1) != 1){
            warn("Output vector order not defined, but it doesn't matter!", 0);
        }

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
                throw new STMetaSyntaxException(line, "Illegal data order definition: room data token " + entry);
            }
            list.add(dataType);
        }
        return list;
    }

    private static <T> LinkedList<T> parseEncodingRedefinition(String input, HashMap<String, T> mapping){
        LinkedList<T> order = new LinkedList<>();
        String[] redef = input.replaceAll(" ", "").split(",");
        for (String token : redef) {
            T piece = mapping.get(token);
            if (!mapping.containsKey(token)) {
                throw new IllegalArgumentException("Invalid token \"" + token + "\"");
            }
            order.add(piece);
        }
        return order;
    }

    private static void warn(String message, int line){
        System.out.println("\u001b[33mSTMETA WARN at line " + line + ": " + message + "\u001b[0m");
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
                    warn("Converting unknown size to 1!", line);
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

}
