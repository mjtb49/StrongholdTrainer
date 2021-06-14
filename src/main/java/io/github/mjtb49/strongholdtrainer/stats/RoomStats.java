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
    private static final Map<Class<? extends StructurePiece>, Identifier> roomCountStats = new HashMap<>();
    private static final Map<Class<? extends StructurePiece>, Identifier> roomTimeStats = new HashMap<>();

    public static void register() {
        for (Class<? extends StructurePiece> pieceType : RoomFormatter.ROOM_TO_STRING.keySet()) {
            String countName = "piece_" + RoomFormatter.ROOM_TO_STRING.get(pieceType).toLowerCase().replace(" ","_") + "_count";
            String timeName = "piece_" + RoomFormatter.ROOM_TO_STRING.get(pieceType).toLowerCase().replace(" ","_") + "_time";

            roomCountStats.put(pieceType, new Identifier(StrongholdTrainerStats.MODID, countName));
            roomTimeStats.put(pieceType, new Identifier(StrongholdTrainerStats.MODID, timeName));

            Registry.register(Registry.CUSTOM_STAT, countName, roomCountStats.get(pieceType));
            Registry.register(Registry.CUSTOM_STAT, timeName, roomTimeStats.get(pieceType));

            Stats.CUSTOM.getOrCreateStat(roomCountStats.get(pieceType), StatFormatter.DEFAULT);
            Stats.CUSTOM.getOrCreateStat(roomTimeStats.get(pieceType), StatFormatter.TIME);
        }
    }

    public static void updateRoomStats(ServerPlayerEntity playerEntity, Class<? extends StructurePiece> pieceType, int timeInTicks) {
        playerEntity.incrementStat(roomCountStats.get(pieceType));
        playerEntity.increaseStat(roomTimeStats.get(pieceType), timeInTicks);
    }

}
