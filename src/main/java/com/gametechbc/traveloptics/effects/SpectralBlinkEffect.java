package com.gametechbc.traveloptics.effects;

import com.gametechbc.traveloptics.init.TravelopticsItems;
import com.gametechbc.traveloptics.init.TravelopticsSounds;
import com.gametechbc.traveloptics.util.TravelopticsTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;

public class SpectralBlinkEffect
extends MobEffect {
    public SpectralBlinkEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFFFF);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity != null) {
            Level world = entity.level();
            MobEffectInstance effectInstance = entity.getEffect(this);
            if (effectInstance != null) {
                int duration = effectInstance.getDuration();
                int level = amplifier + 1;
                double range = 1.0 + (double)level;
                if (duration == 60) {
                    world.playSound(null, entity.blockPosition(), TravelopticsSounds.SPECTRAL_BLINK_CHARGE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                if (duration > 60) {
                    entity.removeEffect(this);
                    entity.addEffect(new MobEffectInstance(this, 60, amplifier, false, true));
                }
                for (Entity nearbyEntity : world.getEntities(entity, entity.getBoundingBox().inflate(range))) {
                    if (!(nearbyEntity instanceof LivingEntity)) continue;
                    LivingEntity livingEntity = (LivingEntity)nearbyEntity;
                    if (nearbyEntity == entity) continue;
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10, 0, false, false));
                }
                if (duration == 1) {
                    this.handleBlinkLogic(entity, amplifier, range, world);
                }
            }
        }
    }

    private void handleBlinkLogic(LivingEntity entity, int amplifier, double range, Level world) {
        boolean hasSpectralShift = CuriosApi.getCuriosHelper().findEquippedCurio(TravelopticsItems.AMULET_OF_SPECTRAL_SHIFT.get(), entity).isPresent();
        if (entity.isCrouching() && hasSpectralShift) {
            this.teleportEntityToPlayer(entity, range, world);
        } else {
            this.teleportPlayerToEntity(entity, range, world);
        }
    }

    private void teleportEntityToPlayer(LivingEntity entity, double range, Level world) {
        AABB boundingBox;
        Vec3 viewVec;
        Vec3 endVec;
        Vec3 startVec = entity.getEyePosition(1.0f);
        EntityHitResult entityHitResult = this.getForgivingEntityHitResult(world, entity, startVec, endVec = startVec.add((viewVec = entity.getLookAngle()).scale(range)), boundingBox = entity.getBoundingBox().expandTowards(viewVec.scale(range)).inflate(1.0));
        if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
            Entity hitEntity = entityHitResult.getEntity();
            if (hitEntity.getType().is(TravelopticsTags.SPECTRAL_SHIFT_BLACKLIST)) {
                if (entity instanceof Player) {
                    Player player = (Player)entity;
                    player.displayClientMessage(Component.literal(hitEntity.getName().getString() + " cannot be teleported!"), true);
                }
                return;
            }
            Vec3 targetPos = entity.position();
            hitEntity.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            world.playSound(null, entity.blockPosition(), TravelopticsSounds.SPECTRAL_BLINK_SUCCESS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            if (entity instanceof Player) {
                Player player = (Player)entity;
                player.displayClientMessage(Component.literal("Teleported " + hitEntity.getName().getString() + " to you!"), true);
            }
        } else {
            this.playFailedBlinkPostEffects(world, entity);
        }
    }

    private void teleportPlayerToEntity(LivingEntity entity, double range, Level world) {
        AABB boundingBox;
        Vec3 viewVec;
        Vec3 endVec;
        Vec3 startVec = entity.getEyePosition(1.0f);
        EntityHitResult entityHitResult = this.getForgivingEntityHitResult(world, entity, startVec, endVec = startVec.add((viewVec = entity.getLookAngle()).scale(range)), boundingBox = entity.getBoundingBox().expandTowards(viewVec.scale(range)).inflate(1.0));
        if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
            Entity hitEntity = entityHitResult.getEntity();
            if (hitEntity.getType().is(TravelopticsTags.SPECTRAL_BLINK_BLACKLIST)) {
                if (entity instanceof Player) {
                    Player player = (Player)entity;
                    player.displayClientMessage(Component.literal(hitEntity.getName().getString() + " cannot be teleported to!"), true);
                }
                return;
            }
            Vec3 targetPos = hitEntity.position();
            entity.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            world.playSound(null, entity.blockPosition(), TravelopticsSounds.SPECTRAL_BLINK_SUCCESS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            if (entity instanceof Player) {
                Player player = (Player)entity;
                player.displayClientMessage(Component.translatable("effect.traveloptics.spectral_blink.success", hitEntity.getName()), true);
            }
        } else {
            this.playFailedBlinkPostEffects(world, entity);
        }
    }

    private void playFailedBlinkPostEffects(Level world, Entity entity) {
        world.playSound(null, entity.blockPosition(), TravelopticsSounds.SPECTRAL_BLINK_FAILED.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        if (entity instanceof Player) {
            Player player = (Player)entity;
            player.displayClientMessage(Component.translatable("effect.traveloptics.spectral_blink.missed").withStyle(ChatFormatting.RED), true);
        }
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    private EntityHitResult getForgivingEntityHitResult(Level world, LivingEntity entity, Vec3 startVec, Vec3 endVec, AABB boundingBox) {
        EntityHitResult entityHitResult = null;
        double closestDistance = Double.MAX_VALUE;
        double forgivenessRadius = 0.5;
        for (Entity targetEntity : world.getEntities(entity, boundingBox, e -> !e.isSpectator() && e.isPickable())) {
            double distance;
            AABB entityBoundingBox = targetEntity.getBoundingBox().inflate((double)targetEntity.getPickRadius() + forgivenessRadius);
            if (!entityBoundingBox.clip(startVec, endVec).isPresent() || !((distance = startVec.distanceTo(targetEntity.getBoundingBox().getCenter())) < closestDistance)) continue;
            closestDistance = distance;
            entityHitResult = new EntityHitResult(targetEntity);
        }
        return entityHitResult;
    }
}
