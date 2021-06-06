package io.github.mjtb49.strongholdtrainer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mjtb49.strongholdtrainer.render.*;
import net.fabricmc.api.ModInitializer;

public class StrongholdTrainer implements ModInitializer  {

    static RendererGroup<Cuboid> cuboidRendererGroup = new RendererGroup<>(7, RendererGroup.RenderOption.RENDER_FRONT);

    static public void submitRoom(Cuboid cuboid) {
        cuboidRendererGroup.addRenderer(cuboid);
    }

    @Override
    public void onInitialize() {
        RenderQueue.get().add("hand", matrixStack -> {
            RenderSystem.pushMatrix();
            RenderSystem.multMatrix(matrixStack.peek().getModel());
            GlStateManager.disableTexture();
            GlStateManager.disableDepthTest();
            RenderSystem.defaultBlendFunc();

            if (cuboidRendererGroup != null)
                cuboidRendererGroup.render();

            RenderSystem.popMatrix();

        });
    }
}
