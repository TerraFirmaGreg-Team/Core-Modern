package su.terrafirmagreg.core.mixins.common.gtceu.tools;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.ShapedEnergyTransferRecipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

@Mixin(value = ShapedEnergyTransferRecipe.class, remap = true)
public class ShapedEnergyTransferRecipeMixin extends ShapedRecipe {

	// Copies forging bonuses from tool heads onto power tools

    public ShapedEnergyTransferRecipeMixin(ResourceLocation id, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> recipeItems, ItemStack result) {
        super(id, group, category, width, height, recipeItems, result);
    }

    @Inject(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "RETURN", ordinal = 0), remap = true)
    private void tfg$assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir) {

        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            final ItemStack craftingItem = craftingContainer.getItem(i);

            final TagPrefix tp = ChemicalHelper.getPrefix(craftingItem.getItem());
            if (tp == TagPrefix.toolHeadBuzzSaw || tp == TagPrefix.toolHeadDrill || tp == TagPrefix.toolHeadChainsaw
                    || tp == TagPrefix.toolHeadScrewdriver || tp == TagPrefix.toolHeadWireCutter || tp == TagPrefix.toolHeadWrench) {

                final CompoundTag tag = craftingItem.getTag();
                final String KEY = "tfc:forging_bonus";

                if (tag != null && tag.contains(KEY, Tag.TAG_INT)) {
                    int bonus = craftingItem.getOrCreateTag().getInt(KEY);
                    cir.getReturnValue().getOrCreateTag().putInt(KEY, bonus);
                }
            }
        }
    }
}
