package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.EntranceAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(StrongholdGenerator.class)
public abstract class MixinStrongholdGenerator {

//    @Inject(method = "generateEntrance", at = @At("HEAD"))
//    private void retainEntrances(WorldAccess world, Random random, BlockBox box, StrongholdGenerator.Piece.EntranceType type, int x, int y, int z, CallbackInfo ci) {
//        this.entrances.add(new BlockBox(
//                new BlockPos(this.applyXTransform(x, z), this.applyYTransform(y), this.applyZTransform(x, z)),
//                new BlockPos(this.applyXTransform(x + 3, z), this.applyYTransform(y + 3), this.applyZTransform(x + 3, z + 1))
//        ));
//    }

//    @Inject(method = "method_14854", at = @At("RETURN"))
//    private static void getGeneratedNewPiece(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int x, int y, int z, Direction direction, int l, CallbackInfoReturnable<StructurePiece> cir) {
//        StructurePiece piece = cir.getReturnValue();
//        if (piece != null) {
//            ((EntranceAccessor)piece).getEntrances().add(
//                    new BlockBox(
//                            new BlockPos(x, y, z),
//                            new BlockPos(x + 3, y + 3, z + 3)
//                    )
//            );
//
////            this.entrances.add(new BlockBox(
////                new BlockPos(this.applyXTransform(x, z), this.applyYTransform(y), this.applyZTransform(x, z)),
////                new BlockPos(this.applyXTransform(x + 3, z), this.applyYTransform(y + 3), this.applyZTransform(x + 3, z + 1))
////        ));
//        }
//    }
}
