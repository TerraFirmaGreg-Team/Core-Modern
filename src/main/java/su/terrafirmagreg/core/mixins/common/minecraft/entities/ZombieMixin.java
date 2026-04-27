package su.terrafirmagreg.core.mixins.common.minecraft.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Metal;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(value = Zombie.class)
public abstract class ZombieMixin extends Monster {
    protected ZombieMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author Pyritie
     * @reason Remove the vanilla iron shovel/sword from zombies default held items
     */
    @Overwrite
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);

        if (this.level().getDifficulty() == Difficulty.HARD && random.nextFloat() < 0.25f) {
            int i = random.nextInt(5);
            if (i == 0) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ToolHelper.get(GTToolType.AXE, GTMaterials.Copper));
            } else if (i == 1) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFCItems.METAL_ITEMS.get(Metal.Default.COPPER).get(Metal.ItemType.MACE).get()));
            } else if (i == 2) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ToolHelper.get(GTToolType.SWORD, GTMaterials.Copper));
            } else if (i == 3) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ToolHelper.get(GTToolType.KNIFE, GTMaterials.Copper));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, ToolHelper.get(GTToolType.SHOVEL, GTMaterials.Copper));
            }
        }
    }
}
