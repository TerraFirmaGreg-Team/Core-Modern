package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = Contraption.class, remap = false)
public class ContraptionMixin {

    // Writes a message in chat to nearby players when a contraption is formed in the beneath or on hot planets.
    // Yes, this means it'll also announce to players setting up a tree farm or something. It's fine!

    @Inject(method = "onEntityCreated", at = @At("TAIL"), remap = false)
    private void tfg$onEntityCreated(AbstractContraptionEntity entity, CallbackInfo ci) {
		var level = entity.level();
        var server = level.getServer();
        if (server == null)
            return;

		var dim = level.dimension();
        if (server.isSingleplayer()) {
            if (dim == Level.NETHER && TFGConfig.SERVER.enableBeneathMiningRestrictions.get()) {

                final int y = TFGConfig.SERVER.disabledBeneathMiningYLevel.get();
                server.sendSystemMessage(Component.translatable("tfg.clientmessage.nether_contraption_created", y));

            } else if ((dim == Planet.VENUS || dim == Planet.MERCURY) && TFGConfig.SERVER.enableHotPlanetMiningRestrictions.get()) {

                server.sendSystemMessage(Component.translatable("tfg.clientmessage.hot_planet_contraption_created"));
            }
        } else {
            for (var player : server.getPlayerList().getPlayers()) {
                if (player.position().distanceTo(entity.position()) < 20) {
                    if (dim == Level.NETHER && TFGConfig.SERVER.enableBeneathMiningRestrictions.get()) {

                        final int y = TFGConfig.SERVER.disabledBeneathMiningYLevel.get();
                        player.sendSystemMessage(Component.translatable("tfg.clientmessage.nether_contraption_created", y));

                    } else if ((dim == Planet.VENUS || dim == Planet.MERCURY) && TFGConfig.SERVER.enableHotPlanetMiningRestrictions.get()) {

                        player.sendSystemMessage(Component.translatable("tfg.clientmessage.hot_planet_contraption_created"));
                    }
                }
            }
        }
    }
}
