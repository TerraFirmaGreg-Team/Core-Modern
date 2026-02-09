package su.terrafirmagreg.core.common.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import su.terrafirmagreg.core.TFGCore;

public class TFGSoundProvider extends SoundDefinitionsProvider {
    protected TFGSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, TFGCore.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {

    }
}
