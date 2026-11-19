package su.terrafirmagreg.core.mixins.common.aliencraft;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import en.predator97427fr.aliencraft.entity.XenoscorpionSurfaceEntity;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = XenoscorpionSurfaceEntity.class, remap = false)
public class XenoscorpionSurfaceEntityMixin extends Monster {

    @Shadow
    @Final
    public static EntityDataAccessor<Integer> DATA_STATE;

    @Shadow
    private int stateTimer;

    protected XenoscorpionSurfaceEntityMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void setPersistenceRequired() {
        // Remove call
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FLYING_SPEED, 0.65)
                .add(Attributes.ATTACK_DAMAGE, 20.0F)
                .add(Attributes.ATTACK_KNOCKBACK, 0.4F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5F)
                .add(Attributes.FOLLOW_RANGE, 32.0F)
                .add(Attributes.ARMOR, 10F));
    }

    @Inject(method = "registerGoals", at = @At("TAIL"), remap = true)
    private void tfg$registerGoals(CallbackInfo ci) {
        var scorp = (XenoscorpionSurfaceEntity) (Object) this;
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(scorp, 1.0F));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(scorp, Player.class, 0.6F));
        this.goalSelector.addGoal(4, new NearestAttackableTargetGoal<>(scorp, Animal.class, true, (entity) -> entity.getType().is(TFGTags.Entities.VenusPrey)));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    // Stop it from burying - anim state 0 seems to be "is buried", and 2 is "is going to bury", so just keep it locked to 1
    @Inject(method = "setAnimState", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$setAnimState(int state, CallbackInfo ci) {
        this.entityData.set(DATA_STATE, 1);
        this.stateTimer = 0;
        ci.cancel();
    }

    // Stop creating a new biome resource key every second
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceKey;create(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceKey;"), remap = true)
    private ResourceKey<Biome> tfg$tick(ResourceKey<? extends Registry<Biome>> registryKey, ResourceLocation location) {
        return null;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z"), remap = true)
    private boolean tfg$tick2(Holder<Biome> instance, ResourceKey<Biome> tResourceKey) {
        return false;
    }

    // Don't force it to land during the daytime
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z", remap = true))
    private boolean tfg$tick3(Level instance) {
        return false;
    }

    // Make them immune to fall damage because their AI is stupid when flying
    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
        resetFallDistance();
    }
}
