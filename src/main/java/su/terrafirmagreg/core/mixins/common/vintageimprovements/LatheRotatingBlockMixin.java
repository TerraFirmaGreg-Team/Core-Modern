package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;

import com.negodya1.vintageimprovements.content.kinetics.lathe.LatheRotatingBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = LatheRotatingBlock.class, remap = false)
public class LatheRotatingBlockMixin implements IWrenchable {

    // Stops the lathe from being wrenchable to rotate it, because that crashes

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.FAIL;
    }
}
