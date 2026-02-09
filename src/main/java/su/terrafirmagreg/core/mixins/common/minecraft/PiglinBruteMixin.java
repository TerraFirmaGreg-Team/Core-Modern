package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@Mixin(value = PiglinBrute.class)
public abstract class PiglinBruteMixin extends AbstractPiglin {

    public PiglinBruteMixin(EntityType<? extends AbstractPiglin> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    // Equips piglin brutes with items in all slots

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"))
    private void tfg$populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty, CallbackInfo ci) {
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        // TFC replaces these with random equipment from copper to wrought iron tier
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
    }
}
