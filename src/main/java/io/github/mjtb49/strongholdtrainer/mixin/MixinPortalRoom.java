package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.minecraft.structure.StrongholdGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(StrongholdGenerator.PortalRoom.class)

public class MixinPortalRoom {
    @ModifyConstant(method = "generate", constant = @Constant(floatValue = 0.9f))
    public float modifyEyeThreshold(float oldThreshold){
        if(OptionTracker.getBoolean(OptionTracker.Option.FILL_PORTAL)){
            return 0.0f;
        } else {
            return oldThreshold;
        }
    }
}
