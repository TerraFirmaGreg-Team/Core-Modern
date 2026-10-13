package su.terrafirmagreg.core.client;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.cake.struts.compat.flywheel.StrutsFlywheelCompatLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.client.render.entity.SimpleMobRenderer;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.model.CamelModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.asphalt.AsphaltRoadColorHandlers;
import su.terrafirmagreg.core.client.util.UVRange;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.TFGEntities;
import su.terrafirmagreg.core.common.data.TFGPlant;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;
import su.terrafirmagreg.core.common.entity.camels.TFCBactrianCamelModel;
import su.terrafirmagreg.core.common.entity.camels.TFCBactrianCamelRenderer;
import su.terrafirmagreg.core.common.food.nutrient.NutrientEffectsHandler;
import su.terrafirmagreg.core.common.perf.SupportCache;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ForgeClientEventListener {

    private static final ResourceLocation VENUS_SKY = TFGCore.id("textures/environment/venus_sky.png");

    public static final UVRange[] TEXTURE_FACES = new UVRange[] {
            new UVRange(0, 0, 1.0F / 3.0F, 1.0F / 2.0F), // bottom
            new UVRange(1.0F / 3.0F, 1.0F / 2.0F, 2.0F / 3.0F, 1), // north
            new UVRange(2.0F / 3.0F, 0, 1, 1.0F / 2.0F), // south
            new UVRange(1.0F / 3.0F, 0, 2.0F / 3.0F, 1.0F / 2.0F), // top
            new UVRange(2.0F / 3.0F, 1.0F / 2.0F, 1, 1), // east
            new UVRange(0, 1.0F / 2.0F, 1.0F / 3.0F, 1) // west
    };

    /**
     * Clear the overworld version on disconnect so it doesnt leak into next world
     */
    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientWorldgenVersion.clear();
    }

    /**
     * Evict client-side SupportCache chunk to prevent stale cache info.
     * Clients don't get placement/removal updates for chunks that aren't in range, so we can't trust the cache
     * for those chunks.
     */
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level) {
            ChunkPos pos = event.getChunk().getPos();
            SupportCache.forLevel(level).evictChunk(pos.x, pos.z);
        }
    }

    /**
     * Prevents FOV change for custom speed modifiers.
     */
    @SubscribeEvent
    public static void onComputeFovModifier(ComputeFovModifierEvent event) {
        if (Minecraft.getInstance().options.fovEffectScale().get() <= 0) {
            return;
        }

        Player player = event.getPlayer();
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            AttributeModifier grainModifier = speedAttr.getModifier(NutrientEffectsHandler.GRAIN_SPEED_MODIFIER_UUID);
            if (grainModifier != null) {
                float walkingSpeed = player.getAbilities().getWalkingSpeed();
                if (walkingSpeed > 0) {
                    double speedWithGrain = speedAttr.getValue();
                    double speedWithoutGrain = speedWithGrain / (1.0 + grainModifier.getAmount());

                    float fWith = (float) ((speedWithGrain / walkingSpeed + 1.0) / 2.0);
                    float fWithout = (float) ((speedWithoutGrain / walkingSpeed + 1.0) / 2.0);

                    if (fWith > 0) {
                        event.setNewFovModifier(event.getNewFovModifier() * (fWithout / fWith));
                    }
                }
            }
        }
    }

    public static void registerColorHandlerBlocks(RegisterColorHandlersEvent.Block event) {
        final BlockColor grassColor = (state, level, pos, tintIndex) -> TFCColors.getGrassColor(pos, tintIndex);
        final BlockColor tallGrassColor = (state, level, pos, tintIndex) -> TFCColors.getTallGrassColor(pos, tintIndex);
        final BlockColor magmaColor = (blockState, blockAndTintGetter, blockPos, i) -> 0xFF5500;
        final BlockColor grassBlockColor = (state, level, pos, tintIndex) -> state.getValue(ConnectedGrassBlock.SNOWY) || tintIndex != 1 ? -1 : grassColor.getColor(state, level, pos, tintIndex);

        event.register(tallGrassColor,
                TFGBlocks_Earth.PLANTS.get(TFGPlant.RED_OAT_GRASS).get());
        event.register(grassColor,
                TFGBlocks_Earth.PLANTS.get(TFGPlant.CYCAD).get(),
                TFGBlocks_Earth.PLANTS.get(TFGPlant.CYCAD_PLANT).get(),
                TFGBlocks_Earth.PLANTS.get(TFGPlant.TANK_BROMELIAD).get());
        event.register(grassBlockColor,
                TFGBlocks_Earth.ALFISOL_GRASS.get(),
                TFGBlocks_Earth.ALFISOL_CLAY_GRASS.get(),
                TFGBlocks_Earth.MOLLISOL_GRASS.get(),
                TFGBlocks_Earth.MOLLISOL_CLAY_GRASS.get(),
                TFGBlocks_Earth.OXISOL_GRASS.get(),
                TFGBlocks_Earth.OXISOL_CLAY_GRASS.get(),
                TFGBlocks_Earth.PODZOL_GRASS.get(),
                TFGBlocks_Earth.PODZOL_CLAY_GRASS.get());
        event.register(magmaColor, Blocks.MAGMA_BLOCK);
        TFCBlocks.MAGMA_BLOCKS.values().forEach(registryObject -> {
            event.register(magmaColor, registryObject.get());
        });

        AsphaltRoadColorHandlers.registerBlocks(event);
    }

    public static void registerColorHandlerItems(RegisterColorHandlersEvent.Item event) {
        final ItemColor grassColor = (stack, tintIndex) -> TFCColors.getGrassColor(null, tintIndex);

        event.register(grassColor,
                TFGBlocks_Earth.PLANTS.get(TFGPlant.RED_OAT_GRASS).get());

        AsphaltRoadColorHandlers.registerItems(event);
    }

    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        StrutsFlywheelCompatLoader.registerStrutVisual(TFGBlockEntities.STRUT.get());

        event.registerEntityRenderer(TFGEntities.DROMEDARY_CAMEL.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, CamelModel::new, "dromedary_camel").shadow(0.7f)
                .texture((e) -> ResourceLocation.withDefaultNamespace("textures/entity/camel/camel.png")).build());
        event.registerEntityRenderer(TFGEntities.BACTRIAN_CAMEL.get(),
                ctx -> new TFCBactrianCamelRenderer<>(ctx, new TFCBactrianCamelModel(ctx.bakeLayer(TFCBactrianCamelModel.LAYER_LOCATION)), 0.6f));
    }

    // Taken from the mod Nether Skyboxes
    // https://www.curseforge.com/minecraft/mc-mods/nether-skyboxes
    @SubscribeEvent
    public static void renderVenusSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY)
            return;

        Level level = Minecraft.getInstance().level;
        if (level == null || level.dimension() != Planet.VENUS)
            return;

        PoseStack poseStack = event.getPoseStack();

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.4f);

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        textureManager.bindForSetup(VENUS_SKY);
        RenderSystem.setShaderTexture(0, VENUS_SKY);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        for (int face = 0; face < 6; face++) {
            poseStack.pushPose();
            if (face == 1) {
                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(90.0F)));
            } else if (face == 2) {
                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-90.0F)));
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(180.0F)));
            } else if (face == 3) {
                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(180.0F)));
            } else if (face == 4) {
                poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(90.0F)));
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(-90.0F)));
            } else if (face == 5) {
                poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(-90.0F)));
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(90.0F)));
            }

            Matrix4f matrix4f = poseStack.last().pose();
            UVRange tex = TEXTURE_FACES[face];
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(tex.minU(), tex.minV()).endVertex();
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(tex.minU(), tex.maxV()).endVertex();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(tex.maxU(), tex.maxV()).endVertex();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(tex.maxU(), tex.minV()).endVertex();

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }
}
