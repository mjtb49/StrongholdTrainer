package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ConfiguredCarver.class)
public class MixinConfiguredCarver {
    @Inject(method = "shouldCarve(Ljava/util/Random;II)Z", at = @At("TAIL"), cancellable = true)
    public void shouldCarve(Random random, int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && StrongholdTrainer.getBoolean("allowScuffed"));
    }
}
