package io.github.mjtb49.strongholdtrainer.ml.model;

import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;

import java.util.List;
import java.util.function.BiFunction;

// TODO: Rework how room data are specified (this enum is too big, probably should be its own class w/ static instances), but this works for now.
public enum RoomData {
    CURRENT(((start, piece) -> piece), RoomDataType.STRUCTURE_PIECE_VECTOR),
    PREV((((start, piece) -> {
        return start.getParents().get(piece);
    })), RoomDataType.STRUCTURE_PIECE_VECTOR),
    PREV_EXIT_INDEX(((start, piece) -> {
        List<StructurePiece> parentChildren = start.getTree().get(start.getParents().get(piece));
        try{
            return parentChildren.indexOf(piece);

        } catch (Exception e){
            return 0;

        }
    }), RoomDataType.INT_SCALAR),
    EXIT_1((((start, piece) -> {
        List<StructurePiece> children = start.getTree().get(piece);
        try{
            return children.get(0);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException){
            return null;
        }
    })), RoomDataType.STRUCTURE_PIECE_VECTOR),
    EXIT_2((((start, piece) -> {
        List<StructurePiece> children = start.getTree().get(piece);
        try{
            return children.get(1);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException){
            return null;
        }
    })), RoomDataType.STRUCTURE_PIECE_VECTOR),
    EXIT_3(((start, piece) -> {
        List<StructurePiece> children = start.getTree().get(piece);
        try{
            return children.get(2);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException){
            return null;
        }
    }), RoomDataType.STRUCTURE_PIECE_VECTOR),
    EXIT_4(((start, piece) -> {
        List<StructurePiece> children = start.getTree().get(piece);
        try{
            return children.get(3);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException){
            return null;
        }
    }), RoomDataType.STRUCTURE_PIECE_VECTOR),
    EXIT_5(((start, piece) -> {
        List<StructurePiece> children = start.getTree().get(piece);
        try{
            return children.get(4);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException){
            return null;
        }
    }), RoomDataType.STRUCTURE_PIECE_VECTOR),
    EXIT_BACK(((start, piece) -> {
        return start.getParents().get(piece);
    }), RoomDataType.STRUCTURE_PIECE_VECTOR),
    PORTAL_EXIT_INDEX(((start, piece) -> {
        List<StructurePiece> children = start.getTree().get(piece);
        for(StructurePiece child : children){
            if(child instanceof StrongholdGenerator.PortalRoom){
                return children.indexOf(child) + 1; // 0 -> no portal exit :(
            }
        }
        return 0;
    }), RoomDataType.INT_SCALAR),
    DIRECTION(((start, piece) -> {
        return piece.getFacing();
    }), RoomDataType.DIRECTION_VECTOR),
    DEPTH(((start, piece) -> {
        return piece.getLength();
    }), RoomDataType.INT_SCALAR);

    public enum RoomDataType{
        INT_SCALAR,
        DIRECTION_VECTOR,
        STRUCTURE_PIECE_VECTOR
    }

    public final BiFunction<StrongholdTreeAccessor, StrongholdGenerator.Piece, ?> roomDataFunction;
    public final RoomDataType roomDataType;
    RoomData(BiFunction<StrongholdTreeAccessor, StrongholdGenerator.Piece, ?> biFunction, RoomDataType roomDataType){
        this.roomDataFunction = biFunction;
        this.roomDataType = roomDataType;
    }
}
