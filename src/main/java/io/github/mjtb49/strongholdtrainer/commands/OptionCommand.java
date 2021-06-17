package io.github.mjtb49.strongholdtrainer.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OptionCommand {
    public static void register(OptionTracker.Option option, CommandDispatcher<ServerCommandSource> dispatcher) {
        String optionID = option.id;
        dispatcher.register(
                literal(optionID).then(
                        argument(optionID, bool()).executes(
                                c -> {
                                    OptionTracker.setBoolOption(option, getBool(c, optionID));
                                    c.getSource().getPlayer().sendMessage(new LiteralText(optionID + " is now " + OptionTracker.getBoolOption(option)), false);
                                    return 1;
                                }
                        )
                ).executes(
                        c -> {
                            OptionTracker.setBoolOption(option, !OptionTracker.getBoolOption(option));
                            c.getSource().getPlayer().sendMessage(new LiteralText(optionID + " is now " + OptionTracker.getBoolOption(option)), false);
                            return 1;
                        }
                )
        );
    }
}
