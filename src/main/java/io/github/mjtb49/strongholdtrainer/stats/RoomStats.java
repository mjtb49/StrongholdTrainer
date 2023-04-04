package io.github.mjtb49.strongholdtrainer.stats;

import io.github.mjtb49.strongholdtrainer.util.RoomFormatter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class RoomStats {
    private static final Map<Class<? extends StructurePiece>, Identifier[][]> roomCountStats = new HashMap<>();
    private static final Map<Class<? extends StructurePiece>, Identifier[][]> roomTimeStats = new HashMap<>();
    private static final Map<Class<? extends StructurePiece>, Identifier[][]> averageTimeStats = new HashMap<>();

    private static boolean isValid(Class<? extends StructurePiece> pieceType) {
        return (!RoomFormatter.ROOM_TO_STRING.get(pieceType).equals("None"));
    }

    public static void register() {
        for (Class<? extends StructurePiece> pieceType : RoomFormatter.ROOM_TO_STRING.keySet()) {
            if (isValid(pieceType)) {
                int num_exits = RoomFormatter.ROOM_TO_NUM_EXITS.get(pieceType);

                // +2 added for entrance and wormholes
                Identifier[][] pieceRoomCountStats = new Identifier[num_exits+2][num_exits+2];
                Identifier[][] pieceRoomTimeStats = new Identifier[num_exits+2][num_exits+2];
                Identifier[][] pieceAverageCountStats = new Identifier[num_exits+2][num_exits+2];
                
                // +1 added for wormholes, <= used for entrance
                for (int entrance = 0; entrance <= num_exits+1; entrance++) {
                    for (int exit = 0; exit <= num_exits+1; exit++) {
                        // if > num_exits, converts to "worm" for clarity
                        String entranceString = doorToString(num_exits, entrance);
                        String exitString = doorToString(num_exits, exit);

                        String countName = "piece_" + RoomFormatter.ROOM_TO_STRING.get(pieceType).toLowerCase().replace(" ", "_") + "_" + entranceString + "_" + exitString + "_count";
                        String timeName = "piece_" + RoomFormatter.ROOM_TO_STRING.get(pieceType).toLowerCase().replace(" ", "_") + "_" + entranceString + "_" + exitString + "_total";
                        String avgName = "piece_" + RoomFormatter.ROOM_TO_STRING.get(pieceType).toLowerCase().replace(" ", "_") + "_" + entranceString + "_" + exitString + "_avg";

                        pieceRoomCountStats[entrance][exit] = new Identifier(StrongholdTrainerStats.MODID, countName);
                        pieceRoomTimeStats[entrance][exit] = new Identifier(StrongholdTrainerStats.MODID, timeName);
                        pieceAverageCountStats[entrance][exit] = new Identifier(StrongholdTrainerStats.MODID, avgName);

                        Registry.register(Registry.CUSTOM_STAT, countName, pieceRoomCountStats[entrance][exit]);
                        Registry.register(Registry.CUSTOM_STAT, timeName, pieceRoomTimeStats[entrance][exit]);
                        Registry.register(Registry.CUSTOM_STAT, avgName, pieceAverageCountStats[entrance][exit]);

                        Stats.CUSTOM.getOrCreateStat(pieceRoomCountStats[entrance][exit], StatFormatter.DEFAULT);
                        Stats.CUSTOM.getOrCreateStat(pieceRoomTimeStats[entrance][exit], StatFormatter.TIME);
                        Stats.CUSTOM.getOrCreateStat(pieceAverageCountStats[entrance][exit], StatFormatter.TIME);
                    }
                }

                roomCountStats.put(pieceType, pieceRoomCountStats);
                roomTimeStats.put(pieceType, pieceRoomTimeStats);
                averageTimeStats.put(pieceType, pieceAverageCountStats);
            }
        }
    }

    private static String doorToString(int num_exits, int door) {
        return door > num_exits ? "worm" : Integer.toString(door);
    }

    public static void updateRoomStats(ServerPlayerEntity playerEntity, Class<? extends StructurePiece> pieceType, int timeInTicks, int entrance, int exit) {
        //because of this increment count should never be 0
        if (isValid(pieceType)) {
            playerEntity.incrementStat(roomCountStats.get(pieceType)[entrance][exit]);
            playerEntity.increaseStat(roomTimeStats.get(pieceType)[entrance][exit], timeInTicks);
            int count = playerEntity.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(roomCountStats.get(pieceType)[entrance][exit]));
            int time = playerEntity.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(roomTimeStats.get(pieceType)[entrance][exit]));
            int avg = time / count;
            playerEntity.resetStat(Stats.CUSTOM.getOrCreateStat(averageTimeStats.get(pieceType)[entrance][exit]));
            playerEntity.increaseStat(averageTimeStats.get(pieceType)[entrance][exit], avg);
        }
    }
}
