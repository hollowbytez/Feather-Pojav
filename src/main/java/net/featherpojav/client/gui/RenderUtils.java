package net.featherpojav.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class RenderUtils {
    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float f = (float)(color >> 24 & 255) / 255.0F;
        float f1 = (float)(color >> 16 & 255) / 255.0F;
        float f2 = (float)(color >> 8 & 255) / 255.0F;
        float f3 = (float)(color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Tessellator tessellator = Tessellator.getInstance();
        
        // Draw the center rectangles to fill the body
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        // Center block
        bufferbuilder.vertex(matrix, x + radius, y, 0).color(color);
        bufferbuilder.vertex(matrix, x + radius, y + height, 0).color(color);
        bufferbuilder.vertex(matrix, x + width - radius, y + height, 0).color(color);
        bufferbuilder.vertex(matrix, x + width - radius, y, 0).color(color);
        
        // Left block
        bufferbuilder.vertex(matrix, x, y + radius, 0).color(color);
        bufferbuilder.vertex(matrix, x, y + height - radius, 0).color(color);
        bufferbuilder.vertex(matrix, x + radius, y + height - radius, 0).color(color);
        bufferbuilder.vertex(matrix, x + radius, y + radius, 0).color(color);
        
        // Right block
        bufferbuilder.vertex(matrix, x + width - radius, y + radius, 0).color(color);
        bufferbuilder.vertex(matrix, x + width - radius, y + height - radius, 0).color(color);
        bufferbuilder.vertex(matrix, x + width, y + height - radius, 0).color(color);
        bufferbuilder.vertex(matrix, x + width, y + radius, 0).color(color);
        
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());

        // Draw the 4 rounded corners using triangles
        drawCorner(matrix, x + radius, y + radius, radius, 180, 270, color); // Top-Left
        drawCorner(matrix, x + width - radius, y + radius, radius, 270, 360, color); // Top-Right
        drawCorner(matrix, x + width - radius, y + height - radius, radius, 0, 90, color); // Bottom-Right
        drawCorner(matrix, x + radius, y + height - radius, radius, 90, 180, color); // Bottom-Left

        RenderSystem.disableBlend();
    }

    private static void drawCorner(Matrix4f matrix, float cx, float cy, float r, int startAngle, int endAngle, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        
        float lastVx = cx + (float) Math.cos(Math.toRadians(startAngle)) * r;
        float lastVy = cy + (float) Math.sin(Math.toRadians(startAngle)) * r;

        for (int i = startAngle + 5; i <= endAngle; i += 5) {
            float rad = (float) Math.toRadians(i);
            float vx = cx + (float) Math.cos(rad) * r;
            float vy = cy + (float) Math.sin(rad) * r;
            
            bufferbuilder.vertex(matrix, cx, cy, 0).color(color);
            bufferbuilder.vertex(matrix, lastVx, lastVy, 0).color(color);
            bufferbuilder.vertex(matrix, vx, vy, 0).color(color);
            
            lastVx = vx;
            lastVy = vy;
        }
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
    }

    public static void drawRoundedOutline(MatrixStack matrices, float x, float y, float width, float height, float radius, float lineWidth, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float f = (float)(color >> 24 & 255) / 255.0F;
        float f1 = (float)(color >> 16 & 255) / 255.0F;
        float f2 = (float)(color >> 8 & 255) / 255.0F;
        float f3 = (float)(color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(lineWidth);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        // Top-Left corner to Top-Right corner
        for (int i = 180; i <= 270; i += 5) {
            float rad = (float) Math.toRadians(i);
            bufferbuilder.vertex(matrix, x + radius + (float)Math.cos(rad) * radius, y + radius + (float)Math.sin(rad) * radius, 0).color(color);
        }
        for (int i = 270; i <= 360; i += 5) {
            float rad = (float) Math.toRadians(i);
            bufferbuilder.vertex(matrix, x + width - radius + (float)Math.cos(rad) * radius, y + radius + (float)Math.sin(rad) * radius, 0).color(color);
        }
        for (int i = 0; i <= 90; i += 5) {
            float rad = (float) Math.toRadians(i);
            bufferbuilder.vertex(matrix, x + width - radius + (float)Math.cos(rad) * radius, y + height - radius + (float)Math.sin(rad) * radius, 0).color(color);
        }
        for (int i = 90; i <= 180; i += 5) {
            float rad = (float) Math.toRadians(i);
            bufferbuilder.vertex(matrix, x + radius + (float)Math.cos(rad) * radius, y + height - radius + (float)Math.sin(rad) * radius, 0).color(color);
        }
        // Close the loop
        bufferbuilder.vertex(matrix, x, y + radius, 0).color(color);

        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
    }
}
