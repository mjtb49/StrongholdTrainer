package io.github.mjtb49.strongholdtrainer.util;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public final class EntranceHelper {
    public static void addForwards(Direction direction, List<EntryNode> entrances, BlockBox boundingBox, int i, int j, EntryNode.Type type) {
        switch(direction) {
            case NORTH:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.minX + i, boundingBox.minY + j, boundingBox.minZ - 1),
                        new BlockPos(boundingBox.minX + i + 3, boundingBox.minY + j + 3, boundingBox.minZ - 1 + 1)
                ), type));
                break;
            case SOUTH:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.minX + i, boundingBox.minY + j, boundingBox.maxZ + 1),
                        new BlockPos(boundingBox.minX + i + 3, boundingBox.minY + j + 3, boundingBox.maxZ + 1 + 1)
                ), type));
                break;
            case WEST:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.minX - 1, boundingBox.minY + j, boundingBox.minZ + i),
                        new BlockPos(boundingBox.minX - 1 + 1, boundingBox.minY + j + 3, boundingBox.minZ + i + 3)
                ), type));
                break;
            case EAST:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.maxX + 1, boundingBox.minY + j, boundingBox.minZ + i),
                        new BlockPos(boundingBox.maxX + 1 + 1, boundingBox.minY + j + 3, boundingBox.minZ + i + 3)
                ), type));
                break;
        }
    }

    public static void addLeft(Direction direction, List<EntryNode> entrances, BlockBox boundingBox, int i, int j, EntryNode.Type type) {
        switch(direction) {
            case NORTH:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.minX - 1, boundingBox.minY + i, boundingBox.minZ + j),
                        new BlockPos(boundingBox.minX - 1 + 1, boundingBox.minY + i + 3, boundingBox.minZ + j + 3)
                ), type));
                break;
            case SOUTH:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.minX - 1, boundingBox.minY + i, boundingBox.minZ + j),
                        new BlockPos(boundingBox.minX - 1 + 1, boundingBox.minY + i + 3, boundingBox.minZ + j + 3)
                ), type));
                break;
            case WEST:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.minX + j, boundingBox.minY + i, boundingBox.minZ - 1),
                        new BlockPos(boundingBox.minX + j + 3, boundingBox.minY + i + 3, boundingBox.minZ - 1 - 1)
                ), type));
                break;
            case EAST:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.minX + j, boundingBox.minY + i, boundingBox.minZ - 1),
                        new BlockPos(boundingBox.minX + j + 3, boundingBox.minY + i + 3, boundingBox.minZ - 1 + 1)
                ), type));
                break;
        }
    }

    public static void addRight(Direction direction, List<EntryNode> entrances, BlockBox boundingBox, int i, int j, EntryNode.Type type) {
        switch(direction) {
            case NORTH:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.maxX + 1, boundingBox.minY + i, boundingBox.minZ + j),
                        new BlockPos(boundingBox.maxX + 1 + 1, boundingBox.minY + i + 3, boundingBox.minZ + j + 3)
                ), type));
                break;
            case SOUTH:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.maxX + 1, boundingBox.minY + i, boundingBox.minZ + j),
                        new BlockPos(boundingBox.maxX + 1 + 1, boundingBox.minY + i + 3, boundingBox.minZ + j + 3)
                ), type));
                break;
            case WEST:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.minX + j, boundingBox.minY + i, boundingBox.maxZ + 1),
                        new BlockPos(boundingBox.minX + j + 3, boundingBox.minY + i + 3, boundingBox.maxZ + 1 - 1)
                ), type));
                break;
            case EAST:
                entrances.add(new EntryNode(new BlockBox(
                        new BlockPos(boundingBox.minX + j, boundingBox.minY + i, boundingBox.maxZ + 1),
                        new BlockPos(boundingBox.minX + j + 3, boundingBox.minY + i + 3, boundingBox.maxZ + 1 + 1)
                ), type));
                break;
        }
    }
}
