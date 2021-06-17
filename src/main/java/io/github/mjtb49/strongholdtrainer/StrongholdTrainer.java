package io.github.mjtb49.strongholdtrainer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdRoomClassifier;
import io.github.mjtb49.strongholdtrainer.render.*;
import io.github.mjtb49.strongholdtrainer.stats.StrongholdTrainerStats;
import net.fabricmc.api.ModInitializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StrongholdTrainer implements ModInitializer  {

    static RendererGroup<Cuboid> cuboidRendererGroup = new RendererGroup<>(1, RendererGroup.RenderOption.RENDER_FRONT);
    static RendererGroup<Cuboid> doorRendererGroup = new RendererGroup<>(6, RendererGroup.RenderOption.RENDER_FRONT);
    static RendererGroup<Line> playerTracerGroup = new RendererGroup<>(20 * 60 * 5, RendererGroup.RenderOption.RENDER_BACK);

    private final static Map<String, JsonElement> OPTIONS = new HashMap<String, JsonElement>(){{
//        put("trace",true);
//        put("hints",true);
//        put("isReviewing", false); //TODO weird to have this in here when its not a command
//        put("doorLabels", false);
//        put("allowScuffed", true);
    }};
    public static boolean ML_DISABLED = false;
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
        StrongholdTrainerStats.register();
        System.out.println(System.getProperty("os.arch"));
        if(!(System.getProperty("os.arch").contains("64") || !System.getProperty("os.arch").contains("arm"))){
            System.out.println(System.getProperty("os.arch") + " not supported. Disabling ML operations.");
            ML_DISABLED = true;
        }
        StrongholdRoomClassifier.init("model2.zip", "rnn.zip", "rnn_4.zip");
        RenderQueue.get().add("hand", matrixStack -> {
            RenderSystem.pushMatrix();
            RenderSystem.multMatrix(matrixStack.peek().getModel());
            GlStateManager.disableTexture();
            GlStateManager.disableDepthTest();
            RenderSystem.defaultBlendFunc();

            if (cuboidRendererGroup != null && (getBoolean("hints") || getBoolean("isReviewing"))) {
                doorRendererGroup.render();
                cuboidRendererGroup.render();
                GlStateManager.enableBlend();
                if (getBoolean("trace"))
                    playerTracerGroup.render();
                GlStateManager.disableBlend();
                TextRenderer.render();
            }

            RenderSystem.popMatrix();

        });
    }

    public static void setOption(String optionID, boolean value){
        setOption(optionID, new JsonPrimitive(value));
    }

    public static void setOption(String optionID, JsonElement value){
        OPTIONS.put(optionID, value);
    }

    public static void markDefault(String optionID){
//        OPTIONS.remove(optionID);
    }

    public static JsonElement getOption(String optionID){
        if(OPTIONS.containsKey(optionID)){
            return OPTIONS.get(optionID);
        }
        return null;
    }

    public static boolean getBoolean(String optionID){
        JsonElement element = getOption(optionID);
        if(element == null){
            return false;
        }
        return element.getAsBoolean();
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
