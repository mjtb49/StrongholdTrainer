package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Random;

@Mixin(StrongholdGenerator.Corridor.class)
public abstract class MixinStrongholdGeneratorCorridor extends StrongholdGenerator.Piece {

    @Shadow @Final private boolean leftExitExists;

    @Shadow @Final private boolean rightExitExists;

    protected MixinStrongholdGeneratorCorridor(StructurePieceType structurePieceType, int i) {
        super(structurePieceType, i);
    }

    /**
     * @author mjtb49
     */
    @Overwrite
    public void placeJigsaw(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
        this.method_14874((StrongholdGenerator.Start)structurePiece, list, random, 1, 1);

        if (this.leftExitExists) {
            this.method_14870((StrongholdGenerator.Start)structurePiece, list, random, 1, 2);
        } else {
            ((StrongholdTreeAccessor) structurePiece).addPiece(null);
        }

        if (this.rightExitExists) {
            this.method_14873((StrongholdGenerator.Start)structurePiece, list, random, 1, 2);
        } else {
            ((StrongholdTreeAccessor) structurePiece).addPiece(null);
        }

        ((StrongholdTreeAccessor) structurePiece).correctOrderSquareAndCorridor();
    }

}
