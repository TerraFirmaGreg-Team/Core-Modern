package su.terrafirmagreg.core.mixins.common.opposing_force;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unusualmodding.opposing_force.blocks.SlugEggBlock;
import com.unusualmodding.opposing_force.entity.Slug;
import com.unusualmodding.opposing_force.registry.OPEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(value = SlugEggBlock.class, remap = false)
public class SlugEggBlockMixin extends Block {

    public SlugEggBlockMixin(Properties properties) {
        super(properties);
    }

    // Stop slug eggs from randomly breaking
    @Inject(method = "tick", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        ci.cancel();
    }

    // Spawn slugs when broken, instead of on a random tick
    @Override
    public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);

        level.playSound(null, pos, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + level.getRandom().nextFloat() * 0.2F);

        int i = state.getValue(SlugEggBlock.EGGS);

        for (int j = 0; j < i; ++j) {
            Vec3 vec3 = pos.getCenter();
            Slug slug = (Slug) ((EntityType<?>) OPEntities.SLUG.get()).create(level);
            if (slug != null) {
                slug.moveTo(vec3.x(), vec3.y(), vec3.z(), Mth.wrapDegrees(level.random.nextFloat() * 360.0F), 0.0F);
                level.addFreshEntity(slug);
            }
        }
    }
}
