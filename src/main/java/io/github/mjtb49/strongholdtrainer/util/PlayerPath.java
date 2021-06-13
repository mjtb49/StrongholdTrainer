package io.github.mjtb49.strongholdtrainer.util;

import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdRoomClassifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.text.DecimalFormat;
import java.util.*;

public class PlayerPath {

    private static final int INACCURACY_THRESHOLD = 20 * 5;
    private static final int MISTAKE_THRESHOLD = 20 * 10;
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private final StartAccessor startAccessor;
    private final StrongholdTreeAccessor strongholdTreeAccessor;
    private final ArrayList<Pair<StrongholdGenerator.Piece, Integer>> rooms;
    StrongholdGenerator.PortalRoom portalRoom;

    private final ArrayList<StrongholdGenerator.Piece> inaccurateRooms;
    private final ArrayList<StrongholdGenerator.Piece> mistakeRooms;

    public PlayerPath(StructureStart<?> start) {
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
            difficulty *= getWeightOfCorrectDoor(solution,(StrongholdGenerator.Piece) solution.get(i), StrongholdRoomClassifier.getPredictions(startAccessor.getStart(), (StrongholdGenerator.Piece) solution.get(i)));
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

    public void review() {
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
                    double[] policy = StrongholdRoomClassifier.getPredictions(startAccessor.getStart(), currentGoodPiece);

                    double maximumWeight = -1;
                    for (double d : policy)
                        maximumWeight = Math.max(d, maximumWeight);

                    StrongholdGenerator.Piece nextPiece = rooms.get(indexOfGoodRoom + 1).getLeft();
                    //did the player choose a child?
                    if (strongholdTreeAccessor.getTree().get(currentGoodPiece).contains(nextPiece)) {
                        if (!solution.contains(nextPiece)) {
                            double chosenWeight = policy[strongholdTreeAccessor.getTree().get(currentGoodPiece).indexOf(nextPiece)];
                            int j = indexOfGoodRoom;
                            while (!solution.contains(nextPiece)) {
                                j++;
                                nextPiece = rooms.get(j + 1).getLeft();
                                currentWastedTime += rooms.get(j + 1).getRight();
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
                        } else bestMoveCount++;
                    }
                }
                roomsReviewed++;
                indexOfGoodRoom++;
            }

            MinecraftClient.getInstance().player.sendMessage(new LiteralText("Wasted Time " + wastedTime / 20.0 + " seconds").formatted(Formatting.YELLOW), false);
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("Bot one shots this 1 in " + DF.format(1/difficulty)).formatted(Formatting.DARK_GREEN), false);
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("Rooms Reviwed " + roomsReviewed).formatted(Formatting.DARK_GREEN), false);
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("Best Moves " + bestMoveCount).formatted(Formatting.GREEN), false);
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("Inaccuracies " + inaccuracyCount).formatted(Formatting.YELLOW), false);
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("Mistakes " + mistakeCount).formatted(Formatting.RED), false);
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("Wormholes " + wormholeCount).formatted(Formatting.BLUE), false);
            printTheTravel();
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

    private void printTheTravel() {
        for (Pair<StrongholdGenerator.Piece, Integer> pair : rooms) {
            System.out.println(pair.getLeft().getClass().getSimpleName() + " " + pair.getRight());
        }
    }

    public ArrayList<StrongholdGenerator.Piece> getMistakes() {
        return mistakeRooms;
    }

    public ArrayList<StrongholdGenerator.Piece> getInaccuracies() {
        return inaccurateRooms;
    }
}
