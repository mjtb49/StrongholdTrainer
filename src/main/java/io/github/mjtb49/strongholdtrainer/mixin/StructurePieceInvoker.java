package io.github.mjtb49.strongholdtrainer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructurePiece.class)
public interface StructurePieceInvoker {
    @Invoker
    void invokeAddBlock(WorldAccess world, BlockState block, int x, int y, int z, BlockBox blockBox);
}
