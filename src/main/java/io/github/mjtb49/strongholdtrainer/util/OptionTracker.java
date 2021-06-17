package io.github.mjtb49.strongholdtrainer.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.Properties;

public class OptionTracker {

    public enum Option {
        TRACE("trace"),
        HINTS("hints"),
        DOOR_LABELS("doorLabels"),
        ALLOW_SCUFFED("allowScuffed");

        public String id;

        Option(String id) {
            this.id = id;
        }
    }

    private static final Path OPTIONS_PATH = FabricLoader.getInstance().getGameDir().resolve("strongholdOptions.txt");
    static Properties DEFAULTS;
    static Properties OPTIONS;

    public static void init() {
        DEFAULTS = new Properties();
        DEFAULTS.setProperty(Option.TRACE.id, "true");
        DEFAULTS.setProperty(Option.HINTS.id, "true");
        DEFAULTS.setProperty(Option.DOOR_LABELS.id, "false");
        DEFAULTS.setProperty(Option.ALLOW_SCUFFED.id, "true");
        OPTIONS = new Properties(DEFAULTS);
        try {
            BufferedReader br = new BufferedReader(new FileReader(String.valueOf(OPTIONS_PATH)));
            OPTIONS.load(br);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    public static void setBoolOption(Option option, boolean bool) {
        OPTIONS.setProperty(option.id, Boolean.toString(bool));
    }

    public static void setOption(Option option, String value) {
        OPTIONS.setProperty(option.id, value);
    }

    public static boolean getBoolOption(Option option) {
        return Boolean.parseBoolean(OPTIONS.getProperty(option.id));
    }

    public static String getOption(Option option) {
        return OPTIONS.getProperty(option.id);
    }

}
