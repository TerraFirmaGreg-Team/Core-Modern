package su.terrafirmagreg.core.common.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTMaterials;

public class TFGModifyMaterials {

    public static void modify() {
        enableCustomStill(GTMaterials.Iron3Chloride);
        enableCustomStill(GTMaterials.SolderingAlloy);
        enableCustomStill(GTMaterials.Tin);
    }

    private static void enableCustomStill(Material material) {

        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        if (property == null)
            return;

        FluidBuilder builder = material.getProperty(PropertyKey.FLUID).getQueuedBuilder(FluidStorageKeys.LIQUID);
        if (builder != null) {
            builder.textures(true);
        }
    }
}
