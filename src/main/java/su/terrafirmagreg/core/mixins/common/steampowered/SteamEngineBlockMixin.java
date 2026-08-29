package su.terrafirmagreg.core.mixins.common.steampowered;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.teammoeg.steampowered.content.engine.SteamEngineBlock;
import com.teammoeg.steampowered.oldcreatestuff.OldEngineBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

// Adds redstone support to the steam engine

@Mixin(value = SteamEngineBlock.class, remap = false)
public abstract class SteamEngineBlockMixin extends OldEngineBlock {

    @Shadow
    @Final
    public static BooleanProperty LIT;

    protected SteamEngineBlockMixin(Properties builder) {
        super(builder);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/teammoeg/steampowered/content/engine/SteamEngineBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V"), remap = true)
    private void tfg$init(SteamEngineBlock instance, BlockState state) {
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(BlockStateProperties.POWERED, false));
    }

    @ModifyArg(method = "createBlockStateDefinition", at = @At(value = "INVOKE", target = "Lcom/teammoeg/steampowered/oldcreatestuff/OldEngineBlock;createBlockStateDefinition(Lnet/minecraft/world/level/block/state/StateDefinition$Builder;)V"), remap = true)
    private StateDefinition.Builder<Block, BlockState> tfg$createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        return builder.add(BlockStateProperties.POWERED);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
        level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, level.hasNeighborSignal(pos)), 2);
        super.neighborChanged(state, level, pos, block, otherPos, moving);
    }
}
