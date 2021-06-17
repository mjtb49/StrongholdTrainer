package io.github.mjtb49.strongholdtrainer.util;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class OptionTracker {

    public enum Option {
        TRACE("trace"),
        HINTS("hints"),
        DOOR_LABELS("doorLabels"),
        ALLOW_SCUFFED("allowScuffed");

        private static final HashMap<String, Option> strToOption = new HashMap<>();
        static {
            for(Option option : Option.values()){
                strToOption.put(option.id, option);
            }
        }

        public static Option getOption(String str){
            return strToOption.get(str);
        }

        public String id;

        Option(String id) {
            this.id = id;
        }
    }

    private static final Path OPTIONS_PATH = FabricLoader.getInstance().getGameDir().resolve("strongholdOptions.json");

    private static final Map<Option, Boolean> DEFAULT = new EnumMap<>(Option.class);
    private static final Map<Option, JsonElement> OPTIONS = new EnumMap<>(Option.class);

    private static final Gson gson = new Gson();
    private static final JsonParser parser = new JsonParser();

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
                DEFAULT.put(option, false);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeOptions() {
        JsonObject obj = new JsonObject();
        for(Map.Entry<Option, JsonElement> option : OPTIONS.entrySet()){
            if(!DEFAULT.containsKey(option.getKey()) || !DEFAULT.get(option.getKey())){
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
        OPTIONS.put(optionID, value);
    }

    public static void markDefault(Option optionID, boolean isDefault){
        DEFAULT.put(optionID, isDefault);
    }

    public static JsonElement getOption(Option optionID){
        if(OPTIONS.containsKey(optionID)){
            return OPTIONS.get(optionID);
        }
        return null;
    }

    public static boolean getBoolean(Option optionID){
        JsonElement element = getOption(optionID);
        if(element == null){
            return false;
        }
        return element.getAsBoolean();
    }

}
