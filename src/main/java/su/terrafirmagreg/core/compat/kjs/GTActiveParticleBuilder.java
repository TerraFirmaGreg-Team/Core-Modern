package su.terrafirmagreg.core.compat.kjs;

import java.util.ArrayList;
import java.util.List;
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

// KubeJS Builder for an GT active/inactive dual particle block.
public class GTActiveParticleBuilder extends ExtendedPropertiesBlockBuilder {

    public static final List<net.minecraft.world.level.block.Block> REGISTERED_BLOCKS = new ArrayList<>();

    public transient VoxelShape cachedShape;
    public transient Supplier<Item> preexistingItem;
    public transient Supplier<Item> itemBuilder;

    // Inactive defaults
    private transient Supplier<SimpleParticleType> inactiveParticle = () -> (SimpleParticleType) net.minecraft.core.particles.ParticleTypes.ASH;
    private transient boolean hasInactive = false;
    private transient double inactiveBaseX = 0.5, inactiveBaseY = 0.5, inactiveBaseZ = 0.5;
    private transient double inactiveOffsetX, inactiveOffsetY, inactiveOffsetZ;
    private transient double inactiveVelX, inactiveVelY, inactiveVelZ;
    private transient int inactiveCount = 1;
    private transient boolean inactiveForced = false;
    private transient boolean inactiveUseDust = false;
    private transient float inactiveDustRed, inactiveDustGreen, inactiveDustBlue, inactiveDustScale;
    private transient int inactiveLight = 0;

    // Active defaults
    private transient Supplier<SimpleParticleType> activeParticle = () -> (SimpleParticleType) net.minecraft.core.particles.ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
    private transient boolean hasActive = false;
    private transient double activeBaseX = 0.5, activeBaseY = 0.5, activeBaseZ = 0.5;
    private transient double activeOffsetX, activeOffsetY, activeOffsetZ;
    private transient double activeVelX, activeVelY, activeVelZ;
    private transient int activeCount = 1;
    private transient boolean activeForced = false;
    private transient boolean activeUseDust = false;
    private transient float activeDustRed, activeDustGreen, activeDustBlue, activeDustScale;
    private transient int activeLight = 0;

    private transient boolean hasTicker = false;
    private transient int emitDelay = 0;

    public GTActiveParticleBuilder(ResourceLocation id) {
        super(id);
        property(GTBlockStateProperties.ACTIVE);
    }

    @Info("Enable/disable block entity ticker (default false).")
    public GTActiveParticleBuilder hasTicker(boolean enabled) {
        this.hasTicker = enabled;
        return this;
    }

    @Info("Random emission delay scale")
    public GTActiveParticleBuilder emitDelay(int delay) {
        this.emitDelay = Math.max(0, delay);
        return this;
    }

    /**
     * Inactive particle configuration.
     */
    @Info("Inactive particle id.")
    public GTActiveParticleBuilder inactiveParticle(String id) {
        this.inactiveParticle = resolveParticle(id, true);
        this.hasInactive = true;
        return this;
    }

    @Info("Inactive base position.")
    public GTActiveParticleBuilder inactiveBase(double x, double y, double z) {
        this.inactiveBaseX = x;
        this.inactiveBaseY = y;
        this.inactiveBaseZ = z;
        return this;
    }

    @Info("Inactive spread offsets.")
    public GTActiveParticleBuilder inactiveOffset(double x, double y, double z) {
        this.inactiveOffsetX = x;
        this.inactiveOffsetY = y;
        this.inactiveOffsetZ = z;
        return this;
    }

    @Info("Inactive velocity.")
    public GTActiveParticleBuilder inactiveVelocity(double x, double y, double z) {
        this.inactiveVelX = x;
        this.inactiveVelY = y;
        this.inactiveVelZ = z;
        return this;
    }

    @Info("Inactive particle count.")
    public GTActiveParticleBuilder inactiveCount(int count) {
        this.inactiveCount = count;
        return this;
    }

    @Info("Inactive always visible.")
    public GTActiveParticleBuilder inactiveForced(boolean forced) {
        this.inactiveForced = forced;
        return this;
    }

