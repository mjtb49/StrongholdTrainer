package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.Objects;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NewStrongholdCommand {

    private static final int GAP = 128;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("newStronghold").executes(c -> {

                    int x = (int) c.getSource().getEntity().getX() / 16 / GAP;
                    int z = (int) c.getSource().getEntity().getZ() / 16 / GAP;

                    if (x * GAP * 16 + GAP * 16 > 30000000)
                        x -= (60000000 / (GAP * 16) - 1);
                    if (z * GAP * 16 + GAP * 16 > 30000000)
                        z -= (60000000 / (GAP * 16) - 1);

                    x += 1;
                    double blockX = x * GAP * 16 + 4.5;
                    double blockZ = z * GAP * 16 + 4.5;

                    System.out.println(x + " " + z);
                    System.out.println(blockX + " " + blockZ);
                    StructureStart<?> start = c.getSource().getWorld().getStructureAccessor().method_28388(new BlockPos(blockX, 40, blockZ), true, StructureFeature.STRONGHOLD);
                    StrongholdGenerator.Start strongholdStart = ((StartAccessor)start).getStart();
                    double yFinal = strongholdStart.getBoundingBox().getCenter().getY() - 2.5;

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

                    ((ServerPlayerEntity)c.getSource().getEntity()).teleport(c.getSource().getWorld(), blockX, yFinal, blockZ, yaw, 0);

                    return 1;
                })
        );
    }
}
