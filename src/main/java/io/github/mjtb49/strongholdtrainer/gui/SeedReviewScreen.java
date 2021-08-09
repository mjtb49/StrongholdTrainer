package io.github.mjtb49.strongholdtrainer.gui;

import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SeedReviewScreen extends Screen {
    private static final Text TITLE_TEXT = new LiteralText("Review Seed")
            .append(new LiteralText(" (Beta)")
                    .formatted(Formatting.GREEN));
    Screen parent;
    long seed;
    LevelInfo levelInfo;
    StrongholdMapWidget mapWidget;

    public SeedReviewScreen(Screen parent, LevelInfo levelInfo) {
        super(TITLE_TEXT);
        this.levelInfo = levelInfo;
        this.parent = parent;
    }

    private static long chunkSeed(long world, ChunkPos pos) {
        return new ChunkRandom().setCarverSeed(world, pos.x, pos.z);
    }

    @Override
    protected void init() {
        super.init();
        int mapSize = (int) (0.3 * this.width);
        int oneThird = this.width / 3;
        int twoThirds = (2 * this.width) / 3;
        int halfHeight = this.height / 2;
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, new LiteralText("Cancel"), button -> client.openScreen(parent)));
        this.addButton(new ButtonWidget(twoThirds - 80, halfHeight, 160, 20, new LiteralText("Create World"), button -> this.createLevel()));
        TextFieldWidget seedTextField = this.addButton(new TextFieldWidget(this.textRenderer, twoThirds - 75, this.height / 2 - 50, 150, 20, new LiteralText(this.seed + "")));
        this.addButton(new ButtonWidget(twoThirds - 40, halfHeight - 25, 80, 20, new LiteralText("Load Seed"), button -> {
            try {
                this.seed = Long.parseLong(seedTextField.getText());
                this.mapWidget.refresh(Long.parseLong(seedTextField.getText()));
            } catch (Exception e) {
                this.seed = seedTextField.getText().hashCode();
                seedTextField.setText(seedTextField.getText().hashCode() + "");
                this.mapWidget.refresh(Long.parseLong(seedTextField.getText()));
            }
        }));
        mapWidget = this.addButton(new StrongholdMapWidget(oneThird - ((3 * mapSize) / 4), this.height / 2 - mapSize / 2, mapSize, seed));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, -1);
        String ind = mapWidget.getSelected() == null ? "Select a stronghold!" : "Coords: " + (mapWidget.getSelected().getStartX() + 4) + ", " + (mapWidget.getSelected().getStartZ() + 4);
        String se = mapWidget.getSelected() == null ? "" : "Chunk seed: " + chunkSeed(this.seed, mapWidget.getSelected());
        this.drawCenteredString(matrices, this.textRenderer, ind, (2 * this.width) / 3, height / 2 + 30, -1);
        this.drawCenteredString(matrices, this.textRenderer, se, (2 * this.width) / 3, height / 2 + 40, -1);
        super.render(matrices, mouseX, mouseY, delta);
//        this.drawCenteredString(matrices, textRenderer, mouseX + "," +mouseY, mouseX, mouseY, -1);
    }

    private void createLevel() {
        assert this.client != null;
        this.client.method_29970(new SaveLevelScreen(new TranslatableText("createWorld.preparing")));
        if (this.parent instanceof CreateWorldScreen) {
            GeneratorOptions options = ((CreateWorldScreen) parent).moreOptionsDialog.getGeneratorOptions(false);
            if (this.mapWidget.getSelected() != null) {
                String name = "rev-" + UUID.randomUUID().toString().split("-")[0];
                GeneratorOptions newOptions = new GeneratorOptions(chunkSeed(seed, mapWidget.getSelected()), true, false, options.getDimensionMap());
                this.levelInfo = new LevelInfo(name, GameMode.SPECTATOR, false, Difficulty.EASY, false, new GameRules(), this.levelInfo.method_29558());
                client.method_29607(name, this.levelInfo, ((CreateWorldScreen) parent).moreOptionsDialog.method_29700(), newOptions);
            } else {
                this.client.openScreen(this);
                this.client.getToastManager().add(new SystemToast(SystemToast.Type.WORLD_ACCESS_FAILURE, new LiteralText("Choose a stronghold!"), new LiteralText("")));
            }
        }
    }

    @Override
    public void onClose() {
        this.client.openScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
