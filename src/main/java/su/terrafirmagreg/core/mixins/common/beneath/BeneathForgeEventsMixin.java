package su.terrafirmagreg.core.mixins.common.beneath;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.eerussianguy.beneath.ForgeEvents;
import com.eerussianguy.beneath.common.blockentities.HellforgeBlockEntity;
import com.eerussianguy.beneath.common.blocks.HellforgeBlock;
import com.eerussianguy.beneath.common.blocks.HellforgeSideBlock;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.CharcoalPileBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.MultiBlock;
import net.dries007.tfc.util.events.StartFireEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
        } else if (entity instanceof WitherSkeleton ws) {
            if (slot == EquipmentSlot.MAINHAND) {
                entity.setItemSlot(slot, new ItemStack(TFCItems.METAL_ITEMS.get(Metal.Default.BLACK_STEEL).get(Metal.ItemType.MACE).get()));
                ws.setDropChance(slot, 0f);
            }
        } else if (entity instanceof ZombifiedPiglin) {
            entity.setItemSlot(slot, new ItemStack(Helpers.getRandomElement(ForgeRegistries.ITEMS, TFCTags.Items.mobEquipmentSlotTag(slot), (entity).getRandom()).orElse(Items.AIR)));
        }
    }

    // Replacement multiblock for the middle of the hellforge, using charcoal piles instead of cursecoal piles.
    @Unique
    private static final MultiBlock NEW_PRE_HELLFORGE_MULTIBLOCK = HellforgeBlockInvoker.makeMultiblock(
            s -> (s.getBlock() instanceof CharcoalPileBlock && s.getValue(CharcoalPileBlock.LAYERS) >= 7)
                    || s.getBlock() instanceof HellforgeBlock
                    || s.getBlock() instanceof HellforgeSideBlock,
            s -> (s.getBlock() instanceof CharcoalPileBlock && s.getValue(CharcoalPileBlock.LAYERS) >= 7)
                    || s.getBlock() instanceof HellforgeBlock
                    || s.getBlock() instanceof HellforgeSideBlock);

    /**
     * @author Pyritie
     * @reason Change the cursecoal piles to charcoal piles.
     * Too complicated/lazy to mixin, and the Beneath isn't really getting updated any more.
     */
    @Overwrite(remap = false)
    private static void onFireStart(StartFireEvent event) {
        final Level level = event.getLevel();
        final BlockPos pos = event.getPos();
        final BlockState state = event.getState();
        final Block block = state.getBlock();

        final boolean hfSide = block instanceof HellforgeSideBlock;
        final boolean hf = block instanceof HellforgeBlock;
        if (hf || hfSide) {
            BlockPos forgePos = pos;
            if (hfSide) {
                forgePos = HellforgeSideBlock.getCenterPos(level, pos);
            }
            if (forgePos != null && level.getBlockEntity(forgePos) instanceof HellforgeBlockEntity forge && HellforgeBlock.HELLFORGE_MULTIBLOCK.test(level, forgePos)
                    && state.getValue(CharcoalForgeBlock.HEAT) == 0 && forge.light()) {
                event.setCanceled(true);
            }
        } else if (block == TFCBlocks.CHARCOAL_PILE.get() && state.getValue(CharcoalPileBlock.LAYERS) >= 7) {
            final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (NEW_PRE_HELLFORGE_MULTIBLOCK.test(level, cursor.setWithOffset(pos, x, 0, z))) {
                        HellforgeBlockEntity.createFromCharcoalPile(level, cursor);
                        event.setCanceled(true);
                        return;
                    }
                }
            }

        }
    }
}
