package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.literal;


public class NextMistakeCommand {

    private static final ArrayDeque<Pair<StrongholdGenerator.Piece, DecisionType>> badDecisions = new ArrayDeque<>();
    // this character will render as a box
    private final static String command = "\uffff";

    private final static Text NEXT_INACCURACY = Texts.bracketed(new LiteralText("Next Inaccuracy").formatted(Formatting.YELLOW)).styled(
            (style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next inaccuracy"))));
    private final static Text NEXT_MISTAKE = Texts.bracketed(new LiteralText("Next Mistake").formatted(Formatting.RED)).styled(
            (style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next mistake"))));
    private final static Text NEXT_BLUNDER = Texts.bracketed(new LiteralText("Next Blunder").formatted(Formatting.DARK_RED)).styled(
            (style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next blunder"))));
    private final static Text REVIEW_BLUNDERS = Texts.bracketed(new LiteralText("Review Blunders").formatted(Formatting.DARK_RED)).styled(
            (style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next blunder"))));
    private final static Text REVIEW_MISTAKES = Texts.bracketed(new LiteralText("Review Mistakes").formatted(Formatting.RED)).styled(
            (style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next mistake"))));
    private final static Text REVIEW_INACCURACIES = Texts.bracketed(new LiteralText("Review Inaccuracies").formatted(Formatting.YELLOW)).styled(
            (style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("Review next inaccuracy"))));

    public static void submitMistakesAndInaccuracies(ArrayList<StrongholdGenerator.Piece> mistakes, ArrayList<StrongholdGenerator.Piece> inaccuracies, ArrayList<StrongholdGenerator.Piece> blunders) {
        badDecisions.clear();
        blunders.forEach(piece -> badDecisions.push(new Pair<>(piece, DecisionType.BLUNDER)));
        mistakes.forEach(piece -> badDecisions.push(new Pair<>(piece, DecisionType.MISTAKE)));
        inaccuracies.forEach(piece -> badDecisions.push(new Pair<>(piece, DecisionType.INACCURACY)));
    }

    private static void teleportPlayerToNextMistake(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        Vec3i pos = null;
        float yaw = 0;
        if (badDecisions.size() != 0) {
            StrongholdGenerator.Piece piece = badDecisions.pop().getLeft();
            pos = piece.getBoundingBox().getCenter();
            yaw = angleFromFacing(piece.getFacing());
        }
        if (pos != null) {
            c.getSource().getPlayer().teleport(c.getSource().getWorld(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        }
    }

    public static void sendInitialMessage(ServerPlayerEntity playerEntity) {
        if (badDecisions.size() == 0) {
            playerEntity.sendMessage(new LiteralText("No significant errors to review! ").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC), false);
        } else if (badDecisions.peek() != null) {
            switch (badDecisions.peek().getRight()) {
                case INACCURACY:
                    playerEntity.sendMessage(REVIEW_INACCURACIES, false);
                    break;
                case MISTAKE:
                    playerEntity.sendMessage(REVIEW_MISTAKES, false);
                    break;
                case BLUNDER:
                    playerEntity.sendMessage(REVIEW_BLUNDERS, false);
                    break;
                default:
                    System.err.println("How?");
            }
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
               literal(command).executes( c -> {
                   StrongholdTrainer.IS_REVIEWING = true;
                   if (badDecisions.size() == 0)
                       c.getSource().getPlayer().sendMessage(new LiteralText("Nothing left to review!").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC), false);
                   else if (badDecisions.peek() != null) {
                       switch (badDecisions.peek().getRight()) {
                           case INACCURACY:
                               teleportPlayerToNextMistake(c);
                               c.getSource().getPlayer().sendMessage(NEXT_INACCURACY, false);
                               break;
                           case MISTAKE:
                               teleportPlayerToNextMistake(c);
                               c.getSource().getPlayer().sendMessage(NEXT_MISTAKE, false);
                               break;
                           case BLUNDER:
                               teleportPlayerToNextMistake(c);
                               c.getSource().getPlayer().sendMessage(NEXT_BLUNDER, false);
                               break;
                           default:
                               System.err.println("How?");
                       }
                   }
                   return 1;
               })
        );
    }

    private static int angleFromFacing(Direction direction) {
        switch (Objects.requireNonNull((direction))) {
            case NORTH:
                return 180;
            case SOUTH:
                return 0;
            case WEST:
                return 90;
            case EAST:
                return -90;
            default:
                return 0;
        }
    }

    private enum DecisionType {
        INACCURACY,
        MISTAKE,
        BLUNDER,
        BEST_MOVE
    }


}
