package su.terrafirmagreg.core.common;

import java.util.*;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.mixins.common.tfc.IIngotPileBlockEntityEntryAccessor;

public final class TFGHelpers {

    public static final Random RANDOM = new Random();

    public static boolean isMaterialRegistrationFinished;

    /**
     * Returns GTMaterials.NULL when the material isn't found
     */
    @NotNull
    public static Material getMaterial(@NotNull String materialName) {
        var material = GTCEuAPI.materialManager.getMaterial(materialName);
        if (material == GTMaterials.NULL) {
            material = GTCEuAPI.materialManager.getMaterial(TFGCore.MOD_ID + ":" + materialName);
        }

        return material;
    }

    /**
     * Метод получает стак из списка стаков с доп проверками.
     */
    public static ItemStack getStackFromIngotPileTileEntityByIndex(List<?> entries, int index) {
        try {
            return ((IIngotPileBlockEntityEntryAccessor) (Object) entries.get(index)).getStack();
        } catch (IndexOutOfBoundsException e) {
            return ItemStack.EMPTY;
        }
    }

    public static void sendChatMessagePortalsIsDisabled(Level level, Entity entity) {
        if (level.isClientSide()) {
            if (level.getGameTime() % 100 == 0) {
                entity.sendSystemMessage(
                        Component.translatable("tfg.disabled_portal").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    // Second parameter should be like [Material, double, Material, double, ...]
    public static void registerMaterialInfo(ItemStack itemStack, Object[] materialStacks) {
        if (itemStack.isEmpty()) {
            TFGCore.LOGGER.error("Error in registerMaterialInfoAdv - item not found: {}", itemStack);
            return;
        }

        if (materialStacks.length % 2 != 0) {
            TFGCore.LOGGER.error("Error in registerMaterialInfoAdv - input array length is not a multiple of 2: {}", itemStack);
            return;
        }

        var matStacks = new ArrayList<MaterialStack>();
        for (int i = 0; i < materialStacks.length; i += 2) {
            if (materialStacks[i] instanceof Material mat && materialStacks[i + 1] instanceof Double count) {
                matStacks.add(new MaterialStack(mat, Math.round(count * GTValues.M)));
            } else {
                TFGCore.LOGGER.error("Error in registerMaterialInfoAdv - input item is not a material or double: {}", itemStack);
                return;
            }
        }

        ItemMaterialData.registerMaterialInfo(itemStack.getItem(), new ItemMaterialInfo(matStacks));
    }

    public static void registerCobbleBlock(String tagPrefix, ResourceLocation cobbleBlock) {
        GTBlocks.registerCobbleBlock(TagPrefix.get(tagPrefix),
                () -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(cobbleBlock)).defaultBlockState());
    }
}
