package io.github.mjtb49.strongholdtrainer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EndPortalBlock.class, priority = 100)
public class MixinEndPortalBlock {
    /**
     * @author fsharpseven
     * @reason custom portal
     */
    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void strongholdtrainer$onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (world instanceof ServerWorld &&
                !entity.hasVehicle() &&
                !entity.hasPassengers() &&
                entity.canUsePortals() &&
                VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())), state.getOutlineShape(world, pos), BooleanBiFunction.AND) &&
                entity instanceof ServerPlayerEntity) {
            if(world.getServer().getCommandManager() == null){
                return;
            }
            world.getServer().getCommandManager().execute(entity.getCommandSource(), "newStronghold");
        }
        ci.cancel();
    }
}
