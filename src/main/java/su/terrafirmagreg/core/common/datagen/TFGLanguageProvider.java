package su.terrafirmagreg.core.common.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import su.terrafirmagreg.core.TFGCore;

public class TFGLanguageProvider extends LanguageProvider {
    public TFGLanguageProvider(PackOutput output, String locale) {
        super(output, TFGCore.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {

    }
}
