package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.Objects;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

public class NewStrongholdCommand {

    private static final int GAP = 128;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("newStronghold").executes(c -> {

                    StrongholdTrainer.clearAll();
                    StrongholdTrainer.IS_REVIEWING = false;

                    int x = (int) (c.getSource().getPlayer().getX() + GAP * 8) / 16 / GAP;
                    int z = (int) (c.getSource().getPlayer().getZ() + GAP * 8) / 16 / GAP;

                    if (x * GAP * 16 + GAP * 16 > 30000000) {
                        x -= (60000000 / (GAP * 16) - 1);
                        z += 1;
                    }
                    if (z * GAP * 16 + GAP * 16 > 30000000)
                        z -= (60000000 / (GAP * 16) - 1);

                    x += 1;
                    double blockX = x * GAP * 16 + 4.5;
                    double blockZ = z * GAP * 16 + 4.5;

                    Optional<? extends StructureStart<?>> start = c.getSource().getWorld().getStructureAccessor().getStructuresWithChildren(ChunkSectionPos.from(x * GAP, 0, z * GAP), StructureFeature.STRONGHOLD).findFirst();
                    if (start.isPresent()) {
                        StrongholdGenerator.Start strongholdStart = ((StartAccessor) start.get()).getStart();
                        double yFinal = strongholdStart.getBoundingBox().getCenter().getY() - 4;

                        float yaw = 0;
                        switch (Objects.requireNonNull((strongholdStart.getFacing()))) {

                            case NORTH:
                                yaw = 180;
                                break;
                            case SOUTH:
                                yaw = 0;
                                break;
                            case WEST:
                                yaw = 90;
                                break;
                            case EAST:
                                yaw = -90;
                                break;
                        }
                        c.getSource().getPlayer().teleport(c.getSource().getWorld(), blockX, yFinal, blockZ, yaw, 0);
                        c.getSource().getPlayer().getServerWorld().setBlockState(new BlockPos(blockX, yFinal - 1, blockZ), Blocks.TARGET.getDefaultState());
                        c.getSource().getPlayer().setSpawnPoint(c.getSource().getWorld().getWorld().getRegistryKey(), new BlockPos(blockX, yFinal, blockZ), true, false);

                    } else {
                        c.getSource().getPlayer().sendMessage(new LiteralText("Didn't find a stronghold, but try digging down here").formatted(Formatting.RED), false);
                        c.getSource().getPlayer().teleport(c.getSource().getWorld(), blockX, 90, blockZ, 0, 0);
                        c.getSource().getPlayer().setSpawnPoint(c.getSource().getWorld().getWorld().getRegistryKey(), new BlockPos(blockX, 90, blockZ), true, false);
                    }

                    return 1;
                })
        );
    }
}
