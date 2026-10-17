package su.terrafirmagreg.core.common.data.tfgt;

import java.util.function.Supplier;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.GTHoeItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.item.tool.behavior.AOEConfigUIBehavior;
import com.gregtechceu.gtceu.common.item.tool.behavior.HarvestCropsBehavior;
import com.gregtechceu.gtceu.common.item.tool.behavior.HoeGroundBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ToolActions;
import net.neganote.gtutilities.common.tools.UtilToolHelper;

/**
 * Custom electric tool types using Modern Utilities
 */

public final class TFGToolTypes {

    public static final GTToolType SCYTHE_LV = electricScythe("lv", GTValues.LV, 3.0F, 2, 2, ToolHelper.SUPPLY_POWER_UNIT_LV);
    public static final GTToolType SCYTHE_MV = electricScythe("mv", GTValues.MV, 4.0F, 3, 2, ToolHelper.SUPPLY_POWER_UNIT_MV);
    public static final GTToolType SCYTHE_HV = electricScythe("hv", GTValues.HV, 5.0F, 4, 3, ToolHelper.SUPPLY_POWER_UNIT_HV);
    public static final GTToolType SCYTHE_EV = electricScythe("ev", GTValues.EV, 6.0F, 5, 3, ToolHelper.SUPPLY_POWER_UNIT_EV);
    public static final GTToolType SCYTHE_IV = electricScythe("iv", GTValues.IV, 7.0F, 6, 4, ToolHelper.SUPPLY_POWER_UNIT_IV);
    public static final GTToolType SCYTHE_ZPM = electricScythe("zpm", GTValues.ZPM, 9.0F, 8, 4, UtilToolHelper.SUPPLY_POWER_UNIT_ZPM);

    public static final GTToolType[] ELECTRIC_SCYTHES = { SCYTHE_LV, SCYTHE_MV, SCYTHE_HV, SCYTHE_EV, SCYTHE_IV, SCYTHE_ZPM };

    private TFGToolTypes() {
    }

    public static void init() {
    }

    private static GTToolType electricScythe(String voltage, int tier, float durability, int radius, int depth, Supplier<ItemStack> powerUnit) {
        return GTToolType.builder(voltage + "_scythe")
                .idFormat(voltage + "_%s_scythe")
                .toolTag(CustomTags.SCYTHES)
                .harvestTag(BlockTags.MINEABLE_WITH_HOE)
                .toolStats(b -> b.blockBreaking().attacking()
                        .attackDamage(5.0F).attackSpeed(-3.0F).durabilityMultiplier(durability)
                        .aoe(radius, radius, depth)
                        .brokenStack(powerUnit)
                        .behaviors(AOEConfigUIBehavior.INSTANCE, HoeGroundBehavior.INSTANCE, HarvestCropsBehavior.INSTANCE)
                        .canApplyEnchantment(EnchantmentCategory.DIGGER))
                .constructor(GTHoeItem::new)
                .sound(GTSoundEntries.CHAINSAW_TOOL, true)
                .electric(tier)
                .toolClassNames("scythe")
                .toolClasses(GTToolType.HOE)
                .defaultActions(ToolActions.HOE_DIG)
                .build();
    }
}
