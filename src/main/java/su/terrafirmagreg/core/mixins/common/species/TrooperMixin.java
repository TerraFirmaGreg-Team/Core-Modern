package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ninni.species.server.entity.mob.update_2.Trooper;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import earth.terrarium.adastra.common.tags.ModBlockTags;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = Trooper.class, remap = false)
public abstract class TrooperMixin extends TamableAnimal {
    public TrooperMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    // Enable spawns

    @Inject(method = "canSpawn", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$canSpawn(EntityType<Trooper> entity, ServerLevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        BlockState thisBlock = world.getBlockState(pos);
        BlockState belowBlock = world.getBlockState(pos.below());
        cir.setReturnValue(thisBlock.is(Blocks.AIR) && belowBlock.is(ModBlockTags.VENUS_STONE_REPLACEABLES));
    }

    // Replace their "food" with sulfur

    @Redirect(method = "registerGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Ingredient;of([Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/crafting/Ingredient;"), remap = true)
    private Ingredient tfg$registerGoals(ItemLike[] items) {
        return Ingredient.of(TFGTags.Items.SULFIE_FOOD);
    }

    @Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 1), remap = true)
    private boolean tfg$mobInteract(ItemStack instance, Item item) {
        return instance.is(TFGTags.Items.SULFIE_FOOD);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
