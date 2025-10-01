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

public class ParticleEmitterBlockBuilder extends ExtendedPropertiesBlockBuilder {

    public static final List<net.minecraft.world.level.block.Block> REGISTERED_BLOCKS = new ArrayList<>();

    public transient VoxelShape cachedShape;
    public transient Supplier<Item> preexistingItem;
    public transient Supplier<Item> itemBuilder;

    public transient Supplier<SimpleParticleType> particleType = () -> (SimpleParticleType) net.minecraft.core.particles.ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;

    public transient double baseX = 0.5, baseY = 0.5, baseZ = 0.5;
    public transient double offsetX = 0.25, offsetY = 1.0, offsetZ = 0.25;
    public transient double velocityX = 0.0, velocityY = 0.07, velocityZ = 0.0;
    public transient int particleCount = 1;
    public transient boolean particleForced = false;
    public transient boolean useDustOptions = false;
    public transient float dustRed = 1.0f, dustGreen = 0.0f, dustBlue = 0.0f, dustScale = 1.0f;

    private transient boolean hasTicker = false;

    public ParticleEmitterBlockBuilder(ResourceLocation id) {
        super(id);
        soundType = SoundType.STONE;
        hardness = 1.5f;
        resistance = 6.0f;
        mapColor(MapColor.STONE);
    }

    @Info("Enable/disable block entity ticker (default true)")
    public ParticleEmitterBlockBuilder hasTicker(boolean enabled) {
        this.hasTicker = enabled;
        return this;
    }

    @Info("Sets the initial base position inside the block (default 0.5, 0.5, 0.5)")
    public ParticleEmitterBlockBuilder particleBase(double x, double y, double z) {
        baseX = x;
        baseY = y;
        baseZ = z;
        return this;
    }

    @Info("Sets the particle type (example: 'minecraft:bubble')")
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

    @Info("Sets random spread ranges (default 0.25,1.0,0.25)")
    public ParticleEmitterBlockBuilder particleOffset(double x, double y, double z) {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        return this;
    }

    @Info("Sets particle velocity (default 0,0.07,0)")
    public ParticleEmitterBlockBuilder particleVelocity(double x, double y, double z) {
        velocityX = x;
        velocityY = y;
        velocityZ = z;
        return this;
    }

    @Info("Sets number of particles per emission (default 1)")
    public ParticleEmitterBlockBuilder particleCount(int count) {
        particleCount = count;
        return this;
    }

    @Info("Particles always visible (default false)")
    public ParticleEmitterBlockBuilder particleForced(boolean forced) {
        particleForced = forced;
        return this;
    }

    @Info("Dust particle RGB + scale")
    public ParticleEmitterBlockBuilder dustColor(float r, float g, float b, float scale) {
        dustRed = r;
        dustGreen = g;
        dustBlue = b;
        dustScale = scale;
        return this;
    }

    @Info("Use an existing item as the block item")
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
                hasTicker);
        if (hasTicker) {
            REGISTERED_BLOCKS.add(block);
        }
        return block;
    }
}
