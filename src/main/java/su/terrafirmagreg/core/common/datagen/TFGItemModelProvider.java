package su.terrafirmagreg.core.common.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import su.terrafirmagreg.core.TFGCore;

public class TFGItemModelProvider extends ItemModelProvider {
    public TFGItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, TFGCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}
