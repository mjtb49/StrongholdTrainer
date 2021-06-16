package io.github.mjtb49.strongholdtrainer.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringRenderable;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldListWidget.Entry.class)
public class MixinEntry {
    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private LevelSummary level;

    @Shadow @Final private SelectWorldScreen screen;

    @Shadow @Final @Nullable private NativeImageBackedTexture icon;

    @Shadow @Final private Identifier iconLocation;

    private final static Identifier UNKNOWN = new Identifier("textures/misc/unknown_server.png");

    private final static Identifier WORLD_SELECTION_ICONS =  new Identifier("textures/gui/world_selection.png");


    @Inject(at=@At("TAIL"), method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIIIIIIZF)V")
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci){
        if(hovered || this.client.options.touchscreen){
            if(!this.level.getFile().toPath().getParent().resolve("strongholds/").toFile().exists()){
                // re-render world icon
                this.client.getTextureManager().bindTexture(this.icon != null ? this.iconLocation : UNKNOWN);
                RenderSystem.enableBlend();
                DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
                RenderSystem.disableBlend();
                this.client.getTextureManager().bindTexture(WORLD_SELECTION_ICONS);
                // Offset play icon
                DrawableHelper.drawTexture(matrices, x, y, 32.0F, hovered ? 32 : 0, 32, 32, 256, 256);
                // Red exclamation
                DrawableHelper.drawTexture(matrices, x, y, 96.0f, hovered ? 32: 0, 32, 32, 256, 256);
                if (hovered) {
                    StringRenderable stringRenderable = (new LiteralText("This world was not created with StrongholdTrainer. Open at your own risk!")).formatted(Formatting.RED, Formatting.BOLD);
                    this.screen.setTooltip(this.client.textRenderer.wrapLines(stringRenderable, 175));
                }
            }
        }
    }

}
