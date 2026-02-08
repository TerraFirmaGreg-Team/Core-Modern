package su.terrafirmagreg.core;

import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.CommonProxy;
import net.minecraftforge.fml.DistExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

import su.terrafirmagreg.core.config.TFGConfig;

@Mod(TFGCore.MOD_ID)
public final class TFGCore {

    public static final String MOD_ID = "tfg";
    public static final String NAME = "TerraFirmaGreg-Core";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static MaterialRegistry MATERIAL_REGISTRY;
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(TFGCore.MOD_ID);

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public TFGCore() {
        setupFixForGlobalServerConfig();
        TFGConfig.init();

        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }


    @SuppressWarnings("removal")
    private static void setupFixForGlobalServerConfig() {
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }
}
