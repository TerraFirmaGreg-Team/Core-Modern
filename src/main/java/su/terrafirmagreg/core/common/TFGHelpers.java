package su.terrafirmagreg.core.common;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.mixins.common.tfc.IIngotPileBlockEntityEntryAccessor;

public final class TFGHelpers {

    public static final Random RANDOM = new Random();

    public static boolean isMaterialRegistrationFinished;

    /**
     * Used in KubeJS!
     */
    @Nullable
    public static Material getMaterial(@NotNull String materialName) {
        var material = GTCEuAPI.materialManager.getMaterial(materialName);
        if (material == null) {
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

    public static void registerMaterialInfo(ResourceLocation itemId, Map<String, Double> materialStacks) {
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        //System.out.println(item);
        if (item == Items.AIR || item == null) {
            TFGCore.LOGGER.error("Error in registerMaterialInfo - item not found: {}", itemId);
            return;
        }

        Reference2LongOpenHashMap<Material> matStacks = new Reference2LongOpenHashMap<>();
        for (var tuple : materialStacks.entrySet()) {
            var material = getMaterial(tuple.getKey());
            if (material == null) {
                TFGCore.LOGGER.error("Error in registerMaterialInfo - material not found: {}", tuple.getKey());
                return;
            }

            matStacks.addTo(material, Math.round(tuple.getValue() * GTValues.M));
        }

        ItemMaterialData.registerMaterialInfo(item, new ItemMaterialInfo(matStacks));
    }

    public static void registerCobbleBlock(String tagPrefix, ResourceLocation cobbleBlock) {
        GTBlocks.registerCobbleBlock(TagPrefix.get(tagPrefix),
                () -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(cobbleBlock)).defaultBlockState());
    }
}
