package su.terrafirmagreg.core.compat.kjs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.notenoughmail.kubejs_tfc.block.internal.ExtendedPropertiesBlockBuilder;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;

import su.terrafirmagreg.core.common.data.blocks.ActiveParticleBlock;
import su.terrafirmagreg.core.common.data.blocks.ActiveParticleBlock.ParticleConfig;

/**
 * KubeJS builder for ActiveParticleBlock with consumer configuration.
 *
 * <p>Particle emitter configuration consumers:
 * <p>
 * <p>- activeParticles(consumer) for emissions when the block state is active.
 * <p>- inactiveParticles(consumer) for emissions when the block state is inactive.
 *
 * <p>Other options:
 * <p>
 * <p>- hasTicker: Whether a block entity ticker manages emissions.
 * <p>- emitDelay: Random delay scaling for hasTicker.
 * <p>- activeLight/inactiveLight: Light levels for each state.
 */
@SuppressWarnings("unused")
public class GTActiveParticleBuilder extends ExtendedPropertiesBlockBuilder {

    public static final List<net.minecraft.world.level.block.Block> REGISTERED_BLOCKS = new ArrayList<>();

    public transient VoxelShape cachedShape;
    public transient Supplier<Item> preexistingItem;
    public transient Supplier<Item> itemBuilder;

    private transient boolean hasTicker = false;
    private transient int emitDelay = 0;

    private final List<ParticleConfig> inactiveConfigs = new ArrayList<>();
    private final List<ParticleConfig> activeConfigs = new ArrayList<>();
    private transient int inactiveLight = 0;
    private transient int activeLight = 0;

    /**
     * Create a new builder.
     * <p>Ensures the produced block has the ACTIVE state property.
     *
     * @param id registry id
     */
    public GTActiveParticleBuilder(ResourceLocation id) {
        super(id);
        property(GTBlockStateProperties.ACTIVE);
    }

    /**
     * Enable or disable a block entity ticker to control emission source.
     *
     * @param enabled ticker support
     * @return builder
     */
    public GTActiveParticleBuilder hasTicker(boolean enabled) {
        this.hasTicker = enabled;
        return this;
    }

    /**
     * Set the random emission delay scaling factor.
     * <p>Higher values reduce the chance of emission per tick.
     *
     * @param delay non-negative delay scale
     * @return builder
     */
    public GTActiveParticleBuilder emitDelay(int delay) {
        this.emitDelay = Math.max(0, delay);
        return this;
    }

    /**
     * Light level when the block is inactive.
     *
     * @param level 0-15
     * @return builder
     */
    @Info("Inactive light level (0-15).")
    public GTActiveParticleBuilder inactiveLight(int level) {
        this.inactiveLight = Math.max(0, Math.min(15, level));
        return this;
    }

    /**
     * Light level when the block is active.
     *
     * @param level 0-15
     * @return builder
     */
    @Info("Active light level (0-15).")
    public GTActiveParticleBuilder activeLight(int level) {
        this.activeLight = Math.max(0, Math.min(15, level));
        return this;
    }

    @HideFromJS
    public VoxelShape getShape() {
        if (customShape.isEmpty())
            return ActiveParticleBlock.DEFAULT_SHAPE;
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
     * <p>All fields are mutable during construction via JS, then converted
     * into an immutable ParticleConfig with build().
     *
     * <p>Fields:
     * <p>
     * <p>- particle: SimpleParticleType supplier (or 'minecraft:dust' with dust options).
     * <p>- position: Base local offset (x,y,z).
     * <p>- range: Random spread on each axis.
     * <p>- velocity: Particle velocity vector.
     * <p>- count: Particle emission count.
     * <p>- forced: Whether particles are always visible.
     * <p>- dust: Optional RGB + scale for DustParticleOptions.
     */
    public static class ParticleSetBuilder {
        private Supplier<SimpleParticleType> particle = () -> (SimpleParticleType) net.minecraft.core.particles.ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
        private double posX = 0.5, posY = 0.5, posZ = 0.5;
        private double rangeX = 0.0, rangeY = 0.0, rangeZ = 0.0;
        private double velX = 0.0, velY = 0.0, velZ = 0.0;
        private int count = 1;
        private boolean forced = false;
        private boolean useDust = false;
        private float dustR = 1.0f, dustG = 1.0f, dustB = 1.0f, dustScale = 1.0f;

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

        // Legacy alias
        public ParticleSetBuilder base(double x, double y, double z) {
            return position(x, y, z);
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

        // Legacy alias
        public ParticleSetBuilder offset(double x, double y, double z) {
            return range(x, y, z);
        }

        /**
         * Set particle velocity vector.
         * @return builder
         */
        public ParticleSetBuilder velocity(double x, double y, double z) {
            this.velX = x;
            this.velY = y;
            this.velZ = z;
            return this;
        }

        /**
         * Set the number of particles per emission.
         * @return builder
         */
        @Info("Count.")
        public ParticleSetBuilder count(int c) {
            this.count = c;
            return this;
        }

        /**
         * Force particles to be always visible.
         * @return this builder
         */
        @Info("Always visible.")
        public ParticleSetBuilder forced(boolean f) {
            this.forced = f;
            return this;
        }

        /**
         * Configure dust RGB and scale.
         * @return this builder
         */
        @Info("Dust color + scale.")
        public ParticleSetBuilder dust(float r, float g, float b, float scale) {
            this.useDust = true;
            this.dustR = r;
            this.dustG = g;
            this.dustB = b;
            this.dustScale = scale;
            return this;
        }

        /**
         * Convert to immutable ParticleConfig.
         * @return built ParticleConfig
         */
        private ParticleConfig build() {
            return new ParticleConfig(
                    this.particle,
                    this.posX, this.posY, this.posZ,
                    this.rangeX, this.rangeY, this.rangeZ,
                    this.velX, this.velY, this.velZ,
                    this.count,
                    this.forced,
                    this.useDust,
                    this.dustR, this.dustG, this.dustB, this.dustScale);
        }
    }

    /**
     * Add an inactive particle set using a JS consumer.
     * @param consumer configures a ParticleSetBuilder
     * @return builder
     */
    public GTActiveParticleBuilder inactiveParticles(Consumer<ParticleSetBuilder> consumer) {
        ParticleSetBuilder b = new ParticleSetBuilder();
        consumer.accept(b);
        inactiveConfigs.add(b.build());
        return this;
    }

    /**
     * Add an active particle set using a JS consumer.
     * @param consumer configures a ParticleSetBuilder
     * @return builder
     */
    public GTActiveParticleBuilder activeParticles(Consumer<ParticleSetBuilder> consumer) {
        ParticleSetBuilder b = new ParticleSetBuilder();
        consumer.accept(b);
        activeConfigs.add(b.build());
        return this;
    }

    /**
     * Build and return the ActiveParticleBlock.
     * @return constructed ActiveParticleBlock
     */
    @Override
    public ActiveParticleBlock createObject() {
        var block = new ActiveParticleBlock(
                createProperties(),
                getShape(),
                itemSupplier(),
                inactiveConfigs,
                activeConfigs,
                hasTicker,
                emitDelay,
                inactiveLight,
                activeLight);
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
