package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.gui.STOptionsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class MixinOptionsScreen {

    @Inject(at = @At("TAIL"), method = "init()V")
    public void init(CallbackInfo ci) {
        ((ScreenInvoker) this).invokeAddButton(new ButtonWidget(((Screen) (Object) this).width / 2 - 100, ((Screen) (Object) this).height / 6 + 140, 200, 20, new LiteralText("StrongholdTrainer Options..."), button -> MinecraftClient.getInstance().openScreen(new STOptionsScreen(new LiteralText("StrongholdTrainer Settings"), ((Screen) (Object) this)))));
    }
}
