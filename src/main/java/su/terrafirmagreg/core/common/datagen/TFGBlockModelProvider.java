package su.terrafirmagreg.core.common.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import su.terrafirmagreg.core.TFGCore;

public class TFGBlockModelProvider extends BlockModelProvider {
    public TFGBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TFGCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}
