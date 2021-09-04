package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow
    private MinecraftClient client;


    @Inject(method = "onGameJoin", at = @At(value = "TAIL"))
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        assert MinecraftClient.getInstance().player != null;
        client.player.sendMessage(new LiteralText("Loaded Stronghold Trainer (use /newStronghold to go to a new stronghold, /stinfo help for more help). For licenses and attribution, do /stinfo licenses").formatted(Formatting.AQUA), false);
        client.getServer().getCommandManager().execute(client.getServer().getCommandSource(), "/stinfo licenses");
        Optional<? extends StructureStart<?>> start = client.getServer().getWorld(World.OVERWORLD).getStructureAccessor().getStructuresWithChildren(ChunkSectionPos.from(0, 0, 0), StructureFeature.STRONGHOLD).findFirst();
        start.ifPresent(structureStart -> client.getServer().getCommandManager().execute(client.getServer().getCommandSource(), "/tp @p 4 " + (((StartAccessor) structureStart).getStart().getBoundingBox().getCenter().getY() - 4) + " 4"));
    }

}

