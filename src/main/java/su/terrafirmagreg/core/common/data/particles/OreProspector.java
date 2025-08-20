package su.terrafirmagreg.core.common.data.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OreProspector extends TextureSheetParticle {

    protected OreProspector(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.lifetime = 120;
        this.gravity = 0;
        this.xd = this.yd = this.zd = 0;
        this.quadSize = 0.5f;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH;
    }

    @Override
    public int getLightColor(float partialTicks) {
        return 0x00F000F0;  // Max brightness packed light value
    }

    // particle sheet with no depth testing
    public static final ParticleRenderType PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tessellator) {
            tessellator.end();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    };
}