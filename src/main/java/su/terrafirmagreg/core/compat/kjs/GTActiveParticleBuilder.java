package su.terrafirmagreg.core.compat.kjs;

import java.util.function.Supplier;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.notenoughmail.kubejs_tfc.block.internal.ExtendedPropertiesBlockBuilder;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.shapes.VoxelShape;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.rhino.util.HideFromJS;

import su.terrafirmagreg.core.common.data.blocks.ActiveParticleBlock;
import su.terrafirmagreg.core.common.data.blocks.ActiveParticleBlock.ParticleConfig;

/**
 * KubeJS builder for ActiveParticleBlock.
 * Allows independent particle configs for active and inactive states.
 */
public class GTActiveParticleBuilder extends ExtendedPropertiesBlockBuilder {

    /**
     * The Cached shape.
     */
    public transient VoxelShape cachedShape;
    /**
     * The Preexisting item.
     */
    public transient Supplier<Item> preexistingItem;
    /**
     * The Item builder.
     */
    public transient Supplier<Item> itemBuilder;

    // Inactive Config
    private transient Supplier<SimpleParticleType> inactiveParticle = () -> (SimpleParticleType) net.minecraft.core.particles.ParticleTypes.ASH;
    private transient boolean hasInactive = false;
    private transient double inactiveOffsetX, inactiveOffsetY, inactiveOffsetZ;
    private transient double inactiveVelX, inactiveVelY, inactiveVelZ;
    private transient int inactiveCount = 1;
    private transient boolean inactiveForced = false;
    private transient boolean inactiveUseDust = false;
    private transient float inactiveDustRed, inactiveDustGreen, inactiveDustBlue, inactiveDustScale;

    // Active Config
    private transient Supplier<SimpleParticleType> activeParticle = () -> (SimpleParticleType) net.minecraft.core.particles.ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
    private transient boolean hasActive = false;
    private transient double activeOffsetX, activeOffsetY, activeOffsetZ;
    private transient double activeVelX, activeVelY, activeVelZ;
    private transient int activeCount = 1;
    private transient boolean activeForced = false;
    private transient boolean activeUseDust = false;
    private transient float activeDustRed, activeDustGreen, activeDustBlue, activeDustScale;

    /**
     * Instantiates a new Gt active particle builder.
     *
     * @param id the id
     */
    public GTActiveParticleBuilder(ResourceLocation id) {
        super(id);
        property(GTBlockStateProperties.ACTIVE);
    }

