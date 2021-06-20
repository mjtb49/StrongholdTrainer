package io.github.mjtb49.strongholdtrainer.commands;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.minecraft.server.command.ServerCommandSource;

public abstract class OptionCommand {
    public final OptionTracker.Option optionID;

    OptionCommand(OptionTracker.Option optionID){
        this.optionID = optionID;
    }

    abstract public void register(CommandDispatcher<ServerCommandSource> dispatcher);

    protected void setOption(JsonElement value){
        OptionTracker.setOption(optionID, value);
    }

    protected JsonElement getOption(){
        return OptionTracker.getOption(optionID);
    }
}
