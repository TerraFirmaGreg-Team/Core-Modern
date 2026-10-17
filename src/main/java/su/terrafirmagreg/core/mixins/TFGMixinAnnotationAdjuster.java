package su.terrafirmagreg.core.mixins;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.Inject;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableInjectNode;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;

/**
 * Lets you change other mods' mixins or cancel them. Unlike TFGMixinPlugin, this also works to disable
 * specific methods/injectors/etc instead of the entire mixin class.
 * See: https://github.com/Bawnorton/MixinSquared/wiki/Mixin-Annotation-Adjuster
 */

public class TFGMixinAnnotationAdjuster implements MixinAnnotationAdjuster {
    @Override
    public @Nullable AdjustableAnnotationNode adjust(List<String> targetClassNames, String mixinClassName, MethodNode method, AdjustableAnnotationNode annotation) {

        // Remove ad astra's cloud rendering for acid rain, while keeping its mixin for changing the rain itself
        if (mixinClassName.equals("earth.terrarium.adastra.mixins.client.LevelRendererMixin") && annotation.is(Inject.class)) {
            var injectNode = annotation.as(AdjustableInjectNode.class);
            if (injectNode.getMethod().contains("renderClouds")) {
                return null;
            }
        }

        return annotation;
    }
}
