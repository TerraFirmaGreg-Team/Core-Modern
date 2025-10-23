package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

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
    private static final ResourceLocation JUPITER_TEXTURE = new ResourceLocation(TFGCore.MOD_ID, "textures/gui/jupiter_planet");

    static {
        AdAstraClientEvents.RenderSolarSystemEvent.register((graphics, solarSystem, width, height) -> {
            if (PlanetConstants.SOLAR_SYSTEM.equals(solarSystem)) {
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuilder();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                int color = 0xff24327b;
                PlanetsScreen.drawCircle(bufferBuilder, (float) width / 2.0F, (float) height / 2.0F, 180, 75, color);
                tessellator.end();

                // Jupiter
                graphics.pose().pushPose();
                graphics.pose().translate(width / 2f, height / 2f, 0);
                graphics.pose().mulPose(Axis.ZP.rotationDegrees((Util.getMillis() / 100f) / 5 / 2));
                graphics.pose().translate(145, 0, 0);
                graphics.blit(JUPITER_TEXTURE, 0, 0, 0, 0, 12, 12, 12, 12);
                graphics.pose().popPose();

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
