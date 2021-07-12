package io.github.mjtb49.strongholdtrainer.ml;

import net.minecraft.structure.StrongholdGenerator;

import java.util.*;

public class StrongholdPath {
    private final StrongholdGenerator.Start start;
    private final List<StrongholdPathEntry> history;

    public StrongholdPath(StrongholdGenerator.Start start){
        this.start = start;
        history = Collections.synchronizedList(new LinkedList<>());
    }

    public void add(StrongholdGenerator.Piece current, StrongholdGenerator.Piece previous){
        this.history.add(new StrongholdPathEntry(current, previous,0));
    }

    public StrongholdGenerator.Start getStart() {
        return start;
    }

    public Iterator<StrongholdPathEntry> iterator(){
        return history.iterator();
    }

    public StrongholdPathEntry getLatest(){
        if(history.size() > 1){
            return history.get(history.size() - 1);
        } else {
            return null;
        }
    }

    public List<StrongholdPathEntry> getHistory() {
        return history;
    }
}
