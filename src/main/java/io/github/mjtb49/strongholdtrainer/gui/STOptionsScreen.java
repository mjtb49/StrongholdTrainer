package io.github.mjtb49.strongholdtrainer.gui;

import com.google.gson.JsonPrimitive;
import io.github.mjtb49.strongholdtrainer.api.MinecraftServerAccessor;
import io.github.mjtb49.strongholdtrainer.ml.StrongholdMachineLearning;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;

public class STOptionsScreen extends Screen {

    private static final OptionTracker.Option[] OPTIONS = OptionTracker.Option.values();
    private final Screen parent;
    private String selectedModel;
    public STOptionsScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    private static final HashMap<Boolean, Text> ON_OFF_MAP = new HashMap<>();
    @Override
    protected void init() {
        int y = this.height / 6 + 18;
        int modelHeight = this.height / 6 - 12;
        selectedModel = OptionTracker.getString(OptionTracker.Option.MODEL);
        boolean row = false;
        ButtonWidget modelOptionWidget = new ButtonWidget(this.width / 2 - 155, modelHeight, 310, 20, new LiteralText("Loaded Model: " + selectedModel), buttonWidget -> {
            List<String> ids = StrongholdMachineLearning.MODEL_REGISTRY.getRegisteredIdentifiers();
            int index = ids.indexOf(selectedModel);
            if (index + 1 >= ids.size()) {
                index = 0;
            } else {
                index++;
            }
            selectedModel = ids.get(index);
            OptionTracker.setOption(OptionTracker.Option.MODEL, new JsonPrimitive(selectedModel));
            StrongholdMachineLearning.MODEL_REGISTRY.setActiveModel(selectedModel);
            if (client != null && client.getServer() != null) {
                ((MinecraftServerAccessor) client.getServer()).refreshRooms();
            }
            buttonWidget.setMessage(new LiteralText("Loaded Model: " + selectedModel));
        }, ((button, matrices, mouseX, mouseY) -> this.renderTooltip(matrices, textRenderer.wrapLines(new LiteralText(OptionTracker.Option.MODEL.tooltip), 128), mouseX, mouseY)));
        for (OptionTracker.Option option : OPTIONS) {
            if (option != OptionTracker.Option.MODEL) {
                ButtonWidget widget = new ButtonWidget((this.width / 2) - (row ? -5 : 155),
                        y += row ? 0 : 24,
                        150,
                        20,
                        new LiteralText(option.label + ": ")
                                .append(ON_OFF_MAP.get(OptionTracker.getBoolean(option))),
                        buttonWidget -> {
                            OptionTracker.setOption(option, new JsonPrimitive(!OptionTracker.getBoolean(option)));
                            buttonWidget.setMessage(new LiteralText(option.label + ": ").append(ON_OFF_MAP.get(OptionTracker.getBoolean(option))));

                        }, ((button, matrices, mouseX, mouseY) -> this.renderTooltip(matrices, textRenderer.wrapLines(new LiteralText(option.tooltip), 128), mouseX, mouseY)));
                this.addButton(widget);
                row = !row;
            }
        }
        this.addButton(new ButtonWidget((this).width / 2 - 100, (this).height / 6 + 168, 200, 20, ScreenTexts.BACK, (buttonWidget) -> MinecraftClient.getInstance().openScreen(this.parent)));
        this.addButton(modelOptionWidget);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

        this.renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, -1);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }


    static {
        ON_OFF_MAP.put(false, new LiteralText("OFF").formatted(Formatting.RED));
        ON_OFF_MAP.put(true, new LiteralText("ON").formatted(Formatting.GREEN));
    }
}