    @Info("Inactive dust color + scale.")
    public GTActiveParticleBuilder inactiveDust(float r, float g, float b, float scale) {
        this.inactiveUseDust = true;
        this.inactiveDustRed = r;
        this.inactiveDustGreen = g;
        this.inactiveDustBlue = b;
        this.inactiveDustScale = scale;
        return this;
    }

    @Info("Inactive light level (0-15).")
    public GTActiveParticleBuilder inactiveLight(int level) {
        this.inactiveLight = Math.max(0, Math.min(15, level));
        return this;
    }

    /**
     * Active particle configuration.
     */
    @Info("Active particle id.")
    public GTActiveParticleBuilder activeParticle(String id) {
        this.activeParticle = resolveParticle(id, false);
        this.hasActive = true;
        return this;
    }

    @Info("Active base position.")
    public GTActiveParticleBuilder activeBase(double x, double y, double z) {
        this.activeBaseX = x;
        this.activeBaseY = y;
        this.activeBaseZ = z;
        return this;
    }

    @Info("Active spread offsets.")
    public GTActiveParticleBuilder activeOffset(double x, double y, double z) {
        this.activeOffsetX = x;
        this.activeOffsetY = y;
        this.activeOffsetZ = z;
        return this;
    }

    @Info("Active velocity.")
    public GTActiveParticleBuilder activeVelocity(double x, double y, double z) {
        this.activeVelX = x;
        this.activeVelY = y;
        this.activeVelZ = z;
        return this;
    }

    @Info("Active particle count.")
    public GTActiveParticleBuilder activeCount(int count) {
        this.activeCount = count;
        return this;
    }

    @Info("Active always visible.")
    public GTActiveParticleBuilder activeForced(boolean forced) {
        this.activeForced = forced;
        return this;
    }

    @Info("Active dust color + scale.")
    public GTActiveParticleBuilder activeDust(float r, float g, float b, float scale) {
        this.activeUseDust = true;
        this.activeDustRed = r;
        this.activeDustGreen = g;
        this.activeDustBlue = b;
        this.activeDustScale = scale;
        return this;
    }

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

    // Creates dual-state particle block with optional ticker registration.
    @Override
    public ActiveParticleBlock createObject() {
        ParticleConfig inactiveCfg = hasInactive ? new ParticleConfig(
                inactiveParticle,
                inactiveBaseX, inactiveBaseY, inactiveBaseZ,
                inactiveOffsetX, inactiveOffsetY, inactiveOffsetZ,
                inactiveVelX, inactiveVelY, inactiveVelZ,
                inactiveCount, inactiveForced,
                inactiveUseDust, inactiveDustRed, inactiveDustGreen, inactiveDustBlue, inactiveDustScale) : null;

        ParticleConfig activeCfg = hasActive ? new ParticleConfig(
                activeParticle,
                activeBaseX, activeBaseY, activeBaseZ,
                activeOffsetX, activeOffsetY, activeOffsetZ,
                activeVelX, activeVelY, activeVelZ,
                activeCount, activeForced,
                activeUseDust, activeDustRed, activeDustGreen, activeDustBlue, activeDustScale) : null;

        var block = new ActiveParticleBlock(
                createProperties(),
                getShape(),
                itemSupplier(),
                inactiveCfg,
                activeCfg,
                hasTicker,
                emitDelay,
                inactiveLight,
                activeLight);
        if (hasTicker) {
            REGISTERED_BLOCKS.add(block);
        }
        return block;
    }

    private Supplier<SimpleParticleType> resolveParticle(String id, boolean isInactive) {
        ResourceLocation rl = ResourceLocation.tryParse(id);

        if ("minecraft:dust".equals(id)) {
            if (isInactive)
                this.inactiveUseDust = true;
            else
                this.activeUseDust = true;
        }

        // Defer registry lookup until first use to avoid ordering issues
        return Lazy.of(() -> {
            ParticleType<?> pt = RegistryInfo.PARTICLE_TYPE.getValue(rl);
            if (pt instanceof SimpleParticleType simple) {
                return simple;
            }
            throw new IllegalArgumentException("Particle type '" + id + "' is not a SimpleParticleType");
        });
    }
}
