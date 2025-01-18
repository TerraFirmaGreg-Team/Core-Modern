package su.terrafirmagreg.core;

import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;


@Mod(TFGCore.MOD_ID)
public final class TFGCore {
    public static final String MOD_ID = "tfg";
    public static final String NAME = "TerraFirmaGreg";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final GTRegistrate REGISTRATE = GTRegistrate.create(TFGCore.MOD_ID);
    public static MaterialRegistry MATERIAL_REGISTRY;

    public TFGCore() {}

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
}
