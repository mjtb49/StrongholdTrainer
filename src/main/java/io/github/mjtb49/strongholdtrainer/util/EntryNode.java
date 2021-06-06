package io.github.mjtb49.strongholdtrainer.util;

import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public final class EntryNode {
    public final BlockBox box;
    public final Type type;
    public final BlockPos startBlock;
    public StructurePiece pointer = null;

    public EntryNode(BlockBox box, Type type) {
        this.box = box;
        this.type = type;
        this.startBlock = new BlockPos(box.minX, box.minY, box.minZ);
    }

    public enum Type {
        FORWARDS,
        BACKWARDS
    }
}
