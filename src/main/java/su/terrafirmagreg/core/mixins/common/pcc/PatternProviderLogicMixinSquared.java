package su.terrafirmagreg.core.mixins.common.pcc;

import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.bawnorton.mixinsquared.TargetHandler;

import appeng.helpers.patternprovider.PatternProviderLogic;

@Mixin(value = PatternProviderLogic.class, priority = 1700)
public abstract class PatternProviderLogicMixinSquared {
    @TargetHandler(mixin = "yuuki1293.pccard.mixins.PatternProviderLogicMixinHP", name = "init")
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;"))
    private Field tfg$getDeclaredField(Class<?> clazz, String name) throws NoSuchFieldException {
        return clazz.getDeclaredField("upgrades");
    }
}
