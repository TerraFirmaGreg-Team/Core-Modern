package su.terrafirmagreg.core.common.data.tfgt.machine;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;

import electrolyte.greate.registry.GreateTagPrefixes;

public class TFGTCraftingComponents {
    public static CraftingComponent WHISK;
    public static CraftingComponent HERMETIC_CASING;

    public TFGTCraftingComponents() {
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
                .add(1, GTBlocks.HERMETIC_CASING_LV.asStack())
                .add(2, GTBlocks.HERMETIC_CASING_MV.asStack())
                .add(3, GTBlocks.HERMETIC_CASING_HV.asStack())
                .add(4, GTBlocks.HERMETIC_CASING_EV.asStack())
                .add(5, GTBlocks.HERMETIC_CASING_IV.asStack())
                .add(6, GTBlocks.HERMETIC_CASING_LuV.asStack())
                .add(7, GTBlocks.HERMETIC_CASING_ZPM.asStack())
                .add(8, GTBlocks.HERMETIC_CASING_UV.asStack())
                .add(9, GTBlocks.HERMETIC_CASING_UHV.asStack());
    }
}
