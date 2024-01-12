package su.terrafirmagreg.core.compat.gtceu.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import javax.annotation.Nullable;

public class TFCProperty implements IMaterialProperty<TFCProperty> {

    private int forgingTemp;
    private int weldingTemp;
    private int meltTemp;

    @Nullable
    private Material outputMaterial;

    private int tier;
    private int percentOfMaterial;

    public TFCProperty(int forgingTemp, int weldingTemp, int meltTemp, int tier) {
        this(forgingTemp, weldingTemp, meltTemp, null, tier);
    }

    public TFCProperty(int forgingTemp, int weldingTemp, int meltTemp, @Nullable Material outputMaterial, int tier) {
        this(forgingTemp, weldingTemp, meltTemp, outputMaterial, tier, 100);
    }

    public TFCProperty(int forgingTemp, int weldingTemp, int meltTemp, @Nullable Material outputMaterial, int tier, int percentOfMaterial) {
        setForgingTemp(forgingTemp);
        setWeldingTemp(weldingTemp);
        setMeltTemp(meltTemp);
        setFluidOutputName(outputMaterial);
        setTier(tier);
        setPercentOfMaterial(percentOfMaterial);
    }


    public int getForgingTemp() {
        return forgingTemp;
    }

    public void setForgingTemp(int forgingTemp) {
        this.forgingTemp = Math.max(forgingTemp, 0);
    }

    public int getWeldingTemp() {
        return weldingTemp;
    }

    public void setWeldingTemp(int weldingTemp) {
        this.weldingTemp = Math.max(weldingTemp, 0);
    }

    public int getMeltTemp() {
        return meltTemp;
    }

    public void setMeltTemp(int meltTemp) {
        this.meltTemp = Math.max(meltTemp, 0);
    }

    @Nullable
    public Material getOutputMaterial() {
        return outputMaterial;
    }

    public void setFluidOutputName(@Nullable Material outputMaterial) {
        this.outputMaterial = outputMaterial;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = 0;

        if (tier < 7 && tier > 0)
            this.tier = tier;
    }

    public int getPercentOfMaterial() {
        return percentOfMaterial;
    }

    public void setPercentOfMaterial(int percentOfMaterial) {
        this.percentOfMaterial = Math.max(percentOfMaterial, 0);
    }

    @Override
    public void verifyProperty(MaterialProperties materialProperties) {

    }
}