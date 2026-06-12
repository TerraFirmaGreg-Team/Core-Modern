package su.terrafirmagreg.tfcambiental.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.tfcambiental.api.EnvironmentalTemperatureProvider;

public class HouseTester extends Item {
    public HouseTester(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide()) {
            boolean alt = pLevel.random.nextFloat() < 0.005;
            if (EnvironmentalTemperatureProvider.calculateEnclosure(pPlayer, 30)) {
                pPlayer.sendSystemMessage(Component.translatable("tfcambiental.message.is_inside" + (alt ? "_alt" : "")));
            } else {
                pPlayer.sendSystemMessage(Component.translatable("tfcambiental.message.is_outside" + (alt ? "_alt" : "")));
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("tfcambiental.house_tester.description"));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
