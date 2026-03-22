package su.terrafirmagreg.core.mixins;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Config to modify the mixins at load and application time */
// Normally this only works on our own mixins, but with a generous sprinkling of reflection, we get more freedom.
public class TFGMixinPlugin implements IMixinConfigPlugin {

    private static final String SPECIES_MIXIN_JSON = "species.mixins.json";
    private static final String SPECIES_BLOCK_ENTITY_TYPE_MIXIN = "com.ninni.species.mixin.BlockEntityTypeMixin";

    private static final String AAAPARTICLES_MIXIN_JSON = "aaa_particles.mixins.json";
    private static final String AAAPARTICLES_GAME_RENDERER_MIXIN = "mod.chloeprime.aaaparticles.mixin.client.MixinGameRenderer";
    private static final String AAAPARTICLES_ITEM_IN_HAND_RENDERER_MIXIN = "mod.chloeprime.aaaparticles.mixin.client.MixinItemInHandRenderer";

    /**
     * acceptTargets fires once after all configs have been collected but before any have been applied,
     * so we can safely modify the mixin configs at this point.
     */
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

        String currentMixin = null;
        String currentConfig = null;

        try {
            for (Object config : getPendingConfigs()) {
                currentConfig = getConfigName(config);

                if (SPECIES_MIXIN_JSON.equals(currentConfig)) {
                    // Remove BlockEntityTypeMixin from the species config avoiding
                    //  CIR allocation on every block entity tick for 0.5 ms/tick
                    currentMixin = SPECIES_BLOCK_ENTITY_TYPE_MIXIN;
                    removeMixin(config, currentMixin);
                }

                if (AAAPARTICLES_MIXIN_JSON.equals(currentConfig)) {
                    // Remove hand rendering mixins from AAA Particles
                    // Only the sandworm mod uses AAA Particles and not for the hands
                    // Causes lag spikes when switching items
                    currentMixin = AAAPARTICLES_GAME_RENDERER_MIXIN;
                    removeMixin(config, currentMixin);
                    currentMixin = AAAPARTICLES_ITEM_IN_HAND_RENDERER_MIXIN;
                    removeMixin(config, currentMixin);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to remove " + currentMixin + " from " + currentConfig, e);
        }
    }

    // Reflection helpers

    @SuppressWarnings("unchecked")
    private static List<Object> getPendingConfigs() throws ReflectiveOperationException {
        // MixinEnvironment -> MixinTransformer -> MixinProcessor -> pendingConfigs
        Object transformer = MixinEnvironment.getDefaultEnvironment().getActiveTransformer();
        Field processorField = transformer.getClass().getDeclaredField("processor");
        processorField.setAccessible(true);
        Object processor = processorField.get(transformer);
        Field pendingConfigsField = processor.getClass().getDeclaredField("pendingConfigs");

        pendingConfigsField.setAccessible(true);
        return (List<Object>) pendingConfigsField.get(processor);
    }

    private static String getConfigName(Object config) throws ReflectiveOperationException {
        Method getName = config.getClass().getMethod("getName");
        getName.setAccessible(true);
        return (String) getName.invoke(config);
    }

    @SuppressWarnings("unchecked")
    private static void removeMixin(Object config, String className) throws ReflectiveOperationException {
        Field mixinsField = config.getClass().getDeclaredField("mixins");
        mixinsField.setAccessible(true);
        ((List<IMixinInfo>) mixinsField.get(config))
                .removeIf(m -> className.equals(m.getClassName()));

        Field mappingField = config.getClass().getDeclaredField("mixinMapping");
        mappingField.setAccessible(true);
        ((Map<String, List<IMixinInfo>>) mappingField.get(config))
                .values().forEach(list -> list.removeIf(m -> className.equals(m.getClassName())));
    }

    // No-op event handlers that need implementation for an IMixinConfigPlugin

    // spotless:off
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {return true;}
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() {return null;}
    @Override public List<String> getMixins() {return null;}
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    // spotless:on
}
