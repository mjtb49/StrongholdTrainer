package io.github.mjtb49.strongholdtrainer.stats;

import net.minecraft.structure.StrongholdGenerator;

public class PlayerPathEntry {
    public StrongholdGenerator.Piece piece;
    public int ticks;
    public int entrance;
    public int exit;

    public PlayerPathEntry(StrongholdGenerator.Piece currentPiece, int i, int entrance2, int exit2) {
        piece = currentPiece;
        ticks = i;
        entrance = entrance2;
        exit = exit2;
    }
}
