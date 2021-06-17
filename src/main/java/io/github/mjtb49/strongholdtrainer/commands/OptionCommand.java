package io.github.mjtb49.strongholdtrainer.commands;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public abstract class OptionCommand {
    public final String optionID;
    private final JsonElement defaultValue;

    OptionCommand(String optionID, JsonElement defaultValue){
        this.optionID = optionID;
        this.defaultValue = defaultValue;
    }

    abstract public void register(CommandDispatcher<ServerCommandSource> dispatcher);

    // get the argument from the command line
    abstract protected JsonElement getArgument(CommandContext<ServerCommandSource> c);

    protected void setOption(CommandContext<ServerCommandSource> c){
        this.setOption(getArgument(c));
    }

    protected void setOption(){
        setOption(defaultValue);
    }

    protected void setOption(JsonElement value){
        if(value.equals(defaultValue)){
            StrongholdTrainer.markDefault(optionID);
        }
        StrongholdTrainer.setOption(optionID, value);
    }

    protected JsonElement getOption(){
        JsonElement option = StrongholdTrainer.getOption(optionID);
        if(option == null){
            return defaultValue;
        }
        return option;
    }
}
