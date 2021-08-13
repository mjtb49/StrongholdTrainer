package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(StrongholdGenerator.PortalRoom.class)

public class MixinPortalRoom {
    @Inject(at = @At("TAIL"), method = "generate(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Ljava/util/Random;Lnet/minecraft/util/math/BlockBox;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/util/math/BlockPos;)Z")
    public void generate(ServerWorldAccess serverWorldAccess, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockState8 = Blocks.END_PORTAL.getDefaultState();
        BlockState blockState4 = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.NORTH);
        BlockState blockState5 = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.SOUTH);
        BlockState blockState6 = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.EAST);
        BlockState blockState7 = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.WEST);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState4.with(EndPortalFrameBlock.EYE, true), 4, 3, 8, boundingBox);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState4.with(EndPortalFrameBlock.EYE, true), 5, 3, 8, boundingBox);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState4.with(EndPortalFrameBlock.EYE, true), 6, 3, 8, boundingBox);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState5.with(EndPortalFrameBlock.EYE, true), 4, 3, 12, boundingBox);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState5.with(EndPortalFrameBlock.EYE, true), 5, 3, 12, boundingBox);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState5.with(EndPortalFrameBlock.EYE, true), 6, 3, 12, boundingBox);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState6.with(EndPortalFrameBlock.EYE, true), 3, 3, 9, boundingBox);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState6.with(EndPortalFrameBlock.EYE, true), 3, 3, 10, boundingBox);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState6.with(EndPortalFrameBlock.EYE, true), 3, 3, 11, boundingBox);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState7.with(EndPortalFrameBlock.EYE, true), 7, 3, 9, boundingBox);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState7.with(EndPortalFrameBlock.EYE, true), 7, 3, 10, boundingBox);
        ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState7.with(EndPortalFrameBlock.EYE, OptionTracker.getBoolean(OptionTracker.Option.FILL_PORTAL)), 7, 3, 11, boundingBox);
        if (OptionTracker.getBoolean(OptionTracker.Option.FILL_PORTAL)) {
            ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState8, 4, 3, 9, boundingBox);
            ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState8, 5, 3, 9, boundingBox);
            ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState8, 6, 3, 9, boundingBox);
            ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState8, 4, 3, 10, boundingBox);
            ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState8, 5, 3, 10, boundingBox);
            ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState8, 6, 3, 10, boundingBox);
            ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState8, 4, 3, 11, boundingBox);
            ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState8, 5, 3, 11, boundingBox);
            ((StructurePieceInvoker) this).invokeAddBlock(serverWorldAccess, blockState8, 6, 3, 11, boundingBox);
        }

    }
}
