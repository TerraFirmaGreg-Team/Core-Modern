package su.terrafirmagreg.core.common.interdim_logistics;

import java.util.Objects;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public record DimensionalBlockPos(String dimension, BlockPos pos) {
    public DimensionalBlockPos(MetaMachine machine) {
        this(Objects.requireNonNull(machine.getLevel()).dimension().location().toString(), machine.getPos());
    }

    public DimensionalBlockPos(CompoundTag tag) {
        this(tag.getString("dim"), new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putString("dim", dimension);
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    public String getUiString() {
        return "%s (%s, %s, %s)".formatted(dimension, pos.getX(), pos.getY(), pos.getZ());
    }
}
