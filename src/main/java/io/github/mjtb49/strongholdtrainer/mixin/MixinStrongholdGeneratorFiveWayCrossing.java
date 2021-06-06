package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.MixinStrongholdGeneratorStartAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Random;

@Mixin(StrongholdGenerator.FiveWayCrossing.class)
public abstract class MixinStrongholdGeneratorFiveWayCrossing extends StrongholdGenerator.Piece {
    @Shadow @Final private boolean lowerLeftExists;

    @Shadow @Final private boolean upperLeftExists;

    @Shadow @Final private boolean lowerRightExists;

    @Shadow @Final private boolean upperRightExists;

    protected MixinStrongholdGeneratorFiveWayCrossing(StructurePieceType structurePieceType, int i) {
        super(structurePieceType, i);
    }

    /**
     * @author mjtb49
     */
    @Overwrite
    public void placeJigsaw(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
        int i = 3;
        int j = 5;
        Direction direction = this.getFacing();
        if (direction == Direction.WEST || direction == Direction.NORTH) {
            i = 8 - i;
            j = 8 - j;
        }

        this.method_14874((StrongholdGenerator.Start)structurePiece, list, random, 5, 1);
        if (this.lowerLeftExists) {
            this.method_14870((StrongholdGenerator.Start)structurePiece, list, random, i, 1);
        } else {
            ((MixinStrongholdGeneratorStartAccessor) structurePiece).addPiece(null);
        }

        if (this.upperLeftExists) {
            this.method_14870((StrongholdGenerator.Start)structurePiece, list, random, j, 7);
        } else {
            ((MixinStrongholdGeneratorStartAccessor) structurePiece).addPiece(null);
        }

        if (this.lowerRightExists) {
            this.method_14873((StrongholdGenerator.Start)structurePiece, list, random, i, 1);
        } else {
            ((MixinStrongholdGeneratorStartAccessor) structurePiece).addPiece(null);
        }

        if (this.upperRightExists) {
            this.method_14873((StrongholdGenerator.Start)structurePiece, list, random, j, 7);
        } else {
            ((MixinStrongholdGeneratorStartAccessor) structurePiece).addPiece(null);
        }

        ((MixinStrongholdGeneratorStartAccessor) structurePiece).correctOrder5Way();
    }
}
