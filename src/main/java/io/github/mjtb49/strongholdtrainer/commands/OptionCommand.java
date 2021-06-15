package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OptionCommand {
    public static void register(String optionID, CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal(optionID).then(
                        argument(optionID, bool()).executes(
                                c -> {
                                    StrongholdTrainer.setOption(optionID, getBool(c, optionID));
                                    c.getSource().getPlayer().sendMessage(new LiteralText(optionID + " is now " + StrongholdTrainer.getOption(optionID)), false);
                                    return 1;
                                }
                        )
                ).executes(
                        c -> {
                            StrongholdTrainer.setOption(optionID, !StrongholdTrainer.getOption(optionID));
                            c.getSource().getPlayer().sendMessage(new LiteralText(optionID + " is now " + StrongholdTrainer.getOption(optionID)), false);
                            return 1;
                        }
                )
        );
    }
}
