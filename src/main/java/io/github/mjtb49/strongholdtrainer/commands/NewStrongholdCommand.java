package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

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

                    int attemptedStrongholdLocations = 0;
                    Optional<? extends StructureStart<?>> start;
                    StrongholdGenerator.Start strongholdStart = null;

                    do {
                        if (x * GAP * 16 + GAP * 16 > 30000000) {
                            x -= (60000000 / (GAP * 16) - 1);
                            z += 1;
                        }
                        if (z * GAP * 16 + GAP * 16 > 30000000)
                            z -= (60000000 / (GAP * 16) - 1);
                        x += 1;
                        start = c.getSource().getWorld().getStructureAccessor().getStructuresWithChildren(ChunkSectionPos.from(x * GAP, 0, z * GAP), StructureFeature.STRONGHOLD).findFirst();
                        if (start.isPresent()) {
                            strongholdStart = ((StartAccessor) start.get()).getStart();
                        }
                    } while ((!start.isPresent() || strongholdStart == null) && attemptedStrongholdLocations++ < 100);

                    double blockX = x * GAP * 16 + 4.5;
                    double blockZ = z * GAP * 16 + 4.5;

                    if (start.isPresent() && strongholdStart != null) {
                        double yFinal = strongholdStart.getBoundingBox().getCenter().getY() - 4;
                        float yaw = 0;
                        if (strongholdStart.getFacing() != null){
                            switch ((strongholdStart.getFacing())) {
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
                        }

                        c.getSource().getPlayer().teleport(c.getSource().getWorld(), blockX, yFinal, blockZ, yaw, 0);
                        if (!c.getSource().getPlayer().getServerWorld().getBlockState(new BlockPos(blockX, yFinal - 1, blockZ)).getMaterial().blocksMovement()) {
                            c.getSource().getPlayer().getServerWorld().setBlockState(new BlockPos(blockX, yFinal - 1, blockZ), Blocks.BARRIER.getDefaultState());
                        }
                        c.getSource().getPlayer().setSpawnPoint(c.getSource().getWorld().getWorld().getRegistryKey(), new BlockPos(blockX, yFinal, blockZ), true, false);
                    } else {
                        c.getSource().getPlayer().sendMessage(new LiteralText("Didn't find a stronghold, but try digging down here").formatted(Formatting.RED), false);
                        c.getSource().getPlayer().teleport(c.getSource().getWorld(), blockX, 90, blockZ, 0, 0);
                        c.getSource().getPlayer().setSpawnPoint(c.getSource().getWorld().getWorld().getRegistryKey(), new BlockPos(blockX, 90, blockZ), true, false);
                    }
                    c.getSource().getPlayer().extinguish();
                    c.getSource().getPlayer().heal(20);
                    c.getSource().getPlayer().getHungerManager().setSaturationLevelClient(5.0f);
                    c.getSource().getPlayer().getHungerManager().setFoodLevel(20);

                    Collection<StatusEffect> types = c.getSource().getPlayer().getStatusEffects().stream().map(StatusEffectInstance::getEffectType).filter(statusEffect -> statusEffect != StatusEffects.NIGHT_VISION).collect(Collectors.toList());
                    for (StatusEffect statusEffect : types) {
                        c.getSource().getPlayer().removeStatusEffect(statusEffect);
                    }
                    if (OptionTracker.getOption(OptionTracker.Option.CUSTOM_INVENTORY).getAsBoolean()) {
                        c.getSource().getPlayer().inventory.clear();
                        c.getSource().getMinecraftServer().getCommandManager().execute(c.getSource(), "/inventory load");
                    }
                    return 1;
                })
        );
    }
}
