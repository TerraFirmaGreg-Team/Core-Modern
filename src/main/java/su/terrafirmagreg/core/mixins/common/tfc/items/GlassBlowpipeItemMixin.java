package su.terrafirmagreg.core.mixins.common.tfc.items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.capabilities.glass.GlassOperation;
import net.dries007.tfc.common.capabilities.glass.GlassWorkData;
import net.dries007.tfc.common.items.GlassBlowpipeItem;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.dries007.tfc.util.Helpers;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;

import su.terrafirmagreg.core.utils.TFGHelpers;

@Mixin(value = GlassBlowpipeItem.class)
public abstract class GlassBlowpipeItemMixin {

    @Shadow(remap = false)
    private static ItemStack getOtherHandItem(Player player) {
        throw new IllegalStateException();
    }

    @Shadow(remap = false)
    protected abstract boolean consumeBlowpipe(Player player, InteractionHand hand, ItemStack item);

    /**
     * @author Redeix
     * @reason Replace hardcoded cooldown with TFG dynamic cooldown.
     */
    @Overwrite(remap = false)
    protected void stopUsing(LivingEntity entity, ItemStack stack) {
        if (entity instanceof Player player) {
            final ItemStack otherHand = getOtherHandItem(player);
            final GlassOperation op = GlassOperation.get(otherHand, player);
            if (op != null && stack.getItem() instanceof GlassBlowpipeItem) {
                GlassWorkData.apply(stack, op);

                final Level level = entity.level();
                level.getRecipeManager().getRecipeFor(TFCRecipeTypes.GLASSWORKING.get(), new ItemStackInventory(stack), level).ifPresent(recipe -> {
                    final boolean broken = consumeBlowpipe(player, player.getUsedItemHand(), stack);
                    ItemHandlerHelper.giveItemToPlayer(player, recipe.getResultItem(level.registryAccess()));
                    level.playSound(null, player.blockPosition(), broken ? SoundEvents.ITEM_BREAK : SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS);
                });
            }
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            int cooldown = TFGHelpers.getGlassworkingStat(player, true);
            // Overwrite cooldown v
            Helpers.allItems(TFCTags.Items.ALL_BLOWPIPES).forEach(item -> player.getCooldowns().addCooldown(item, cooldown));
        }
    }
}
