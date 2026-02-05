package su.terrafirmagreg.core.mixins.client.tfc;

import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;

import net.dries007.tfc.common.blockentities.IngotPileBlockEntity;
import net.dries007.tfc.util.Metal;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.TFGHelpers;

@Mixin(value = IngotPileBlockEntity.class, remap = false)
@OnlyIn(Dist.CLIENT)
public abstract class IngotPileBlockEntityMixin {
    @Shadow
    @Final
    private List<?> entries;

    @Shadow
    public abstract Metal getOrCacheMetal(int index);

    /** Tooltip for the ingot piles
     * @author Ujhik
     * @reason To add correct tooltip to non-tfc ingots in piles (they were shown as unknown)
     */
    @Overwrite
    public void fillTooltip(Consumer<Component> tooltip) {
        Object2IntMap<Metal> mapMetals = new Object2IntOpenHashMap<>();
        Object2IntMap<Material> mapMaterials = new Object2IntOpenHashMap<>();
        Object2IntMap<Component> mapComponents = new Object2IntOpenHashMap<>();
        int numberOfUnknown = 0;

        for (int i = 0; i < entries.size(); i++) {
            try {
                // TFC default behaviour
                Metal metal = this.getOrCacheMetal(i);
                if (metal != null && !(metal == Metal.unknown())) {
                    mapMetals.mergeInt(metal, 1, Integer::sum);
                    continue;
                }

                // Trying with material
                final ItemStack stack = TFGHelpers.getStackFromIngotPileTileEntityByIndex(entries, i);
                MaterialStack materialStack = ChemicalHelper.getMaterialStack(stack);
                if (!materialStack.isEmpty()) {
                    Material material = materialStack.material();
                    mapMaterials.mergeInt(material, 1, Integer::sum);
                    continue;
                }

                // Trying with Item
                Component nameComponent = stack.getHoverName();
                String itemName = nameComponent.getString();
                if (!itemName.isEmpty()) {
                    mapComponents.mergeInt(nameComponent, 1, Integer::sum);
                    continue;
                }

                // Fallback to tfc unknown like in tfc code
                if (metal != null) {
                    mapMetals.mergeInt(metal, 1, Integer::sum);
                    continue;
                }
            } catch (Exception e) {
                TFGCore.LOGGER.warn("Could not show ingot in ingotPile: {}", e.toString());
            }

            // UltraLast fallback to unknown
            numberOfUnknown++;
        }

        mapMetals.forEach((metalItem, quantity) -> tooltip.accept(Component.literal(quantity + "x ").append(metalItem.getDisplayName())));
        mapMaterials.forEach((material, quantity) -> tooltip.accept(Component.literal(quantity + "x ").append(material.getLocalizedName())));
        mapComponents.forEach((component, quantity) -> tooltip.accept(Component.literal(quantity + "x ").append(component)));

        if (numberOfUnknown > 0) {
            tooltip.accept(Component.literal(numberOfUnknown + "x ").append(Component.literal("unknown?")));
        }
    }
}
