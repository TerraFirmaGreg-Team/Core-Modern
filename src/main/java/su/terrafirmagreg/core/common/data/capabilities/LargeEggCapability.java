package su.terrafirmagreg.core.common.data.capabilities;

import net.dries007.tfc.util.Helpers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import su.terrafirmagreg.core.TFGCore;

import javax.annotation.Nullable;

public class LargeEggCapability
{
    public static final Capability<ILargeEgg> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation KEY = TFGCore.id("sniffer_egg");

    @Nullable
    public static ILargeEgg get(ItemStack stack)
    {
        return Helpers.getCapability(stack, CAPABILITY);
    }
}