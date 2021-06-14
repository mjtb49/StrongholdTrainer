package io.github.mjtb49.strongholdtrainer.util;

import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;

import java.util.HashMap;
import java.util.Map;

public class RoomFormatter {
    static final Map<Class<? extends StructurePiece>, String> ROOM_TO_VECTOR = new HashMap<Class<? extends StructurePiece>, String>() {{
        put(StrongholdGenerator.Corridor.class, "Corridor");
        put(StrongholdGenerator.PrisonHall.class, "PrisonHall");
        put(StrongholdGenerator.LeftTurn.class, "LeftTurn");
        put(StrongholdGenerator.RightTurn.class, "RightTurn");
        put(StrongholdGenerator.SquareRoom.class, "SquareRoom");
        put(StrongholdGenerator.Stairs.class, "Stairs");
        put(StrongholdGenerator.SpiralStaircase.class, "SpiralStaircase");
        put(StrongholdGenerator.FiveWayCrossing.class, "FiveWayCrossing");
        put(StrongholdGenerator.ChestCorridor.class, "ChestCorridor");
        put(StrongholdGenerator.Library.class, "Library");
        put(StrongholdGenerator.PortalRoom.class, "PortalRoom");
        put(StrongholdGenerator.SmallCorridor.class, "SmallCorridor");
        put(StrongholdGenerator.Start.class, "Start");
        put(null, "None");
    }};

    public static String getStrongholdPieceAsString(Class<? extends StructurePiece> piece) {
        return ROOM_TO_VECTOR.get(piece);
    }
}
