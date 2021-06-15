package io.github.mjtb49.strongholdtrainer.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.io.*;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class PlayerPathData {

    private static ArrayList<PlayerPathData> allPlayerPathData = new ArrayList<>();
    private static Path STATS_PATH;

    private static final DecimalFormat DF = new DecimalFormat("0.00");
    private final ArrayList<Pair<StrongholdGenerator.Piece, Integer>> rooms;
    @Expose
    private final int ticksInStronghold;
    @Expose
    private final double difficulty;
    @Expose
    private final int wastedTime;
    @Expose
    private final int bestMoveCount;
    @Expose
    private final int inaccuracyCount;
    @Expose
    private final int mistakeCount;
    @Expose
    private final int wormholeCount;
    @Expose
    private final int roomsReviewed;

    public PlayerPathData(ArrayList<Pair<StrongholdGenerator.Piece, Integer>> rooms,
                          int ticksInStronghold,
                          double difficulty,
                          int wastedTime,
                          int bestMoveCount,
                          int inaccuracyCount,
                          int mistakeCount,
                          int wormholeCount,
                          int roomsReviewed) {
        this.rooms = rooms;
        this.ticksInStronghold = ticksInStronghold;
        this.difficulty = difficulty;
        this.wastedTime = wastedTime;
        this.bestMoveCount = bestMoveCount;
        this.inaccuracyCount = inaccuracyCount;
        this.mistakeCount = mistakeCount;
        this.wormholeCount = wormholeCount;
        this.roomsReviewed = roomsReviewed;
        allPlayerPathData.add(this);
    }

    public void updateAndPrintAllStats(ServerPlayerEntity playerEntity) {

        StrongholdTrainerStats.updateStrongholdTimeStats(playerEntity, ticksInStronghold);
        updateRoomStats(playerEntity);

        playerEntity.increaseStat(StrongholdTrainerStats.NUM_REVIEWED_ROOMS, roomsReviewed);
        playerEntity.increaseStat(StrongholdTrainerStats.NUM_BEST_ROOMS, bestMoveCount);
        playerEntity.increaseStat(StrongholdTrainerStats.NUM_INACCURACIES, inaccuracyCount);
        playerEntity.increaseStat(StrongholdTrainerStats.NUM_MISTAKES, mistakeCount);

        playerEntity.sendMessage(new LiteralText(" "), false);
        playerEntity.sendMessage(new LiteralText("Time of " + ticksInStronghold / 20.0 + " seconds").formatted(Formatting.DARK_GREEN), false);
        playerEntity.sendMessage(new LiteralText("Wasted Time " + wastedTime / 20.0 + " seconds").formatted(Formatting.YELLOW), false);
        playerEntity.sendMessage(new LiteralText("Estimated Difficulty " + DF.format(1/difficulty)).formatted(Formatting.DARK_GREEN), false);
        playerEntity.sendMessage(new LiteralText("Rooms Reviewed " + roomsReviewed).formatted(Formatting.DARK_GREEN), false);
        playerEntity.sendMessage(new LiteralText("Best Moves " + bestMoveCount).formatted(Formatting.GREEN), false);
        playerEntity.sendMessage(new LiteralText("Inaccuracies " + inaccuracyCount).formatted(Formatting.YELLOW), false);
        playerEntity.sendMessage(new LiteralText("Mistakes " + mistakeCount).formatted(Formatting.RED), false);
        playerEntity.sendMessage(new LiteralText("Wormholes " + wormholeCount).formatted(Formatting.BLUE), false);
    }

    private void updateRoomStats(ServerPlayerEntity playerEntity) {
        for (Pair<StrongholdGenerator.Piece, Integer> room : rooms) {
            RoomStats.updateRoomStats(playerEntity, room.getLeft().getClass(), room.getRight());
        }
    }

    private void printTheTravel() {
        for (Pair<StrongholdGenerator.Piece, Integer> pair : rooms) {
            System.out.println(pair.getLeft().getClass().getSimpleName() + " " + pair.getRight());
        }
    }

    public static void loadAllPriorPaths(Path path) {
        STATS_PATH = path.resolve("stats.json");
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        try {
            BufferedReader br = new BufferedReader(new FileReader(String.valueOf(STATS_PATH)));
            allPlayerPathData = gson.fromJson(br, new TypeToken<ArrayList<PlayerPathData>>(){}.getType());
            if (allPlayerPathData == null)
                allPlayerPathData = new ArrayList<>();
            //System.out.println(allPlayerPathData.size());
            br.close();
        } catch (FileNotFoundException fileNotFoundException) {
            System.err.println(fileNotFoundException.getMessage());
            allPlayerPathData = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeAllPaths() {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(String.valueOf(STATS_PATH)));
            gson.toJson(allPlayerPathData, bw);
            bw.flush();
            bw.close();
        } catch (IOException ioException) {
            System.err.println(ioException.getMessage());
        }
    }
}
