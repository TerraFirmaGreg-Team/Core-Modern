package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import com.negodya1.vintageimprovements.content.kinetics.vacuum_chamber.VacuumChamberBlock;
import com.negodya1.vintageimprovements.content.kinetics.vacuum_chamber.VacuumChamberBlockEntity;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows GregTech wrenches to also flip the vacuum chamber mode
 */
@Mixin(value = VacuumChamberBlock.class)
public abstract class VacuumChamberBlockMixin extends KineticBlock implements IBE<VacuumChamberBlockEntity>, ICogWheel {
	public VacuumChamberBlockMixin(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context)
	{
		var worldIn = context.getLevel();
		var pos = context.getClickedPos();

		var be = this.getBlockEntity(worldIn, pos);
		if (be != null)
		{
			be.changeMode();
			if (worldIn.isClientSide()) {
				AllSoundEvents.WRENCH_ROTATE.playAt(worldIn, pos, 3, 1, true);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Inject(method="use", at = @At("HEAD"), cancellable = true)
	public void tfg$use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {

		cir.setReturnValue(InteractionResult.PASS);
	}
}
