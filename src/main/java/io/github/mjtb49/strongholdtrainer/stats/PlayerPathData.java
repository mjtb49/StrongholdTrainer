package io.github.mjtb49.strongholdtrainer.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import io.github.mjtb49.strongholdtrainer.util.RoomFormatter;
import io.github.mjtb49.strongholdtrainer.util.TimerHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PlayerPathData {

    private static ArrayList<PlayerPathData> allPlayerPathData = new ArrayList<>();
    private static Path STATS_PATH;

    private static final DecimalFormat DF = new DecimalFormat("0.00");
    private final ArrayList<Pair<StrongholdGenerator.Piece, Integer>> rooms;
    @Expose
    private final HashMap<String, ArrayList<Integer>> roomsToWriteToFile;
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
    private final int blunderCount;
    @Expose
    private final int wormholeCount;
    @Expose
    private final int roomsReviewed;
    // TODO: Should this be serialized?
    @Expose
    private final int ticksLostAgainstFeinberg;


    public PlayerPathData(ArrayList<Pair<StrongholdGenerator.Piece, Integer>> rooms,
                          int ticksInStronghold,
                          double difficulty,
                          int wastedTime,
                          int bestMoveCount,
                          int inaccuracyCount,
                          int mistakeCount,
                          int blunderCount,
                          int wormholeCount,
                          int roomsReviewed,
                          int tickLossAgainstFeinberg) {
        this.rooms = rooms;
        this.ticksInStronghold = ticksInStronghold;
        this.difficulty = difficulty;
        this.wastedTime = wastedTime;
        this.bestMoveCount = bestMoveCount;
        this.inaccuracyCount = inaccuracyCount;
        this.mistakeCount = mistakeCount;
        this.wormholeCount = wormholeCount;
        this.roomsReviewed = roomsReviewed;
        this.ticksLostAgainstFeinberg = tickLossAgainstFeinberg;
        this.blunderCount = blunderCount;

        roomsToWriteToFile = new HashMap<>();
        for (Pair<StrongholdGenerator.Piece, Integer> pair : rooms) {
            ArrayList<Integer> list = roomsToWriteToFile.getOrDefault(RoomFormatter.ROOM_TO_STRING.get(pair.getLeft().getClass()), new ArrayList<>());
            list.add(pair.getRight());
            roomsToWriteToFile.put(RoomFormatter.ROOM_TO_STRING.get(pair.getLeft().getClass()), list);
        }

        allPlayerPathData.add(this);
    }

    public void updateAndPrintAllStats(ServerPlayerEntity playerEntity, @Nullable String realtime, boolean invalidRun) {
        if(!invalidRun){
            StrongholdTrainerStats.updateStrongholdTimeStats(playerEntity, ticksInStronghold);
            updateRoomStats(playerEntity);

            playerEntity.increaseStat(StrongholdTrainerStats.NUM_REVIEWED_ROOMS, roomsReviewed);
            playerEntity.increaseStat(StrongholdTrainerStats.NUM_BEST_ROOMS, bestMoveCount);
            playerEntity.increaseStat(StrongholdTrainerStats.NUM_INACCURACIES, inaccuracyCount);
            playerEntity.increaseStat(StrongholdTrainerStats.NUM_MISTAKES, mistakeCount);

            playerEntity.resetStat(Stats.CUSTOM.getOrCreateStat(StrongholdTrainerStats.MEDIAN_TIME));
            playerEntity.increaseStat(StrongholdTrainerStats.MEDIAN_TIME, computeMedianTimeTaken());
        }


//        playerEntity.sendMessage(new LiteralText(" "), false);

        playerEntity.sendMessage(new LiteralText("Time of " + TimerHelper.ticksToTime(ticksInStronghold) + " IGT" + (realtime != null ? "/" + realtime + " RT " : " ") + "(" + TimerHelper.ticksToTime(wastedTime) + " wasted)").formatted(Formatting.AQUA, Formatting.BOLD, invalidRun ? Formatting.STRIKETHROUGH : Formatting.BOLD), false);
        playerEntity.sendMessage(new LiteralText("Time Loss/Gain Against Feinberg " + (this.ticksLostAgainstFeinberg > 0 ? "+" : "") + this.ticksLostAgainstFeinberg / 20.0 + "s").formatted(this.ticksLostAgainstFeinberg > 0 ? Formatting.RED : Formatting.GREEN).styled(
                style -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("This is calculated by taking the sum of the time you spent in each room minus Feinberg's average time in that room.")))
        ), false);
        playerEntity.sendMessage(new LiteralText("Estimated Difficulty " + DF.format(1 / difficulty)).formatted(Formatting.DARK_GREEN), false);
        playerEntity.sendMessage(new LiteralText("\u2116 Choices Reviewed " + roomsReviewed + "").formatted(Formatting.DARK_GREEN), false);
        playerEntity.sendMessage(new LiteralText("! Best Moves " + bestMoveCount).formatted(Formatting.GOLD), false);
        playerEntity.sendMessage(new LiteralText("? Inaccuracies " + inaccuracyCount).formatted(Formatting.YELLOW), false);
        playerEntity.sendMessage(new LiteralText("\u2048 Mistakes " + mistakeCount).formatted(Formatting.RED), false);
        playerEntity.sendMessage(new LiteralText("\u2047 Blunders " + blunderCount).formatted(Formatting.DARK_RED), false);
        playerEntity.sendMessage(new LiteralText("\u2194 Wormholes " + wormholeCount).formatted(Formatting.DARK_PURPLE), false);
    }

    private void updateRoomStats(ServerPlayerEntity playerEntity) {
        for (Pair<StrongholdGenerator.Piece, Integer> room : rooms) {
            RoomStats.updateRoomStats(playerEntity, room.getLeft().getClass(), room.getRight());
        }
    }

    private int computeMedianTimeTaken() {
        ArrayList<Integer> times = new ArrayList<>();
        for (PlayerPathData playerPathData : allPlayerPathData) {
            times.add(playerPathData.ticksInStronghold);
        }
        Collections.sort(times);
        int index = (times.size() - 1) / 2;
        int index2 = times.size() / 2;
        return (times.get(index) + times.get(index2)) / 2;
    }

    /**
     * @deprecated in favor of sendSplits() or using getHistory().forEach(System::println) in StrongholdPath
     */
    @Deprecated
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
