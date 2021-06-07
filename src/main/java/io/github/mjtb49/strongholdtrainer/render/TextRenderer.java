package io.github.mjtb49.strongholdtrainer.render;

import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TextRenderer {
    private static final List<Entry> TEXTS = Collections.synchronizedList(new ArrayList<>());

    public static void clear() {
        TEXTS.clear();
    }

    public static void add(Vec3d pos, String text) {
        TEXTS.add(new Entry(pos, text, 0.05f));
    }

    public static void add(Vec3d pos, String text, float size) {
        TEXTS.add(new Entry(pos, text, size));
    }

    public static void render() {
        synchronized (TEXTS) {
            for (Entry entry : TEXTS) {
                Vec3d pos = entry.pos;

                DebugRenderer.drawString(entry.text, pos.x, pos.y, pos.z, -1, entry.size, true, 0, true);
            }
        }
    }

    private static class Entry {
        private final Vec3d pos;
        private final String text;
        private final float size;

        private Entry(Vec3d pos, String text, float size) {
            this.pos = pos;
            this.text = text;
            this.size = size;
        }
    }
}
