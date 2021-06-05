package io.github.mjtb49.strongholdtrainer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mjtb49.strongholdtrainer.render.RenderQueue;
import net.fabricmc.api.ModInitializer;

public class StrongholdTrainer implements ModInitializer  {

    @Override
    public void onInitialize() {
        RenderQueue.get().add("hand", matrixStack -> {
            RenderSystem.pushMatrix();
            RenderSystem.multMatrix(matrixStack.peek().getModel());
            GlStateManager.disableTexture();
            GlStateManager.disableDepthTest();


            RenderSystem.popMatrix();

        });
    }
}
