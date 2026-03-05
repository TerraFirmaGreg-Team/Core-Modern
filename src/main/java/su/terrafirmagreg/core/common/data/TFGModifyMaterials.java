package su.terrafirmagreg.core.common.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTMaterials;

public class TFGModifyMaterials {

    public static void modify() {
        enableCustomStill("iron_iii_chloride");
        enableCustomStill("soldering_alloy");
        enableCustomStill("tin");
    }

    private static void enableCustomStill(String materialName) {
        Material material = GTMaterials.get(materialName);

        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        if (property == null)
            return;

        FluidBuilder builder = material.getProperty(PropertyKey.FLUID).getQueuedBuilder(FluidStorageKeys.LIQUID);
        if (builder != null) {
            builder.textures(true);
        }
    }
}
