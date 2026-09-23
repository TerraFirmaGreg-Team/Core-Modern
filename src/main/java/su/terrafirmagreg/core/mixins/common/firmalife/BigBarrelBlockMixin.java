package su.terrafirmagreg.core.mixins.common.firmalife;

import org.spongepowered.asm.mixin.Mixin;

import com.eerussianguy.firmalife.common.blockentities.FLBlockEntities;
import com.eerussianguy.firmalife.common.blocks.BigBarrelBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**  Give every keg part its own block entity so they can accept input/output like the core part. see BigBarrelBlockEntityMixin */
@Mixin(value = BigBarrelBlock.class, remap = false)
public abstract class BigBarrelBlockMixin implements EntityBlock {
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return FLBlockEntities.BIG_BARREL.get().create(pos, state);
    }
}
