package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.StrongholdTrainer;
import io.github.mjtb49.strongholdtrainer.api.EntranceAccessor;
import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdRoomClassifier;
import io.github.mjtb49.strongholdtrainer.render.Color;
import io.github.mjtb49.strongholdtrainer.render.Cuboid;
import io.github.mjtb49.strongholdtrainer.util.EntryNode;
import io.github.mjtb49.strongholdtrainer.util.StrongholdSearcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Shadow private PlayerManager playerManager;
    private StructurePiece lastpiece = null;

    @Inject(method = "tick", at = @At("HEAD"))
    private void inject(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        for (ServerPlayerEntity player : this.playerManager.getPlayerList()) {
            ServerWorld world = player.getServerWorld();

            StructureStart<?> start = world.getStructureAccessor().method_28388(player.getBlockPos(), true, StructureFeature.STRONGHOLD);

            if (start != StructureStart.DEFAULT) {
                StrongholdGenerator.Start strongholdStart = ((StartAccessor)start).getStart();

                for (StructurePiece piece : start.getChildren()) {
                    int yOffset = ((StartAccessor)start).getYOffset();

                    if (piece.getBoundingBox().contains(player.getBlockPos())) {
                        if (lastpiece != piece) {
                            lastpiece = piece;
                            double[] policy = StrongholdRoomClassifier.getPredictions(((StartAccessor) start).getStart(), (StrongholdGenerator.Piece) piece);
                            DecimalFormat df = new DecimalFormat("0.00");
                            StringBuilder s = new StringBuilder();
                            Arrays.stream(policy).forEach(e -> s.append(df.format(e)).append(" "));
                            MinecraftClient.getInstance().player.sendMessage(new LiteralText(s.toString()).formatted(Formatting.YELLOW), false);
                        }
                        Cuboid cuboid = new Cuboid(piece.getBoundingBox(), Color.PURPLE);

                        StrongholdTrainer.submitRoom(cuboid);
                        // TODO: good idea to cache this, doesn't seem to hurt perf too much
                        StructurePiece searchResult = StrongholdSearcher.search(((StrongholdTreeAccessor)strongholdStart).getTree(), piece);

                        for (EntryNode node : ((EntranceAccessor) piece).getEntrances()) {
                            BlockBox entrance = node.box;

                            BlockBox newBox = new BlockBox(entrance.minX, entrance.minY + yOffset, entrance.minZ, entrance.maxX - 1, entrance.maxY + yOffset - 1, entrance.maxZ - 1);

                            Color color = node.type == EntryNode.Type.FORWARDS ? Color.ORANGE : Color.YELLOW;

                            if (searchResult == null) {
                                if (node.type == EntryNode.Type.BACKWARDS) {
                                    color = Color.BLUE;
                                }
                            } else if (node.type == EntryNode.Type.FORWARDS && node.pointer != null && searchResult == node.pointer) {
                                color = Color.BLUE;
                            }

                            StrongholdTrainer.submitRoom(new Cuboid(newBox, color));
                        }
                    }
                }
            }
            //if (start != StructureStart.DEFAULT) {
            //    System.out.println("In stronghold");
            //}
        }
    }
}
