package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class STInfoCommand {
    // Look into maybe reading from files?

    private static final ModContainer container = (ModContainer) FabricLoader.getInstance().getModContainer(StrongholdTrainer.modID).get();

    private static final String authors;
    static {
        StringBuilder builder = new StringBuilder("- ");
        List<Person> people = new ArrayList<>(container.getMetadata().getAuthors());
        for(int i = 0; i < people.size() - 1; i++){
            Person person = people.get(i);
            builder.append(person.getName())
            .append(", ");
        }
        builder.append("and ")
        .append(people.get(people.size() - 1).getName());
        authors = builder.toString();
    }

    public final static Text GITHUB_LINK = new LiteralText("https://github.com/mjtb49/StrongholdTrainer/").styled((style ->
            style.withBold(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/mjtb49/StrongholdTrainer/"))
                    .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Go to the GitHub repository")))));
    private final static Text[] ABOUT_INFO = new Text[]{
            new LiteralText("\nStrongholdTrainer " + container.getMetadata().getVersion().getFriendlyString()).formatted(Formatting.BOLD, Formatting.YELLOW),
            new LiteralText("This mod is designed to help speedrunners navigate strongholds efficiently. It also has a machine learning component to help the community research stronghold navigation, as well as for fun!"),
            new LiteralText("This is a beta testing version of the mod. Please report bugs by opening an issue on the GitHub repository. ").append(GITHUB_LINK).formatted(Formatting.RED)
    };

    private final static Text[] CREDITS_INFO = new Text[]{
            new LiteralText("\nCredits").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("Mod Authors - ").formatted().append(new LiteralText(authors).formatted(Formatting.RESET)),
    };

    private final static Text[] LICENSE_INFO = new Text[]{
            new LiteralText("\nLicense Info").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("© 2021 Matthew Bolan, All rights reserved.").formatted(Formatting.YELLOW),
            new LiteralText("This mod and its source code are licensed under the MIT license.\n" +
                    "See the full license at ").formatted(Formatting.BLUE).append(new LiteralText("https://github.com/mjtb49/StrongholdTrainer/blob/master/LICENSE").styled((style ->
                    style.withFormatting(Formatting.UNDERLINE).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/mjtb49/StrongholdTrainer/blob/master/LICENSE"))
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Go to license on GitHub")))))),
            new LiteralText("This mod includes TensorFlow for Java (© 2021 The TensorFlow Authors) which is licensed under the Apache 2.0 license, available at ").formatted(Formatting.GOLD)
                    .append(new LiteralText("https://www.apache.org/licenses/LICENSE-2.0").styled((style ->
                    style.withFormatting(Formatting.UNDERLINE).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.apache.org/licenses/LICENSE-2.0"))
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Go to the Apache 2.0 license")))))
                    .append(new LiteralText("\n Find the library here: https://www.github.com/tensorflow/java").formatted(Formatting.UNDERLINE).styled(
                            style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.github.com/tensorflow/java")).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Go to the TensorFlow Java repository."))))))
    };

    private final static Text[] HINTS_INFO = new Text[]{
            new LiteralText("\nHelp\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("Hints").formatted(Formatting.BOLD),
            new LiteralText("Hints will show you all of the entrances and exits in a stronghold room via an outline around the entrance.")
                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/hints"))
                    .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("/hints")))),
            new LiteralText("• Blue hints indicate the perfect path to the portal room.").formatted(Formatting.BLUE),
            new LiteralText("• Green hints indicate the best judgement of the nav model.").formatted(Formatting.GREEN),
            new LiteralText("   ◦ The numbers in doorways indicate the weight that the model gives to that entrance. General rule is higher = better").formatted(Formatting.GREEN, Formatting.ITALIC),
            new LiteralText("• The yellow hints indicate the backtracking path.").formatted(Formatting.YELLOW),
            new LiteralText("======"),
            new LiteralText("Traces").formatted(Formatting.BOLD),
            new LiteralText("The player trace shows the exact path you took during a stronghold run. This will only display when hints are on.").formatted(Formatting.RED),
            new LiteralText("======"),
            new LiteralText("Scuffed Strongholds").formatted(Formatting.BOLD),
            new LiteralText("Disabling 'Allow Scuffed' will disable cave, ravine, and mineshaft generation."),
            new LiteralText("======"),
            new LiteralText("Custom Inventory").formatted(Formatting.BOLD),
            new LiteralText("You can save your current inventory using /inventory save. Then if 'Custom Inventory' is enabled, any time you go to a new stronghold your inventory will be cleared and this one will be loaded.")
    };

    private static final Text[] MODEL_INFO = new Text[]{
            new LiteralText("\nModel\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("This mod includes machine learning models for navigating strongholds." +
                    " (If you're curious, the models in SavedModel format are stored in <gamedir>/config/stronghold-trainer).\n" +
                    "This model takes data about the room and its children and gives a weight to each entrance."),
            new LiteralText("Support for external models may come soon").formatted(Formatting.RED, Formatting.BOLD)
    };

    private static final Text[] STATS_INFO = new Text[]{
            new LiteralText("\nStatistics & Timing\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("One of the main functions of the mod is to record and analyze stronghold runs and provide feedback."),
            new LiteralText("Timing").formatted(Formatting.BOLD),
            new LiteralText("The timer above the hotbar and the times recorded in your player stats are IGT. We also provide an RT (including pauses!) time at the end of a run."),
            new LiteralText("There are three hotbar timers. The leftmost is the total time spent outside of the stronghold, middle is total run time, rightmost is the time spend in the current room. The +/=/- are comparing against Feinberg's average time in that room."),
            new LiteralText("======"),
            new LiteralText("Statistics").formatted(Formatting.BOLD),
            new LiteralText("• Time Loss/Gain Against Feinberg: The sum of your time in each room against Feinberg's average time there.").formatted(Formatting.GREEN),
            new LiteralText("• Estimated Difficulty: This a measure of how difficult Geo's model thinks the stronghold is to route. With some exceptions, < 10 is very easy, 10-50 is medium/easy, 50+ is bad RNG.").formatted(Formatting.DARK_GREEN),
            new LiteralText("• Number of decisions: The number of decisions you made.").formatted(Formatting.DARK_GREEN),
            new LiteralText("• Best Moves: Any perfect decision that goes deeper into the stronghold (no backtracking).").formatted(Formatting.GOLD),
            new LiteralText("? Inaccuracies: Mildly bad/incorrect decisions that cost a bit of time.").formatted(Formatting.YELLOW),
            new LiteralText("⁈ Mistakes: Bad decisions that were pretty easy to avoid and/or led to a notable time loss.").formatted(Formatting.RED),
            new LiteralText("⁇ Blunders: Terrible decisions that were obviously terrible and/or led to a disastrous time loss.").formatted(Formatting.DARK_RED),
            new LiteralText("• Wormholes: Moves that weren't moving between two adjacent rooms (ocean travel, SmallCorridors).").formatted(Formatting.DARK_PURPLE),
            new LiteralText("• Review: Allows you to review your mistakes.").formatted(Formatting.LIGHT_PURPLE)
    };
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(
                literal("stinfo").then(
                        literal("about").executes(c ->{
                            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                            if(playerEntity == null){
                                return -1;
                            }

                            for(Text t : ABOUT_INFO){
                                playerEntity.sendMessage(t, false);
                            }
                            return 0;
                        })
                ).then(
                        literal("help").executes(c -> {
                            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                            if(playerEntity == null){
                                return -1;
                            }
                            for(Text t : HINTS_INFO){
                                playerEntity.sendMessage(t, false);
                            }
                            return 0;
                        })
                ).then(
                        literal("credits").executes(c -> {
                            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                            if(playerEntity == null){
                                return -1;
                            }
                            for(Text t : CREDITS_INFO){
                                playerEntity.sendMessage(t, false);
                            }
                            return 0;
                        })
                ).then(
                        literal("licenses").executes(c -> {
                            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                            if(playerEntity == null){
                                return -1;
                            }
                            for(Text t : LICENSE_INFO){
                                playerEntity.sendMessage(t, false);
                            }
                            return 0;
                        })
                ).then(
                        literal("model").executes(c -> {
                            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                            if(playerEntity == null){
                                return -1;
                            }
                            for(Text t : MODEL_INFO){
                                playerEntity.sendMessage(t, false);
                            }
                            return 0;
                        })
                ).then(
                        literal("stats").executes(c -> {
                            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                            if(playerEntity == null){
                                return -1;
                            }
                            for(Text t : STATS_INFO){
                                playerEntity.sendMessage(t, false);
                            }
                            return 0;
                        })
                )
        );
    }
}
