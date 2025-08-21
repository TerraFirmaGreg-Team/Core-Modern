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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGParticles;

import java.util.Arrays;
import java.util.List;

@Mixin(value = ClientForgeEventHandler.class, remap = false)
public abstract class ClientForgeEventHandlerMixin {
    @Unique
    private static int tfg$particleCounter = 0;
    @Unique
    private static List<ResourceLocation> tfg$windyMartianBiomeTags = Arrays.asList(
            ResourceLocation.tryParse("tfg:has_dark_sand_particles"),
            ResourceLocation.tryParse("tfg:has_medium_sand_particles"),
            ResourceLocation.tryParse("tfg:has_light_sand_particles")
    );
    @Definition(id = "WIND", field = "Lnet/dries007/tfc/client/particle/TFCParticles;WIND:Lnet/minecraftforge/registries/RegistryObject;")
    @Definition(id = "get", method = "Lnet/minecraftforge/registries/RegistryObject;get()Ljava/lang/Object;")
    @Definition(id = "ParticleOptions", type = ParticleOptions.class)
    @Expression("(ParticleOptions) WIND.get()")
    @ModifyExpressionValue(method = "tickWind()V", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static ParticleOptions tfg$redirectWindOnMars(ParticleOptions original, @Local Level level, @Local BlockPos pos) {
//            if (AdAstraData.isPlanet(level.dimension()) && AdAstraData.getPlanet(level.dimension()).dimension().location().getPath().equals("mars"))

//        switch
        if (tfg$particleCounter++ > 20) {
            TFGCore.LOGGER.info(level.getBiome(pos).tags().map(TagKey::location).toList().toString());
            level.players().get(0).sendSystemMessage(Component.literal(level.getBiome(pos).tags().map(TagKey::location).toList().toString()));
            tfg$particleCounter = 0;
        }
        if (level.getBiome(pos).tags().map(TagKey::location).anyMatch(tfg$windyMartianBiomeTags::contains))
            {
                return switch (level.random.nextInt(3)) {
                    case 0 -> TFGParticles.LIGHT_MARS_WIND.get();
                    case 1 -> TFGParticles.MEDIUM_MARS_WIND.get();
                    case 2 -> TFGParticles.DEEP_MARS_WIND.get();
                    default -> TFGParticles.LIGHT_MARS_WIND.get();
                };
            } else return original;
    }
}
