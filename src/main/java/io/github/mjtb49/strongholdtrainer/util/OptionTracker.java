package io.github.mjtb49.strongholdtrainer.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.Properties;

public class OptionTracker {

    public enum Options {
        TRACE,
        HINTS,
        DOOR_LABELS,
        ALLOW_SCUFFED;
    }

    private static final Path OPTIONS_PATH = FabricLoader.getInstance().getGameDir().resolve("strongholdOptions.txt");
    static Properties DEFAULTS;
    static Properties OPTIONS;

    public static void init() {
        DEFAULTS = new Properties();
        DEFAULTS.put(Options.TRACE, true);
        DEFAULTS.put(Options.HINTS, true);
        DEFAULTS.put(Options.DOOR_LABELS, false);
        DEFAULTS.put(Options.ALLOW_SCUFFED, true);
        try {
            BufferedReader br = new BufferedReader(new FileReader(String.valueOf(OPTIONS_PATH)));
            OPTIONS.load(br);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            OPTIONS = new Properties(DEFAULTS);
        }
    }

    public static void writeOptions() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(String.valueOf(OPTIONS_PATH)));
            OPTIONS.store(bw,"");
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
