package io.github.mjtb49.strongholdtrainer.path;

public abstract class AbstractPathListener implements StrongholdPathListener{
    StrongholdPath strongholdPath;

    @Override
    public abstract void update(StrongholdPath.PathEvent event);

    @Override
    public void attach(StrongholdPath path) {
        this.strongholdPath = path;
        strongholdPath.addListener(this);
    }

    @Override
    public void detach() {
        this.strongholdPath.removeListener(this);
        this.strongholdPath = null;
    }


}
