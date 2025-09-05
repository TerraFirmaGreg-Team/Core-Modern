package su.terrafirmagreg.core.common.data.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.terrafirmagreg.core.TFGCore;

public class SupportsCapability implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<SupportsCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation KEY = TFGCore.id("supports");




    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        return CAPABILITY.orEmpty(cap, capability);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        data.deserializeNBT(nbt);
    }

    void setData(Supports data)
    {
        this.data = data;
    }

    Supports getData()
    {
        return this.data;
    }
}
