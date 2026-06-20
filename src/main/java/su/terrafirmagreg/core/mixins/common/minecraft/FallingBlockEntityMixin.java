package su.terrafirmagreg.core.mixins.common.minecraft;

import java.util.List;

import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.TFCEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.PalmTrees;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_PalmTrees;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {

    public FallingBlockEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private int tfg$coconutEffectCooldown = 0;

    @Shadow
    public abstract BlockState getBlockState();

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!this.level().isClientSide && this.isAlive()) {
            if (this.tfg$coconutEffectCooldown > 0) {
                this.tfg$coconutEffectCooldown--;
            }

            if (this.tfg$coconutEffectCooldown == 0) {
                BlockState state = this.getBlockState();
                var coconutEntry = TFGBlocks_PalmTrees.PALM_FRUITS.get(PalmTrees.COCONUT);

                if (coconutEntry != null && state.is(coconutEntry.get())) {
                    List<Entity> list = this.level().getEntities(this, this.getBoundingBox(), entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator());
                    boolean hit = false;

                    for (Entity entity : list) {
                        if (entity instanceof Player player) {
                            player.addEffect(new MobEffectInstance(TFCEffects.PINNED.get(), 100));
                            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40));
                            this.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BAMBOO_HIT, SoundSource.PLAYERS, 2.0f, 0.1f);
                            if (this.level() instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 2, player.getZ(), 20, 0.1, 0.1, 0.1, 0.5);
                            }
                            hit = true;
                        }
                    }

                    if (hit) {
                        this.tfg$coconutEffectCooldown = 20;
                    }
                }
            }
        }
    }
}
