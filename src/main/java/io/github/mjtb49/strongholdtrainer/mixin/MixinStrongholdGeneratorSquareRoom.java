package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.MixinStrongholdGeneratorStartAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

@Mixin(StrongholdGenerator.SquareRoom.class)
public class MixinStrongholdGeneratorSquareRoom {

    @Inject(method = "placeJigsaw", at = @At("TAIL"))
    public void placeJigsaw(StructurePiece structurePiece, List<StructurePiece> list, Random random, CallbackInfo ci) {
        ((MixinStrongholdGeneratorStartAccessor) structurePiece).correctOrderSquareAndCorridor();
    }
}
