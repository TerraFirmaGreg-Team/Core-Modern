package su.terrafirmagreg.core.mixins.common.minecraft;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.eerussianguy.firmalife.common.items.FLItems;

import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.entities.ai.JavelinAttackGoal;
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
import net.minecraft.world.level.levelgen.Heightmap;

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

        // Stop them spawning in oceans
        if (accessor.getLevel().getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ()) <= pos.getY())
            return false;

        return checkMonsterSpawnRules(entity, accessor, spawnType, pos, random);
    }

    /**
     * @author Pyritie
     * @reason Replace vanilla fishing rod
     */
    @Overwrite
    protected void populateDefaultEquipmentSlots(RandomSource random, @NotNull DifficultyInstance difficulty) {
        boolean isJavelin = false;
        int i = random.nextInt(10);
        if (i == 0) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFCItems.METAL_ITEMS.get(Metal.Default.COPPER).get(Metal.ItemType.FISHING_ROD).get()));
        } else if (i < 3) {
            // 20% for copper javelin
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFCItems.METAL_ITEMS.get(Metal.Default.COPPER).get(Metal.ItemType.JAVELIN).get()));
            isJavelin = true;
        } else if (i < 4) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFCItems.METAL_ITEMS.get(Metal.Default.BRONZE).get(Metal.ItemType.JAVELIN).get()));
            isJavelin = true;
        } else if (i < 5) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFCItems.METAL_ITEMS.get(Metal.Default.BISMUTH_BRONZE).get(Metal.ItemType.JAVELIN).get()));
            isJavelin = true;
        } else if (i < 6) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_BRONZE).get(Metal.ItemType.JAVELIN).get()));
            isJavelin = true;
        } else if (i == 9) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
        } else {
            // 30% for stone javelin
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFCItems.ROCK_TOOLS.get(RockCategory.IGNEOUS_INTRUSIVE).get(RockCategory.ItemType.JAVELIN).get()));
            isJavelin = true;
        }

        if (isJavelin) {
            this.goalSelector.addGoal(4, new JavelinAttackGoal<>(this, 1, 15f));
        }
    }
}
