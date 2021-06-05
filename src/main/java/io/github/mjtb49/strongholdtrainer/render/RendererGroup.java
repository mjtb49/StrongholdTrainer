package io.github.mjtb49.strongholdtrainer.render;

import com.mojang.blaze3d.platform.GlStateManager;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RendererGroup<T extends Renderer> {

    public enum RenderOption {
        RENDER_FRONT,
        RENDER_BACK,
        NONE
    }

    private final ConcurrentLinkedQueue<T> renderers;
    private int sizeCap;
    private RenderOption renderOption;

    public RendererGroup(int sizeCap, RenderOption option) {
        this.sizeCap = sizeCap;
        this.renderOption = option;
        this.renderers = new ConcurrentLinkedQueue<>();
    }

    public void render() {
        while (renderers.size() > sizeCap)
            renderers.remove();
        if (renderOption != RenderOption.NONE) {
            if (renderOption == RenderOption.RENDER_BACK)
                GlStateManager.enableDepthTest();
            else if (renderOption == RenderOption.RENDER_FRONT)
                GlStateManager.disableDepthTest();
            for (Renderer r : renderers) {
                r.render();
            }
        }
        GlStateManager.disableDepthTest();
    }

    public void addRenderer(T renderer) {
        renderers.add(renderer);
    }

    public void setSizeCap(int sizeCap) {
        this.sizeCap = sizeCap;
    }

    public int getSizeCap() {
        return sizeCap;
    }

    public void setRenderOption(RenderOption renderOption) {
        this.renderOption = renderOption;
    }

    public RenderOption getRenderOption() {
        return renderOption;
    }

    public void clear() {
        renderers.clear();
    }
}
