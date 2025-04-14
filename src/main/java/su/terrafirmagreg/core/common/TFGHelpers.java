package su.terrafirmagreg.core.common;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.mixins.common.tfc.IIngotPileBlockEntityEntryAccessor;

import java.util.List;
import java.util.Random;

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
     * */
    public static ItemStack getStackFromIngotPileTileEntityByIndex(List<?> entries, int index) {
        try
        {
            return  ((IIngotPileBlockEntityEntryAccessor) (Object) entries.get(index)).getStack();
        }
        catch (IndexOutOfBoundsException e)
        {
            return ItemStack.EMPTY;
        }
    }

    public static void sendChatMessagePortalsIsDisabled(Level level, Entity entity) {
        if (level.isClientSide()) {
            if (level.getGameTime() % 100 == 0) {
                entity.sendSystemMessage(Component.translatable("tfg.disabled_portal").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }
}
