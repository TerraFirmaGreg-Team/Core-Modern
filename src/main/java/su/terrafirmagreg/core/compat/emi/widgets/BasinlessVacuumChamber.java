package su.terrafirmagreg.core.compat.emi.widgets;

import com.negodya1.vintageimprovements.compat.jei.category.animations.AnimatedVacuumChamber;
import com.simibubi.create.AllBlocks;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Animated vacuum chamber without a basin.
 */
public class BasinlessVacuumChamber extends AnimatedVacuumChamber {

    public BasinlessVacuumChamber() {
        super();
    }

    @Override
    public GuiGameElement.GuiRenderBuilder blockElement(BlockState state) {
        if (state.is(AllBlocks.BASIN.get())) {
            return super.blockElement(Blocks.AIR.defaultBlockState());
        }
        return super.blockElement(state);
    }
}
