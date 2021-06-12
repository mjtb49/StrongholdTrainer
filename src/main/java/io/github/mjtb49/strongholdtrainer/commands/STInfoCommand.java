package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class STInfoCommand {
    // Look into maybe reading from files?
    private final static Text[] aboutInfo = new Text[]{
            new LiteralText("\nStrongholdTrainer v1.0\n").formatted(Formatting.BOLD, Formatting.YELLOW),
            new LiteralText("This mod is intended to help speedrunners practice stronghold navigation.\n" +
                    "It's also intended (with the included model) to potentially discover new things or" +
                    "confirm existing strategies.")
    };

    private final static Text[] creditsInfo = new Text[]{
            new LiteralText("\nCredits\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("Mod Development").formatted().append(new LiteralText("- Matthew Bolan and SuperCoder79").formatted(Formatting.RESET)),
            new LiteralText("Model Development").formatted().append(new LiteralText("- Geosquare, Matthew Bolan, XeroOl\n").formatted(Formatting.RESET)),
    };

    private final static Text[] licenseInfo = new Text[]{
            new LiteralText("\nLicense Info\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("This mod is licensed under the MIT license.\n" +
                    "See the full license at https://www.github.com/mjtb49/StrongholdTrainer."),
            new LiteralText("TensorFlow for Java licensed under the Apache 2.0 license.")
    };

    private final static Text[] hintsInfo = new Text[]{
            new LiteralText("\nHelp\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("Hints can be toggled using /hints.\n" +
                    "Hints will show you all of the" +
                    "entrances and exits in a stronghold room " +
                    "via an outline around the entrance."),
            new LiteralText("• Blue hints indicate the perfect path to the portal room.").formatted(Formatting.BLUE),
            new LiteralText("• Green hints indicate the best judgement of the nav model.").formatted(Formatting.GREEN),
            new LiteralText("   ◦ The numbers in doorways indicate the weight that the model " +
                    "gives to that entrance.").formatted(Formatting.GREEN, Formatting.ITALIC),
            new LiteralText("• The yellow hints indicate the backtracking path.").formatted(Formatting.YELLOW),
            new LiteralText("Player traces can be turned on/off using /trace. " +
                    " The player trace shows the exact path you took during a stronghold run.").formatted(Formatting.RED)
    };

    private static final Text[] modelInfo = new Text[]{
            new LiteralText("\nModel\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("This mod includes a machine learning model for navigating strongholds." +
                    " (If you're curious, the model in SavedModel format is stored in <gamedir>/config/stronghold-trainer).\n" +
                    "This model takes the current room, its children, and the current room, then gives a weight to each exit in the room.")
    };

    private static final Text[] statsInfo = new Text[]{
            new LiteralText("\nStatistics & Timing\n").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.YELLOW),
            new LiteralText("TODO: write-up on stats")
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
