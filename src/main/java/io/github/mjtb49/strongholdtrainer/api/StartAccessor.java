package io.github.mjtb49.strongholdtrainer.api;

import net.minecraft.structure.StrongholdGenerator;

public interface StartAccessor {
    int getYOffset();

    StrongholdGenerator.Start getStart();
}
