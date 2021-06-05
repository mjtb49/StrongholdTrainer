package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.MixinStrongholdGeneratorStartAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(StrongholdGenerator.class)
public class MixinStrongholdGenerator {

    @Inject(method = "method_14854", at = @At("RETURN"))
    private static void method_14854(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, int k, @Nullable Direction direction, int l, CallbackInfoReturnable<StructurePiece> cir) {
        StructurePiece retVal = cir.getReturnValue();
        ((MixinStrongholdGeneratorStartAccessor) start).addPiece(retVal);
    }
}
