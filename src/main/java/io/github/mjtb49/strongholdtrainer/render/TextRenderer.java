package io.github.mjtb49.strongholdtrainer.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TextRenderer {
    private static final Map<Vec3d, String> TEXTS = new ConcurrentHashMap<>();

    public static void clear() {
        TEXTS.clear();
    }

    public static void add(Vec3d pos, String text) {
        TEXTS.put(pos, text);
    }

    public static void render() {
        for (Map.Entry<Vec3d, String> entry : TEXTS.entrySet()) {
            Vec3d pos = entry.getKey();

            DebugRenderer.drawString(entry.getValue(), pos.x, pos.y, pos.z, -1, 0.05f, true, 0, true);
        }

    }
}
