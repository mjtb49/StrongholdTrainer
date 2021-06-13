package io.github.mjtb49.strongholdtrainer.mixin;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.strongholdtrainer.commands.ModelCommand;
import io.github.mjtb49.strongholdtrainer.commands.NewStrongholdCommand;
import io.github.mjtb49.strongholdtrainer.commands.NextMistakeCommand;
import io.github.mjtb49.strongholdtrainer.commands.OptionCommand;
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
        OptionCommand.register("hints", dispatcher);
        OptionCommand.register("trace", dispatcher);
        ModelCommand.register(dispatcher);
    }
}
