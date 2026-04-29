package su.terrafirmagreg.core.common.entity.animals.tfcwolf;

import java.util.function.IntFunction;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;

import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum TFCWolfVariant implements StringRepresentable {
    DEFAULT(0, "default"),
    ASHEN(1, "ashen"),
    BLACK(2, "black"),
    CHESTNUT(3, "chestnut"),
    RUSTY(4, "rusty"),
    SNOWY(5, "snowy"),
    SPOTTED(6, "spotted"),
    STRIPED(7, "striped"),
    WOODS(8, "woods");

    private static final IntFunction<TFCWolfVariant> BY_ID = ByIdMap.sparse(TFCWolfVariant::id, values(), DEFAULT);
    public static final Codec<TFCWolfVariant> CODEC = StringRepresentable.fromEnum(TFCWolfVariant::values);
    public final int id;
    private final String name;

    TFCWolfVariant(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public @NotNull String getSerializedName() {
        return this.name;
    }

    public int id() {
        return this.id;
    }

    public static TFCWolfVariant byId(int id) {
        return BY_ID.apply(id);
    }
}
