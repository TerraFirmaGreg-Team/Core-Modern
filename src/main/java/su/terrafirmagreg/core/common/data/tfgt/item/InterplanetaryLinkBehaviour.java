package su.terrafirmagreg.core.common.data.tfgt.item;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class InterplanetaryLinkBehaviour implements IInteractionItem, IAddInformation {

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack heldItem = player.getItemInHand(usedHand);
        if (level.isClientSide()) return InteractionResultHolder.success(heldItem);

        if (heldItem.getCount() == 2 && player.isShiftKeyDown()) {
            new InterplanetaryLink(heldItem).initLink(level, player);
            player.sendSystemMessage(Component.translatable("item.tfg.interplanetarylink.pair_linked"));
        } else if (player.isShiftKeyDown()) {
            InterplanetaryLink.clear(heldItem);
            player.sendSystemMessage(Component.translatable("item.tfg.interplanetarylink.link_cleared"));
            player.sendSystemMessage(Component.translatable("item.tfg.interplanetarylink.not_linked_hint"));
        }
        return InteractionResultHolder.success(heldItem);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        new InterplanetaryLink(stack).appendHoverText(level, tooltipComponents);
    }
}
