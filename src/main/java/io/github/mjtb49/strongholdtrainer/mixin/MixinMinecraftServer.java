package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import io.github.mjtb49.strongholdtrainer.api.MinecraftServerAccessor;
import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import io.github.mjtb49.strongholdtrainer.path.DimensionPathListener;
import io.github.mjtb49.strongholdtrainer.path.RenderingPathListener;
import io.github.mjtb49.strongholdtrainer.path.StatsPathListener;
import io.github.mjtb49.strongholdtrainer.path.StrongholdPath;
import io.github.mjtb49.strongholdtrainer.render.Color;
import io.github.mjtb49.strongholdtrainer.render.Line;
import io.github.mjtb49.strongholdtrainer.stats.PlayerPathData;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import io.github.mjtb49.strongholdtrainer.util.TimerHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements MinecraftServerAccessor {

    @Shadow private PlayerManager playerManager;
    private static final DecimalFormat df2 = new DecimalFormat("00.00");

    private StructurePiece lastPiece = null;
    private static final StatsPathListener listener = new StatsPathListener();
    private final List<StructureStart<?>> visitedNull = new ArrayList<>();
    private int ticksInStronghold = -1;
    private Vec3d lastPlayerPosition;
    private StructureStart<?> lastStart;
    private boolean shouldRefreshRooms = false;
    private static StrongholdPath currentPath;
    private static final RenderingPathListener renderListener = new RenderingPathListener();
    private static final DimensionPathListener dimListener = new DimensionPathListener();
    @Shadow
    @Final
    protected LevelStorage.Session session;

    @Inject(method = "tick", at = @At("HEAD"))
    private void inject(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        for (ServerPlayerEntity player : this.playerManager.getPlayerList()) {

            tracePlayer(player);
            ServerWorld world = player.getServerWorld();

            StructureStart<?> start = world.getStructureAccessor().method_28388(player.getBlockPos(), true, StructureFeature.STRONGHOLD);

            if (start != StructureStart.DEFAULT) {
                //Lazy check if we've changed strongholds
                if (lastStart != start) {
                    if(currentPath != null){
                        renderListener.detach();
                        listener.detach();
                        dimListener.detach();
                    }
                    currentPath = new StrongholdPath(start, player);
                    renderListener.attach(currentPath);
                    listener.attach(currentPath);
                    dimListener.attach(currentPath);
                    lastStart = start;
                    ticksInStronghold = -1;
                }

                StrongholdGenerator.Start strongholdStart = ((StartAccessor)start).getStart();

                if (strongholdStart == null) {
                    if (!this.visitedNull.contains(start)) {
                        this.visitedNull.add(start);
                        player.sendMessage(new LiteralText("Please visit a new stronghold!").formatted(Formatting.RED), false);
                    }
                    continue;
                }
                for (StructurePiece piece : start.getChildren()) {
                    if (piece.getBoundingBox().contains(player.getBlockPos())) {
                        if (shouldRefreshRooms) {
                            currentPath.forceEvent(StrongholdPath.PathEvent.PATH_UPDATE);
                            shouldRefreshRooms = false;
                        }

                        if (lastPiece != piece) {
                            if (
                                    lastPiece == null
                                            || !lastPiece.getBoundingBox().contains(player.getBlockPos())
                                            || (lastPiece instanceof StrongholdGenerator.SmallCorridor && !(piece instanceof StrongholdGenerator.SmallCorridor))) {
                                currentPath.add((StrongholdGenerator.Piece) piece, (StrongholdGenerator.Piece) lastPiece);
                                lastPiece = piece;
                            }
                        } else {
                            currentPath.tickLatest();
                        }
                    }
                }
            } else {
                if (currentPath != null && !currentPath.isFinished()) {
                    currentPath.tickOutside();
                }
            }
            if (currentPath != null) {
                String total = TimerHelper.ticksToTime(currentPath.getTotalTime());
                int currentTicks = currentPath.getLatest().getTicksSpentInPiece().get();
                String current = TimerHelper.ticksToTime(currentTicks);
                String outside = TimerHelper.ticksToTime(currentPath.getTicksOutside());
                Formatting formatting = Formatting.ITALIC;
                StrongholdGenerator.Piece currentPiece = currentPath.getLatest().getCurrentPiece();
                if (currentPiece instanceof StrongholdGenerator.PortalRoom || currentPiece instanceof StrongholdGenerator.Start || currentPath.isFinished()) {
                    formatting = Formatting.GRAY;
                    current = "-:--.--";
                    if (currentPath.isFinished()) {
                        total = "|" + total + "|";
                    }
                } else {
                    if (StatsPathListener.FEINBERG_AVG_ROOM_TIMES.get(currentPiece.getClass()) == 0) {
                        formatting = Formatting.GRAY;
                    } else {
                        if (currentTicks > StatsPathListener.FEINBERG_AVG_ROOM_TIMES.get(currentPath.getLatest().getCurrentPiece().getClass())) {
                            formatting = Formatting.RED;
                            current = "⁽⁺⁾" + current;
                        } else if (currentTicks < StatsPathListener.FEINBERG_AVG_ROOM_TIMES.get(currentPath.getLatest().getCurrentPiece().getClass())) {
                            formatting = Formatting.GREEN;
                            current = "⁽⁻⁾" + current;
                        } else if (currentTicks == StatsPathListener.FEINBERG_AVG_ROOM_TIMES.get(currentPiece.getClass())) {
                            formatting = Formatting.GOLD;
                            current = "⁽⁼⁾" + current;
                        }
                    }
                }
                player.sendMessage(new LiteralText(outside + "   " + total).formatted(currentPath.isFinished() ? Formatting.BOLD : Formatting.RESET)
                        .append(new LiteralText("   " + current).formatted(formatting)), true);
            }

            if (ticksInStronghold >= 0) {
                ticksInStronghold++;
            }
        }
    }

    @Inject(method = "loadWorld()V", at = @At("TAIL"))
    private void loadWorld(CallbackInfo ci) {
        Path p = session.getDirectory(WorldSavePathAccessor.createWorldSavePath("strongholds"));
        try {
            p.toFile().mkdirs();
        } catch (SecurityException securityException) {
            securityException.printStackTrace();
        }

        PlayerPathData.loadAllPriorPaths(p);
    }


    private void tracePlayer(ServerPlayerEntity player) {
        if (currentPath != null && !currentPath.isFinished()) {
            if (!player.isSpectator() && !player.isCreative() && OptionTracker.getBoolean(OptionTracker.Option.TRACE)) {
                if (lastPlayerPosition != null)
                    if (lastPlayerPosition.distanceTo(player.getPos()) < 10)
                        StrongholdTrainer.submitPlayerLine(new Line(lastPlayerPosition.add(0,0.01,0), player.getPos().add(0,0.01,0), Color.PINK));
                lastPlayerPosition = player.getPos();
            } else {
                lastPlayerPosition = null;
            }
        }
    }

    @Override
    public void refreshRooms()  {
        shouldRefreshRooms = true;
    }


}
