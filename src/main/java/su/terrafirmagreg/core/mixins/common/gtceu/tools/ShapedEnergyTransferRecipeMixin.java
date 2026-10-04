package su.terrafirmagreg.core.mixins.common.gtceu.tools;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.ShapedEnergyTransferRecipe;

import net.dries007.tfc.common.capabilities.forge.ForgingBonus;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

@Mixin(value = ShapedEnergyTransferRecipe.class, remap = true)
public class ShapedEnergyTransferRecipeMixin {

    // Copies forging bonuses from tool heads onto power tools
    @Inject(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "RETURN", ordinal = 0), remap = true)
    private void tfg$assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir) {

        // Look through the input items
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            final ItemStack craftingItem = craftingContainer.getItem(i);

            // Find the one that's a tool head
            final TagPrefix tp = ChemicalHelper.getPrefix(craftingItem.getItem());

            if (tp == TagPrefix.toolHeadBuzzSaw
                    || tp == TagPrefix.toolHeadDrill
                    || tp == TagPrefix.toolHeadChainsaw
                    || tp == TagPrefix.toolHeadScrewdriver
                    || tp == TagPrefix.toolHeadWireCutter
                    || tp == TagPrefix.toolHeadWrench) {

                // Get the tool head's NBT and copy the forging bonus onto the output power tool
                final CompoundTag tag = craftingItem.getTag();

                if (tag != null && tag.contains(ForgingBonus.KEY, Tag.TAG_INT)) {
                    int bonus = tag.getInt(ForgingBonus.KEY);
                    cir.getReturnValue().getOrCreateTag().putInt(ForgingBonus.KEY, bonus);
                }

                // Return out once we found the tool head
                return;
            }
        }
    }
}
