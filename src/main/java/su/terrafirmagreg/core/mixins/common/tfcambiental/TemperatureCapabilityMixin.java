package su.terrafirmagreg.core.mixins.common.tfcambiental;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.lumintorious.tfcambiental.TFCAmbientalConfig;
import com.lumintorious.tfcambiental.api.*;
import com.lumintorious.tfcambiental.capability.TemperatureCapability;
import com.lumintorious.tfcambiental.modifier.TempModifierStorage;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import su.terrafirmagreg.core.compat.tfcambiental.TFCAmbientalCompat;

/**
 * Adds support for 'fully insulated' armors that ignore tfc-ambiental. Mostly used for the lava diving suit, space
 * suits, and late game armor
 */
@Mixin(value = TemperatureCapability.class, remap = false)
public abstract class TemperatureCapabilityMixin {

    @Shadow
    public abstract Player getPlayer();

    @Shadow
    public abstract void clearModifiers();

    @Shadow
    public TempModifierStorage modifiers;

    @Shadow
    private Player player;
    @Shadow
    public float temperature;
    @Shadow
    private float target;
    @Shadow
    private float targetWetness;
    @Shadow
    private float potency;

    /**
     * @author Mqrius
     * @reason Short circuit when heatproof for performance, merged with old mixin to clamp temp
     * Code mostly copied from original method and old mixin, short circuit added.
     * Should save about 1ms per 10 players wearing fully insulated armor (spacesuits, nano, quark)
     */
    @Overwrite
    public void evaluateModifiers() {
        this.clearModifiers();
        EquipmentTemperatureProvider.evaluateAll(this.player, this.modifiers);

        var equipmentPotency = this.modifiers.getTotalPotency();
        boolean fullyInsulated = equipmentPotency == (TFCAmbientalCompat.FULLY_INSULATED * 4) + 1;
        boolean heatproof = equipmentPotency == (TFCAmbientalCompat.HEATPROOF * 4) + 1;

        EnvironmentalTemperatureProvider.evaluateAll(this.player, this.modifiers);
        if (!fullyInsulated) {
            // Only evaluate everything else if we're not fully insulated
            ItemTemperatureProvider.evaluateAll(this.player, this.modifiers);
            BlockTemperatureProvider.evaluateAll(this.player, this.modifiers);
            BlockEntityTemperatureProvider.evaluateAll(this.player, this.modifiers);
            EntityTemperatureProvider.evaluateAll(this.player, this.modifiers);
        }
        this.modifiers.keepOnlyNEach(3);

        this.potency = this.modifiers.getTotalPotency();
        this.target = this.modifiers.getTargetTemperature();
        this.targetWetness = this.modifiers.getTargetWetness();

        if ((this.target > this.temperature && this.temperature > TFCAmbientalConfig.COMMON.hotThreshold.get().floatValue())
                || (this.target < this.temperature && this.temperature < TFCAmbientalConfig.COMMON.coolThreshold.get().floatValue())) {
            this.potency = 1f;
        }

        this.potency = Math.max(1f, this.potency);

        if (heatproof) {
            this.target = Math.min(this.target, 29f);
        } else if (fullyInsulated) {
            this.target = Mth.clamp(this.target, 5f, 25f);
        }
    }
}
