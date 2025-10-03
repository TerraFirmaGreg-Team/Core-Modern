package su.terrafirmagreg.core.compat.kjs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.notenoughmail.kubejs_tfc.block.internal.ExtendedPropertiesBlockBuilder;

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

import su.terrafirmagreg.core.common.data.blocks.ParticleEmitterBlock;

// KubeJS builder for a simple particle emitter block.
public class ParticleEmitterBlockBuilder extends ExtendedPropertiesBlockBuilder {
    public static final List<net.minecraft.world.level.block.Block> REGISTERED_BLOCKS = new ArrayList<>();

    public transient VoxelShape cachedShape;
    public transient Supplier<Item> preexistingItem;
    public transient Supplier<Item> itemBuilder;

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

    public ParticleEmitterBlockBuilder(ResourceLocation id) {
        super(id);
        soundType = SoundType.STONE;
        hardness = 1.5f;
        resistance = 6.0f;
        mapColor(MapColor.STONE);
    }

    @Info("Enable/disable block entity ticker (default false).")
    public ParticleEmitterBlockBuilder hasTicker(boolean enabled) {
        this.hasTicker = enabled;
        return this;
    }

    @Info("Random emission delay scale")
    public ParticleEmitterBlockBuilder emitDelay(int delay) {
        this.emitDelay = Math.max(0, delay);
        return this;
    }

    @Info("Starting emission position (default: center -> 0.5, 0.5, 0.5).")
    public ParticleEmitterBlockBuilder particleBase(double x, double y, double z) {
        baseX = x;
        baseY = y;
        baseZ = z;
        return this;
    }

    @Info("Particle type id (SimpleParticleType 'minecraft:dust' enables dust options).")
    public ParticleEmitterBlockBuilder particle(String id) {
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
    public ParticleEmitterBlockBuilder particleOffset(double x, double y, double z) {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        return this;
    }

    @Info("Particle velocity (default 0, 0, 0).")
    public ParticleEmitterBlockBuilder particleVelocity(double x, double y, double z) {
        velocityX = x;
        velocityY = y;
        velocityZ = z;
        return this;
    }

    @Info("Particles per emission (>=1; default 1).")
    public ParticleEmitterBlockBuilder particleCount(int count) {
        particleCount = count;
        return this;
    }

    @Info("Always visible (default false).")
    public ParticleEmitterBlockBuilder particleForced(boolean forced) {
        particleForced = forced;
        return this;
    }

    @Info("Dust color r, g, b + scale (only if dust particle chosen).")
    public ParticleEmitterBlockBuilder dustColor(float r, float g, float b, float scale) {
        dustRed = r;
        dustGreen = g;
        dustBlue = b;
        dustScale = scale;
        return this;
    }

    @Info("Attach existing item instead of generating new.")
    public ParticleEmitterBlockBuilder withPreexistingItem(ResourceLocation item) {
        itemBuilder = null;
        preexistingItem = Lazy.of(() -> RegistryInfo.ITEM.getValue(item));
        return this;
    }

    @HideFromJS
    public VoxelShape getShape() {
        if (customShape.isEmpty())
            return ParticleEmitterBlock.DEFAULT_SHAPE;
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
    public ParticleEmitterBlock createObject() {
        BlockBehaviour.Properties props = createProperties();
        ParticleEmitterBlock block = new ParticleEmitterBlock(
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
        }
        return block;
    }
}
