package io.github.mjtb49.strongholdtrainer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Inject(method = "onGameJoin", at = @At(value = "TAIL"))
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(new LiteralText("Loaded Stronghold Trainer (use /newStronghold to go to a new stronghold, /stinfo for more help)").formatted(Formatting.AQUA), false);
        MinecraftClient.getInstance().getServer().getCommandManager().execute(MinecraftClient.getInstance().player.getCommandSource().withLevel(2), "/tp @s 4 ~ 4");
    }
}

