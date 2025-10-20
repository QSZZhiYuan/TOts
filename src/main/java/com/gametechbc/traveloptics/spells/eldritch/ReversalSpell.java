package com.gametechbc.traveloptics.spells.eldritch;

import com.gametechbc.traveloptics.effects.Reversal.ReversalEffect;
import com.gametechbc.traveloptics.init.TravelopticsEffects;
import com.gametechbc.traveloptics.init.TravelopticsItems;
import com.gametechbc.traveloptics.init.TravelopticsSounds;
import com.gametechbc.traveloptics.spells.TravelopticsSpellAnimations;
import com.gametechbc.traveloptics.util.TravelopticsTags;
import com.github.L_Ender.cataclysm.init.ModParticle;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.spells.eldritch.AbstractEldritchSpell;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;

@AutoSpellConfig
public class ReversalSpell
extends AbstractEldritchSpell {
    private final ResourceLocation spellId = new ResourceLocation("traveloptics", "reversal");
    private final DefaultConfig defaultConfig = new DefaultConfig().setMinRarity(SpellRarity.RARE).setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE).setMaxLevel(3).setCooldownSeconds(3.0).build();

    public ReversalSpell() {
        this.manaCostPerLevel = 50;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 50;
    }

    public CastType getCastType() {
        return CastType.INSTANT;
    }

    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(TravelopticsSounds.REVERSAL.get());
    }

    public AnimationHolder getCastFinishAnimation() {
        return TravelopticsSpellAnimations.REVERSAL;
    }

    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        boolean hasCurio = CuriosApi.getCuriosHelper().findEquippedCurio(TravelopticsItems.NIGHTSTALKERS_BAND.get(), entity).isPresent();
        if (hasCurio || entity.getMainHandItem().is(TravelopticsTags.CAN_CAST_REVERSAL)) {
            return true;
        }
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("spell.traveloptics.reversal.warning").withStyle(ChatFormatting.RED), true);
            }
        }
        return false;
    }

    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        MobEffectInstance reversalEffectInstance = new MobEffectInstance(TravelopticsEffects.REVERSAL.get(), 8, 0, true, false, false);
        entity.addEffect(reversalEffectInstance);
        MobEffect mobEffect = reversalEffectInstance.getEffect();
        if (mobEffect instanceof ReversalEffect) {
            ReversalEffect reversalEffect = (ReversalEffect)mobEffect;
            float damageMultiplier = this.calculateDamageMultiplier(spellLevel, entity);
            reversalEffect.setDamageMultiplier(damageMultiplier);
        }
        MagicManager.spawnParticles(level, ModParticle.PHANTOM_WING_FLAME.get(), entity.getX(), entity.getY() + (double)(entity.getBbHeight() * 0.5f), entity.getZ(), 30, entity.getBbWidth() * 0.5f, entity.getBbHeight() * 0.5f, entity.getBbWidth() * 0.5f, 0.03, false);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float calculateDamageMultiplier(int spellLevel, LivingEntity caster) {
        return 1.0f + this.getSpellPower(spellLevel, caster) * 0.25f;
    }

    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        float damageMultiplier = this.calculateDamageMultiplier(spellLevel, caster);
        return List.of(Component.translatable("ui.traveloptics.reversal_damage_multiplier", String.format("%.1f", Float.valueOf(damageMultiplier))), Component.literal("§9T.O Magic 'n Extras"));
    }
}
