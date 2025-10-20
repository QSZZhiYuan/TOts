package com.gametechbc.traveloptics.overlay;

import com.gametechbc.traveloptics.entity.misc.TOPowerInversionEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(value=Dist.CLIENT)
public class ScreenEffectOverlayHelper {
    public static void renderFlashOverlay(GuiGraphics guiGraphics, float alpha, int color) {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        PoseStack poseStack = guiGraphics.pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();
        buffer.vertex(matrix, 0.0f, (float)screenHeight, 0.0f).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, (float)screenWidth, (float)screenHeight, 0.0f).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, (float)screenWidth, 0.0f, 0.0f).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, 0.0f, 0.0f, 0.0f).color(red, green, blue, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }

    public static void renderPowerInversionOverlay(GuiGraphics guiGraphics, TOPowerInversionEntity.PowerEffectData effectData, boolean shouldInvert, int flashColor) {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        if (effectData.flashIntensity > 0.0f) {
            float red = (float)(flashColor >> 16 & 0xFF) / 255.0f;
            float green = (float)(flashColor >> 8 & 0xFF) / 255.0f;
            float blue = (float)(flashColor & 0xFF) / 255.0f;
            float alpha = effectData.flashIntensity;
            PoseStack poseStack = guiGraphics.pose();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            Matrix4f matrix = poseStack.last().pose();
            buffer.vertex(matrix, 0.0f, (float)screenHeight, 0.0f).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, (float)screenWidth, (float)screenHeight, 0.0f).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, (float)screenWidth, 0.0f, 0.0f).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, 0.0f, 0.0f, 0.0f).color(red, green, blue, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
            RenderSystem.disableBlend();
        }
        if (effectData.effectIntensity > 0.0f) {
            if (shouldInvert) {
                ScreenEffectOverlayHelper.renderEnhancedInversionPattern(guiGraphics, screenWidth, screenHeight, effectData.effectIntensity);
            } else {
                ScreenEffectOverlayHelper.renderEnhancedDesaturation(guiGraphics, screenWidth, screenHeight, effectData.effectIntensity);
            }
        }
    }

    public static void renderEnhancedInversionPattern(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float intensity) {
        if (intensity <= 0.0f) {
            return;
        }
        PoseStack poseStack = guiGraphics.pose();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();
        float inversionAlpha = Math.min(intensity * 1.5f, 0.95f);
        inversionAlpha *= inversionAlpha;
        buffer.vertex(matrix, 0.0f, (float)screenHeight, 0.0f).color(0.97f, 0.97f, 0.97f, inversionAlpha).endVertex();
        buffer.vertex(matrix, (float)screenWidth, (float)screenHeight, 0.0f).color(0.97f, 0.97f, 0.97f, inversionAlpha).endVertex();
        buffer.vertex(matrix, (float)screenWidth, 0.0f, 0.0f).color(0.97f, 0.97f, 0.97f, inversionAlpha).endVertex();
        buffer.vertex(matrix, 0.0f, 0.0f, 0.0f).color(0.97f, 0.97f, 0.97f, inversionAlpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ZERO);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float contrastAlpha = intensity * 0.7f;
        contrastAlpha *= contrastAlpha;
        float contrastDarkness = 0.25f;
        buffer.vertex(matrix, 0.0f, (float)screenHeight, 0.0f).color(contrastDarkness, contrastDarkness, contrastDarkness, contrastAlpha).endVertex();
        buffer.vertex(matrix, (float)screenWidth, (float)screenHeight, 0.0f).color(contrastDarkness, contrastDarkness, contrastDarkness, contrastAlpha).endVertex();
        buffer.vertex(matrix, (float)screenWidth, 0.0f, 0.0f).color(contrastDarkness, contrastDarkness, contrastDarkness, contrastAlpha).endVertex();
        buffer.vertex(matrix, 0.0f, 0.0f, 0.0f).color(contrastDarkness, contrastDarkness, contrastDarkness, contrastAlpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public static void renderEnhancedDesaturation(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float intensity) {
        PoseStack poseStack = guiGraphics.pose();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();
        float desatAlpha = Math.min(intensity * 1.4f, 1.0f);
        float grayValue = 0.4f;
        buffer.vertex(matrix, 0.0f, (float)screenHeight, 0.0f).color(grayValue, grayValue, grayValue, desatAlpha).endVertex();
        buffer.vertex(matrix, (float)screenWidth, (float)screenHeight, 0.0f).color(grayValue, grayValue, grayValue, desatAlpha).endVertex();
        buffer.vertex(matrix, (float)screenWidth, 0.0f, 0.0f).color(grayValue, grayValue, grayValue, desatAlpha).endVertex();
        buffer.vertex(matrix, 0.0f, 0.0f, 0.0f).color(grayValue, grayValue, grayValue, desatAlpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public static void renderFullScreenQuad(PoseStack poseStack, int screenWidth, int screenHeight, float red, float green, float blue, float alpha) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();
        buffer.vertex(matrix, 0.0f, (float)screenHeight, 0.0f).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, (float)screenWidth, (float)screenHeight, 0.0f).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, (float)screenWidth, 0.0f, 0.0f).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, 0.0f, 0.0f, 0.0f).color(red, green, blue, alpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }
}
