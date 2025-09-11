/*
 * This file includes code from Wan's Ancient Beasts (https://www.curseforge.com/minecraft/mc-mods/wans-ancient-beasts)
 * Copyright (c) 2024 WanMine
 * All Rights Reserved, though permission to use mixins was given
 */

package su.terrafirmagreg.core.mixins.common.wab;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.wanmine.wab.entity.Surfer;

@Mixin(value = Surfer.class)
public abstract class SurferMixin extends AbstractHorse {

    @Shadow(remap = false)
    public abstract void setCoralColor(String color);

    protected SurferMixin(EntityType<? extends AbstractHorse> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    // Stop surfers from drowning, the mars water doesn't seem to work right even though
    // surfers should be able to breathe in water
    @Inject(method = "tick", at = @At("HEAD"))
    private void tfg$tick(CallbackInfo ci) {
        this.setAirSupply(300);
    }

    // Randomly set their color
    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    public void tfg$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
            SpawnGroupData groupData, CompoundTag tag, CallbackInfoReturnable<SpawnGroupData> cir) {
        RandomSource rand = level.getRandom();
        float r = rand.nextFloat();

        if (r > 0.9)
            this.setCoralColor("brain");
        else if (r > 0.8)
            this.setCoralColor("bubble");
        else if (r > 0.7)
            this.setCoralColor("tube");
        else if (r > 0.6)
            this.setCoralColor("fire");
        else if (r > 0.5)
            this.setCoralColor("horn");
    }
}
