package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.StartAccessor;
import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
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

import java.util.Iterator;
import java.util.List;


@Mixin(StrongholdFeature.Start.class)
public abstract class MixinStrongholdFeatureStart extends StructureStart implements StartAccessor {

    @Shadow @Final private long field_24559;
    @Unique
    private int yOffset;

    @Unique
    private StrongholdGenerator.Start start;

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

            ((StrongholdTreeAccessor) start).registerPiece(start);

            this.children.add(start);
            start.placeJigsaw(start, this.children, this.random);
            List list = start.field_15282;

            while(!list.isEmpty()) {
                int l = this.random.nextInt(list.size());
                StructurePiece structurePiece = (StructurePiece)list.remove(l);
                ((StrongholdTreeAccessor) start).registerPiece(structurePiece);
                structurePiece.placeJigsaw(start, this.children, this.random);
            }

            this.setBoundingBoxFromChildren();

            // **
            int k = chunkGenerator.getSeaLevel() - 10;
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
        this.start = start;

        //((StrongholdTreeAccessor) start).printContents();
    }

    @Override
    public int getYOffset() {
        return this.yOffset;
    }

    @Override
    public StrongholdGenerator.Start getStart() {
        return this.start;
    }
}
