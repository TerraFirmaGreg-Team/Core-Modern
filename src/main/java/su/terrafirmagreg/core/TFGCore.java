package su.terrafirmagreg.core;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import su.terrafirmagreg.core.client.TFGClientEventHandler;
import su.terrafirmagreg.core.common.TFGCommonEventHandler;

@Mod(TFGCore.MOD_ID)
public final class TFGCore {
    public static final String MOD_ID = "tfg";
    public static final String NAME = "TerraFirmaGreg";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TFGCore() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TFGConfig.SPEC);

        TFGCommonEventHandler.init();
        if (FMLEnvironment.dist == Dist.CLIENT) TFGClientEventHandler.init();
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
}
