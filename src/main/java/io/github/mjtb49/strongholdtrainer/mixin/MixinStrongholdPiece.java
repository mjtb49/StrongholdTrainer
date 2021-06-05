package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.EntranceAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(StrongholdGenerator.Piece.class)
public abstract class MixinStrongholdPiece extends StructurePiece implements EntranceAccessor {
    @Unique
    private final List<BlockBox> entrances = new ArrayList<>();

    protected MixinStrongholdPiece(StructurePieceType type, int length) {
        super(type, length);
    }

    @Inject(method = "method_14874", at = @At("HEAD"))
    private void retainForwards(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, CallbackInfoReturnable<StructurePiece> cir) {
        Direction direction = this.getFacing();
        if (direction != null) {
            switch(direction) {
                case NORTH:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.minX + i, this.boundingBox.minY + j, this.boundingBox.minZ - 1),
                            new BlockPos(this.boundingBox.minX + i + 3, this.boundingBox.minY + j + 3, this.boundingBox.minZ - 1 - 1)
                    ));
                    break;
                case SOUTH:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.minX + i, this.boundingBox.minY + j, this.boundingBox.maxZ + 1),
                            new BlockPos(this.boundingBox.minX + i + 3, this.boundingBox.minY + j + 3, this.boundingBox.maxZ + 1 + 1)
                    ));
                    break;
                case WEST:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.minX - 1, this.boundingBox.minY + j, this.boundingBox.minZ + i),
                            new BlockPos(this.boundingBox.minX - 1 - 1, this.boundingBox.minY + j + 3, this.boundingBox.minZ + i + 3)
                    ));
                    break;
                case EAST:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.maxX + 1, this.boundingBox.minY + j, this.boundingBox.minZ + i),
                            new BlockPos(this.boundingBox.maxX + 1 - 1, this.boundingBox.minY + j + 3, this.boundingBox.minZ + i + 3)
                    ));
                    break;
            }
        }
    }

    @Inject(method = "method_14870", at = @At("HEAD"))
    private void retainLeft(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, CallbackInfoReturnable<StructurePiece> cir) {
        Direction direction = this.getFacing();
        if (direction != null) {
            switch(direction) {
                case NORTH:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.minX - 1, this.boundingBox.minY + i, this.boundingBox.minZ + j),
                            new BlockPos(this.boundingBox.minX - 1 - 1, this.boundingBox.minY + i + 3, this.boundingBox.minZ + j + 3)
                    ));
                    break;
                case SOUTH:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.minX - 1, this.boundingBox.minY + i, this.boundingBox.minZ + j),
                            new BlockPos(this.boundingBox.minX - 1 - 1, this.boundingBox.minY + i + 3, this.boundingBox.minZ + j + 3)
                    ));
                    break;
                case WEST:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.minZ - 1),
                            new BlockPos(this.boundingBox.minX + j + 3, this.boundingBox.minY + i + 3, this.boundingBox.minZ - 1 - 1)
                    ));
                    break;
                case EAST:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.minZ - 1),
                            new BlockPos(this.boundingBox.minX + j + 3, this.boundingBox.minY + i + 3, this.boundingBox.minZ - 1 - 1)
                    ));
                    break;
            }
        }
    }

    @Inject(method = "method_14873", at = @At("HEAD"))
    private void retainRight(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, CallbackInfoReturnable<StructurePiece> cir) {
        Direction direction = this.getFacing();
        if (direction != null) {
            switch(direction) {
                case NORTH:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.maxX + 1, this.boundingBox.minY + i, this.boundingBox.minZ + j),
                            new BlockPos(this.boundingBox.maxX + 1 + 1, this.boundingBox.minY + i + 3, this.boundingBox.minZ + j + 3)
                    ));
                    break;
                case SOUTH:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.maxX + 1, this.boundingBox.minY + i, this.boundingBox.minZ + j),
                            new BlockPos(this.boundingBox.maxX + 1 + 1, this.boundingBox.minY + i + 3, this.boundingBox.minZ + j + 3)
                    ));
                    break;
                case WEST:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.maxZ + 1),
                            new BlockPos(this.boundingBox.minX + j + 3, this.boundingBox.minY + i + 3, this.boundingBox.maxZ + 1 + 1)
                    ));
                    break;
                case EAST:
                    this.entrances.add(new BlockBox(
                            new BlockPos(this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.maxZ + 1),
                            new BlockPos(this.boundingBox.minX + j + 3, this.boundingBox.minY + i + 3, this.boundingBox.maxZ + 1 + 1)
                    ));
                    break;
            }
        }
    }

    @Override
    public List<BlockBox> getEntrances() {
        return this.entrances;
    }
}
