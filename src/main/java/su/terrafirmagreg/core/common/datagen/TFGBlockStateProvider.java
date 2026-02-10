package su.terrafirmagreg.core.common.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import su.terrafirmagreg.core.TFGCore;

public class TFGBlockStateProvider extends BlockStateProvider {
    public TFGBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TFGCore.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }
}
