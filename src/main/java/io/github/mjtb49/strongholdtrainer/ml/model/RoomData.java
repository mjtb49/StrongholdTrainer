package io.github.mjtb49.strongholdtrainer.ml.model;

import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import io.github.mjtb49.strongholdtrainer.util.TriFunction;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;

import java.util.List;

public enum RoomData {
    CURRENT((start, current, previous) -> current, RoomDataType.STRUCTURE_PIECE_VECTOR),
    PREVIOUS_ROOM((start, current, previous) -> previous, RoomDataType.STRUCTURE_PIECE_VECTOR),
    PARENT_ROOM((start, current, previous) -> {
        return start.getParents().get(current);
    }, RoomDataType.STRUCTURE_PIECE_VECTOR),
    PREV_EXIT_INDEX((start, current, previous) -> {
        List<StructurePiece> pieces = start.getTree().get(start.getParents().get(current));
        try {
            return Integer.valueOf(pieces.indexOf(current));
        } catch (NullPointerException e){
            return Integer.valueOf(0);
        }
    }, RoomDataType.INT_SCALAR),
    PREV_EXIT_INDEX_COMPAT((start, current, previous) -> {
        List<StructurePiece> pieces = start.getTree().get(start.getParents().get(current));
        try {
            return Integer.valueOf(pieces.indexOf(current) + 1);
        } catch (NullPointerException e){
            return Integer.valueOf(0);
        }
    }, RoomDataType.INDEX_VECTOR),
    EXIT_1((start, current, previous) -> {
        List<StructurePiece> children = start.getTree().get(current);
        try{
            return children.get(0);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException){
            return null;
        }
    }, RoomDataType.STRUCTURE_PIECE_VECTOR),
    EXIT_2((start, current, previous) -> {
        List<StructurePiece> children = start.getTree().get(current);
        try{
            return children.get(1);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException){
            return null;
        }
    }, RoomDataType.STRUCTURE_PIECE_VECTOR),
    EXIT_3((start, current, previous) -> {
        List<StructurePiece> children = start.getTree().get(current);
        try{
            return children.get(2);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException){
            return null;
        }
    }, RoomDataType.STRUCTURE_PIECE_VECTOR),
    EXIT_4((start, current, previous) -> {
        List<StructurePiece> children = start.getTree().get(current);
        try{
            return children.get(3);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException){
            return null;
        }
    }, RoomDataType.STRUCTURE_PIECE_VECTOR),
    EXIT_5((start, current, previous) -> {
        List<StructurePiece> children = start.getTree().get(current);
        try{
            return children.get(4);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException){
            return null;
        }
    }, RoomDataType.STRUCTURE_PIECE_VECTOR),
    DIRECTION((start, current, previous) -> {
        return current.getFacing();
    }, RoomDataType.DIRECTION_VECTOR),
    DEPTH((start, current, previous) -> {
        return Integer.valueOf(current.getLength());
    }, RoomDataType.INT_SCALAR),
    CONSTANT((start,current,previous) -> 0, RoomDataType.INT_SCALAR),
    DOWNWARDS((start,current,previous) -> {
        try{
            if(current.getLength() > previous.getLength()){
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e){
            return 0;
        }

    }, RoomDataType.INT_SCALAR),
    ENTRY((start, current,previous) -> {
        try{
            if(current.getLength() > previous.getLength()){
                return 0;
            } else {
                return start.getTree().get(current).indexOf(previous) + 1;
            }
        } catch (Exception e){
            return 0;
        }
    }, RoomDataType.INDEX_VECTOR);

    public enum RoomDataType{
        INT_SCALAR,
        DIRECTION_VECTOR,
        STRUCTURE_PIECE_VECTOR,
        INDEX_VECTOR
    }

    public final TriFunction<StrongholdTreeAccessor, StrongholdGenerator.Piece, StrongholdGenerator.Piece,?> roomDataFunction;
    public final RoomDataType roomDataType;
    RoomData(TriFunction<StrongholdTreeAccessor, StrongholdGenerator.Piece, StrongholdGenerator.Piece, ?> biFunction, RoomDataType roomDataType){
        this.roomDataFunction = biFunction;
        this.roomDataType = roomDataType;
    }

    public RoomDataType getRoomDataType() {
        return roomDataType;
    }

}
