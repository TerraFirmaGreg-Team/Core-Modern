package su.terrafirmagreg.core.compat.kjs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.notenoughmail.kubejs_tfc.block.internal.ExtendedPropertiesBlockBuilder;
import com.notenoughmail.kubejs_tfc.event.RegisterInteractionsEventJS;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;

import su.terrafirmagreg.core.common.data.blocks.ParticleEmitterDecorationBlock;

// KubeJS builder for decoration particle emitters.
public class ParticleEmitterDecorationBlockBuilder extends ExtendedPropertiesBlockBuilder {

    public static final List<net.minecraft.world.level.block.Block> REGISTERED_BLOCKS = new ArrayList<>();

    public transient VoxelShape cachedShape;
    public transient Supplier<Item> preexistingItem;
    public transient int rotate;

    // Particle configuration defaults.
    public transient Supplier<SimpleParticleType> particleType = () -> (SimpleParticleType) net.minecraft.core.particles.ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
    public transient double baseX = 0.5, baseY = 0.5, baseZ = 0.5;
    public transient double offsetX = 0.25, offsetY = 1.0, offsetZ = 0.25;
    public transient double velocityX = 0.0, velocityY = 0.0, velocityZ = 0.0;
    public transient int particleCount = 1;
    public transient boolean particleForced = false;
    public transient boolean useDustOptions = false;
    public transient float dustRed = 1.0f, dustGreen = 0.0f, dustBlue = 0.0f, dustScale = 1.0f;
    private transient boolean hasTicker = false;
    public transient int emitDelay = 0;

    public ParticleEmitterDecorationBlockBuilder(ResourceLocation i) {
        super(i);
        noCollision = true;
        hardness = 0;
        rotate = 0;
        fullBlock = false;
        opaque = false;
        notSolid = true;
        renderType = "cutout";
        soundType = SoundType.GRASS;
        mapColor(MapColor.NONE);
    }

    @Info("Enable/disable block entity ticker (default false).")
    public ParticleEmitterDecorationBlockBuilder hasTicker(boolean enabled) {
        this.hasTicker = enabled;
        return this;
    }

    @Info("Random emission delay scale")
    public ParticleEmitterDecorationBlockBuilder emitDelay(int delay) {
        this.emitDelay = Math.max(0, delay);
        return this;
    }

    @Info("Starting emission position (default: center -> 0.5, 0.5, 0.5).")
    public ParticleEmitterDecorationBlockBuilder particleBase(double x, double y, double z) {
        baseX = x;
        baseY = y;
        baseZ = z;
        return this;
    }

    @Info("Attach existing item instead of generating new.")
    public ParticleEmitterDecorationBlockBuilder withPreexistingItem(ResourceLocation item) {
        itemBuilder = null;
        preexistingItem = Lazy.of(() -> RegistryInfo.ITEM.getValue(item));
        RegisterInteractionsEventJS.addBlockItemPlacement(preexistingItem, this);
        return this;
    }

    @Info("Rotate generated models 45 degrees.")
    public ParticleEmitterDecorationBlockBuilder notAxisAligned() {
        rotate = 45;
        return this;
    }

    @Info("Particle type id (SimpleParticleType 'minecraft:dust' enables dust options).")
    public ParticleEmitterDecorationBlockBuilder particle(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        particleType = Lazy.of(() -> {
            ParticleType<?> pt = RegistryInfo.PARTICLE_TYPE.getValue(rl);
            if (pt instanceof SimpleParticleType simple) {
                if (id.equals("minecraft:dust"))
                    useDustOptions = true;
                return simple;
            }
            throw new IllegalArgumentException("Particle type '" + id + "' is not a SimpleParticleType");
        });
        return this;
    }

    @Info("Random spread ranges (default 0.25, 1.0, 0.25).")
    public ParticleEmitterDecorationBlockBuilder particleOffset(double x, double y, double z) {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        return this;
    }

    @Info("Particle velocity (default 0, 0, 0).")
    public ParticleEmitterDecorationBlockBuilder particleVelocity(double x, double y, double z) {
        velocityX = x;
        velocityY = y;
        velocityZ = z;
        return this;
    }

    @Info("Particles per emission (>=1; default 1).")
    public ParticleEmitterDecorationBlockBuilder particleCount(int count) {
        particleCount = count;
        return this;
    }

    @Info("Always visible (default false).")
    public ParticleEmitterDecorationBlockBuilder particleForced(boolean forced) {
        particleForced = forced;
        return this;
    }

    @Info("Dust color r, g, b + scale (only if dust particle chosen).")
    public ParticleEmitterDecorationBlockBuilder dustColor(float r, float g, float b, float scale) {
        dustRed = r;
        dustGreen = g;
        dustBlue = b;
        dustScale = scale;
        return this;
    }

    @HideFromJS
    public VoxelShape getShape() {
        if (customShape.isEmpty())
            return ParticleEmitterDecorationBlock.DEFAULT_SHAPE;
        if (cachedShape == null)
            cachedShape = BlockBuilder.createShape(customShape);
        return cachedShape;
    }

    @HideFromJS
    public Supplier<Item> itemSupplier() {
        if (preexistingItem != null)
            return preexistingItem;
        if (itemBuilder != null)
            return itemBuilder;
        return null;
    }

    // Creates and optionally registers the emitter block.
    @Override
    public ParticleEmitterDecorationBlock createObject() {
        var props = createProperties().offsetType(BlockBehaviour.OffsetType.XZ);
        var block = new ParticleEmitterDecorationBlock(
                props,
                getShape(),
                itemSupplier(),
                particleType,
                baseX, baseY, baseZ,
                offsetX, offsetY, offsetZ,
                velocityX, velocityY, velocityZ,
                particleCount,
                particleForced,
                useDustOptions,
                dustRed, dustGreen, dustBlue, dustScale,
                hasTicker,
                emitDelay);
        if (hasTicker) {
            REGISTERED_BLOCKS.add(block);
            ParticleEmitterBlockBuilder.REGISTERED_BLOCKS.add(block);
        }
        return block;
    }
}
