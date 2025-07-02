package su.terrafirmagreg.core.compat.kjs;

import com.notenoughmail.kubejs_tfc.block.internal.ExtendedPropertiesBlockBuilder;
import com.notenoughmail.kubejs_tfc.event.RegisterInteractionsEventJS;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;
import su.terrafirmagreg.core.common.data.blocks.ParticleEmitterBlock;

import java.util.function.Supplier;

public class ParticleEmitterBlockBuilder extends ExtendedPropertiesBlockBuilder {

	public transient VoxelShape cachedShape;
	public transient Supplier<Item> preexistingItem;
	public transient int rotate;

	public transient Supplier<SimpleParticleType> particleType = () -> (SimpleParticleType) net.minecraft.core.particles.ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
	public transient double offsetX = 0.25, offsetY = 1.0, offsetZ = 0.25;
	public transient double velocityX = 0.0, velocityY = 0.07, velocityZ = 0.0;
	public transient int particleCount = 1;
	public transient boolean particleForced = false;
	public transient boolean useDustOptions = false;
	public transient float dustRed = 1.0f, dustGreen = 0.0f, dustBlue = 0.0f, dustScale = 1.0f;

	public ParticleEmitterBlockBuilder(ResourceLocation i) {
		super(i);
		hardness = 1.5f;
		resistance = 6;
		rotate = 0;
		soundType = SoundType.STONE;

		mapColor(MapColor.NONE);
	}

	@Info("Sets the 'block item' of this block to an existing item")
	public ParticleEmitterBlockBuilder withPreexistingItem(ResourceLocation item) {
		itemBuilder = null;
		preexistingItem = Lazy.of(() -> RegistryInfo.ITEM.getValue(item));
		RegisterInteractionsEventJS.addBlockItemPlacement(preexistingItem, this);
		return this;
	}

	@Info("Sets the particle type emitted by the block (example: 'minecraft:bubble')")
	public ParticleEmitterBlockBuilder particle(String id) {
		ResourceLocation rl = ResourceLocation.tryParse(id);
		particleType = Lazy.of(() -> {
			ParticleType<?> pt = RegistryInfo.PARTICLE_TYPE.getValue(rl);
			if (pt instanceof SimpleParticleType simple) {
				return simple;
			}
			throw new IllegalArgumentException("Particle type '" + id + "' is not a SimpleParticleType");
		});

		if (id.equals("minecraft:dust")) {
			useDustOptions = true;
		}

		return this;
	}

	@Info("Sets the offset range for particles (default: 0.25, 1.0, 0.25)")
	public ParticleEmitterBlockBuilder particleOffset(double x, double y, double z) {
		offsetX = x;
		offsetY = y;
		offsetZ = z;
		return this;
	}

	@Info("Sets the particle velocity (default: 0.0, 0.07, 0.0)")
	public ParticleEmitterBlockBuilder particleVelocity(double x, double y, double z) {
		velocityX = x;
		velocityY = y;
		velocityZ = z;
		return this;
	}

	@Info("Sets the number of particles emitted per tick (default: 1)")
	public ParticleEmitterBlockBuilder particleCount(int count) {
		particleCount = count;
		return this;
	}

	@Info("If true, particles are always visible in 512 blocks and on minimal visual setting. Normal is 32 blocks. (default: false)")
	public ParticleEmitterBlockBuilder particleForced(boolean forced) {
		particleForced = forced;
		return this;
	}

	@Info("Set RGB color and scale for 'dust' particle type (float from 0.0 to 1.0)")
	public ParticleEmitterBlockBuilder dustColor(float r, float g, float b, float scale) {
		this.dustRed = r;
		this.dustGreen = g;
		this.dustBlue = b;
		this.dustScale = scale;
		return this;
	}

	@HideFromJS
	public VoxelShape getShape() {
		if (customShape.isEmpty()) {
			return ParticleEmitterBlock.DEFAULT_SHAPE;
		}
		if (cachedShape == null) {
			cachedShape = BlockBuilder.createShape(customShape);
		}
		return cachedShape;
	}

	@HideFromJS
	public Supplier<Item> itemSupplier() {
		if (preexistingItem != null) return preexistingItem;
		if (itemBuilder != null) return itemBuilder;
		return null;
	}

	@Override
	public ParticleEmitterBlock createObject() {
		return new ParticleEmitterBlock(
				createProperties(),
				getShape(),
				itemSupplier(),
				particleType,
				offsetX, offsetY, offsetZ,
				velocityX, velocityY, velocityZ,
				particleCount,
				particleForced,
				useDustOptions,
				dustRed, dustGreen, dustBlue, dustScale
		);
	}
}
