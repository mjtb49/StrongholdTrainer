package io.github.mjtb49.strongholdtrainer.stats;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PlayerPathData {
    private static final DecimalFormat DF = new DecimalFormat("0.00");
    private final ArrayList<Pair<StrongholdGenerator.Piece, Integer>> rooms;
    private final double difficulty;
    private final int wastedTime;
    private final int bestMoveCount;
    private final int inaccuracyCount;
    private final int mistakeCount;
    private final int wormholeCount;
    private final int roomsReviewed;

    public PlayerPathData(ArrayList<Pair<StrongholdGenerator.Piece, Integer>> rooms,
                          double difficulty,
                          int wastedTime,
                          int bestMoveCount,
                          int inaccuracyCount,
                          int mistakeCount,
                          int wormholeCount,
                          int roomsReviewed) {
        this.rooms = rooms;
        this.difficulty = difficulty;
        this.wastedTime = wastedTime;
        this.bestMoveCount = bestMoveCount;
        this.inaccuracyCount = inaccuracyCount;
        this.mistakeCount = mistakeCount;
        this.wormholeCount = wormholeCount;
        this.roomsReviewed = roomsReviewed;
    }

    public void updateAllStats(ServerPlayerEntity playerEntity) {
        updateRoomStats(playerEntity);
        playerEntity.increaseStat(StrongholdTrainerStats.NUM_REVIEWED_ROOMS, roomsReviewed);
        playerEntity.increaseStat(StrongholdTrainerStats.NUM_BEST_ROOMS, bestMoveCount);
        playerEntity.increaseStat(StrongholdTrainerStats.NUM_INACCURACIES, inaccuracyCount);
        playerEntity.increaseStat(StrongholdTrainerStats.NUM_MISTAKES, mistakeCount);

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
}
