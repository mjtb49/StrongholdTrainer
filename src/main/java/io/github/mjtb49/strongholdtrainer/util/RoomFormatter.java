package io.github.mjtb49.strongholdtrainer.util;

import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;

import java.util.HashMap;
import java.util.Map;

public class RoomFormatter {
    public static final Map<Class<? extends StructurePiece>, String> ROOM_TO_STRING = new HashMap<Class<? extends StructurePiece>, String>() {{
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

    public static final Map<Class<? extends StructurePiece>, Integer> ROOM_TO_NUM_EXITS = new HashMap<Class<? extends StructurePiece>, Integer>() {{
        put(StrongholdGenerator.Corridor.class, 3);
        put(StrongholdGenerator.PrisonHall.class, 1);
        put(StrongholdGenerator.LeftTurn.class, 1);
        put(StrongholdGenerator.RightTurn.class, 1);
        put(StrongholdGenerator.SquareRoom.class, 3);
        put(StrongholdGenerator.Stairs.class, 1);
        put(StrongholdGenerator.SpiralStaircase.class, 1);
        put(StrongholdGenerator.FiveWayCrossing.class, 5);
        put(StrongholdGenerator.ChestCorridor.class, 1);
        put(StrongholdGenerator.Library.class, 0);
        put(StrongholdGenerator.PortalRoom.class, 0);
        put(StrongholdGenerator.SmallCorridor.class, 0);
        put(StrongholdGenerator.Start.class, 1);
        put(null, 0);
    }};

    public static String getStrongholdPieceAsString(Class<? extends StructurePiece> piece) {
        return ROOM_TO_STRING.get(piece);
    }


}
