package io.github.mjtb49.strongholdtrainer.path;

import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import io.github.mjtb49.strongholdtrainer.ml.model.RoomData;
import io.github.mjtb49.strongholdtrainer.util.RoomFormatter;
import io.github.mjtb49.strongholdtrainer.util.TimerHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: Thread safety, cleanup, optimization
public class StrongholdPath {

    private final StrongholdGenerator.Start start;
    private final StructureStart<?> structureStart;
    private final List<StrongholdPathEntry> history;
    private final ServerPlayerEntity playerEntity;
    private final AtomicInteger ticksOutside;
    private final List<StrongholdPathListener> listeners;
    private static final Logger LOGGER = LogManager.getLogger();

    private boolean finished = false;
    private final Map<Class<? extends StrongholdGenerator.Piece>, Boolean> winConditions;
    private final StrongholdTreeAccessor treeAccessor;
    private boolean started;

    public StrongholdPath(StructureStart<?> start2, ServerPlayerEntity entity, List<Class<? extends StrongholdGenerator.Piece>> reqRooms) {
        this.start = ((StartAccessor) start2).getStart();
        history = new ArrayList<>();
        ticksOutside = new AtomicInteger();
        this.listeners = Collections.synchronizedList(new LinkedList<>());
        this.structureStart = start2;
        this.playerEntity = entity;
        this.treeAccessor = (StrongholdTreeAccessor) this.start;
        this.winConditions = new HashMap<>();
        this.started = false;
        reqRooms.forEach(piece -> winConditions.put(piece, false));
    }

    public StructureStart<?> getStructureStart() {
        return structureStart;
    }

    public ServerPlayerEntity getPlayerEntity() {
        return playerEntity;
    }

    public void forceEvent(PathEvent event) {
        this.notifyListeners(event);
    }

    public Text sendSplits() {
        MutableText text = new LiteralText("SPLITS");
        for(StrongholdPathEntry entry : history){
            int r = entry.getTicksSpentInPiece().get();
            String str = RoomFormatter.getStrongholdPieceAsString(entry.getCurrentPiece().getClass()) + " - " ;
            int i = (Integer) RoomData.DOWNWARDS.roomDataFunction.apply(treeAccessor, entry.getCurrentPiece(), entry.getPreviousPiece());
            if (i == 0) {
                str += " ←";
            } else {
                str += " →";
            }

            text.append(new LiteralText("\n"+str).append(new LiteralText(TimerHelper.ticksToTime(r)).formatted(Formatting.GOLD)));
        }
        return text;
    }

    public StrongholdTreeAccessor getTreeAccessor() {
        return treeAccessor;
    }

    public void add(StrongholdGenerator.Piece current, StrongholdGenerator.Piece previous) {
        this.history.add(new StrongholdPathEntry(current, previous, new AtomicInteger()));
        this.notifyListeners(PathEvent.PATH_UPDATE);
        if (winConditions.containsKey(current.getClass())) {
            winConditions.put(current.getClass(), true);
        }
        if (current instanceof StrongholdGenerator.PortalRoom
                && !this.finished
                && !this.winConditions.containsValue(Boolean.FALSE)) {
            this.finished = true;
            this.notifyListeners(PathEvent.PATH_COMPLETE);
        } else if (this.history.size() == 2) { // TODO: better check
            this.notifyListeners(PathEvent.PATH_START);
            this.started = true;
        }
    }

    public StrongholdGenerator.Start getStart() {
        return start;
    }

    public Iterator<StrongholdPathEntry> iterator() {
        return history.iterator();
    }

    public StrongholdPathEntry getLatest() {
        if (this.history.size() >= 1) {
            return history.get(history.size() - 1);
        } else {
            return null;
        }
    }

    public List<StrongholdPathEntry> getHistory() {
        return history;
    }

    public StrongholdPathEntry getPrecedingEntry(StrongholdPathEntry entry) {
        if (this.history.contains(entry) && this.history.indexOf(entry) - 1 >= 0) {
            return this.history.get(this.history.indexOf(entry) - 1);
        } else {
            return null;
        }
    }

    public Map<Class<? extends StrongholdGenerator.Piece>, Boolean> getExtraTasks() {
        return winConditions;
    }

    public StrongholdPathEntry getNextEntry(StrongholdPathEntry entry) {
        if (this.history.contains(entry) && this.history.indexOf(entry) + 1 < this.history.size()) {
            return this.history.get(this.history.indexOf(entry) + 1);
        } else {
            return null;
        }
    }

    public int getTotalTime() {
        if(history.size() == 0){
            return 0;
        }
        return history.stream()
                .skip(history.get(0).getCurrentPiece() instanceof StrongholdGenerator.Start ? 1 : 0) // Skip initial starter for timing.
//                .filter(entry -> !(entry.getCurrentPiece() instanceof StrongholdGenerator.PortalRoom))
                .map(StrongholdPathEntry::getTicksSpentInPiece)
                .mapToInt(AtomicInteger::get)
                .sum() + this.getTicksOutside();
    }

    public void tickOutside() {
        if (!this.started) {
            this.started = true;
            this.notifyListeners(PathEvent.PATH_START);
        }
        notifyListeners(PathEvent.OUTSIDE_TICK);
        ticksOutside.incrementAndGet();
    }

    public int getTicksOutside() {
        return ticksOutside.get();
    }

    public void addListener(@Nullable StrongholdPathListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(StrongholdPathListener listener) {
        this.listeners.remove(listener);
    }

    public void notifyListeners(PathEvent event) {
        if (this.listeners.size() == 0) {
            LOGGER.warn("No listeners listening to " + this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode()) + "!");
            return;
        }
        listeners.forEach(listener1 -> listener1.update(event));
    }

    public boolean isFinished() {
        return finished;
    }

    public void tickLatest() {
        if(this.isFinished()){
            return;
        }
        this.getLatest().incrementTicks();
        this.notifyListeners(PathEvent.PATH_TICK);
    }

    @Override
    public String toString() {
        return "StrongholdPath{" +
                "start=" + start +
                ", history=" + history +
                '}';
    }

    public List<StrongholdPathListener> getListeners() {
        return listeners;
    }

    public enum PathEvent {
        PATH_COMPLETE,
        PATH_START,
        PATH_UPDATE,
        OUTSIDE_TICK,
        PATH_TICK
    }
}
