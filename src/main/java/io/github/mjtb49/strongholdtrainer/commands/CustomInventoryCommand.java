package io.github.mjtb49.strongholdtrainer.commands;

import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.strongholdtrainer.util.InventoryHelper;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.io.File;
import java.io.IOException;

import static net.minecraft.server.command.CommandManager.literal;
public class CustomInventoryCommand {
    private static final File SAVED_INVENTORY = new File(FabricLoader.getInstance().getGameDir().resolve("saved_inventory.nbt").toString());

    public static void register(CommandDispatcher<ServerCommandSource> sourceCommandDispatcher) {
        sourceCommandDispatcher.register(
                literal("inventory").then(
                        literal("enable").executes(context -> {
                            OptionTracker.setOption(OptionTracker.Option.CUSTOM_INVENTORY, new JsonPrimitive(true));
                            context.getSource().getPlayer().sendMessage(new LiteralText("Custom inventory enabled"), false);
                            return 0;
                })).then(
                        literal("disable").executes(context -> {
                            OptionTracker.setOption(OptionTracker.Option.CUSTOM_INVENTORY, new JsonPrimitive(false));
                            context.getSource().getPlayer().sendMessage(new LiteralText("Custom inventory disabled"), false);
                            return 0;
                })).then(
                        literal("save").executes(context -> {
                            InventoryHelper.saveInventoryToFile(SAVED_INVENTORY, context.getSource().getPlayer());
                            return 0;
                        })).then(
                        literal("load").executes(context -> {
                            try {
                                InventoryHelper.loadInventoryFromFile(SAVED_INVENTORY, context.getSource().getPlayer());
                                return 0;
                            } catch (IOException e) {
                                e.printStackTrace();
                                return -1;
                            }
                }))
        );
    }
}
