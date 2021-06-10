package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NextMistakeCommand {
    private static ArrayList<StrongholdGenerator.Piece> mistakes;
    private static ArrayList<StrongholdGenerator.Piece> inaccuracies;
    private static int numMistakesReviewed;

    public static void submitMistakesAndInaccuracies(ArrayList<StrongholdGenerator.Piece> mistakes, ArrayList<StrongholdGenerator.Piece> inaccuracies) {
        NextMistakeCommand.mistakes = mistakes;
        NextMistakeCommand.inaccuracies = inaccuracies;
        numMistakesReviewed = 0;
    }

    private static void teleportPlayerToNextMistake(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        Vec3i pos = null;
        if (mistakes != null && mistakes.size() > 0) {
            numMistakesReviewed++;
            pos = mistakes.remove(0).getBoundingBox().getCenter();
        } else if (inaccuracies != null && inaccuracies.size() > 0) {
            pos = inaccuracies.remove(0).getBoundingBox().getCenter();
        }
        //TODO, orient the player here
        if (pos != null) {
            c.getSource().getPlayer().teleport(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public static void sendInitialMessage(ServerPlayerEntity playerEntity) {
        if (mistakes != null && mistakes.size() > 0) {
            playerEntity.sendMessage(new LiteralText("Review Mistakes?").styled(
                    (style) -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nextMistake")).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next mistake")))),false);

        } else if (inaccuracies != null && inaccuracies.size() > 0) {
            playerEntity.sendMessage(new LiteralText("Review Inaccuracies?").styled(
                    (style) -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nextMistake"))
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next inaccuracy")))),false);

        } else {
            playerEntity.sendMessage(new LiteralText("No significant errors to review! ").formatted(Formatting.GREEN), false);
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
               literal("nextMistake").executes( c -> {
                   StrongholdTrainer.setOption("isReviewing", true);
                   if ((mistakes == null || mistakes.size() == 0) && (inaccuracies == null || inaccuracies.size() == 0))
                       c.getSource().getPlayer().sendMessage(new LiteralText("Nothing left to review!").formatted(Formatting.GREEN), false);
                   else  if (mistakes != null && mistakes.size() == 0 && numMistakesReviewed > 0) {
                       teleportPlayerToNextMistake(c);
                       c.getSource().getPlayer().sendMessage(new LiteralText("Now reviewing Inaccuracies").styled(
                               (style) -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nextMistake")).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next inaccuracy")))),false);
                   } else {
                       teleportPlayerToNextMistake(c);
                   }
                   return 1;
               })
        );
    }
}