package io.github.mjtb49.strongholdtrainer.ml;

import net.minecraft.structure.StrongholdGenerator;

public class StrongholdPathEntry {
    private final StrongholdGenerator.Piece currentPiece;
    private final StrongholdGenerator.Piece previousPiece;
    private final Integer ticksSpentInPiece;

    public StrongholdPathEntry(StrongholdGenerator.Piece c, StrongholdGenerator.Piece p, Integer ticks){
        this.currentPiece = c;
        this.previousPiece = p;
        this.ticksSpentInPiece = ticks;
    }

    public Integer getTicksSpentInPiece() {
        return ticksSpentInPiece;
    }

    public StrongholdGenerator.Piece getCurrentPiece() {
        return currentPiece;
    }

    public StrongholdGenerator.Piece getPreviousPiece() {
        return previousPiece;
    }

    @Override
    public String toString() {
        return "StrongholdPathEntry{" +
                "currentPiece=" + currentPiece +
                ", previousPiece=" + previousPiece +
                ", ticksSpentInPiece=" + ticksSpentInPiece +
                '}';
    }
}
