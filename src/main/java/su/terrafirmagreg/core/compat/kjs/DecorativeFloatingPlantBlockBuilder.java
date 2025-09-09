package su.terrafirmagreg.core.compat.kjs;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import su.terrafirmagreg.core.common.data.blocks.DecorativeFloatingPlantBlock;

public class DecorativeFloatingPlantBlockBuilder extends DecorativePlantBlockBuilder {

	public transient boolean enableXZOffset;

	public DecorativeFloatingPlantBlockBuilder(ResourceLocation i) {
		super(i);

		enableXZOffset = true;
	}

	@Info("Whether to enable the random X/Z offset for each block. Enabled by default")
	public DecorativeFloatingPlantBlockBuilder xz_offset(boolean enable) {
		this.enableXZOffset = enable;
		return this;
	}

	@Override
	public DecorativeFloatingPlantBlock createObject() {
		if (enableXZOffset) {
			return new DecorativeFloatingPlantBlock(createExtendedProperties().offsetType(BlockBehaviour.OffsetType.XZ), getShape());
		}
		else {
			return new DecorativeFloatingPlantBlock(createExtendedProperties(), getShape());
		}
	}
}
