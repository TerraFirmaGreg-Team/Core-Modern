/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */
package su.terrafirmagreg.core.mixins.client.tfc;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.dries007.tfc.client.ClientForgeEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGParticles;
import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = ClientForgeEventHandler.class, remap = false)
public abstract class ClientForgeEventHandlerMixin {
    @Unique
    private static int tfg$particleCounter = 0;

    @SuppressWarnings("LocalMayBeArgsOnly")
    @Definition(id = "WIND", field = "Lnet/dries007/tfc/client/particle/TFCParticles;WIND:Lnet/minecraftforge/registries/RegistryObject;")
    @Definition(id = "get", method = "Lnet/minecraftforge/registries/RegistryObject;get()Ljava/lang/Object;")
    @Definition(id = "ParticleOptions", type = ParticleOptions.class)
    @Expression("(ParticleOptions) WIND.get()")
    @ModifyExpressionValue(method = "tickWind()V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static ParticleOptions tfg$redirectWindOnMars(ParticleOptions original, @Local Level level, @Local BlockPos pos) {
//            if (AdAstraData.isPlanet(level.dimension()) && AdAstraData.getPlanet(level.dimension()).dimension().location().getPath().equals("mars"))

//        switch
        if (tfg$particleCounter++ > 20) {
            if (level.players().get(0).isShiftKeyDown()) {
                TFGCore.LOGGER.info(level.getBiome(pos).value().toString());
                TFGCore.LOGGER.info(level.getBiome(pos).tags().map(TagKey::location).toList().toString());
            }
            tfg$particleCounter = 0;
        }

        Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(TFGTags.Biomes.HasDarkSandWind)) {
            return TFGParticles.DARK_MARS_WIND.get();
        } else if (biome.is(TFGTags.Biomes.HasMediumSandWind)) {
            return TFGParticles.MEDIUM_MARS_WIND.get();
        } else if (biome.is(TFGTags.Biomes.HasLightSandWind)){
            return TFGParticles.LIGHT_MARS_WIND.get();
        } else return original;
    }
}
