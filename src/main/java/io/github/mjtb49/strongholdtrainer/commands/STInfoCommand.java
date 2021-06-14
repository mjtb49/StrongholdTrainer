package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class STInfoCommand {
    // Look into maybe reading from files?

    public final static Text GITHUB_LINK = new LiteralText(" https://github.com/mjtb49/StrongholdTrainer/").styled((style ->
            style.withBold(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/mjtb49/StrongholdTrainer/"))
                    .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Go to the GitHub repository")))));
    private final static Text[] aboutInfo = new Text[]{
            new LiteralText("\nStrongholdTrainer alpha-1.0\n").formatted(Formatting.BOLD, Formatting.YELLOW),
            new LiteralText("This mod is intended to help speedrunners practice stronghold navigation.\n" +
                    "It's also intended (with the included model) to potentially discover new things or " +
                    "confirm existing strategies."),
            new LiteralText("This is an alpha testing version of the mod. Please report bugs by opening an issue on the GitHub repository.").append(GITHUB_LINK).formatted(Formatting.BOLD, Formatting.RED)
    };

    private final static Text[] creditsInfo = new Text[]{
            new LiteralText("\nCredits\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("Mod Development").formatted().append(new LiteralText("- Matthew Bolan, SuperCoder79, fsharpseven, and KaptainWutax").formatted(Formatting.RESET)),
            new LiteralText("\"basic-classifier-nobacktracking\"").formatted().append(new LiteralText("- Geosquare, Matthew Bolan, XeroOl\n").formatted(Formatting.RESET)),
    };

    private final static Text[] licenseInfo = new Text[]{
            new LiteralText("\nLicense Info\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("© 2021 Matthew Bolan, All rights reserved."),
            new LiteralText("This mod and its source code are licensed under the MIT license.\n" +
                    "See the full license at "),
            new LiteralText("https://github.com/mjtb49/StrongholdTrainer/blob/master/LICENSE").styled((style ->
                    style.withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/mjtb49/StrongholdTrainer/blob/master/LICENSE"))
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Go to license on GitHub"))))),
            new LiteralText("TensorFlow for Java licensed under the Apache 2.0 license.")
    };

    private final static Text[] hintsInfo = new Text[]{
            new LiteralText("\nHelp\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("Hints").formatted(Formatting.BOLD),
            new LiteralText("Hints can be toggled using /hints.\n" +
                    "Hints will show you all of the /" +
                    "entrances and exits in a stronghold room " +
                    "via an outline around the entrance."),
            new LiteralText("• Blue hints indicate the perfect path to the portal room.").formatted(Formatting.BLUE),
            new LiteralText("• Green hints indicate the best judgement of the nav model.").formatted(Formatting.GREEN),
            new LiteralText("   ◦ The numbers in doorways indicate the weight that the model " +
                    "gives to that entrance.").formatted(Formatting.GREEN, Formatting.ITALIC),
            new LiteralText("• The yellow hints indicate the backtracking path.").formatted(Formatting.YELLOW),
            new LiteralText("======"),
            new LiteralText("Traces").formatted(Formatting.BOLD),
            new LiteralText("Player traces can be turned on/off using /trace. " +
                    " The player trace shows the exact path you took during a stronghold run.").formatted(Formatting.RED),
            new LiteralText("======"),
            new LiteralText("Helpful Commands").formatted(Formatting.BOLD),
            new LiteralText("/doorLabels - adds labels to doors denoting anything with a color indicator")
                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/doorLabels"))
                    .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("/doorLabels")))),
            new LiteralText("/newStronghold - teleports you to a new stronghold").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/doorLabels"))
                    .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("/doorLabels"))))
    };

    private static final Text[] modelInfo = new Text[]{
            new LiteralText("\nModel\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("This mod includes machine learning models for navigating strongholds." +
                    " (If you're curious, the models in SavedModel format are stored in <gamedir>/config/stronghold-trainer).\n" +
                    "This model takes data about the room and its children and gives a weight to each entrance."),
            new LiteralText("Support for external models may come soon").formatted(Formatting.RED, Formatting.BOLD)
    };

    private static final Text[] statsInfo = new Text[]{
            new LiteralText("\nStatistics & Timing\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("TODO: write-up on stats").formatted(Formatting.OBFUSCATED)
    };
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(
                literal("stinfo").then(
                        literal("about").executes(c ->{
                            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                            if(playerEntity == null){
                                return -1;
                            }
                            for(Text t : aboutInfo){
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
                            for(Text t : hintsInfo){
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
                            for(Text t : creditsInfo){
                                playerEntity.sendMessage(t, false);
                            }
                            return 0;
                        })
                ).then(
                        literal("legal").executes(c -> {
                            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
                            if(playerEntity == null){
                                return -1;
                            }
                            for(Text t : licenseInfo){
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
                            for(Text t : modelInfo){
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
                            for(Text t : statsInfo){
                                playerEntity.sendMessage(t, false);
                            }
                            return 0;
                        })
                )
        );
    }
}
