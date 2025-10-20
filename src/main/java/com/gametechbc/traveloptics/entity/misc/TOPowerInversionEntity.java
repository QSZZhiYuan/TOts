package com.gametechbc.traveloptics.entity.misc;

import com.gametechbc.traveloptics.init.TravelopticsEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

public class TOPowerInversionEntity
extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(TOPowerInversionEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> INTENSITY = SynchedEntityData.defineId(TOPowerInversionEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> FADE_DURATION = SynchedEntityData.defineId(TOPowerInversionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> INVERT_COLORS = SynchedEntityData.defineId(TOPowerInversionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FLASH_COLOR = SynchedEntityData.defineId(TOPowerInversionEntity.class, EntityDataSerializers.INT);

    public TOPowerInversionEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    public TOPowerInversionEntity(Level world, Vec3 position, float radius, float intensity, int fadeDuration, boolean invertColors, int flashColor) {
        super(TravelopticsEntities.SCREEN_POWER_INVERSION.get(), world);
        this.setRadius(radius);
        this.setIntensity(intensity);
        this.setFadeDuration(fadeDuration);
        this.setInvertColors(invertColors);
        this.setFlashColor(flashColor);
        this.setPos(position.x, position.y, position.z);
    }

    @OnlyIn(value=Dist.CLIENT)
    public PowerEffectData getEffectData(Player player, float delta) {
        float ticksDelta = (float)this.tickCount + delta;
        Vec3 playerPos = player.getEyePosition(delta);
        double distance = this.position().distanceTo(playerPos);
        if (distance > (double)this.getRadius()) {
            return new PowerEffectData(0.0f, 0.0f, EffectPhase.NONE);
        }
        float distanceFalloff = (float)Math.max(0.0, 1.0 - distance / (double)this.getRadius());
        float baseIntensity = this.getIntensity() * distanceFalloff;
        if (ticksDelta < 2.0f) {
            return new PowerEffectData(baseIntensity, 0.0f, EffectPhase.FIRST_FLASH);
        }
        if (ticksDelta < 4.0f) {
            return new PowerEffectData(0.0f, baseIntensity, EffectPhase.SECOND_FLASH);
        }
        if (ticksDelta < 4.0f + (float)this.getFadeDuration()) {
            float fadeProgress = (ticksDelta - 4.0f) / (float)this.getFadeDuration();
            fadeProgress = Math.max(0.0f, Math.min(1.0f, fadeProgress));
            float fadeIntensity = baseIntensity * (1.0f - fadeProgress);
            return new PowerEffectData(0.0f, fadeIntensity, EffectPhase.FADE);
        }
        return new PowerEffectData(0.0f, 0.0f, EffectPhase.NONE);
    }

    @OnlyIn(value=Dist.CLIENT)
    public int getFlashColor() {
        return this.getFlashColorValue();
    }

    @OnlyIn(value=Dist.CLIENT)
    public boolean shouldInvertColors() {
        return this.getInvertColors();
    }

    public void tick() {
        super.tick();
        int totalDuration = 4 + this.getFadeDuration();
        if (this.tickCount > totalDuration) {
            this.discard();
        }
    }

    protected void defineSynchedData() {
        this.entityData.define(RADIUS, Float.valueOf(15.0f));
        this.entityData.define(INTENSITY, Float.valueOf(1.0f));
        this.entityData.define(FADE_DURATION, 20);
        this.entityData.define(INVERT_COLORS, true);
        this.entityData.define(FLASH_COLOR, 0xF8F8F8);
    }

    public float getRadius() {
        return this.entityData.get(RADIUS).floatValue();
    }

    public void setRadius(float radius) {
        this.entityData.set(RADIUS, Float.valueOf(radius));
    }

    public float getIntensity() {
        return this.entityData.get(INTENSITY).floatValue();
    }

    public void setIntensity(float intensity) {
        this.entityData.set(INTENSITY, Float.valueOf(intensity));
    }

    public int getFadeDuration() {
        return this.entityData.get(FADE_DURATION);
    }

    public void setFadeDuration(int duration) {
        this.entityData.set(FADE_DURATION, duration);
    }

    public boolean getInvertColors() {
        return this.entityData.get(INVERT_COLORS);
    }

    public void setInvertColors(boolean invert) {
        this.entityData.set(INVERT_COLORS, invert);
    }

    public int getFlashColorValue() {
        return this.entityData.get(FLASH_COLOR);
    }

    public void setFlashColor(int color) {
        this.entityData.set(FLASH_COLOR, color);
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        this.setRadius(compound.getFloat("radius"));
        this.setIntensity(compound.getFloat("intensity"));
        this.setFadeDuration(compound.getInt("fade_duration"));
        this.setInvertColors(compound.getBoolean("invert_colors"));
        this.setFlashColor(compound.getInt("flash_color"));
        this.tickCount = compound.getInt("ticks_existed");
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("radius", this.getRadius());
        compound.putFloat("intensity", this.getIntensity());
        compound.putInt("fade_duration", this.getFadeDuration());
        compound.putBoolean("invert_colors", this.getInvertColors());
        compound.putInt("flash_color", this.getFlashColorValue());
        compound.putInt("ticks_existed", this.tickCount);
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static void createPowerInversion(Level world, Vec3 position, float radius, float intensity, int fadeDuration, boolean invertColors, int flashColor) {
        if (!world.isClientSide) {
            TOPowerInversionEntity effect = new TOPowerInversionEntity(world, position, radius, intensity, fadeDuration, invertColors, flashColor);
            world.addFreshEntity(effect);
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class PowerEffectData {
        public final float flashIntensity;
        public final float effectIntensity;
        public final EffectPhase phase;

        public PowerEffectData(float flashIntensity, float effectIntensity, EffectPhase phase) {
            this.flashIntensity = flashIntensity;
            this.effectIntensity = effectIntensity;
            this.phase = phase;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static enum EffectPhase {
        NONE,
        FIRST_FLASH,
        SECOND_FLASH,
        FADE;
    }
}
