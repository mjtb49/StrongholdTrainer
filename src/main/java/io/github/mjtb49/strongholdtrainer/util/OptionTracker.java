package io.github.mjtb49.strongholdtrainer.util;

import com.google.gson.*;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdMachineLearning;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class OptionTracker {

    public enum Option {
        TRACE("trace", true, "Player Trace", "When hints are on, the trace shows the path you took."),
        HINTS("hints", true, "Hints", "Shows outlines and text around/in exits with perfect travel and ML output."),
        DOOR_LABELS("doorLabels", false, "Door Labels", "When hints are on, shows labels for hints."),
        ALLOW_SCUFFED("allowScuffed", true, "Allow Scuffed", "Allows caves, ravines, etc. to intersect strongholds."),
        CUSTOM_INVENTORY("customInventory", false, "Custom Inventory", "Clears and loads a custom inventory (set with /inventory save) before each run."),
        FILL_PORTAL("fillPortal", false, "Fill Portal", "Fills the end portal. Turn off for pie-ray.\nTip: the end portal will take you to a new stronghold."),
        PRE19_MODE("oldMode", false, "Unprepared Pre-1.9 Mode", "When enabled, a run won't end until you've entered a chest corridor and a library."),
        MODEL("model", new JsonPrimitive(StrongholdMachineLearning.MODEL_REGISTRY.getDefaultModelIdentifier()), "Loaded Model", "The machine learning model used in hints and analysis.");

        private static final HashMap<String, Option> strToOption = new HashMap<>();

        static {
            for (Option option : Option.values()) {
                strToOption.put(option.id, option);
            }
        }

        public final String tooltip;

        public final String id;
        public final JsonElement defaultValue;
        public final String label;
        Option(String id, boolean defaultValue, String s, String tooltip) {
            this(id, new JsonPrimitive(defaultValue), s, tooltip);
        }

        Option(String id, JsonElement defaultValue, String s, String tooltip) {
            this.id = id;
            this.defaultValue = defaultValue;
            this.label = s;
            this.tooltip = tooltip;
        }

        public static Option getOption(String str) {
            return strToOption.get(str);
        }
    }

    private static final Path OPTIONS_PATH = FabricLoader.getInstance().getGameDir().resolve("strongholdOptions.json");

    private static final JsonParser parser = new JsonParser();
    private static final Gson gson = new Gson();

    private static final Map<Option, JsonElement> OPTIONS = new EnumMap<>(Option.class);

    public static void init() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(String.valueOf(OPTIONS_PATH)));
            JsonObject obj = parser.parse(br).getAsJsonObject();
            for(Map.Entry<String, JsonElement> entry : obj.entrySet()){
                Option option = Option.getOption(entry.getKey());
                if(option == null){
                    continue;
                }
                OPTIONS.put(option, entry.getValue());
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeOptions() {
        JsonObject obj = new JsonObject();
        for(Map.Entry<Option, JsonElement> option : OPTIONS.entrySet()){
            if(!option.getKey().defaultValue.equals(option.getValue())){
                obj.add(option.getKey().id, option.getValue());
            }
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(String.valueOf(OPTIONS_PATH)));
            gson.toJson(obj, bw);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setOption(Option optionID, JsonElement value){
        if(optionID.defaultValue.equals(value)){
            OPTIONS.remove(optionID);
        }
        else {
            OPTIONS.put(optionID, value);
        }
    }

    public static JsonElement getOption(Option optionID){
        if(OPTIONS.containsKey(optionID)){
            return OPTIONS.get(optionID);
        }
        return optionID.defaultValue;
    }

    public static boolean getBoolean(Option optionID){
        return getOption(optionID).getAsBoolean();
    }

    public static String getString(Option optionID){
        return getOption(optionID).getAsString();
    }
}
