package su.terrafirmagreg.tfcambiental.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.tfcambiental.TFCAmbiental;

public class Snowshoes extends Item {
    public Snowshoes(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable(TFCAmbiental.MOD_ID + ".tooltip.snowshoes").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        super.onArmorTick(stack, level, player);
        // Save last position and damage if the player has moved on non-snow blocks
        var tag = stack.getOrCreateTag();
        double distance = 0;
        if (tag.contains("lastPos")) {
            distance = BlockPos.of(tag.getLong("lastPos")).distSqr(player.getOnPos());
        }
        if (distance > 0 && !level.getBlockState(player.getOnPos()).is(TFCAmbiental.COLD_STUFF)) {
            stack.hurtAndBreak(5, player, e -> e.playSound(SoundEvents.ITEM_BREAK));
        }
        tag.putLong("lastPos", player.getOnPos().asLong());
    }
}
