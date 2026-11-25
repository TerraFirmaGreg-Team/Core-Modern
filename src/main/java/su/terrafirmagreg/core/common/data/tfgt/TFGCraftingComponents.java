package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import electrolyte.greate.registry.GreateTagPrefixes;

public class TFGCraftingComponents {
    public static CraftingComponent WHISK;
    public static CraftingComponent HERMETIC_CASING;
    public static CraftingComponent FIELD_GENERATOR_TIER_DOWN;
    public static CraftingComponent BATTERY;
    public static CraftingComponent DOUBLE_PLATE;

    public TFGCraftingComponents() {
    }

    public static void register() {
        WHISK = CraftingComponent.of("whisk", GreateTagPrefixes.whisk, GTMaterials.WroughtIron)
                .add(0, GreateTagPrefixes.whisk, GTMaterials.WroughtIron)
                .add(1, GreateTagPrefixes.whisk, GTMaterials.Steel)
                .add(2, GreateTagPrefixes.whisk, GTMaterials.Aluminium)
                .add(3, GreateTagPrefixes.whisk, GTMaterials.StainlessSteel)
                .add(4, GreateTagPrefixes.whisk, GTMaterials.Titanium)
                .add(5, GreateTagPrefixes.whisk, GTMaterials.TungstenSteel)
                .add(6, GreateTagPrefixes.whisk, GTMaterials.RhodiumPlatedPalladium)
                .add(7, GreateTagPrefixes.whisk, GTMaterials.NaquadahAlloy)
                .add(8, GreateTagPrefixes.whisk, GTMaterials.Darmstadtium)
                .add(9, GreateTagPrefixes.whisk, GTMaterials.Neutronium);

        HERMETIC_CASING = CraftingComponent.of("hermetic_casing", GTBlocks.HERMETIC_CASING_LV.asStack())
                .add(0, TFGMachines.HERMETIC_CASING_ULV.asStack())
                .add(1, GTBlocks.HERMETIC_CASING_LV.asStack())
                .add(2, GTBlocks.HERMETIC_CASING_MV.asStack())
                .add(3, GTBlocks.HERMETIC_CASING_HV.asStack())
                .add(4, GTBlocks.HERMETIC_CASING_EV.asStack())
                .add(5, GTBlocks.HERMETIC_CASING_IV.asStack())
                .add(6, GTBlocks.HERMETIC_CASING_LuV.asStack())
                .add(7, GTBlocks.HERMETIC_CASING_ZPM.asStack())
                .add(8, GTBlocks.HERMETIC_CASING_UV.asStack())
                .add(9, GTBlocks.HERMETIC_CASING_UHV.asStack());

        FIELD_GENERATOR_TIER_DOWN = CraftingComponent.of("field_generator_tier_down", GTItems.FIELD_GENERATOR_LV.asStack())
                .add(GTValues.HV, GTItems.FIELD_GENERATOR_MV.asStack())
                .add(GTValues.EV, GTItems.FIELD_GENERATOR_HV.asStack())
                .add(GTValues.IV, GTItems.FIELD_GENERATOR_EV.asStack())
                .add(GTValues.LuV, GTItems.FIELD_GENERATOR_IV.asStack())
                .add(GTValues.ZPM, GTItems.FIELD_GENERATOR_LuV.asStack())
                .add(GTValues.UV, GTItems.FIELD_GENERATOR_ZPM.asStack())
                .add(GTValues.UHV, GTItems.FIELD_GENERATOR_UV.asStack());

        BATTERY = CraftingComponent.of("battery", CustomTags.LV_BATTERIES)
                .add(GTValues.ULV, CustomTags.ULV_BATTERIES)
                .add(GTValues.LV, CustomTags.LV_BATTERIES)
                .add(GTValues.MV, CustomTags.MV_BATTERIES)
                .add(GTValues.HV, CustomTags.HV_BATTERIES)
                .add(GTValues.EV, CustomTags.EV_BATTERIES)
                .add(GTValues.IV, CustomTags.IV_BATTERIES)
                .add(GTValues.LuV, CustomTags.LuV_BATTERIES)
                .add(GTValues.ZPM, CustomTags.ZPM_BATTERIES)
                .add(GTValues.UV, CustomTags.UV_BATTERIES);

        // Could be a bit original and use the new plates to see
        DOUBLE_PLATE = CraftingComponent.of("tfg_double_plate", TagPrefix.plateDouble, GTMaterials.Iron)
                .add(GTValues.ULV, TagPrefix.plateDouble, GTMaterials.WroughtIron)
                .add(GTValues.LV, TagPrefix.plateDouble, GTMaterials.Steel)
                .add(GTValues.MV, TagPrefix.plateDouble, GTMaterials.Aluminium)
                .add(GTValues.HV, TagPrefix.plateDouble, GTMaterials.StainlessSteel)
                .add(GTValues.EV, TagPrefix.plateDouble, GTMaterials.Titanium)
                .add(GTValues.IV, TagPrefix.plateDouble, GTMaterials.TungstenSteel)
                .add(GTValues.LuV, TagPrefix.plateDouble, GTMaterials.RhodiumPlatedPalladium)
                .add(GTValues.ZPM, TagPrefix.plateDouble, GTMaterials.NaquadahAlloy)
                .add(GTValues.UV, TagPrefix.plateDouble, GTMaterials.Darmstadtium)
                .add(GTValues.UHV, TagPrefix.plateDouble, GTMaterials.Neutronium);
    }
}
