package su.terrafirmagreg.core.compat.gtceu.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public final class TFCProperty implements IMaterialProperty {

    private int forgingTemp;
    private int weldingTemp;
    private int meltTemp;
    private int tier;
    private int percentOfMaterial;

    @Nullable
    private Material outputMaterial;

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

    public void setForgingTemp(int forgingTemp) {
        this.forgingTemp = Math.max(forgingTemp, 0);
    }

    public void setWeldingTemp(int weldingTemp) {
        this.weldingTemp = Math.max(weldingTemp, 0);
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

    public void setTier(int tier) {
        this.tier = 0;

        if (tier < 7 && tier > 0)
            this.tier = tier;
    }

    public void setPercentOfMaterial(int percentOfMaterial) {
        this.percentOfMaterial = Math.max(percentOfMaterial, 0);
    }

    @Override
    public void verifyProperty(MaterialProperties materialProperties) {}
}