package io.github.mjtb49.strongholdtrainer.mixin;

import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldSavePath.class)
public interface WorldSavePathAccessor {
    @Invoker(value = "<init>")
    static WorldSavePath createWorldSavePath(String relativePath) {
        throw new UnsupportedOperationException();
    }
}