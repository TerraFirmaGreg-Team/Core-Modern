package su.terrafirmagreg.core.compat.emi.widgets;

import com.simibubi.create.AllBlocks;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import electrolyte.greate.compat.jei.category.animations.TieredAnimatedMixer;
import electrolyte.greate.content.kinetics.mixer.TieredMechanicalMixerBlock;

/**
 * Animated mixer draw without a basin.
 */
public class BasinlessTieredMixer extends TieredAnimatedMixer {

    public BasinlessTieredMixer(TieredMechanicalMixerBlock block) {
        super(block);
    }

    @Override
    public GuiGameElement.GuiRenderBuilder blockElement(BlockState state) {
        if (state.is(AllBlocks.BASIN.get())) {
            return super.blockElement(Blocks.AIR.defaultBlockState());
        }
        return super.blockElement(state);
    }
}
