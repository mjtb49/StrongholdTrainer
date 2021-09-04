package io.github.mjtb49.strongholdtrainer.path;

import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import io.github.mjtb49.strongholdtrainer.api.EntranceAccessor;
import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdMachineLearning;
import io.github.mjtb49.strongholdtrainer.render.Color;
import io.github.mjtb49.strongholdtrainer.render.Cuboid;
import io.github.mjtb49.strongholdtrainer.render.TextRenderer;
import io.github.mjtb49.strongholdtrainer.util.EntryNode;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import io.github.mjtb49.strongholdtrainer.util.RoomFormatter;
import io.github.mjtb49.strongholdtrainer.util.StrongholdSearcher;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderingPathListener extends AbstractPathListener {

    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<StructurePiece, Double> percents;
    private StructurePiece mlChosen;
    private boolean loadedModelSupportsBacktracking;

    public RenderingPathListener() {
        percents = new HashMap<>();
        loadedModelSupportsBacktracking = false;
    }

    @Override
    public void update(StrongholdPath.PathEvent event) {
        try {
            if (event.equals(StrongholdPath.PathEvent.PATH_UPDATE)) {
                updateMLChoice(strongholdPath.getStructureStart(), strongholdPath.getLatest().getCurrentPiece());
                drawRoomsAndDoors(strongholdPath.getStructureStart(), strongholdPath.getStart(), strongholdPath.getLatest().getCurrentPiece());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update " + this + ": ", e);
        }
    }

    private void updateMLChoice(StructureStart<?> start, StructurePiece piece) {
        double[] policy;
        try {
            policy = StrongholdMachineLearning.getPredictions(strongholdPath);
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
        double min = Double.NEGATIVE_INFINITY;
        if (policy.length == 5) {
            this.loadedModelSupportsBacktracking = false;
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

    private void drawRoomsAndDoors(StructureStart<?> start, StrongholdGenerator.Start strongholdStart, StructurePiece piece) {
        int yOffset = ((StartAccessor) start).getYOffset();

        Cuboid cuboid = new Cuboid(piece.getBoundingBox(), Color.PURPLE);

        StrongholdTrainer.submitRoom(cuboid);
        StructurePiece searchResult = StrongholdSearcher.search(((StrongholdTreeAccessor) strongholdStart).getTree(), piece);

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

            if (OptionTracker.getBoolean(OptionTracker.Option.DOOR_LABELS)) {
                if (color == Color.GREEN) {
                    TextRenderer.add(door.getVec().subtract(0, 0.5, 0), "Model Choice", 0.02f);
                } else if (color == Color.BLUE) {
                    TextRenderer.add(door.getVec().subtract(0, 0.5, 0), bothFlag ? "Perfect Choice & Model Choice" : "Perfect Choice", 0.02f);
                } else if (color == Color.YELLOW) {
                    TextRenderer.add(door.getVec().subtract(0, 0.5, 0), "Reverse", 0.02f);
                }
            }

            if (node.pointer != null) {
                String text = df.format(this.percents.getOrDefault(node.pointer, 0.0));
                TextRenderer.add(door.getVec(), text);
            } else if (node.type == EntryNode.Type.BACKWARDS) {
                if (loadedModelSupportsBacktracking) {
                    StructurePiece parent = ((StrongholdTreeAccessor) strongholdStart).getParents().get(piece);
                    String text = df.format(this.percents.getOrDefault(parent, 0.0));
                    TextRenderer.add(door.getVec(), text);
                } else {
                    TextRenderer.add(door.getVec(), "not supported", 0.01f);
                }
            }

        }
    }

}
