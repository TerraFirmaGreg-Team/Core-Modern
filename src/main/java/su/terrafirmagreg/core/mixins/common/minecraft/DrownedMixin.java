package su.terrafirmagreg.core.mixins.common.minecraft;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.eerussianguy.firmalife.common.items.FLItems;

import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(value = Drowned.class)
public abstract class DrownedMixin extends Zombie {

    @Shadow
    public abstract void updateSwimming();

    public DrownedMixin(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    /**
     * @author Pyritie
     * @reason Change nautilus shell for a firmalife hollow shell
     */
    @Overwrite
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor accessor, DifficultyInstance difficulty, MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnGroup, @Nullable CompoundTag tag) {

        spawnGroup = super.finalizeSpawn(accessor, difficulty, spawnType, spawnGroup, tag);

        if (this.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty() && accessor.getRandom().nextFloat() < 0.03F) {
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(FLItems.HOLLOW_SHELL.get()));
            this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
        }
        return spawnGroup;
    }

    /**
     * @author Pyritie
     * @reason Make them spawn like normal zombies
     */
    @Overwrite
    public static boolean checkDrownedSpawnRules(EntityType<Husk> entity, ServerLevelAccessor accessor, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (accessor.getFluidState(pos.below()).is(FluidTags.WATER))
            return false;

        return checkMonsterSpawnRules(entity, accessor, spawnType, pos, random);
    }

    /**
     * @author Pyritie
     * @reason Replace vanilla fishing rod
     */
    @Overwrite
    protected void populateDefaultEquipmentSlots(RandomSource random, @NotNull DifficultyInstance difficulty) {
		int i = random.nextInt(10);
		if (i < 10) {
			this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFCItems.METAL_ITEMS.get(Metal.Default.COPPER).get(Metal.ItemType.FISHING_ROD).get()));
		} else {
			this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
		}
    }
}
