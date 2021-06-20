package io.github.mjtb49.strongholdtrainer.commands;

import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.strongholdtrainer.api.MinecraftServerAccessor;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DoorLabelCommand extends BooleanOptionCommand {

    public DoorLabelCommand() {
        super(OptionTracker.Option.DOOR_LABELS);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal(optionID.id).then(
                        argument(optionID.id, bool()).executes(
                                c -> {
                                    this.setOption(new JsonPrimitive(getBool(c, optionID.id)));
                                    c.getSource().getPlayer().sendMessage(new LiteralText(this.optionID + " is now " + this.getOption()), false);
                                    ((MinecraftServerAccessor) c.getSource().getMinecraftServer()).refreshRooms();
                                    return 1;
                                }
                        )
                ).executes(
                        c -> {
                            boolean newValue = !this.getOption().getAsBoolean();
                            this.setOption(newValue);
                            c.getSource().getPlayer().sendMessage(new LiteralText(optionID + " is now " + newValue), false);
                            ((MinecraftServerAccessor) c.getSource().getMinecraftServer()).refreshRooms();
                            return 1;
                        }
                )
        );
    }

    private void setOption(boolean value){
        super.setOption(new JsonPrimitive(value));
    }
}
