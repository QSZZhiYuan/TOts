/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.L_Ender.cataclysm.Cataclysm
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderGuiOverlayEvent$Post
 *  net.minecraftforge.client.event.RenderGuiOverlayEvent$Pre
 *  net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.gametechbc.traveloptics.overlay.armor_overlay;

import com.gametechbc.traveloptics.api.utils.TOArmorUtils;
import com.gametechbc.traveloptics.config.ClientConfig;
import com.gametechbc.traveloptics.data_manager.CooldownManager;
import com.gametechbc.traveloptics.init.TravelopticsItems;
import com.gametechbc.traveloptics.item.armor.PrimordialCrestArmorItem;
import com.github.L_Ender.cataclysm.Cataclysm;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="traveloptics", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class PrimordialCrestOverlay {
    private static final ResourceLocation PRIMORDIAL_CREST_TEXTURE = new ResourceLocation("traveloptics", "textures/gui/armor/primordial_crest_bar.png");
    private static final boolean USE_XP_BAR = (Boolean)ClientConfig.armorReplaceXPBars.get();
    private static long lastFuelTime = 0L;
    private static boolean shouldRenderFuelBar = true;
    private static final int BASE_WIDTH = 135;
    private static final int BASE_HEIGHT = 25;
    private static final float BAR_SCALE = 1.0f;
    private static final int BAR_X_OFFSET = (Integer)ClientConfig.primordialCrestXOffset.get();
    private static final int BAR_Y_OFFSET = 100 + (Integer)ClientConfig.primordialCrestYOffset.get();
    private static final float BAR_ROTATION = 0.0f;
    private static final int XP_BAR_WIDTH = 182;
    private static final int XP_BAR_HEIGHT = 5;
    private static final int XP_BAR_U = 29;
    private static final int XP_BAR_V = 89;
    private static final int XP_FILL_U = 29;
    private static final int XP_FILL_V = 99;
    private static final int XP_BAR_X_OFFSET = 0;
    private static final int XP_BAR_Y_OFFSET = 9;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        double progress;
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Player player = Cataclysm.PROXY.getClientSidePlayer();
        if (player == null) {
            return;
        }
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!TOArmorUtils.isWearingFullSet(player, PrimordialCrestArmorItem.class)) {
            return;
        }
        int currentFuel = CooldownManager.getCooldown(chestplate);
        int maxFuel = 400;
        double d = progress = maxFuel > 0 ? (double)currentFuel / (double)maxFuel : 0.0;
        if (progress <= 0.0) {
            if (lastFuelTime == 0L) {
                lastFuelTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastFuelTime >= 3000L) {
                shouldRenderFuelBar = false;
            }
        } else {
            lastFuelTime = 0L;
            shouldRenderFuelBar = true;
        }
        if (!shouldRenderFuelBar) {
            return;
        }
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        if (USE_XP_BAR) {
            int x = screenWidth / 2 - 91 + 0;
            int y = screenHeight - 32 + 3 - 8 + 9;
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            guiGraphics.blit(PRIMORDIAL_CREST_TEXTURE, x, y, 29, 89, 182, 5, 241, 115);
            int fillWidth = (int)(182.0 * (1.0 - progress));
            if (fillWidth > 0) {
                guiGraphics.blit(PRIMORDIAL_CREST_TEXTURE, x, y, 29, 99, fillWidth, 5, 241, 115);
            }
            poseStack.popPose();
        } else {
            int barWidth = 135;
            int barHeight = 25;
            int x = (screenWidth - barWidth) / 2 + BAR_X_OFFSET;
            int y = screenHeight - BAR_Y_OFFSET;
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate((double)x + (double)barWidth / 2.0, (double)y + (double)barHeight / 2.0, 0.0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(0.0f));
            poseStack.scale(1.0f, 1.0f, 1.0f);
            poseStack.translate((float)(-barWidth) / 2.0f, (float)(-barHeight) / 2.0f, 0.0f);
            guiGraphics.blit(PRIMORDIAL_CREST_TEXTURE, 0, 0, 53, 20, 135, 25, 241, 115);
            int fillWidth = (int)(135.0 * (1.0 - progress));
            if (fillWidth > 0) {
                guiGraphics.blit(PRIMORDIAL_CREST_TEXTURE, 0, 0, 53, 48, fillWidth, 25, 241, 115);
            }
            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public static void onRenderXpBar(RenderGuiOverlayEvent.Pre event) {
        Player player = Cataclysm.PROXY.getClientSidePlayer();
        if (player == null) {
            return;
        }
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || chestplate.getItem() != TravelopticsItems.PRIMORDIAL_CREST_ARMOR_CHESTPLATE.get()) {
            return;
        }
        if (USE_XP_BAR) {
            if (!shouldRenderFuelBar) {
                return;
            }
            if (event.getOverlay() == VanillaGuiOverlay.EXPERIENCE_BAR.type()) {
                event.setCanceled(true);
            }
        }
    }
}