    /**
     * Inactive particle gt active particle builder.
     *
     * @param id the id
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder inactiveParticle(String id) {
        this.inactiveParticle = resolveParticle(id, true);
        this.hasInactive = true;
        return this;
    }

    /**
     * Inactive offset gt active particle builder.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder inactiveOffset(double x, double y, double z) {
        this.inactiveOffsetX = x;
        this.inactiveOffsetY = y;
        this.inactiveOffsetZ = z;
        return this;
    }

    /**
     * Inactive velocity gt active particle builder.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder inactiveVelocity(double x, double y, double z) {
        this.inactiveVelX = x;
        this.inactiveVelY = y;
        this.inactiveVelZ = z;
        return this;
    }

    /**
     * Inactive count gt active particle builder.
     *
     * @param count the count
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder inactiveCount(int count) {
        this.inactiveCount = count;
        return this;
    }

    /**
     * Inactive forced gt active particle builder.
     *
     * @param forced the forced
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder inactiveForced(boolean forced) {
        this.inactiveForced = forced;
        return this;
    }

    /**
     * Inactive dust gt active particle builder.
     *
     * @param r     the r
     * @param g     the g
     * @param b     the b
     * @param scale the scale
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder inactiveDust(float r, float g, float b, float scale) {
        this.inactiveUseDust = true;
        this.inactiveDustRed = r;
        this.inactiveDustGreen = g;
        this.inactiveDustBlue = b;
        this.inactiveDustScale = scale;
        return this;
    }

    /**
     * Active particle gt active particle builder.
     *
     * @param id the id
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder activeParticle(String id) {
        this.activeParticle = resolveParticle(id, false);
        this.hasActive = true;
        return this;
    }

    /**
     * Active offset gt active particle builder.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder activeOffset(double x, double y, double z) {
        this.activeOffsetX = x;
        this.activeOffsetY = y;
        this.activeOffsetZ = z;
        return this;
    }

    /**
     * Active velocity gt active particle builder.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder activeVelocity(double x, double y, double z) {
        this.activeVelX = x;
        this.activeVelY = y;
        this.activeVelZ = z;
        return this;
    }

    /**
     * Active count gt active particle builder.
     *
     * @param count the count
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder activeCount(int count) {
        this.activeCount = count;
        return this;
    }

    /**
     * Active forced gt active particle builder.
     *
     * @param forced the forced
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder activeForced(boolean forced) {
        this.activeForced = forced;
        return this;
    }

    /**
     * Active dust gt active particle builder.
     *
     * @param r     the r
     * @param g     the g
     * @param b     the b
     * @param scale the scale
     * @return the gt active particle builder
     */
    public GTActiveParticleBuilder activeDust(float r, float g, float b, float scale) {
        this.activeUseDust = true;
        this.activeDustRed = r;
        this.activeDustGreen = g;
        this.activeDustBlue = b;
        this.activeDustScale = scale;
        return this;
    }

    /**
     * Gets shape.
     *
     * @return the shape
     */
    @HideFromJS
    public VoxelShape getShape() {
        if (customShape.isEmpty()) {
            return ActiveParticleBlock.DEFAULT_SHAPE;
        }
        if (cachedShape == null) {
            cachedShape = BlockBuilder.createShape(customShape);
        }
        return cachedShape;
    }

    /**
     * Item supplier supplier.
     *
     * @return the supplier
     */
    @HideFromJS
    public Supplier<Item> itemSupplier() {
        if (preexistingItem != null)
            return preexistingItem;
        if (itemBuilder != null)
            return itemBuilder;
        return null;
    }

    @Override
    public ActiveParticleBlock createObject() {
        ParticleConfig inactiveCfg = hasInactive ? new ParticleConfig(
                inactiveParticle, inactiveOffsetX, inactiveOffsetY, inactiveOffsetZ,
                inactiveVelX, inactiveVelY, inactiveVelZ,
                inactiveCount, inactiveForced,
                inactiveUseDust, inactiveDustRed, inactiveDustGreen, inactiveDustBlue, inactiveDustScale) : null;

        ParticleConfig activeCfg = hasActive ? new ParticleConfig(
                activeParticle, activeOffsetX, activeOffsetY, activeOffsetZ,
                activeVelX, activeVelY, activeVelZ,
                activeCount, activeForced,
                activeUseDust, activeDustRed, activeDustGreen, activeDustBlue, activeDustScale) : null;

        return new ActiveParticleBlock(
                createProperties(),
                getShape(),
                itemSupplier(),
                inactiveCfg,
                activeCfg);
    }

    /**
     * Item supplier public supplier.
     *
     * @return the supplier
     */
    @HideFromJS
    public Supplier<Item> itemSupplierPublic() {
        return itemSupplier();
    }

    private Supplier<SimpleParticleType> resolveParticle(String id, boolean isInactive) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        ParticleType<?> pt = RegistryInfo.PARTICLE_TYPE.getValue(rl);
        if (pt instanceof SimpleParticleType simple) {
            if (id.equals("minecraft:dust")) {
                if (isInactive)
                    this.inactiveUseDust = true;
                else
                    this.activeUseDust = true;
            }
            return () -> simple;
        }
        throw new IllegalArgumentException("Particle type '" + id + "' is not a SimpleParticleType");
    }
}
