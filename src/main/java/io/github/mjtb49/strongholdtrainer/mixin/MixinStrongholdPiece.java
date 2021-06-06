package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.EntranceAccessor;
import io.github.mjtb49.strongholdtrainer.util.EntranceHelper;
import io.github.mjtb49.strongholdtrainer.util.EntryNode;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(StrongholdGenerator.Piece.class)
public abstract class MixinStrongholdPiece extends StructurePiece implements EntranceAccessor {
    @Unique
    private final List<EntryNode> entrances = new ArrayList<>();

    protected MixinStrongholdPiece(StructurePieceType type, int length) {
        super(type, length);
    }

    // ***** FORWARDS
    // Add partial node

    @Inject(method = "method_14874", at = @At("HEAD"))
    private void retainForwards(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, CallbackInfoReturnable<StructurePiece> cir) {
        Direction direction = this.getFacing();
        if (direction != null) {
            EntranceHelper.addForwards(direction, this.entrances, this.boundingBox, i, j, EntryNode.Type.FORWARDS);
        }
    }

    @Inject(method = "method_14870", at = @At("HEAD"))
    private void retainLeft(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, CallbackInfoReturnable<StructurePiece> cir) {
        Direction direction = this.getFacing();
        if (direction != null) {
            EntranceHelper.addLeft(direction, this.entrances, this.boundingBox, i, j, EntryNode.Type.FORWARDS);
        }
    }

    @Inject(method = "method_14873", at = @At("HEAD"))
    private void retainRight(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, CallbackInfoReturnable<StructurePiece> cir) {
        Direction direction = this.getFacing();
        if (direction != null) {
            EntranceHelper.addRight(direction, this.entrances, this.boundingBox, i, j, EntryNode.Type.FORWARDS);
        }
    }

    // ***** BACKWARDS
    // Adds backwards node and calculates node pointers

    @Inject(method = "method_14874", at = @At("RETURN"))
    private void retainForwardsBack(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, CallbackInfoReturnable<StructurePiece> cir) {
        Direction direction = this.getFacing();
        if (direction != null && cir.getReturnValue() != null) {
            ArrayList<EntryNode> temp = new ArrayList<>();

            EntranceHelper.addForwards(direction, temp, this.boundingBox, i, j, EntryNode.Type.BACKWARDS);
            EntryNode node = temp.get(0);

            for (EntryNode entrance : this.getEntrances()) {
                if (entrance.startBlock.equals(node.startBlock)) {
                    entrance.pointer = cir.getReturnValue();
                }
            }

            ((EntranceAccessor)cir.getReturnValue()).getEntrances().addAll(temp);
        }
    }

    @Inject(method = "method_14870", at = @At("RETURN"))
    private void retainLeftBack(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, CallbackInfoReturnable<StructurePiece> cir) {
        Direction direction = this.getFacing();
        if (direction != null && cir.getReturnValue() != null) {
            ArrayList<EntryNode> temp = new ArrayList<>();

            EntranceHelper.addLeft(direction, temp, this.boundingBox, i, j, EntryNode.Type.BACKWARDS);
            EntryNode node = temp.get(0);

            for (EntryNode entrance : this.getEntrances()) {
                if (entrance.startBlock.equals(node.startBlock)) {
                    entrance.pointer = cir.getReturnValue();
                }
            }

            ((EntranceAccessor)cir.getReturnValue()).getEntrances().addAll(temp);
        }
    }

    @Inject(method = "method_14873", at = @At("RETURN"))
    private void retainRightBack(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, CallbackInfoReturnable<StructurePiece> cir) {
        Direction direction = this.getFacing();
        if (direction != null && cir.getReturnValue() != null) {
            ArrayList<EntryNode> temp = new ArrayList<>();

            EntranceHelper.addRight(direction, temp, this.boundingBox, i, j, EntryNode.Type.BACKWARDS);
            EntryNode node = temp.get(0);

            for (EntryNode entrance : this.getEntrances()) {
                if (entrance.startBlock.equals(node.startBlock)) {
                    entrance.pointer = cir.getReturnValue();
                }
            }

            ((EntranceAccessor)cir.getReturnValue()).getEntrances().addAll(temp);
        }
    }

    @Override
    public List<EntryNode> getEntrances() {
        return this.entrances;
    }
}
