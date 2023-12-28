package su.terrafirmagreg.tfgcore;

import com.mojang.logging.LogUtils;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.format.ConfigFormats;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import su.terrafirmagreg.tfgcore.compat.gtceu.TFGGTAddon;
import su.terrafirmagreg.tfgcore.compat.tfcambiental.TFCAmbientalCompat;

@Mod(TerraFirmaGreg.MOD_ID)
public class TerraFirmaGreg {

    public static final String MOD_ID = "terrafirmagreg";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static TFGConfig CONFIG;

    public TerraFirmaGreg() {
        CONFIG = Configuration.registerConfig(TFGConfig.class, ConfigFormats.json()).getConfigInstance();

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);

        //TFCAmbientalCompat.register();
    }
}
