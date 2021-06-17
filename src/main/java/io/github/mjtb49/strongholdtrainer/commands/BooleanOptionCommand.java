package io.github.mjtb49.strongholdtrainer.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BooleanOptionCommand extends OptionCommand {

    public BooleanOptionCommand(String optionID) {
        this(optionID, false);
    }

    public BooleanOptionCommand(String optionID, boolean defaultValue) {
        super(optionID, new JsonPrimitive(defaultValue));
        setOption();
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal(optionID).then(
                        argument(optionID, bool()).executes(
                                c -> {
                                    this.setOption(c);
                                    c.getSource().getPlayer().sendMessage(new LiteralText(this.optionID + " is now " + this.getOption()), false);
                                    return 1;
                                }
                        )
                ).executes(
                        c -> {
                            boolean newValue = !this.getOption().getAsBoolean();
                            this.setOption(newValue);
                            c.getSource().getPlayer().sendMessage(new LiteralText(optionID + " is now " + newValue), false);
                            return 1;
                        }
                )
        );
    }

    @Override
    protected JsonElement getArgument(CommandContext<ServerCommandSource> c) {
        return new JsonPrimitive(getBool(c, optionID));
    }

    private void setOption(boolean value){
        super.setOption(new JsonPrimitive(value));
    }
}
