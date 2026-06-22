package su.terrafirmagreg.core.common.block.girder;

import java.util.function.Predicate;
import java.util.function.Supplier;

import com.simibubi.create.content.decoration.girder.GirderPlacementHelper;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/***
 * Credit: Create: More Girders
 */
public class TFGGirderPlacementHelper extends GirderPlacementHelper {
    private final Supplier<? extends Block> block;

    public TFGGirderPlacementHelper(Supplier<? extends Block> block) {
        this.block = block;
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return state -> state.getBlock() == block.get();
    }

    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return stack -> stack.getItem() == block.get().asItem();
    }
}
