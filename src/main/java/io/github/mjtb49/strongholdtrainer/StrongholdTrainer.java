package io.github.mjtb49.strongholdtrainer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdMachineLearning;
import io.github.mjtb49.strongholdtrainer.render.*;
import io.github.mjtb49.strongholdtrainer.stats.StrongholdTrainerStats;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StrongholdTrainer implements ModInitializer  {

    public static final String modID = "stronghold-trainer";
    private static final Logger LOGGER = LogManager.getLogger();

    static RendererGroup<Cuboid> cuboidRendererGroup = new RendererGroup<>(1, RendererGroup.RenderOption.RENDER_FRONT);
    static RendererGroup<Cuboid> doorRendererGroup = new RendererGroup<>(6, RendererGroup.RenderOption.RENDER_FRONT);
    static RendererGroup<Line> playerTracerGroup = new RendererGroup<>(20 * 60 * 5, RendererGroup.RenderOption.RENDER_BACK);

    public static boolean ML_DISABLED = false;
    public static boolean IS_REVIEWING = false;

    static public void submitRoom(Cuboid cuboid) {
        cuboidRendererGroup.addRenderer(cuboid);
    }
    static public void submitDoor(Cuboid cuboid) {
        doorRendererGroup.addRenderer(cuboid);
    }
    static public void submitPlayerLine(Line line) {
        playerTracerGroup.addRenderer(line);
    }

    @Override
    public void onInitialize() {
        OptionTracker.init();
        StrongholdTrainerStats.register();

        LOGGER.info("Checking if TensorFlow Java supports this architecture...");
        if(!(System.getProperty("os.arch").contains("64") || !System.getProperty("os.arch").contains("arm"))){
            LOGGER.error(System.getProperty("os.arch") + " not supported! Disabling ML operations.");
            ML_DISABLED = true;
        }
        StrongholdMachineLearning.init("models/model2.zip", "models/rnn.zip", "models/rnn_4.zip", "models/rl_rnn_2.2.zip", "models/rl_rnn_2.3.zip", "models/rl_rnn_3.zip");

        LOGGER.info(System.getProperty("os.arch") + " is supported! Initializing ML... ");
        RenderQueue.get().add("hand", matrixStack -> {
            RenderSystem.pushMatrix();
            RenderSystem.multMatrix(matrixStack.peek().getModel());
            GlStateManager.disableTexture();
            GlStateManager.disableDepthTest();
            RenderSystem.defaultBlendFunc();

            if (cuboidRendererGroup != null && (OptionTracker.getBoolean(OptionTracker.Option.HINTS) || IS_REVIEWING)) {
                doorRendererGroup.render();
                cuboidRendererGroup.render();
                GlStateManager.enableBlend();
                if (OptionTracker.getBoolean(OptionTracker.Option.TRACE))
                    playerTracerGroup.render();
                GlStateManager.disableBlend();
                TextRenderer.render();
            }

            RenderSystem.popMatrix();

        });
    }

    public static void clearAll() {
        cuboidRendererGroup.clear();
        playerTracerGroup.clear();
        doorRendererGroup.clear();
        TextRenderer.clear();
    }

    public static void clearDoors() {
        doorRendererGroup.clear();
    }

}
