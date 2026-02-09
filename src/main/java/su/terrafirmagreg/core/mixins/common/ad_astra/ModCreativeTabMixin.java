package su.terrafirmagreg.core.mixins.common.ad_astra;

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.item.ItemStack;

import earth.terrarium.adastra.common.registry.ModCreativeTab;
import earth.terrarium.adastra.common.registry.ModItems;
import earth.terrarium.adastra.common.utils.EnergyUtils;

@Mixin(value = ModCreativeTab.class, remap = false)
public class ModCreativeTabMixin {

    /**
     * @author yes
     * @reason we don't want them
     */
    @Overwrite()
    public static Stream<ItemStack> getCustomNbtItems() {
        //Just left something in there so I don't have to deal with empty stream issues
        return Stream.of(EnergyUtils.energyFilledItem(ModItems.ETRIONIC_CAPACITOR));
    }
}
