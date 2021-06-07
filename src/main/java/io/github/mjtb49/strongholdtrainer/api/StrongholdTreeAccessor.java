package io.github.mjtb49.strongholdtrainer.api;

import net.minecraft.structure.StructurePiece;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface StrongholdTreeAccessor {
    void addPiece(StructurePiece piece);
    void registerPiece(StructurePiece piece);
    ArrayList<StructurePiece> getPieces();
    Map<StructurePiece, List<StructurePiece>> getTree();
    Map<StructurePiece, StructurePiece> getParents();
    void printContents();
    void correctOrder5Way();
    void correctOrderSquareAndCorridor();
}
