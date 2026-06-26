package su.terrafirmagreg.core.mixins.common.tfc;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import net.dries007.tfc.common.TFCEffects;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.misc.TFCFallingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.block.palmtree.PalmFruitBlock;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_PalmTrees;

/**
 * Ensure blocks tagged with TFCTags.Blocks.SUPPORTS_LANDSLIDE are not considered "fall-through".
 * This prevents Landslide logic from having a road fall right into another.
 */
@Mixin(TFCFallingBlockEntity.class)
public class TFCFallingBlockEntityMixin {

    @Unique
    private int tfg$coconutEffectCooldown = 0;

    // 5-arg method:
    @Inject(method = "canFallThrough(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onCanFallThroughFull(BlockGetter level, BlockPos pos, BlockState state, Direction fallingDirection, BlockState fallingState, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (state.is(TFCTags.Blocks.SUPPORTS_LANDSLIDE)) {
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {
        }
    }

    // 3-arg method:
    @Inject(method = "canFallThrough(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onCanFallThroughSimple(BlockGetter world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (state.is(TFCTags.Blocks.SUPPORTS_LANDSLIDE)) {
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {
        }
    }

    // Crush coconut blocks and palm husks when entities land on them.
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean onCrushBlock(Level instance, BlockPos pos, boolean drop) {
        BlockState state = instance.getBlockState(pos);
        if (state.getBlock() instanceof PalmFruitBlock || state.is(TFGBlocks_PalmTrees.PALM_HUSK.get())) {
            instance.removeBlockEntity(pos);
            return instance.destroyBlock(pos, false);
        }
        return instance.destroyBlock(pos, drop);
    }

    // Prevent coconut blocks from being placed on top of each other and handle custom hit effects.
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        TFCFallingBlockEntity entity = (TFCFallingBlockEntity) (Object) this;

        if (!entity.level().isClientSide && entity.isAlive()) {
            if (this.tfg$coconutEffectCooldown > 0) {
                this.tfg$coconutEffectCooldown--;
            }

            if (this.tfg$coconutEffectCooldown == 0) {
                BlockState state = entity.getBlockState();

                if (state.is(TFGTags.Blocks.FALLING_CONCUSSIVE)) {
                    List<Entity> list = entity.level().getEntities(entity, entity.getBoundingBox(),
                            e -> e instanceof LivingEntity && e.isAlive() && !e.isInvulnerable() && !e.isSpectator());
                    boolean hit = false;

                    for (Entity e : list) {
                        if (e instanceof Player player && !player.isCreative()) {
                            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

                            int armorValue = 0;

                            if (!helmet.isEmpty() && helmet.getItem() instanceof ArmorItem armorItem) {
                                armorValue = armorItem.getDefense();
                            }

                            if (helmet.isEmpty() || armorValue < 1) {
                                player.addEffect(new MobEffectInstance(TFCEffects.PINNED.get(), 60, 0, false, false));
                                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 1, false, false));

                                if (player.getRandom().nextFloat() < 0.01f) {
                                    entity.level().playSound(null, e.getX(), e.getY(), e.getZ(), GTSoundEntries.METAL_PIPE.getMainEvent(), SoundSource.AMBIENT, 1.0f, 1.0f);
                                }
                            }
                        }

                        entity.level().playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.BAMBOO_HIT, SoundSource.AMBIENT, 2.0f, 0.1f);
                        if (entity.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.CRIT, e.getX() + 0.5, e.getY() + 2, e.getZ() + 0.5, 20, 0.1, 0.1, 0.1, 0.5);
                        }
                        hit = true;
                    }

                    if (hit) {
                        this.tfg$coconutEffectCooldown = 80;
                    }
                }
            }
        }

        if (entity.time == 0 && entity.getBlockState().getBlock() instanceof PalmFruitBlock) {
            entity.time = 1;
            ci.cancel();
        }
    }
}
