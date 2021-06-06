package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import io.github.mjtb49.strongholdtrainer.api.EntranceAccessor;
import io.github.mjtb49.strongholdtrainer.api.OffsetAccessor;
import io.github.mjtb49.strongholdtrainer.render.Color;
import io.github.mjtb49.strongholdtrainer.render.Cuboid;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Shadow private PlayerManager playerManager;

    @Inject(method = "tick", at = @At("HEAD"))
    private void inject(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        for (ServerPlayerEntity player : this.playerManager.getPlayerList()) {
            ServerWorld world = player.getServerWorld();

            StructureStart<?> start = world.getStructureAccessor().method_28388(player.getBlockPos(), true, StructureFeature.STRONGHOLD);

            if (start != StructureStart.DEFAULT) {
                for (StructurePiece piece : start.getChildren()) {
                    int yOffset = ((OffsetAccessor)start).getYOffset();

                    if (piece.getBoundingBox().contains(player.getBlockPos())) {
                        Cuboid cuboid = new Cuboid(piece.getBoundingBox(), Color.PURPLE);

                        StrongholdTrainer.submitRoom(cuboid);

                        for (BlockBox entrance : ((EntranceAccessor) piece).getEntrances()) {
                            BlockBox newBox = new BlockBox(entrance.minX, entrance.minY + yOffset, entrance.minZ, entrance.maxX, entrance.maxY + yOffset, entrance.maxZ);

                            StrongholdTrainer.submitRoom(new Cuboid(newBox, Color.ORANGE));
                        }
                    }
                }
            }
            //if (start != StructureStart.DEFAULT) {
            //    System.out.println("In stronghold");
            //}
        }
    }
}
