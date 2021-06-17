package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import io.github.mjtb49.strongholdtrainer.api.EntranceAccessor;
import io.github.mjtb49.strongholdtrainer.api.MinecraftServerAccessor;
import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import io.github.mjtb49.strongholdtrainer.commands.NextMistakeCommand;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdPath;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdRoomClassifier;
import io.github.mjtb49.strongholdtrainer.render.Color;
import io.github.mjtb49.strongholdtrainer.render.Cuboid;
import io.github.mjtb49.strongholdtrainer.render.Line;
import io.github.mjtb49.strongholdtrainer.render.TextRenderer;
import io.github.mjtb49.strongholdtrainer.stats.PlayerPathData;
import io.github.mjtb49.strongholdtrainer.stats.PlayerPathTracker;
import io.github.mjtb49.strongholdtrainer.util.EntryNode;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import io.github.mjtb49.strongholdtrainer.util.RoomFormatter;
import io.github.mjtb49.strongholdtrainer.util.StrongholdSearcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements MinecraftServerAccessor {

    @Shadow private PlayerManager playerManager;
    @Shadow @Final protected LevelStorage.Session session;

    private StructurePiece lastPiece = null;
    private StructurePiece mlChosen = null;
    private final Map<StructurePiece, Double> percents = new HashMap<>();
    DecimalFormat df = new DecimalFormat("0.00");
    private final List<StructureStart<?>> visitedNull = new ArrayList<>();
    private int ticksInStronghold = -1;
    private int currentRoomTime = 0;
    private Vec3d lastPlayerPosition;
    private PlayerPathTracker playerPath;
    private StructureStart<?> lastStart;
    private boolean shouldRefreshRooms = false;
    private static StrongholdPath currentPath;
    private boolean loadedModelSupportsBacktracking = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void inject(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        for (ServerPlayerEntity player : this.playerManager.getPlayerList()) {

            tracePlayer(player);

            ServerWorld world = player.getServerWorld();

            StructureStart<?> start = world.getStructureAccessor().method_28388(player.getBlockPos(), true, StructureFeature.STRONGHOLD);

            if (start != StructureStart.DEFAULT) {

                //Lazy check if we've changed strongholds
                if (lastStart != start) {
                    currentPath = new StrongholdPath(((StartAccessor)start).getStart());
                    lastStart = start;
                    ticksInStronghold = -1;
                    currentRoomTime = 0;
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
                            updateMLChoice(start, piece, player);
                            drawRoomsAndDoors(start, strongholdStart, piece, player);
                            shouldRefreshRooms = false;
                        }

                        if (lastPiece != piece) {
                            if (
                                    lastPiece == null
                                    || !lastPiece.getBoundingBox().contains(player.getBlockPos())
                                    || (lastPiece instanceof StrongholdGenerator.SmallCorridor && !(piece instanceof StrongholdGenerator.SmallCorridor)))
                            {

                                onRoomUpdate(start, piece, player);
                                drawRoomsAndDoors(start, strongholdStart, piece, player);

                                currentRoomTime = 0;
                                lastPiece = piece;
                            }
                        }
                    }
                }

            }

            if (ticksInStronghold >= 0) {
                ticksInStronghold++;
                currentRoomTime++;
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

    private void drawRoomsAndDoors(StructureStart<?> start, StrongholdGenerator.Start strongholdStart, StructurePiece piece, ServerPlayerEntity player) {
        int yOffset = ((StartAccessor)start).getYOffset();

        Cuboid cuboid = new Cuboid(piece.getBoundingBox(), Color.PURPLE);

        StrongholdTrainer.submitRoom(cuboid);
        // TODO: good idea to cache this, doesn't seem to hurt perf too much
        StructurePiece searchResult = StrongholdSearcher.search(((StrongholdTreeAccessor)strongholdStart).getTree(), piece);

        StrongholdTrainer.clearDoors();
        TextRenderer.clear();

        TextRenderer.add(cuboid.getVec(), "Depth: " + piece.getLength(), 0.01f);
        TextRenderer.add(cuboid.getVec().add(0, -0.2, 0), "Direction: " + piece.getFacing(), 0.01f);
        TextRenderer.add(cuboid.getVec().add(0, -0.4, 0), "Type: " + RoomFormatter.getStrongholdPieceAsString(piece.getClass()), 0.01f);

        for (EntryNode node : ((EntranceAccessor) piece).getEntrances()) {
            // Means we've reached a dead end- don't render forwards entries
            if (node.pointer == null && node.type == EntryNode.Type.FORWARDS) {
                continue;
            }

            BlockBox entrance = node.box;

            BlockBox newBox = new BlockBox(entrance.minX, entrance.minY + yOffset, entrance.minZ, entrance.maxX - 1, entrance.maxY + yOffset - 1, entrance.maxZ - 1);

            Color color = node.type == EntryNode.Type.FORWARDS ? Color.WHITE : Color.YELLOW;

            boolean isBlue = false;
            if (searchResult == null) {
                if (node.type == EntryNode.Type.BACKWARDS) {
                    color = Color.BLUE;
                    isBlue = true;
                }
            } else if (node.type == EntryNode.Type.FORWARDS && searchResult == node.pointer) {
                color = Color.BLUE;
                isBlue = true;
            }
            boolean bothFlag = false;
            if ((node.pointer != null && node.pointer == this.mlChosen)
            || (this.mlChosen == ((StrongholdTreeAccessor) strongholdStart).getParents().get(piece) && node.type == EntryNode.Type.BACKWARDS && loadedModelSupportsBacktracking)) {
                if (isBlue) {
                    StrongholdTrainer.submitDoor(new Cuboid(Box.from(newBox).expand(0.05), Color.GREEN));
                    bothFlag = true;
                } else {
                    color = Color.GREEN;
                }
            }

            Cuboid door = new Cuboid(newBox, color);
            StrongholdTrainer.submitDoor(door);

            if (OptionTracker.getBoolOption(OptionTracker.Option.DOOR_LABELS)) {
                if (color == Color.GREEN) {
                    TextRenderer.add(door.getVec().subtract(0, 0.5, 0), "Model Choice", 0.02f);
                } else if (color == Color.BLUE) {
                    TextRenderer.add(door.getVec().subtract(0, 0.5, 0), bothFlag ? "Perfect Choice & Model Choice" : "Perfect Choice", 0.02f);
                } else if (color == Color.YELLOW) {
                    TextRenderer.add(door.getVec().subtract(0, 0.5, 0), "Reverse", 0.02f);
                }
            }

            if (node.pointer != null) {
                String text = df.format(this.percents.getOrDefault(node.pointer, 0.0) * 100) + "%";
                TextRenderer.add(door.getVec(), text);
            } else if (node.type == EntryNode.Type.BACKWARDS) {
                if (loadedModelSupportsBacktracking) {
                    StructurePiece parent = ((StrongholdTreeAccessor) strongholdStart).getParents().get(piece);
                    String text = df.format(this.percents.getOrDefault(parent, 0.0) * 100) + "%";
                    TextRenderer.add(door.getVec(), text);
                } else {
                    TextRenderer.add(door.getVec(), "not supported", 0.01f);
                }
            }

        }
    }

    private void onRoomUpdate(StructureStart<?> start, StructurePiece piece, ServerPlayerEntity player) {
        if (playerPath != null && !((StartAccessor) start).hasBeenRouted())
            playerPath.addPiece((StrongholdGenerator.Piece) lastPiece, currentRoomTime);
        updateStats(start, piece, player);
        updateMLChoice(start, piece, player);
    }

    private void updateStats(StructureStart<?> start, StructurePiece piece, ServerPlayerEntity player) {
        if (
                lastPiece instanceof StrongholdGenerator.Start
                        && ticksInStronghold < 0 && !((StartAccessor)start).hasBeenRouted()
                        && !lastPiece.getBoundingBox().contains(player.getBlockPos())
        ) {
            playerPath = new PlayerPathTracker(start);
            ticksInStronghold = 0;
        }

        if (piece instanceof StrongholdGenerator.PortalRoom && ticksInStronghold >= 0) {

            playerPath.addPiece((StrongholdGenerator.PortalRoom) piece, 0);
            playerPath.reviewAndUpdateStats(player, ticksInStronghold);

            NextMistakeCommand.submitMistakesAndInaccuracies(playerPath.getMistakes(), playerPath.getInaccuracies());
            NextMistakeCommand.sendInitialMessage(player);

            playerPath = null;
            ticksInStronghold = -1;
            ((StartAccessor)start).setHasBeenRouted(true);
        }
    }

    private void updateMLChoice(StructureStart<?> start, StructurePiece piece, ServerPlayerEntity player) {
            currentPath.add((StrongholdGenerator.Piece) piece, (StrongholdGenerator.Piece) lastPiece);
//        currentPath.iterator().forEachRemaining(System.out::println);
            double[] policy;
            try {
                policy = StrongholdRoomClassifier.getPredictions(currentPath);
            } catch (Exception e) {
                e.printStackTrace();
                policy = new double[5];
            }

            //StringBuilder s = new StringBuilder();
            //Arrays.stream(policy).forEach(e -> s.append(df.format(e)).append(" "));
            //player.sendMessage(new LiteralText(s.toString()).formatted(Formatting.YELLOW), false);
            // hack fixes for backtracking, need to be cleaned up.

            List<StructurePiece> pieces = ((StrongholdTreeAccessor) ((StartAccessor) start).getStart()).getTree().getOrDefault(piece, new ArrayList<>());
            StructurePiece parent = ((StrongholdTreeAccessor) (((StartAccessor) start).getStart())).getParents().getOrDefault(piece, null);

            this.percents.clear();
            int idx = -1;
            double min = 0;
            if (policy.length == 5) {
                loadedModelSupportsBacktracking = false;
                for (int i = 0; i < policy.length; i++) {
                    double p = policy[i];

                    if (i < pieces.size()) {
                        this.percents.put(pieces.get(i), p);
                    }

                    if (p > min) {
                        idx = i;
                        min = p;
                    }
                }
                if (idx < pieces.size()) {
                    this.mlChosen = pieces.get(idx);
                } else {
                    this.mlChosen = null;
                }
            } else {
                loadedModelSupportsBacktracking = true;
                for (int i = 0; i < policy.length; i++) {
                    double p = policy[i];

                    if ((i - 1) < pieces.size() && i != 0) {
                        this.percents.put(pieces.get(i - 1), p);
                    } else if (i == 0) {
                        this.percents.put(parent, p);
                    }

                    if (p > min) {
                        idx = i;
                        min = p;
                    }
                }
                if (idx == 0) {
                    this.mlChosen = parent;
                } else if (idx - 1 < pieces.size()) {
                    this.mlChosen = pieces.get(idx - 1);
                } else {
                    this.mlChosen = null;
                }
            }

    }

    private void tracePlayer(ServerPlayerEntity player) {
        if (ticksInStronghold >= 0) {
            if (!player.isSpectator() && !player.isCreative() && StrongholdTrainer.getOption("trace")) {
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
