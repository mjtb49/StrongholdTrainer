package io.github.mjtb49.strongholdtrainer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.gen.feature.StructureFeature;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class StrongholdMapWidget extends AbstractButtonWidget implements Drawable, Element {

    private static final Identifier MAP_BG = new Identifier("textures/map/map_background_checkerboard.png");
    private static final Identifier COMPASS_ICON = new Identifier("textures/item/compass_16.png");
    private static final Identifier EYE_ICON = new Identifier("textures/block/mossy_stone_bricks.png");

    private static final int SCALE = 16;
    private static final int[] RINGS = new int[]{3, 9, 19, 34, 55, 83, 119, 128};
    private static final HashMap<Biome, Color> BIOME_COLORS = new HashMap<>();

    static {
        // Amidst colors :)
        BIOME_COLORS.put(Biomes.OCEAN, Color.decode("#000070"));
        BIOME_COLORS.put(Biomes.PLAINS, Color.decode("#8DB360"));
        BIOME_COLORS.put(Biomes.DESERT, Color.decode("#FA9418"));
        BIOME_COLORS.put(Biomes.MOUNTAINS, Color.decode("#606060"));
        BIOME_COLORS.put(Biomes.FOREST, Color.decode("#056621"));
        BIOME_COLORS.put(Biomes.TAIGA, Color.decode("#0B6659"));
        BIOME_COLORS.put(Biomes.SWAMP, Color.decode("#07F9B2"));
        BIOME_COLORS.put(Biomes.RIVER, Color.decode("#0000FF"));
        BIOME_COLORS.put(Biomes.FROZEN_OCEAN, Color.decode("#7070D6"));
        BIOME_COLORS.put(Biomes.FROZEN_RIVER, Color.decode("#A0A0FF"));
        BIOME_COLORS.put(Biomes.SNOWY_TUNDRA, Color.decode("#FFFFFF"));
        BIOME_COLORS.put(Biomes.SNOWY_MOUNTAINS, Color.decode("#A0A0A0"));
        BIOME_COLORS.put(Biomes.MUSHROOM_FIELDS, Color.decode("#FF00FF"));
        BIOME_COLORS.put(Biomes.MUSHROOM_FIELD_SHORE, Color.decode("#A000FF"));
        BIOME_COLORS.put(Biomes.BEACH, Color.decode("#FADE55"));
        BIOME_COLORS.put(Biomes.DESERT_HILLS, Color.decode("#D25F12"));
        BIOME_COLORS.put(Biomes.WOODED_HILLS, Color.decode("#22551C"));
        BIOME_COLORS.put(Biomes.TAIGA_HILLS, Color.decode("#163933"));
        BIOME_COLORS.put(Biomes.MOUNTAIN_EDGE, Color.decode("#72789A"));
        BIOME_COLORS.put(Biomes.JUNGLE, Color.decode("#537B09"));
        BIOME_COLORS.put(Biomes.JUNGLE_HILLS, Color.decode("#2C4205"));
        BIOME_COLORS.put(Biomes.JUNGLE_EDGE, Color.decode("#628B17"));
        BIOME_COLORS.put(Biomes.DEEP_OCEAN, Color.decode("#000030"));
        BIOME_COLORS.put(Biomes.STONE_SHORE, Color.decode("#A2A284"));
        BIOME_COLORS.put(Biomes.SNOWY_BEACH, Color.decode("#FAF0C0"));
        BIOME_COLORS.put(Biomes.BIRCH_FOREST, Color.decode("#307444"));
        BIOME_COLORS.put(Biomes.BIRCH_FOREST_HILLS, Color.decode("#1F5F32"));
        BIOME_COLORS.put(Biomes.DARK_FOREST, Color.decode("#40511A"));
        BIOME_COLORS.put(Biomes.SNOWY_TAIGA, Color.decode("#31554A"));
        BIOME_COLORS.put(Biomes.SNOWY_TAIGA_HILLS, Color.decode("#243F36"));
        BIOME_COLORS.put(Biomes.GIANT_TREE_TAIGA, Color.decode("#596651"));
        BIOME_COLORS.put(Biomes.GIANT_TREE_TAIGA_HILLS, Color.decode("#454F3E"));
        BIOME_COLORS.put(Biomes.WOODED_MOUNTAINS, Color.decode("#507050"));
        BIOME_COLORS.put(Biomes.SAVANNA, Color.decode("#BDB25F"));
        BIOME_COLORS.put(Biomes.SAVANNA_PLATEAU, Color.decode("#A79D64"));
        BIOME_COLORS.put(Biomes.BADLANDS, Color.decode("#D94515"));
        BIOME_COLORS.put(Biomes.WOODED_BADLANDS_PLATEAU, Color.decode("#B09765"));
        BIOME_COLORS.put(Biomes.BADLANDS_PLATEAU, Color.decode("#CA8C65"));
        BIOME_COLORS.put(Biomes.WARM_OCEAN, Color.decode("#0000AC"));
        BIOME_COLORS.put(Biomes.LUKEWARM_OCEAN, Color.decode("#000090"));
        BIOME_COLORS.put(Biomes.COLD_OCEAN, Color.decode("#202070"));
        BIOME_COLORS.put(Biomes.DEEP_WARM_OCEAN, Color.decode("#000050"));
        BIOME_COLORS.put(Biomes.DEEP_LUKEWARM_OCEAN, Color.decode("#000040"));
        BIOME_COLORS.put(Biomes.DEEP_COLD_OCEAN, Color.decode("#202038"));
        BIOME_COLORS.put(Biomes.DEEP_FROZEN_OCEAN, Color.decode("#404090"));
        BIOME_COLORS.put(Biomes.SUNFLOWER_PLAINS, Color.decode("#B5DB88"));
        BIOME_COLORS.put(Biomes.DESERT_LAKES, Color.decode("#FFBC40"));
        BIOME_COLORS.put(Biomes.GRAVELLY_MOUNTAINS, Color.decode("#888888"));
        BIOME_COLORS.put(Biomes.FLOWER_FOREST, Color.decode("#2D8E49"));
        BIOME_COLORS.put(Biomes.TAIGA_MOUNTAINS, Color.decode("#2D8E49"));
        BIOME_COLORS.put(Biomes.SWAMP_HILLS, Color.decode("#2FFFDA"));
        BIOME_COLORS.put(Biomes.ICE_SPIKES, Color.decode("#B4DCDC"));
        BIOME_COLORS.put(Biomes.MODIFIED_JUNGLE, Color.decode("#7BA331"));
        BIOME_COLORS.put(Biomes.MODIFIED_JUNGLE_EDGE, Color.decode("#8AB33F"));
        BIOME_COLORS.put(Biomes.TALL_BIRCH_FOREST, Color.decode("#589C6C"));
        BIOME_COLORS.put(Biomes.TALL_BIRCH_HILLS, Color.decode("#47875A"));
        BIOME_COLORS.put(Biomes.DARK_FOREST_HILLS, Color.decode("#687942"));
        BIOME_COLORS.put(Biomes.SNOWY_TAIGA_MOUNTAINS, Color.decode("#597D72"));
        BIOME_COLORS.put(Biomes.GIANT_SPRUCE_TAIGA, Color.decode("#818E79"));
        BIOME_COLORS.put(Biomes.GIANT_SPRUCE_TAIGA_HILLS, Color.decode("#6D7766"));
        BIOME_COLORS.put(Biomes.MODIFIED_GRAVELLY_MOUNTAINS, Color.decode("#789878"));
        BIOME_COLORS.put(Biomes.SHATTERED_SAVANNA, Color.decode("#E5DA87"));
        BIOME_COLORS.put(Biomes.SHATTERED_SAVANNA_PLATEAU, Color.decode("#CFC58C"));
        BIOME_COLORS.put(Biomes.ERODED_BADLANDS, Color.decode("#FF6D3D"));
        BIOME_COLORS.put(Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, Color.decode("#D8BF8D"));
        BIOME_COLORS.put(Biomes.MODIFIED_BADLANDS_PLATEAU, Color.decode("#F2B48D"));
        BIOME_COLORS.put(Biomes.BAMBOO_JUNGLE, Color.decode("#768E14"));
        BIOME_COLORS.put(Biomes.BAMBOO_JUNGLE_HILLS, Color.decode("#3B470A"));
    }

    private final List<ChunkPos> strongholds;
    private final int centerX;
    private final int centerY;
    private final HashMap<Pair<Integer, Integer>, ChunkPos> strongholdToIconPosMap;
    private final Identifier mapTexID;
    private final NativeImageBackedTexture backedTexture;
    private final AtomicBoolean textureLock;
    private ChunkPos selected;
    private long seed;
    private VanillaLayeredBiomeSource biomeSource;
    private final int size;

    public StrongholdMapWidget(int x, int y, int size, long s) {
        super(x, y, (size), (size), LiteralText.EMPTY);
        textureLock = new AtomicBoolean();
        this.centerX = x + (this.width / 2);
        this.centerY = y + (this.height / 2);
        this.size = size;
        this.strongholds = Collections.synchronizedList(new ArrayList<>());
        selected = null;
        strongholdToIconPosMap = new HashMap<>();
        this.seed = s;
        this.backedTexture = new NativeImageBackedTexture(size, size, true);
        mapTexID = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("seedrev/", backedTexture);
        this.biomeSource = new VanillaLayeredBiomeSource(this.seed, false, false);
        refresh(this.seed);
    }

    private static int centerCoordinate(int coord) {
        return (coord) - (16 / 2);
    }

    public void refresh(long l) {
        this.seed = l;
        this.biomeSource = new VanillaLayeredBiomeSource(this.seed, false, false);
        this.updateStrongholdPositions();
        strongholdToIconPosMap.clear();
        (new Thread(this::updateMapTexture)).start(); // Offload map calc to different thread
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();
        TextRenderer textRenderer = client.textRenderer;
        // Render map outline
        textureManager.bindTexture(MAP_BG);
        drawTexture(matrices, this.x, this.y, 0, 0, this.width, this.height, size, size);
        // Render map iff the texture is finished updating
        if (!textureLock.get()) {
            textureManager.bindTexture(mapTexID);
            drawTexture(matrices, this.x + (6 * (size / 128)), this.y + (6 * (size / 128)), 0, 0, size - (12 * (size / 128)), size - (12 * (size / 128)), size, size);
        }
        // Render 0,0 icon
        textureManager.bindTexture(COMPASS_ICON);
        drawTexture(matrices, centerCoordinate(centerX), centerCoordinate(centerY), 0, 0, 16, 16, 16, 16);
        // Render 0,0 text
        drawCenteredString(matrices, textRenderer, "(0,0)", this.centerX, this.centerY, -1);
        // Render strongholds
        if (this.strongholds.size() != 0) {
            if(strongholdToIconPosMap.isEmpty()){
                this.strongholds.forEach(e -> {
                    int coordX = centerCoordinate(this.centerX + (e.x / 4));
                    int coordY = centerCoordinate(this.centerY + (e.z / 4));
                    if ((coordX >= this.x) && (coordY >= this.y) && (coordY <= this.y + this.height) && (coordX <= this.x + this.width)) {
                        strongholdToIconPosMap.put(new Pair<>(coordX, coordY), e);
                    }
                });
            }
            textureManager.bindTexture(EYE_ICON);
            strongholdToIconPosMap.keySet().forEach(pos -> {
                drawTexture(matrices, pos.getLeft(), pos.getRight(), 32, 0, 16, 16, 16, 16);
            });
        }
        if ((mouseX >= this.x) && (mouseY >= this.y) && (mouseY <= this.y + this.height) && (mouseX <= this.x + this.width)) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean flag = false;
        for (Pair<Integer, Integer> pos : strongholdToIconPosMap.keySet()) {
            if ((mouseX < pos.getLeft() + 16) && (mouseX > pos.getLeft()) && (mouseY < pos.getRight() + 16) && (mouseY > pos.getRight())) {
                this.selected = strongholdToIconPosMap.get(pos);
                flag = true;
            }
        }
        if (flag) {
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.5f));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.onClick(mouseX, mouseY);
        return true;
    }

    public void updateMapTexture() {
        textureLock.set(true);
        Color color;
        Biome biome;
        int cx, cz;
        for (int x = 0; x < size; ++x) {
            for (int z = 0; z < size; ++z) {
                cx = (x * SCALE) - 16*(size/2);
                cz = (z * SCALE) - 16*(size/2);
                biome = biomeSource.getBiomeForNoiseGen(cx, 0, cz);
                if (BIOME_COLORS.containsKey(biome) && this.textureLock.get()) {
                    color = BIOME_COLORS.get(biome);
                    backedTexture.getImage().setPixelColor(x, z, (255) << 24 | (color.getBlue() & 255) << 16 | (color.getGreen() & 255) << 8 | (color.getRed() & 255));
                }
            }
        }
        backedTexture.upload();
        textureLock.set(false);
    }

    @Override
    public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
        super.renderToolTip(matrices, mouseX, mouseY);
        int x = ((mouseX + 6 - this.x) * SCALE) - 16 * (size / 2);
        int z = ((mouseY + 6 - this.y) * SCALE) - 16 * (size / 2);
        x *= 5;
        z *= 5;
        this.drawCenteredString(matrices, MinecraftClient.getInstance().textRenderer, x + ", " + z, mouseX, mouseY, -1);
    }

    private void updateStrongholdPositions() {
        this.strongholds.clear();
        List<Biome> list = this.biomeSource.method_28443().stream().filter(biome -> biome.hasStructureFeature(StructureFeature.STRONGHOLD)).collect(Collectors.toList());

        int i = 32;
        int j = RINGS[size / 192];
        int k = 3;
        Random random = new Random();
        random.setSeed(this.seed);
        double d = random.nextDouble() * 3.141592653589793D * 2.0D;
        int m = 0;

        for (int n = 0; n < j; ++n) {
            double e = (double) (4 * i + i * m * 6) + (random.nextDouble() - 0.5D) * (double) i * 2.5D;
            int o = (int) Math.round(Math.cos(d) * e);
            int p = (int) Math.round(Math.sin(d) * e);
            BlockPos blockPos = this.biomeSource.locateBiome((o << 4) + 8, 0, (p << 4) + 8, 112, list, random);
            if (blockPos != null) {
                o = blockPos.getX() >> 4;
                p = blockPos.getZ() >> 4;
            }

            this.strongholds.add(new ChunkPos(o, p));
            d += 6.283185307179586D / (double) k;

            if (n + 1 == k) {
                ++m;
                k += 2 * k / (m + 1);
                k = Math.min(k, 128 - n);
                d += random.nextDouble() * 3.141592653589793D * 2.0D;
            }
        }
    }

    public ChunkPos getSelected() {
        return selected;
    }
}
