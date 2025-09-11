package su.terrafirmagreg.core.compat.gtceu.behaviors;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolActions;

public final class CanoeCreatorBehavior implements IToolBehavior {

    public static final CanoeCreatorBehavior INSTANCE = create();

    protected CanoeCreatorBehavior() {
        /**/}

    protected static CanoeCreatorBehavior create() {
        return new CanoeCreatorBehavior();
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        level.getBlockState(pos).getToolModifiedState(context, ToolActions.AXE_STRIP, false);

        return InteractionResult.PASS;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level Level, @NotNull List<Component> tooltip,
            @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.canoe_creator"));
    }
}
