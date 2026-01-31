package su.terrafirmagreg.core.mixins.common.beneath;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.eerussianguy.beneath.ForgeEvents;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

@Mixin(value = ForgeEvents.class)
public class BeneathForgeEventsMixin {

    // Overwrite Beneath's method which controls what beneath mobs spawn with,
    // and replace it with our own, so we can give them more appropriate items.

    @Redirect(method = "onEntityJoinLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V", remap = true), remap = false)
    private static void tfg$onEntityJoinLevel(LivingEntity entity, EquipmentSlot slot, ItemStack itemStack) {

        if (entity instanceof Piglin) {
            if (slot == EquipmentSlot.MAINHAND) {
                if (entity.getRandom().nextFloat() < 0.4f) {
                    entity.setItemSlot(slot, new ItemStack(Items.CROSSBOW));
                } else {
                    entity.setItemSlot(slot, new ItemStack(Helpers.getRandomElement(ForgeRegistries.ITEMS, TFCTags.Items.mobEquipmentSlotTag(slot), (entity).getRandom()).orElse(Items.AIR)));
                }
            } else {
                entity.setItemSlot(slot, new ItemStack(Helpers.getRandomElement(ForgeRegistries.ITEMS, TFCTags.Items.mobEquipmentSlotTag(slot), (entity).getRandom()).orElse(Items.AIR)));
            }
        } else if (entity instanceof PiglinBrute) {
            if (slot == EquipmentSlot.MAINHAND) {
                if (entity.getRandom().nextFloat() < 0.5f) {
                    entity.setItemSlot(slot, ToolHelper.get(GTToolType.AXE, GTMaterials.DamascusSteel));
                } else {
                    entity.setItemSlot(slot, ToolHelper.get(GTToolType.SWORD, GTMaterials.DamascusSteel));
                }
            } else {
                entity.setItemSlot(slot, new ItemStack(Helpers.getRandomElement(ForgeRegistries.ITEMS, TFCTags.Items.mobEquipmentSlotTag(slot), (entity).getRandom()).orElse(Items.AIR)));
            }
        } else if (entity instanceof WitherSkeleton) {
            if (slot == EquipmentSlot.MAINHAND) {
                entity.setItemSlot(slot, new ItemStack(TFCItems.METAL_ITEMS.get(Metal.Default.BISMUTH_BRONZE).get(Metal.ItemType.MACE).get()));
            }
        } else if (entity instanceof ZombifiedPiglin) {
            entity.setItemSlot(slot, new ItemStack(Helpers.getRandomElement(ForgeRegistries.ITEMS, TFCTags.Items.mobEquipmentSlotTag(slot), (entity).getRandom()).orElse(Items.AIR)));
        }
    }
}
