/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */
package su.terrafirmagreg.core.mixins.client.tfc;

import earth.terrarium.adastra.api.planets.Planet;
import net.dries007.tfc.client.ClientForgeEventHandler;
import net.dries007.tfc.client.ClientHelpers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import su.terrafirmagreg.core.client.TFGWindManager;

@Mixin(value = ClientForgeEventHandler.class, remap = false)
public abstract class ClientForgeEventHandlerMixin {

    /**
     * @author Zippity
     * @reason too much custom planet-specific behavior required to only use injects
     */
    @Overwrite
    private static void tickWind() {
        final Level level = ClientHelpers.getLevel();
        if (level == null) return;
        if (level.dimension().equals(Planet.MARS)) {
            TFGWindManager.Mars.tickWind(level);
        } else {
            TFGWindManager.Overworld.tickWind(level);
        }
    }
}
