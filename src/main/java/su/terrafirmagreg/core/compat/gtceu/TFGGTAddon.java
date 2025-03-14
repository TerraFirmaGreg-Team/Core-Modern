package su.terrafirmagreg.core.compat.gtceu;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import su.terrafirmagreg.core.TFGCore;

@SuppressWarnings("unused")
@GTAddon
public final class TFGGTAddon implements IGTAddon {

    @Override
    public void initializeAddon() {}

    @Override
    public GTRegistrate getRegistrate() {
        return TFGCore.REGISTRATE;
    }

    @Override
    public String addonModId() {
        return TFGCore.MOD_ID;
    }
}
