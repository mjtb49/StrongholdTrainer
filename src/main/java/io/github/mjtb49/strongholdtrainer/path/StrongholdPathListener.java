package io.github.mjtb49.strongholdtrainer.path;

public interface StrongholdPathListener {
    void update(StrongholdPath.PathEvent event);

    void attach(StrongholdPath path);

    void detach();
}
