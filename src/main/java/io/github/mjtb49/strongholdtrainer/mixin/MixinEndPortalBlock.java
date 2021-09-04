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

@Mixin(EndPortalBlock.class)
public class MixinEndPortalBlock {
    /**
     * @author fsharpseven
     * @reason custom portal
     */
    @Overwrite
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
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
    }
}
