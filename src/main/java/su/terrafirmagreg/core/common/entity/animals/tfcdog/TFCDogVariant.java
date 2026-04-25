package su.terrafirmagreg.core.common.entity.animals.tfcdog;

import java.util.function.IntFunction;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;

import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum TFCDogVariant implements StringRepresentable {
    DEFAULT(0, "default"),
    ASHEN(1, "ashen"),
    BLACK(2, "black"),
    CHESTNUT(3, "chestnut"),
    RUSTY(4, "rusty"),
    SNOWY(5, "snowy"),
    SPOTTED(6, "spotted"),
    STRIPED(7, "striped"),
    WOODS(8, "woods");

    private static final IntFunction<TFCDogVariant> BY_ID = ByIdMap.sparse(TFCDogVariant::id, values(), DEFAULT);
    public static final Codec<TFCDogVariant> CODEC = StringRepresentable.fromEnum(TFCDogVariant::values);
    public final int id;
    private final String name;

    TFCDogVariant(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public @NotNull String getSerializedName() {
        return this.name;
    }

    public int id() {
        return this.id;
    }

    public static TFCDogVariant byId(int id) {
        return (TFCDogVariant) BY_ID.apply(id);
    }
}
