package io.github.mjtb49.strongholdtrainer.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CubeWithSides extends Renderer {
    Vec3d startPos;
    Vec3d endPos;
    Color color;
    float alpha;
    public CubeWithSides(Vec3d startPos, Vec3d endPos, Color color, float alpha) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.color = color;
        this.alpha = alpha;
    }

    public void render() {
        GlStateManager.enableBlend();
        Vec3d camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        float startX = (float) (startPos.x - camPos.x);
        float startY = (float) (startPos.y - camPos.y);
        float startZ = (float) (startPos.z - camPos.z);
        float endX = (float) (endPos.x - camPos.x);
        float endY = (float) (endPos.y - camPos.y);
        float endZ = (float) (endPos.z - camPos.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin( 7, VertexFormats.POSITION_COLOR);
        //Matrix4f matrix = matrixStack.peek().getModel();


        float red = color.getFRed();
        float green = color.getFGreen();
        float blue = color.getFBlue();
// West side
        bufferBuilder.vertex(startX, startY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, startY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, endY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, endY, startZ).color(red, green, blue, alpha).next();
// East side,
        bufferBuilder.vertex(endX, startY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, startY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, endZ).color(red, green, blue, alpha).next();
// North side
        bufferBuilder.vertex(endX, startY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, startY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, endY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, startZ).color(red, green, blue, alpha).next();
// South side
        bufferBuilder.vertex(startX, startY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, startY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, endY, endZ).color(red, green, blue, alpha).next();
// Top side
        bufferBuilder.vertex(startX, endY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, endY, startZ).color(red, green, blue, alpha).next();
// Bottom side
        bufferBuilder.vertex(endX, startY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, startY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, startY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, startY, startZ).color(red, green, blue, alpha).next();


        // West side
        bufferBuilder.vertex(startX, startY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, endY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, endY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, startY, endZ).color(red, green, blue, alpha).next();
// East side,
        bufferBuilder.vertex(endX, startY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, startY, startZ).color(red, green, blue, alpha).next();
// North side
        bufferBuilder.vertex(endX, startY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, endY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, startY, startZ).color(red, green, blue, alpha).next();
// South side
        bufferBuilder.vertex(startX, startY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, endY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, startY, endZ).color(red, green, blue, alpha).next();
// Top side
        bufferBuilder.vertex(startX, endY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, endY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, endY, endZ).color(red, green, blue, alpha).next();
// Bottom side
        bufferBuilder.vertex(endX, startY, endZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(endX, startY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, startY, startZ).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(startX, startY, endZ).color(red, green, blue, alpha).next();

        tessellator.draw();
        GlStateManager.disableBlend();
    }

    @Override
    public BlockPos getPos() {
        return new BlockPos(startPos);
    }
}
