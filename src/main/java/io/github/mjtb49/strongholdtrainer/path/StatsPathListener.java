package io.github.mjtb49.strongholdtrainer.path;

import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import io.github.mjtb49.strongholdtrainer.commands.NextMistakeCommand;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdMachineLearning;
import io.github.mjtb49.strongholdtrainer.stats.PlayerPathData;
import io.github.mjtb49.strongholdtrainer.stats.PlayerPathEntry;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import io.github.mjtb49.strongholdtrainer.util.RoomFormatter;
import io.github.mjtb49.strongholdtrainer.util.TimerHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StatsPathListener extends AbstractPathListener {

    public static final HashMap<Class<? extends StrongholdGenerator.Piece>, Integer> FEINBERG_AVG_ROOM_TIMES = new HashMap<>();
    private static final int BLUNDER_THRESHOLD = 20 * 10;
    private static final int MISTAKE_THRESHOLD = 20 * 5;
    private static final int INACCURACY_THRESHOLD = 20 * 2;
    private static final Logger LOGGER = LogManager.getLogger();

    static {
        FEINBERG_AVG_ROOM_TIMES.put(StrongholdGenerator.ChestCorridor.class, 25);
        FEINBERG_AVG_ROOM_TIMES.put(StrongholdGenerator.SquareRoom.class, 39);
        FEINBERG_AVG_ROOM_TIMES.put(StrongholdGenerator.PrisonHall.class, 42);
        FEINBERG_AVG_ROOM_TIMES.put(StrongholdGenerator.Stairs.class, 33);
        FEINBERG_AVG_ROOM_TIMES.put(StrongholdGenerator.Corridor.class, 26);
        FEINBERG_AVG_ROOM_TIMES.put(StrongholdGenerator.SpiralStaircase.class, 52);
        FEINBERG_AVG_ROOM_TIMES.put(StrongholdGenerator.FiveWayCrossing.class, 60);
        FEINBERG_AVG_ROOM_TIMES.put(StrongholdGenerator.LeftTurn.class, 20);
        FEINBERG_AVG_ROOM_TIMES.put(StrongholdGenerator.RightTurn.class, 18);
    }

    List<StrongholdGenerator.Piece> mistakes;
    List<StrongholdGenerator.Piece> inaccuracies;
    List<StrongholdGenerator.Piece> blunders;

    private ServerPlayerEntity playerEntity;
    private boolean completed;
    private Instant start;
    private boolean invalidRun;

    public StatsPathListener() {
        this.playerEntity = null;
        this.mistakes = new ArrayList<>();
        this.inaccuracies = new ArrayList<>();
    }

    protected static double loss(StrongholdPath path, StrongholdPathEntry entry, ArrayList<StructurePiece> solution) {
        // Tree accessor, previous entry and policy, history
        StrongholdTreeAccessor treeAccessor = (StrongholdTreeAccessor) path.getStart();
        StrongholdPathEntry previousEntry = path.getPrecedingEntry(entry);
        double[] policy = previousEntry.getPolicy();
        List<StrongholdPathEntry> history = path.getHistory();
        int j = history.indexOf(entry);

        // Relevant weights
        double chosenWeight = policy[treeAccessor.getTree().get(previousEntry.getCurrentPiece()).indexOf(entry.getCurrentPiece())];
        if (policy.length == 6) {
            chosenWeight = policy[treeAccessor.getTree().get(previousEntry.getCurrentPiece()).indexOf(entry.getCurrentPiece()) + 1];
        }
        double maxWeight = Collections.max(Arrays.asList(ArrayUtils.toObject(policy)));

        int wastedTickCounter = entry.getTicksSpentInPiece().get();
        while (!solution.contains(history.get(j).getCurrentPiece())) {
            wastedTickCounter += history.get(j).getTicksSpentInPiece().get();
            j++;
        }
        LOGGER.debug("Loss for " + entry + ": " + (wastedTickCounter) * (maxWeight - chosenWeight));
        return (wastedTickCounter) * (maxWeight - chosenWeight);
    }

    protected static boolean validateEntryForLoss(StrongholdPath path, StrongholdPathEntry entry) {
        StrongholdTreeAccessor treeAccessor = (StrongholdTreeAccessor) path.getStart();
        try {
            return path.getPrecedingEntry(entry) != null
                    && path.getPrecedingEntry(entry).getPolicy() != null
                    && entry.getCurrentPiece() != null
                    && entry.getPreviousPiece() != null
                    && path.getNextEntry(entry) != null
                    && treeAccessor.getTree().get(path.getPrecedingEntry(entry).getCurrentPiece()).contains(entry.getCurrentPiece())
                    && entry.getPolicy() != null
                    && !(entry.getCurrentPiece() instanceof StrongholdGenerator.Start);
        } catch (Exception e) {
            return false;
        }

    }

    protected static boolean areAdjacent(StrongholdGenerator.Piece piece1, StrongholdGenerator.Piece piece2, StrongholdTreeAccessor strongholdTreeAccessor) {
        if (piece1 == null || piece2 == null || (strongholdTreeAccessor.getTree().get(piece2)) == null || (strongholdTreeAccessor.getTree().get(piece1) == null)) {
            return false;
        }
        return piece1 == (strongholdTreeAccessor.getParents().get(piece2)) ||
                (strongholdTreeAccessor.getTree().get(piece2)).contains(piece1) ||
                piece2 == (strongholdTreeAccessor.getParents().get(piece1)) ||
                (strongholdTreeAccessor.getTree().get(piece1)).contains(piece2);
    }

    @Deprecated
    public void update(boolean completed) {
        this.completed = completed;
        if (this.completed) {
            this.populateStats().updateAndPrintAllStats(playerEntity, null, invalidRun);
        }
    }

    @Override
    public void update(StrongholdPath.PathEvent event) {
        playerEntity = this.strongholdPath.getPlayerEntity();
        if (event == StrongholdPath.PathEvent.PATH_COMPLETE) {
            Instant end = Instant.now();
            this.completed = true;
            this.populateStats().updateAndPrintAllStats(playerEntity, TimerHelper.millisToTime(Duration.between(start, end).toMillis()), invalidRun);
            NextMistakeCommand.submitMistakesAndInaccuracies(getMistakes(), getInaccuracies(), getBlunders());
            NextMistakeCommand.sendInitialMessage(playerEntity);
            ((StartAccessor) strongholdPath.getStructureStart()).setHasBeenRouted(true);
            playerEntity.sendMessage(Texts.bracketed(new LiteralText("New Stronghold").formatted(Formatting.BOLD, Formatting.DARK_AQUA)).styled(
                    (style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/newStronghold")).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Go to a new stronghold")))), false);
        } else if (event == StrongholdPath.PathEvent.PATH_START) {
            this.invalidRun = false;
            start = Instant.now();
        }
        this.playerEntity = strongholdPath.getPlayerEntity();
        if (event == StrongholdPath.PathEvent.PATH_TICK) {
            this.invalidRun |= playerEntity.isSpectator()
                    || playerEntity.isCreative()
                    || OptionTracker.getOption(OptionTracker.Option.HINTS).getAsBoolean();
        }
    }

    public ArrayList<StrongholdGenerator.Piece> getMistakes() {
        return new ArrayList<>(mistakes);
    }

    public ArrayList<StrongholdGenerator.Piece> getInaccuracies() {
        return new ArrayList<>(inaccuracies);
    }

    public ArrayList<StrongholdGenerator.Piece> getBlunders() {
        return new ArrayList<>(blunders);
    }


    public PlayerPathData populateStats() {
        this.playerEntity = strongholdPath.getPlayerEntity();
        StrongholdGenerator.Start start = this.strongholdPath.getStart();
        StrongholdTreeAccessor treeAccessor = (StrongholdTreeAccessor) start;
        List<StrongholdPathEntry> history = this.strongholdPath.getHistory();
        ArrayList<StructurePiece> solution = new ArrayList<>();
        StrongholdGenerator.Piece current = this.strongholdPath.getHistory().get(strongholdPath.getHistory().size() - 1).getCurrentPiece();
        while (current != null) {
            solution.add(current);
            current = (StrongholdGenerator.Piece) treeAccessor.getParents().get(current);
        }
        List<StrongholdPathEntry> validEntries = history.stream()
                .filter(entry -> validateEntryForLoss(strongholdPath, strongholdPath.getNextEntry(entry)))
                .filter(entry -> !solution.contains(strongholdPath.getNextEntry(entry).getCurrentPiece()) && solution.contains(entry.getCurrentPiece()))
                .collect(Collectors.toList());
        List<Pair<StrongholdPathEntry, Double>> losses = new ArrayList<>();
        validEntries.forEach(strongholdPathEntry -> losses.add(new Pair<>(strongholdPathEntry, loss(strongholdPath, strongholdPath.getNextEntry(strongholdPathEntry), solution))));
        this.inaccuracies = losses.stream().filter(pair -> pair.getRight() >= INACCURACY_THRESHOLD).map(Pair::getLeft).map(StrongholdPathEntry::getCurrentPiece).collect(Collectors.toList());
        this.mistakes = losses.stream().filter(pair -> pair.getRight() >= MISTAKE_THRESHOLD).map(Pair::getLeft).map(StrongholdPathEntry::getCurrentPiece).collect(Collectors.toList());
        this.blunders = losses.stream().filter(pair -> pair.getRight() >= BLUNDER_THRESHOLD).map(Pair::getLeft).map(StrongholdPathEntry::getCurrentPiece).collect(Collectors.toList());
        inaccuracies.removeAll(this.mistakes);
        mistakes.removeAll(this.blunders);
        ArrayList<PlayerPathEntry> playerPath = new ArrayList<>();
        for (int i = 0; i < history.size() - 1; i++) {
            StrongholdPathEntry currentEntry = history.get(i);
            StrongholdGenerator.Piece currentPiece = currentEntry.getCurrentPiece();
            StrongholdGenerator.Piece previousPiece = currentEntry.getPreviousPiece();
            StrongholdGenerator.Piece nextPiece = history.get(i+1).getCurrentPiece();

            int entrance = getDoorToRoom(currentPiece, previousPiece);
            int exit = getDoorToRoom(currentPiece, nextPiece);

            playerPath.add(new PlayerPathEntry(currentPiece, currentEntry.getTicksSpentInPiece().get(), entrance, exit));
        }

        return new PlayerPathData(
                playerPath,
                strongholdPath.getTotalTime(),
                computeDifficulty(solution),
                history.stream()
                        .filter(pathEntry -> !solution.contains(pathEntry.getCurrentPiece()))
                        .map(StrongholdPathEntry::getTicksSpentInPiece)
                        .mapToInt(AtomicInteger::get)
                        .sum(),
                // TODO: don't count entering the first Five-Way
                (int) history.stream()
                        .map(strongholdPathEntry -> strongholdPath.getNextEntry(strongholdPathEntry))
                        .filter(Objects::nonNull)
                        .map(StrongholdPathEntry::getCurrentPiece)
                        .filter(solution::contains)
                        .count(),
                this.inaccuracies.size(),
                this.mistakes.size(),
                this.blunders.size(),
                (int) history.stream()
                        .filter(entry -> !(entry.getCurrentPiece() instanceof StrongholdGenerator.PortalRoom))
                        .filter(entry -> !areAdjacent(entry.getCurrentPiece(), strongholdPath.getNextEntry(entry).getCurrentPiece(), treeAccessor))
                        .count(),
                history.size() - 1,
                history.stream()
                        .filter(entry -> FEINBERG_AVG_ROOM_TIMES.containsKey(entry.getCurrentPiece().getClass()))
                        .mapToInt(value -> value.getTicksSpentInPiece().get() - FEINBERG_AVG_ROOM_TIMES.get(value.getCurrentPiece().getClass()))
                        .sum()
        );
    }

    private double computeDifficulty(ArrayList<StructurePiece> solution) {
        double difficulty = 1.0;
        //Loop starts at 2 because the portal room and the room before it have messed up policy but are always trivial
        for (int i = 2; i < solution.size(); i++) {
            difficulty *= getWeightOfCorrectDoor(solution, (StrongholdGenerator.Piece) solution.get(i),
                    StrongholdMachineLearning.MODEL_REGISTRY.getModel("basic-classifier-nobacktracking").getPredictions(this.strongholdPath.getStart(), (StrongholdGenerator.Piece) solution.get(i)));

        }
        return difficulty;
    }

    private double getWeightOfCorrectDoor(ArrayList<StructurePiece> solution, StrongholdGenerator.Piece piece, double[] policy) {
        List<StructurePiece> children = ((StrongholdTreeAccessor) this.strongholdPath.getStart()).getTree().get(piece);

        for (int i = 0; i < children.size(); i++) {
            if (solution.contains(children.get(i))) {
                return policy[i];
            }
        }
        //should never run
        return -1;
    }

    /** 
     * This function has very specific behavior tailored for use in RoomStats.java. It will:
     * - return 0 if the target is the parent of the current room
     * - return 0 if the target is `null` and current is the starter staircase
     * - return the exit index of the target from the current room, ONE-INDEXED!!
     * - return num_exits(current) PLUS ONE!! if the target room is not adjacent to the current room.
    */
    private int getDoorToRoom(StrongholdGenerator.Piece current, StrongholdGenerator.Piece target) {
        StrongholdTreeAccessor treeAccessor = (StrongholdTreeAccessor) this.strongholdPath.getStart();
        if ((current.getClass() != StrongholdGenerator.Start.class && treeAccessor.getParents().get(current).equals(target)) || current.getClass() == StrongholdGenerator.Start.class && target == null) {
            return 0;
        } else {
            List<StructurePiece> exits = treeAccessor.getTree().get(current);
            if (exits == null) {  // no exits? Must be a wormhole
                return RoomFormatter.ROOM_TO_NUM_EXITS.get(current.getClass()) + 1;
            }
            int i = exits.indexOf(target);
            if (i == -1) {  // target not in the exit list? Must be a wormhole
                return RoomFormatter.ROOM_TO_NUM_EXITS.get(current.getClass()) + 1;
            }
            // This can never be a crash-causing value, which is nice.
            return i + 1;
        }

    }
}

