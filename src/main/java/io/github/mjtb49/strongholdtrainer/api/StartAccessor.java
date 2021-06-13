package io.github.mjtb49.strongholdtrainer.api;

import io.github.mjtb49.strongholdtrainer.util.PlayerPath;
import net.minecraft.structure.StrongholdGenerator;

public interface StartAccessor {
    int getYOffset();
    boolean hasBeenRouted();
    void setHasBeenRouted(boolean hasBeenRouted);

    StrongholdGenerator.Start getStart();
}
