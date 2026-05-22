package su.terrafirmagreg.core.mixins.common.minecraft.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.TFGCore;

@Mixin(value = MagmaCube.class)
public class MagmaCubeMixin extends Slime {
    @Unique
    private static final TagKey<Item> MAGMA_FOOD = TagKey.create(Registries.ITEM, TFGCore.id("magma_food"));

    public MagmaCubeMixin(EntityType<? extends MagmaCube> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        final ItemStack held = player.getItemInHand(hand);
        if (held.is(MAGMA_FOOD) && held.getCount() >= 4) {
            if (!level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(4);
                }
                if (this.getHealth() - 1.0F <= 0.0F) {
                    this.kill();
                    level().explode(this, this.getX(), this.getY(), this.getZ(), 2.5F, Level.ExplosionInteraction.MOB);
                } else {
                    this.hurt(this.damageSources().generic(), 1.0F);
                    playSound(SoundEvents.PLAYER_BURP);
                    spawnAtLocation(ChemicalHelper.getDust(GTMaterials.RawRubber, GTValues.M));
                }
            }

            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }
}
