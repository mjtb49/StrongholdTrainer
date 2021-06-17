package io.github.mjtb49.strongholdtrainer.mixin;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.strongholdtrainer.commands.*;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
// word()
// literal("foo")
// argument("bar", word())
// Import everything


@Mixin(CommandManager.class)
public abstract class MixinCommandManager {
    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;


    @Inject(method = "<init>(Lnet/minecraft/server/command/CommandManager$RegistrationEnvironment;)V", at = @At("RETURN"))
    public void CommandManager(CommandManager.RegistrationEnvironment environment, CallbackInfo ci) {
        NewStrongholdCommand.register(dispatcher);
        NextMistakeCommand.register(dispatcher);
        new BooleanOptionCommand(OptionTracker.Option.HINTS).register(dispatcher);
        new BooleanOptionCommand(OptionTracker.Option.DOOR_LABELS).register(dispatcher);
        new BooleanOptionCommand(OptionTracker.Option.TRACE).register(dispatcher);
        new BooleanOptionCommand(OptionTracker.Option.ALLOW_SCUFFED).register(dispatcher);
        ModelCommand.register(dispatcher);
        STInfoCommand.register(dispatcher);
    }
}
