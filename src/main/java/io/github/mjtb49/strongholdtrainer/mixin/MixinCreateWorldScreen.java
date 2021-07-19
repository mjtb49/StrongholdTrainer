package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.gui.SeedReviewScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(CreateWorldScreen.class)
public abstract class MixinCreateWorldScreen {

    @Shadow
    protected DataPackSettings field_25479;
    @Shadow
    private GameRules gameRules;

    @Shadow
    protected abstract <T extends AbstractButtonWidget> T addButton(T button);

    @Inject(at = @At("TAIL"), method = "init()V")
    public void init(CallbackInfo ci) {
        int x = ((Screen) (Object) this).width / 2;
        int height = ((Screen) (Object) this).height;
        MinecraftClient client = MinecraftClient.getInstance();
        // TODO: make this work with small resolutions
        this.addButton(new ButtonWidget(x - 100, ((Screen)(Object) this).height - 56, 200, 20, new LiteralText("Review Stronghold From Seed").formatted(Formatting.BOLD),
                button -> client
                        .openScreen(
                                new SeedReviewScreen(
                                        ((CreateWorldScreen) (Object) this),
                                        new LevelInfo(UUID.randomUUID().toString(),
                                                GameMode.CREATIVE,
                                                true, Difficulty.EASY,
                                                false, this.gameRules,
                                                this.field_25479)))));

    }

}
