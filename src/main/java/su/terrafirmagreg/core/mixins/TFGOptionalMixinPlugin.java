package su.terrafirmagreg.core.mixins;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraftforge.fml.loading.FMLLoader;

public class TFGOptionalMixinPlugin implements IMixinConfigPlugin {

    // mixins get loaded way before the rest of tfg (including its logger) does, so need to use separate logger to avoid null pointer exceptions
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("tfg-mixin");

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("Loading Optional Mixin Plugin!");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // loads mixins if targeted mod present, otherwise ignores them
        // depends on mixins being in the format [side].[modid].[classmixin] within tfg.optional.mixins.json
        // when actually loading the mixinClassName takes the form of "su.terrafirmagreg.core.mixins.[side].[modid].[classmixin]"
        // example: "client.ihearttfc.IngameOverlaysMixin" will only load if ihearttfc is present
        String mixinModID = mixinClassName.split("\\.")[5];
        boolean isModPresent = FMLLoader.getLoadingModList().getModFileById(mixinModID) != null;

        if (!isModPresent) {
            LOGGER.info("Optional mod {} not present, skipping mixin application", mixinModID);
        }
        return isModPresent;
    }

    // spotless:off
    @Override public String getRefMapperConfig() {return null;}
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() {return null;}
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    // spotless:on
}
