package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.OffsetAccessor;
import io.github.mjtb49.strongholdtrainer.api.MixinStrongholdGeneratorStartAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StrongholdFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


@Mixin(StrongholdFeature.Start.class)
public class MixinStrongholdFeatureStart extends StructureStart<DefaultFeatureConfig> {

    @Shadow @Final private long field_24559;
    @Unique
    private int yOffset;

    public MixinStrongholdFeatureStart(StructureFeature<DefaultFeatureConfig> feature, int chunkX, int chunkZ, BlockBox box, int references, long seed, long field_24559) {
        super(feature, chunkX, chunkZ, box, references, seed);
    }

    /**
     * @author SuperCoder79
     * @reason Redirect doesn't work and I can't figure out why
     */
    @Overwrite
    public void init(ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, DefaultFeatureConfig defaultFeatureConfig) {
        int var7 = 0;

        StrongholdGenerator.Start start;
        do {
            this.children.clear();
            this.boundingBox = BlockBox.empty();
            this.random.setCarverSeed(this.field_24559 + (long)(var7++), i, j);
            StrongholdGenerator.init();
            start = new StrongholdGenerator.Start(this.random, (i << 4) + 2, (j << 4) + 2);

            ((MixinStrongholdGeneratorStartAccessor) start).registerPiece(start);

            this.children.add(start);
            start.placeJigsaw(start, this.children, this.random);
            List list = start.field_15282;

            while(!list.isEmpty()) {
                int l = this.random.nextInt(list.size());
                StructurePiece structurePiece = (StructurePiece)list.remove(l);

                ((MixinStrongholdGeneratorStartAccessor) start).registerPiece(structurePiece);
                structurePiece.placeJigsaw(start, this.children, this.random);
            }

            this.setBoundingBoxFromChildren();


            // **
            int k = i - j;
            int l = this.boundingBox.getBlockCountY() + 1;
            if (l < k) {
                l += random.nextInt(k - l);
            }

            int m = l - this.boundingBox.maxY;
            this.boundingBox.offset(0, m, 0);
            Iterator iterator = this.children.iterator();

            while(iterator.hasNext()) {
                StructurePiece structurePiece = (StructurePiece) iterator.next();
                structurePiece.translate(0, m, 0);
            }

            this.yOffset = m;
            // **
        } while(this.children.isEmpty() || start.field_15283 == null);

    }

    @Override
    public int getYOffset() {
        return this.yOffset;
            this.method_14978(chunkGenerator.getSeaLevel(), this.random, 10);
        } while(this.children.isEmpty() || start.field_15283 == null);

        ((MixinStrongholdGeneratorStartAccessor) start).printContents();
    }
}
