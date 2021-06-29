package io.github.mjtb49.strongholdtrainer.path;

import net.minecraft.structure.StrongholdGenerator;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class StrongholdPathEntry {
    private final StrongholdGenerator.Piece currentPiece;
    private final StrongholdGenerator.Piece previousPiece;
    private final AtomicInteger ticksSpentInPiece;
    private double[] policy;

    public StrongholdPathEntry(StrongholdGenerator.Piece c, StrongholdGenerator.Piece p, AtomicInteger ticks) {
        this.currentPiece = c;
        this.previousPiece = p;
        this.ticksSpentInPiece = ticks;
        this.policy = null;
    }

    public AtomicInteger getTicksSpentInPiece() {
        return ticksSpentInPiece;
    }

    public StrongholdGenerator.Piece getCurrentPiece() {
        return currentPiece;
    }

    public StrongholdGenerator.Piece getPreviousPiece() {
        return previousPiece;
    }

    public void updatePolicy(double[] newPolicy) {
        this.policy = newPolicy;
    }

    public double[] getPolicy() {
        return policy;
    }

    public void incrementTicks() {
        this.ticksSpentInPiece.getAndIncrement();
    }

    @Override
    public String toString() {
        return "StrongholdPathEntry{" +
                "currentPiece=" + currentPiece +
                ", previousPiece=" + previousPiece +
                ", ticksSpentInPiece=" + ticksSpentInPiece +
                ", policy=" + Arrays.toString(policy) +
                '}';
    }
}
