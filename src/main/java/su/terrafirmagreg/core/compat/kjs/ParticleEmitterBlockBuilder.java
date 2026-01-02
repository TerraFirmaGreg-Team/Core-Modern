package su.terrafirmagreg.core.compat.kjs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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

/**
 * KubeJS builder for ParticleEmitterBlock with consumer configuration.
 *
 * <p>Particle emitter configuration:
 * <p>- particles(consumer): configure a single emission set via ParticleSetBuilder.
 */
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class ParticleEmitterBlockBuilder extends ExtendedPropertiesBlockBuilder {
    public static final List<net.minecraft.world.level.block.Block> REGISTERED_BLOCKS = new ArrayList<>();

    public transient VoxelShape cachedShape;
    public transient Supplier<Item> preexistingItem;
    public transient Supplier<Item> itemBuilder;

    private final ParticleSetBuilder set = new ParticleSetBuilder();
    private transient boolean hasTicker = false;
    public transient int emitDelay = 0;

    public ParticleEmitterBlockBuilder(ResourceLocation id) {
        super(id);
        soundType = SoundType.STONE;
        hardness = 1.5f;
        resistance = 6.0f;
        mapColor(MapColor.STONE);
    }

    public static class ParticleSetBuilder {
        private Supplier<SimpleParticleType> particle = () -> (SimpleParticleType) net.minecraft.core.particles.ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
        private double posX = 0.5, posY = 0.5, posZ = 0.5;
        private double rangeX = 0.25, rangeY = 1.0, rangeZ = 0.25;
        private double velX = 0.0, velY = 0.0, velZ = 0.0;
        private int count = 1;
        private boolean forced = false;
        private boolean useDust = false;
        private float dustR = 1.0f, dustG = 0.0f, dustB = 0.0f, dustScale = 1.0f;

        /**
         * Set particle type by id. Use 'minecraft:dust' to enable dust options.
         * @param id particle resource id
         * @return builder
         */
        public ParticleSetBuilder particle(String id) {
            this.particle = resolveParticleStatic(id, this);
            return this;
        }

        /**
         * Set base position offset relative to the block.
         * @return builder
         */
        public ParticleSetBuilder position(double x, double y, double z) {
            this.posX = x;
            this.posY = y;
            this.posZ = z;
            return this;
        }

        /**
         * Set random spread range for particle spawn.
         * @return builder
         */
        public ParticleSetBuilder range(double x, double y, double z) {
            this.rangeX = x;
            this.rangeY = y;
            this.rangeZ = z;
            return this;
        }

        /**
         * Set particle velocity vector.
         */
        public ParticleSetBuilder velocity(double x, double y, double z) {
            this.velX = x;
            this.velY = y;
            this.velZ = z;
            return this;
        }

        /** Particles per emission (>=1). */
        public ParticleSetBuilder count(int c) {
            this.count = c;
            return this;
        }

        /**
         * Always visible when true.
         */
        public ParticleSetBuilder forced(boolean f) {
            this.forced = f;
            return this;
        }

        /** Configure dust RGB and scale. */
        @Info("Dust color r, g, b + scale (only if dust particle chosen).")
        public ParticleSetBuilder dust(float r, float g, float b, float scale) {
            this.useDust = true;
            this.dustR = r;
            this.dustG = g;
            this.dustB = b;
            this.dustScale = scale;
            return this;
        }
    }

    /**
     * Configure particle emission using a consumer.
     * @param consumer configures a ParticleSetBuilder
     * @return builder
     */
    public ParticleEmitterBlockBuilder particles(Consumer<ParticleSetBuilder> consumer) {
        consumer.accept(set);
        return this;
    }

    /** Enable or disable a block entity ticker to control emission source. */
    public ParticleEmitterBlockBuilder hasTicker(boolean enabled) {
        this.hasTicker = enabled;
        return this;
    }

    /** Set the random emission delay scaling factor (0 = every tick). */
    public ParticleEmitterBlockBuilder emitDelay(int delay) {
        this.emitDelay = Math.max(0, delay);
        return this;
    }

    public ParticleEmitterBlockBuilder particle(String id) {
        set.particle(id);
        return this;
    }

    public ParticleEmitterBlockBuilder position(double x, double y, double z) {
        set.position(x, y, z);
        return this;
    }

    public ParticleEmitterBlockBuilder range(double x, double y, double z) {
        set.range(x, y, z);
        return this;
    }

    public ParticleEmitterBlockBuilder particleVelocity(double x, double y, double z) {
        set.velocity(x, y, z);
        return this;
    }

    public ParticleEmitterBlockBuilder particleCount(int count) {
        set.count(count);
        return this;
    }

    public ParticleEmitterBlockBuilder particleForced(boolean forced) {
        set.forced(forced);
        return this;
    }

    public ParticleEmitterBlockBuilder dustColor(float r, float g, float b, float scale) {
        set.dust(r, g, b, scale);
        return this;
    }

    public ParticleEmitterBlockBuilder dust(float r, float g, float b, float scale) {
        return dustColor(r, g, b, scale);
    }

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

    /**
     * Build and return the ParticleEmitterBlock.
     * @return constructed ParticleEmitterBlock
     */
    @Override
    public ParticleEmitterBlock createObject() {
        BlockBehaviour.Properties props = createProperties();
        ParticleEmitterBlock block = new ParticleEmitterBlock(
                props,
                getShape(),
                itemSupplier(),
                set.particle,
                set.posX, set.posY, set.posZ,
                set.rangeX, set.rangeY, set.rangeZ,
                set.velX, set.velY, set.velZ,
                set.count,
                set.forced,
                set.useDust,
                set.dustR, set.dustG, set.dustB, set.dustScale,
                hasTicker,
                emitDelay);
        if (hasTicker) {
            REGISTERED_BLOCKS.add(block);
        }
        return block;
    }

    /**
     * Resolve a particle id to a SimpleParticleType supplier for ParticleSetBuilder.
     *
     * @param id particle resource id
     * @param b  builder to toggle dust mode on
     * @return lazy supplier of SimpleParticleType
     */
    private static Supplier<SimpleParticleType> resolveParticleStatic(String id, ParticleSetBuilder b) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if ("minecraft:dust".equals(id)) {
            b.useDust = true;
        }
        return Lazy.of(() -> {
            ParticleType<?> pt = RegistryInfo.PARTICLE_TYPE.getValue(rl);
            if (pt instanceof SimpleParticleType simple) {
                return simple;
            }
            throw new IllegalArgumentException("Particle type '" + id + "' is not a SimpleParticleType");
        });
    }
}
