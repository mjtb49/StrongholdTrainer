package io.github.mjtb49.strongholdtrainer.stats;

import io.github.mjtb49.strongholdtrainer.util.RoomFormatter;
import net.minecraft.structure.StructurePiece;

import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RoomStats {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static final ArrayList<ArrayList<PlayerPathEntry>> roomTimeStats = new ArrayList<>();

    private static Path path;

    private static String pieceToString(StructurePiece piece) {
        return RoomFormatter.ROOM_TO_STRING.get(piece.getClass());
    }

    private static String doorToString(StructurePiece piece, int door) {
        if (door > RoomFormatter.ROOM_TO_NUM_EXITS.get(piece.getClass())) {
            return "worm";
        }
        return Integer.toString(door);
    }

    public static void init(Path path) {
        RoomStats.path = path;
    }

    // ChatGPT wrote this function... I'm impressed
    public static void writeStatsToFile() {
        // Create file to write to
        LocalDateTime timestamp = LocalDateTime.now();
        String filename = timestamp.format(dateFormatter) + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(String.valueOf(path.resolve(filename))))) {
            // Write to file
            for (ArrayList<PlayerPathEntry> path : roomTimeStats) {
                writer.write(path.size() + "\n");
                for (PlayerPathEntry entry : path) {
                    writer.write(pieceToString(entry.piece) + " " + doorToString(entry.piece, entry.entrance) + " " + doorToString(entry.piece, entry.exit) + " " + entry.ticks + "\n");
                }
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateRoomStats(ArrayList<PlayerPathEntry> path) {
        roomTimeStats.add(path);
    }
}