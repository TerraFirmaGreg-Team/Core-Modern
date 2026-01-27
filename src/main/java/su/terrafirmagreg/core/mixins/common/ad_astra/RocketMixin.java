package su.terrafirmagreg.core.mixins.common.ad_astra;

import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.entities.vehicles.Rocket;
import earth.terrarium.adastra.common.registry.ModEntityTypes;
import earth.terrarium.adastra.common.tags.ModFluidTags;

import su.terrafirmagreg.core.common.data.TFGEntities;
import su.terrafirmagreg.core.common.data.TFGItems;

@Mixin(value = Rocket.class, remap = false)
@Debug(export = true)
public abstract class RocketMixin extends Entity {
    public RocketMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /* Other things we could change for the double rockets:
    * Make them consume 2x the amount of fuel, would need 2x the tank size
    *
    *
    * */

    @Mutable
    @Final
    @Shadow
    public static Map<EntityType<?>, Rocket.RocketProperties> ROCKET_TO_PROPERTIES;

    @Final
    @Shadow
    private static Rocket.RocketProperties TIER_1_PROPERTIES;

    @Unique
    private static Rocket.RocketProperties TIER_1_DOUBLE_PROPERTIES;

    @Final
    @Shadow
    private static Rocket.RocketProperties TIER_2_PROPERTIES;

    @Final
    @Shadow
    private static Rocket.RocketProperties TIER_3_PROPERTIES;

    @Final
    @Shadow
    private static Rocket.RocketProperties TIER_4_PROPERTIES;

    @Inject(method = "<clinit>", at = @At("HEAD"))
    private static void tfg$injectToClinit(CallbackInfo ci) {
        TIER_1_DOUBLE_PROPERTIES = new Rocket.RocketProperties(1, TFGItems.TIER_1_DOUBLE_ROCKET.get(), 1.0F, ModFluidTags.TIER_1_ROCKET_FUEL);
    }

    @Redirect(method = "<clinit>", at = @At(value = "FIELD", target = "earth/terrarium/adastra/common/entities/vehicles/Rocket.ROCKET_TO_PROPERTIES : Ljava/util/Map;", opcode = Opcodes.PUTSTATIC))
    private static void tfg$modifyPropertiesMap(Map<EntityType<?>, Rocket.RocketProperties> value) {
        ROCKET_TO_PROPERTIES = Map.of(
                ModEntityTypes.TIER_1_ROCKET.get(), TIER_1_PROPERTIES,
                ModEntityTypes.TIER_2_ROCKET.get(), TIER_2_PROPERTIES,
                ModEntityTypes.TIER_3_ROCKET.get(), TIER_3_PROPERTIES,
                ModEntityTypes.TIER_4_ROCKET.get(), TIER_4_PROPERTIES,
                TFGEntities.TIER_1_DOUBLE_ROCKET.get(), TIER_1_DOUBLE_PROPERTIES);
    }

    @Unique
    private final Rocket tfg$self = (Rocket) (Object) this;

    @Override
    protected boolean canAddPassenger(Entity pPassenger) {
        System.out.println(tfg$self.getPassengers().size());
        if (this.getType() == TFGEntities.TIER_1_DOUBLE_ROCKET.get()) {
            return tfg$self.getPassengers().size() < 2;
        }

        return super.canAddPassenger(pPassenger);
    }

}
