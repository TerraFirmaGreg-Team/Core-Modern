package su.terrafirmagreg.core.common.entity.slime;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import su.terrafirmagreg.core.TFGCore;

public enum TFGSlimeVariant implements StringRepresentable {
    GREEN(TFGCore.id("textures/entity/slime/green.png")),
    YELLOW(TFGCore.id("textures/entity/slime/yellow.png")),
    BLUE(TFGCore.id("textures/entity/slime/blue.png")),
    RED(TFGCore.id("textures/entity/slime/red.png"));

    public static final TFGSlimeVariant[] VALUES = values();

    private final String name;
    private final ResourceLocation texture;

    TFGSlimeVariant(ResourceLocation texture) {
        this.name = this.name().toLowerCase(Locale.ROOT);
        this.texture = texture;
    }

    public @NotNull String getSerializedName() {
        return this.name;
    }

    public @NotNull ResourceLocation getTexture() {
        return this.texture;
    }

    public static TFGSlimeVariant byName(String name) {
        for (TFGSlimeVariant variant : VALUES) {
            if (variant.getSerializedName().equals(name))
                return variant;
        }
        return GREEN;
    }
}
