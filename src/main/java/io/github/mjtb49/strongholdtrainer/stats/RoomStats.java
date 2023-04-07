package io.github.mjtb49.strongholdtrainer.stats;

import io.github.mjtb49.strongholdtrainer.util.RoomFormatter;
import net.minecraft.structure.StructurePiece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RoomStats {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static final Map<Class<? extends StructurePiece>, ArrayList<Integer>[][]> roomTimeStats = new HashMap<>();

    private static Path path;

    private static String pieceToString(Class<? extends StructurePiece> pieceType) {
        return RoomFormatter.ROOM_TO_STRING.get(pieceType);
    }

    private static int pieceToNumExits(Class<? extends StructurePiece> pieceType) {
        return RoomFormatter.ROOM_TO_NUM_EXITS.get(pieceType);
    }

    private static boolean isValid(Class<? extends StructurePiece> pieceType) {
        return (!RoomFormatter.ROOM_TO_STRING.get(pieceType).equals("None"));
    }

    @SuppressWarnings("unchecked")
    public static void init(Path path) {
        RoomStats.path = path; 
        for (Class<? extends StructurePiece> pieceType: RoomFormatter.ROOM_TO_STRING.keySet()) {
            int numExits = pieceToNumExits(pieceType);
            // +2 
            ArrayList<Integer>[][] data = new ArrayList[numExits+2][numExits+2];
            for (int entrance = 0; entrance <= numExits + 1; entrance++) {
                for (int exit = 0; exit <= numExits + 1; exit++) {
                    data[entrance][exit] = new ArrayList<>();
                }
            }
            roomTimeStats.put(pieceType, data);
        }
    }

    // ChatGPT wrote this function... I'm impressed
    public static void writeStatsToFile() {
        // Create file to write to
        LocalDateTime timestamp = LocalDateTime.now();
        String filename = timestamp.format(dateFormatter) + ".txt";
        try (FileWriter writer = new FileWriter(String.valueOf(path.resolve(filename)))) {
            // Write to file
            for (Class<? extends StructurePiece> pieceType : roomTimeStats.keySet()) {
                if (!isValid(pieceType)) {
                    continue;
                }
                writer.write(pieceToString(pieceType) + " " + pieceToNumExits(pieceType) + "\n");
                ArrayList<Integer>[][] stats = roomTimeStats.get(pieceType);
                for (int i = 0; i < stats.length; i++) {
                    for (int j = 0; j < stats[i].length; j++) {
                        writer.write(i + " " + j + ": ");
                        StringJoiner sj = new StringJoiner(", ");
                        for (int time : stats[i][j]) {
                            sj.add(Integer.toString(time));
                        }
                        writer.write(sj.toString() + "\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateRoomStats(Class<? extends StructurePiece> pieceType, int timeInTicks, int entrance, int exit) {
        if (isValid(pieceType)) {
            roomTimeStats.get(pieceType)[entrance][exit].add(timeInTicks);
        }
    }
}