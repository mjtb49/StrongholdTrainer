package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.MineshaftFeature;
import net.minecraft.world.gen.feature.MineshaftFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MineshaftFeature.class)
public class MixinMineshaftFeature {
    @Inject(method = "shouldStartAt(Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/world/biome/source/BiomeSource;JLnet/minecraft/world/gen/ChunkRandom;IILnet/minecraft/world/biome/Biome;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/gen/feature/MineshaftFeatureConfig;)Z", at = @At("TAIL"), cancellable = true)
    protected void shouldStartAt(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long l, ChunkRandom chunkRandom, int i, int j, Biome biome, ChunkPos chunkPos, MineshaftFeatureConfig mineshaftFeatureConfig, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && OptionTracker.getBoolOption(OptionTracker.Option.ALLOW_SCUFFED));
    }
}
