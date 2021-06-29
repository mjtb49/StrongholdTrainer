package io.github.mjtb49.strongholdtrainer.stats;

import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdMachineLearning;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class PlayerPathTracker {

    private static final int INACCURACY_THRESHOLD = 20 * 5;
    private static final int MISTAKE_THRESHOLD = 20 * 10;

    private final StrongholdTreeAccessor strongholdTreeAccessor;
    private final StartAccessor startAccessor;

    private final ArrayList<Pair<StrongholdGenerator.Piece, Integer>> rooms;
    StrongholdGenerator.PortalRoom portalRoom;

    private final ArrayList<StrongholdGenerator.Piece> inaccurateRooms;
    private final ArrayList<StrongholdGenerator.Piece> mistakeRooms;

    public PlayerPathTracker(StructureStart<?> start) {
        startAccessor = (StartAccessor) start;
        strongholdTreeAccessor = (StrongholdTreeAccessor) startAccessor.getStart();
        rooms = new ArrayList<>();
        inaccurateRooms = new ArrayList<>();
        mistakeRooms = new ArrayList<>();
    }

    public void addPiece(StrongholdGenerator.Piece piece, int time) {
        if (piece instanceof StrongholdGenerator.PortalRoom)
            portalRoom = (StrongholdGenerator.PortalRoom) piece;
        rooms.add(new Pair<>(piece, time));
    }

    private double computeDifficulty(ArrayList<StructurePiece> solution) {
        double difficulty = 1.0;
        //Loop starts at 2 because the portal room and the room before it have messed up policy but are always trivial
        for (int i = 2; i < solution.size(); i++) {
            //difficulty *= 0.5;
            difficulty *= getWeightOfCorrectDoor(solution,(StrongholdGenerator.Piece) solution.get(i),
                    StrongholdMachineLearning.MODEL_REGISTRY.getModel("basic-classifier-nobacktracking").getPredictions(startAccessor.getStart(), (StrongholdGenerator.Piece) solution.get(i)));

        }
        return difficulty;
    }

    private int computeWastedTime(ArrayList<StructurePiece> solution) {
        int wastedTime = 0;
        for (int i = 0; i < rooms.size() - 1; i++) {
            if (!solution.contains(rooms.get(i).getLeft()))
                wastedTime += rooms.get(i).getRight();
        }
        return wastedTime;
    }

    private int countWormholes() {
        int wormholes = 0;
        for (int i = 0; i < rooms.size() - 1; i++) {
            if (strongholdTreeAccessor.getParents().get(rooms.get(i+1).getLeft()) != rooms.get(i).getLeft()
                    && strongholdTreeAccessor.getParents().get(rooms.get(i).getLeft()) != rooms.get(i + 1).getLeft()) {
                wormholes++;
            }
        }
        return wormholes;
    }

    public void reviewAndUpdateStats(ServerPlayerEntity playerEntity, int ticksInStronghold) {
        if (portalRoom != null) {
            ArrayList<StructurePiece> solution = new ArrayList<>();
            StrongholdGenerator.Piece current = portalRoom;
            while (current != null) {
                solution.add(current);
                current = (StrongholdGenerator.Piece) strongholdTreeAccessor.getParents().get(current);
            }

            double difficulty = computeDifficulty(solution);
            int wastedTime = computeWastedTime(solution);
            int wormholeCount = countWormholes();

            int bestMoveCount = 0;
            int inaccuracyCount = 0;
            int mistakeCount = 0;
            int roomsReviewed = 0;

            int indexOfGoodRoom = 0;
            while (indexOfGoodRoom < rooms.size() - 1) {
                int currentWastedTime = 0;

                //force the piece to have a portal room in its future
                StrongholdGenerator.Piece currentGoodPiece = rooms.get(indexOfGoodRoom).getLeft();
                while (!solution.contains(currentGoodPiece)) {
                    indexOfGoodRoom++;
                    currentGoodPiece = rooms.get(indexOfGoodRoom).getLeft();
                }

                if (currentGoodPiece != portalRoom) {
                    double[] policy = StrongholdMachineLearning.MODEL_REGISTRY.getModel("basic-classifier-nobacktracking").getPredictions(startAccessor.getStart(), currentGoodPiece);

                    double maximumWeight = -1;
                    for (double d : policy)
                        maximumWeight = Math.max(d, maximumWeight);

                    StrongholdGenerator.Piece nextPiece = rooms.get(indexOfGoodRoom + 1).getLeft();
                    //did the player choose a child?
                    if (strongholdTreeAccessor.getTree().get(currentGoodPiece).contains(nextPiece)) {
                        if (!solution.contains(nextPiece)) {
                            roomsReviewed++;
                            double chosenWeight = policy[strongholdTreeAccessor.getTree().get(currentGoodPiece).indexOf(nextPiece)];
                            int j = indexOfGoodRoom;
                            while (!solution.contains(nextPiece)) {
                                nextPiece = rooms.get(j + 1).getLeft();
                                currentWastedTime += rooms.get(j + 1).getRight();
                                j++;
                            }

                            int expectedLostTicks = (int) (currentWastedTime * (maximumWeight - chosenWeight));
                            //TODO, deal with correct choices the AI marks as mistakes
                            if (expectedLostTicks >= MISTAKE_THRESHOLD) {
                                mistakeCount++;
                                mistakeRooms.add(currentGoodPiece);
                            } else if (expectedLostTicks >= INACCURACY_THRESHOLD) {
                                inaccuracyCount++;
                                inaccurateRooms.add(currentGoodPiece);
                            }
                        } else {
                            int numNonNull = 0;
                            for (StructurePiece piece : strongholdTreeAccessor.getTree().get(currentGoodPiece)) {
                                if (piece != null)
                                    numNonNull++;
                            }
                            if (numNonNull > 1) {
                                roomsReviewed++;
                                bestMoveCount++;
                            }
                        }
                    }
                }
                indexOfGoodRoom++;
            }

            PlayerPathData playerPathData = new PlayerPathData(
                    rooms,
                    ticksInStronghold,
                    difficulty,
                    wastedTime,
                    bestMoveCount,
                    inaccuracyCount,
                    mistakeCount,
                    wormholeCount,
                    roomsReviewed,
                    0
            );

            playerPathData.updateAndPrintAllStats(playerEntity);
        }
    }

    private double getWeightOfCorrectDoor(ArrayList<StructurePiece> solution, StrongholdGenerator.Piece piece, double[] policy) {
        List<StructurePiece> children = strongholdTreeAccessor.getTree().get(piece);

        for (int i = 0; i < children.size(); i++) {
            if (solution.contains(children.get(i))) {
                return policy[i];
            }
        }
        //should never run
        return -1;
    }

    public ArrayList<StrongholdGenerator.Piece> getMistakes() {
        return mistakeRooms;
    }

    public ArrayList<StrongholdGenerator.Piece> getInaccuracies() {
        return inaccurateRooms;
    }
}
