package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import earth.terrarium.adastra.api.client.events.AdAstraClientEvents;
import earth.terrarium.adastra.client.screens.PlanetsScreen;
import earth.terrarium.adastra.common.constants.PlanetConstants;

import su.terrafirmagreg.core.TFGCore;

@Mixin(PlanetsScreen.class)
public class PlanetsScreenMixin {
    @Unique
    private static final ResourceLocation JUPITER_TEXTURE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/jupiter_planet.png");
    @Unique
    private static final ResourceLocation MERCURY_BELT_TEXTURE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/mercury_belt.png");
    @Unique
    private static final ResourceLocation ASTEROID_BELT_TEXTURE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/asteroid_belt.png");
    @Unique
    private static final ResourceLocation MOON_ORBIT_TEXTURE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/moon_orbit.png");
    @Unique
    private static final ResourceLocation EUROPA_ORBIT_TEXTURE = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/europa_orbit.png");

    @ModifyArg(method = "lambda$static$11", at = @At(value = "INVOKE", target = "Learth/terrarium/adastra/client/screens/PlanetsScreen;drawCircles(IIILcom/mojang/blaze3d/vertex/BufferBuilder;II)V", ordinal = 0), index = 0, remap = false)
    private static int tfg$modifySolarOrbits0(int value) {
        return 1;
    }

    @ModifyArg(method = "lambda$static$11", at = @At(value = "INVOKE", target = "Learth/terrarium/adastra/client/screens/PlanetsScreen;drawCircles(IIILcom/mojang/blaze3d/vertex/BufferBuilder;II)V", ordinal = 0), index = 1, remap = false)
    private static int tfg$modifySolarOrbits1(int value) {
        return 3;
    }

    @ModifyConstant(method = "lambda$static$11", constant = @Constant(intValue = 31), remap = false)
    private static int tfg$modifyOrbitDist0(int value) {
        return 30;
    }

    @ModifyConstant(method = "lambda$static$11", constant = @Constant(intValue = 10), remap = false)
    private static int tfg$modifyOrbitDist1(int value) {
        return 6;
    }

    static {
        AdAstraClientEvents.RenderSolarSystemEvent.register((graphics, solarSystem, width, height) -> {
            if (PlanetConstants.SOLAR_SYSTEM.equals(solarSystem)) {
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuilder();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                int color = 0xff24327b;
                int jupiterDist = 190;
                PlanetsScreen.drawCircle(bufferBuilder, (float) width / 2.0F, (float) height / 2.0F, jupiterDist, 75, color);
                //For Saturn//PlanetsScreen.drawCircle(bufferBuilder, (float) width / 2.0F, (float) height / 2.0F, 250, 75, color);

                tessellator.end();

                float rotation = (float) Util.getMillis() / 100.0F;

                int mercury_belt_size = 72;
                int mercury_belt_center = mercury_belt_size / 2;

                // Mercury but a belt
                graphics.pose().pushPose();
                graphics.pose().translate(width / 2f - mercury_belt_center, height / 2f - mercury_belt_center, 0);
                graphics.pose().rotateAround(Axis.ZP.rotationDegrees(rotation * 3 / 2), mercury_belt_center, mercury_belt_center, 0);
                graphics.blit(MERCURY_BELT_TEXTURE, 0, 0, 0, 0, mercury_belt_size, mercury_belt_size, mercury_belt_size, mercury_belt_size);
                graphics.pose().popPose();

                int asteroid_belt_size = 342;
                int asteroid_belt_center = asteroid_belt_size / 2;

                // Asteroid Belt
                graphics.pose().pushPose();
                graphics.pose().translate(width / 2f - asteroid_belt_center, height / 2f - asteroid_belt_center, 0);
                graphics.pose().rotateAround(Axis.ZP.rotationDegrees(rotation / 4 / 2), asteroid_belt_center, asteroid_belt_center, 0);
                graphics.blit(ASTEROID_BELT_TEXTURE, 0, 0, 0, 0, asteroid_belt_size, asteroid_belt_size, asteroid_belt_size, asteroid_belt_size);
                graphics.pose().popPose();

                // Moon
                graphics.pose().pushPose();
                graphics.pose().translate((float) width / 2f, (float) height / 2f, 0);
                graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation));
                graphics.pose().translate(-8, -8, 0);
                graphics.pose().translate((30 * 3 - 6), 0, 0);
                graphics.pose().rotateAround(Axis.ZP.rotationDegrees(rotation * 3 / 2), 14, 14, 0);
                graphics.blit(MOON_ORBIT_TEXTURE, 0, 0, 0, 0, 28, 28, 28, 28);
                graphics.pose().popPose();

                // Jupiter
                graphics.pose().pushPose();
                graphics.pose().translate(width / 2f, height / 2f, 0);
                graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation / 5 / 2));
                graphics.pose().translate(jupiterDist - 8, 0, 0);
                graphics.blit(JUPITER_TEXTURE, 0, 0, 0, 0, 16, 16, 16, 16);
                graphics.pose().popPose();

                // Europa
                graphics.pose().pushPose();
                graphics.pose().translate((float) width / 2f, (float) height / 2f, 0);
                graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation / 5 / 2));
                graphics.pose().translate(-10, -10, 0);
                graphics.pose().translate(jupiterDist - 8, 0, 0);
                graphics.pose().rotateAround(Axis.ZP.rotationDegrees(rotation * 6 / 2), 18, 18, 0);
                graphics.blit(MOON_ORBIT_TEXTURE, 0, 0, 0, 0, 36, 36, 36, 36);
                graphics.pose().popPose();

                /*// Saturn
                graphics.pose().pushPose();
                graphics.pose().translate(width / 2f, height / 2f, 0);
                graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation / 6 / 2));
                graphics.pose().translate(250 - 10, 0, 0);
                graphics.blit(JUPITER_TEXTURE, 0, 0, 0, 0, 16, 16, 16, 16);
                graphics.pose().popPose();*/

            }

            /*  if (PlanetConstants.PROXIMA_CENTAURI.equals(solarSystem)) {
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuilder();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                int color = 0xff008080;
               // PlanetsScreen.drawCircle(bufferBuilder, (double)((float)width / 2.0F), (double)((float)height / 2.0F), 30.0D, 75, color);
                tessellator.end();
            
            }*/
        });
    }
}
