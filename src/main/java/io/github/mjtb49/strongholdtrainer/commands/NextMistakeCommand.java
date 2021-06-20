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
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.literal;



public class NextMistakeCommand {
    private static ArrayList<StrongholdGenerator.Piece> mistakes;
    private static ArrayList<StrongholdGenerator.Piece> inaccuracies;
    private static int numMistakesReviewed;
    private final static String command = "⨒strongholdTrainer#nextMistake";

    public static void submitMistakesAndInaccuracies(ArrayList<StrongholdGenerator.Piece> mistakes, ArrayList<StrongholdGenerator.Piece> inaccuracies) {
        NextMistakeCommand.mistakes = mistakes;
        NextMistakeCommand.inaccuracies = inaccuracies;
        numMistakesReviewed = 0;
    }

    private static void teleportPlayerToNextMistake(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        Vec3i pos = null;
        float yaw = 0;
        if (mistakes != null && mistakes.size() > 0) {
            numMistakesReviewed++;
            StrongholdGenerator.Piece piece = mistakes.remove(0);
            pos = piece.getBoundingBox().getCenter();

            switch (Objects.requireNonNull((piece.getFacing()))) {
                case NORTH:
                    yaw = 180;
                    break;
                case SOUTH:
                    yaw = 0;
                    break;
                case WEST:
                    yaw = 90;
                    break;
                case EAST:
                    yaw = -90;
                    break;
            }
        } else if (inaccuracies != null && inaccuracies.size() > 0) {
            pos = inaccuracies.remove(0).getBoundingBox().getCenter();
        }

        if (pos != null) {
            c.getSource().getPlayer().teleport(c.getSource().getWorld(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        }
    }

    public static void sendInitialMessage(ServerPlayerEntity playerEntity) {
        if (mistakes != null && mistakes.size() > 0) {
            playerEntity.sendMessage(new LiteralText("Review Mistakes?").styled(
                    (style) -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next mistake")))),false);

        } else if (inaccuracies != null && inaccuracies.size() > 0) {
            playerEntity.sendMessage(new LiteralText("Review Inaccuracies?").styled(
                    (style) -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command))
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next inaccuracy")))),false);

        } else {
            playerEntity.sendMessage(new LiteralText("No significant errors to review! ").formatted(Formatting.GREEN), false);
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
               literal(command).executes( c -> {
                   StrongholdTrainer.IS_REVIEWING = true;
                   if ((mistakes == null || mistakes.size() == 0) && (inaccuracies == null || inaccuracies.size() == 0))
                       c.getSource().getPlayer().sendMessage(new LiteralText("Nothing left to review!").formatted(Formatting.GREEN), false);
                   else  if (mistakes != null && mistakes.size() == 0 && numMistakesReviewed > 0) {
                       teleportPlayerToNextMistake(c);
                       c.getSource().getPlayer().sendMessage(new LiteralText("Now reviewing Inaccuracies").styled(
                               (style) -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next inaccuracy")))),false);
                   } else {
                       teleportPlayerToNextMistake(c);
                   }
                   return 1;
               })
        );
    }
}
