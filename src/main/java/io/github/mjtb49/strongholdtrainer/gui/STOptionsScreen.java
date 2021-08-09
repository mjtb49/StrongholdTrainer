package io.github.mjtb49.strongholdtrainer.gui;

import com.google.gson.JsonPrimitive;
import io.github.mjtb49.strongholdtrainer.util.OptionTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;

public class STOptionsScreen extends Screen {

    private static final OptionTracker.Option[] OPTIONS = OptionTracker.Option.values();
    private static final int VERTICAL_SPACING = 10;
    private final Screen parent;
    public STOptionsScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    private static final HashMap<Boolean, Text> ON_OFF_MAP = new HashMap();
    // TODO: do side-by-side buttons to prevent overflow
    @Override
    protected void init() {
        int y = 30;
        for(OptionTracker.Option option : OPTIONS){
            if(option != OptionTracker.Option.MODEL){
                ButtonWidget widget = new ButtonWidget((this.width / 2) - 100,
                        y += 25,
                        200,
                        20,
                        new LiteralText(option.label + ": ")
                                .append(ON_OFF_MAP.get(OptionTracker.getBoolean(option))),
                        buttonWidget -> {
                    OptionTracker.setOption(option, new JsonPrimitive(!OptionTracker.getBoolean(option)));
                    buttonWidget.setMessage(new LiteralText(option.label + ": ").append(ON_OFF_MAP.get(OptionTracker.getBoolean(option))));
                });
                this.addButton(widget);
            }
        }
        this.addButton(new ButtonWidget((this).width / 2 - 100, (this).height / 6 + 168, 200, 20, ScreenTexts.BACK, (buttonWidget) -> MinecraftClient.getInstance().openScreen(this.parent)));

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
