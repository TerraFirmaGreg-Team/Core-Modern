package su.terrafirmagreg.core.mixins.common.species;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.ninni.species.entity.enums.LimpetType;
import com.ninni.species.registry.SpeciesSoundEvents;
import com.ninni.species.registry.SpeciesTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import com.ninni.species.entity.Limpet;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix;

import java.util.Arrays;
import java.util.Optional;

@Mixin(value = Limpet.class, remap = false)
public abstract class LimpetMixin extends PathfinderMob {

	protected LimpetMixin(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Shadow @Final
	private static UniformInt RETREAT_DURATION;
	@Shadow
	public abstract LimpetType getLimpetType();
	@Shadow
	public abstract void setLimpetType(int id);
	@Shadow
	protected abstract void spawnBreakingParticles();
	@Shadow
	public abstract int getCrackedStage();
	@Shadow
	public abstract void setCrackedStage(int crackedStage);
	@Shadow
	public abstract void setScaredTicks(int scaredTicks);

	private boolean canMine(ItemStack stack)
	{
		return GTToolType.PICKAXE.is(stack)
			|| GTToolType.MINING_HAMMER.is(stack)
			|| GTToolType.DRILL_LV.is(stack)
			|| GTToolType.DRILL_MV.is(stack)
			|| GTToolType.DRILL_HV.is(stack)
			|| GTToolType.DRILL_EV.is(stack)
			|| GTToolType.DRILL_IV.is(stack);
	}

	/**
	 * @author Pyritie
	 * @reason Normally it looks for an item that inherits from PickaxeItem which GT tools don't do
	 */
	@Overwrite
	public boolean isValidEntity(Player player) {
		Optional<ItemStack> stack = this.getStackInHand(player);
		return this.getLimpetType().getId() > 0
			&& !player.isSpectator()
			&& player.isAlive()
			&& !player.getAbilities().instabuild
			&& !player.isShiftKeyDown() ||
				(this.getLimpetType().getId() > 0 && stack.isPresent() && canMine(stack.get()));
	}

	/**
	 * @author Pyritie
	 * @reason Normally it looks for an item that inherits from PickaxeItem which GT tools don't do
	 */
	@Overwrite
	public boolean isValidEntityHoldingPickaxe(Player player) {
		return this.getLimpetType().getId() > 0 && this.getStackInHand(player).isPresent() && canMine(this.getStackInHand(player).get());
	}

	/**
	 * @author Pyritie
	 * @reason Normally it looks for an item that inherits from PickaxeItem which GT tools don't do
	 */
	@Overwrite
	public Optional<ItemStack> getStackInHand(Player player) {
		return Arrays.stream(InteractionHand.values()).filter(hand -> canMine(player.getItemInHand(hand))).map(player::getItemInHand).findFirst();
	}

	/**
	 * @author Pyritie
	 * @reason Normally it looks for an item that inherits from PickaxeItem which GT tools don't do
	 */
	@Overwrite
	public boolean hurt(DamageSource source, float amount) {
		LimpetType type = this.getLimpetType();
		if (source.getEntity() instanceof Player player
				&& type.getId() > 0
				&& !player.getMainHandItem().isEmpty()
				&& canMine(player.getMainHandItem())) {

			if (type.getId() > 0) spawnBreakingParticles();

			if (this.getCrackedStage() < 3) {
				this.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, player, RETREAT_DURATION.sample(this.level().random));
				this.setCrackedStage(this.getCrackedStage() + 1);
				this.playSound(type.getAdditionalBreakSound(), 1, (float) this.getCrackedStage() * 0.3f + 0.5f);
				this.playSound(SpeciesSoundEvents.LIMPET_BREAK.get(), 0.6f, this.getCrackedStage() + 1);
				this.setScaredTicks(0);
				for (ItemStack itemStack : player.getInventory().items) {
					if (canMine(itemStack)) {
						player.getCooldowns().addCooldown(itemStack.getItem(), player.getAbilities().instabuild ? 0 : 80);
					}
				}
				return false;
			} else {
				int count = (type.getMaxCount()/2 + random.nextInt(type.getMaxCount()/2)) / 2;
				if (type.getId() > 0) {
					Item item = null;
					if (type == LimpetType.SHELL)
						item = ChemicalHelper.get(TFGTagPrefix.poorRawOre, GTMaterials.Calcite).getItem();
					else if (type == LimpetType.COAL)
						item = ChemicalHelper.get(TFGTagPrefix.poorRawOre, GTMaterials.Graphite).getItem();
					else if (type == LimpetType.LAPIS)
						item = ChemicalHelper.get(TFGTagPrefix.poorRawOre, GTMaterials.Lapis).getItem();
					else if (type == LimpetType.EMERALD)
						item = ChemicalHelper.get(TFGTagPrefix.poorRawOre, GTMaterials.Emerald).getItem();
					else if (type == LimpetType.AMETHYST)
						item = ChemicalHelper.get(TFGTagPrefix.poorRawOre, GTMaterials.Amethyst).getItem();
					else if (type == LimpetType.DIAMOND)
						item = ChemicalHelper.get(TFGTagPrefix.poorRawOre, GTMaterials.Diamond).getItem();

					if (item != null) {
						for (int i = 0; i < count; i++) {
							this.spawnAtLocation(item, 1);
						}
					}
				}

				this.playSound(type.getAdditionalBreakSound(), 1, (float) this.getCrackedStage() * 0.3f + 1f);
				this.playSound(SpeciesSoundEvents.LIMPET_BREAK.get(), 0.6f, this.getCrackedStage() + 1.5f);
				this.setCrackedStage(0);
				this.setLimpetType(0);
				this.setScaredTicks(0);
			}

		} else if (source.getEntity() instanceof LivingEntity && amount < 12 && !this.level().isClientSide && type.getId() > 0) {
			this.playSound(SpeciesSoundEvents.LIMPET_DEFLECT.get(), 1, 1);
			if (!this.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET)) this.setScaredTicks(300);
			return false;
		}
		return super.hurt(source, amount);
	}

	// Just spawn all types randomly, ignore the default Y level exclusivity
	@Inject(method = "chooseLimpetType", at = @At("HEAD"), remap = false, cancellable = true)
	public void chooseLimpetType(LevelAccessor world, CallbackInfoReturnable<Integer> cir) {
		float random = this.random.nextFloat();

		if (random < 0.01) cir.setReturnValue(LimpetType.NO_SHELL.getId());
		else if (random < 0.25) cir.setReturnValue(LimpetType.SHELL.getId());
		else if (random < 0.45) cir.setReturnValue(LimpetType.COAL.getId());
		else if (random < 0.6) cir.setReturnValue(LimpetType.LAPIS.getId());
		else if (random < 0.75) cir.setReturnValue(LimpetType.AMETHYST.getId());
		else if (random < 0.9) cir.setReturnValue(LimpetType.EMERALD.getId());
		else cir.setReturnValue(LimpetType.DIAMOND.getId());
	}

	// Let it spawn anywhere instead of only underground
	@Inject(method = "canSpawn", at = @At("HEAD"), remap = false, cancellable = true)
	private static void tfg$canSpawn(EntityType<? extends PathfinderMob> entityType, ServerLevelAccessor levelAccessor, MobSpawnType spawnType, BlockPos blockPos, RandomSource randomSource, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(
			levelAccessor.getBrightness(LightLayer.BLOCK, blockPos) == 0
			&& levelAccessor.getBlockState(blockPos.below()).is(SpeciesTags.LIMPET_SPAWNABLE_ON)
			&& levelAccessor.getBlockState(blockPos.below()).isValidSpawn(levelAccessor, blockPos, entityType));
	}
}
