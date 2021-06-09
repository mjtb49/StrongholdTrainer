package io.github.mjtb49.strongholdtrainer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdRoomClassifier;
import io.github.mjtb49.strongholdtrainer.render.*;
import net.fabricmc.api.ModInitializer;

public class StrongholdTrainer implements ModInitializer  {

    static RendererGroup<Cuboid> cuboidRendererGroup = new RendererGroup<>(7, RendererGroup.RenderOption.RENDER_FRONT);
    static RendererGroup<Line> playerTracerGroup = new RendererGroup<>(20 * 60 * 5, RendererGroup.RenderOption.RENDER_FRONT);

    static public void submitRoom(Cuboid cuboid) {
        cuboidRendererGroup.addRenderer(cuboid);
    }
    static public void submitPlayerLine(Line line) {
        playerTracerGroup.addRenderer(line);
    }

    private static boolean renderHints = true;

    @Override
    public void onInitialize() {
        StrongholdRoomClassifier.init("model102.keras");
        RenderQueue.get().add("hand", matrixStack -> {
            RenderSystem.pushMatrix();
            RenderSystem.multMatrix(matrixStack.peek().getModel());
            GlStateManager.disableTexture();
            GlStateManager.disableDepthTest();
            RenderSystem.defaultBlendFunc();

            if (cuboidRendererGroup != null && renderHints) {
                cuboidRendererGroup.render();
                GlStateManager.enableDepthTest();
                playerTracerGroup.render();
                GlStateManager.disableDepthTest();
                TextRenderer.render();
            }

            RenderSystem.popMatrix();

        });
    }

    public static void setRenderHints(boolean renderHints) {
       StrongholdTrainer.renderHints = renderHints;
    }

    public static boolean getRenderHints() {
        return renderHints;
    }

    public static void clearAll() {
        cuboidRendererGroup.clear();
    }

}
