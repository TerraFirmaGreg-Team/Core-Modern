package su.terrafirmagreg.core.common.entity.slime;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import dev.ftb.mods.ftbquests.MethodsReturnNonnullByDefault;
import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

@MethodsReturnNonnullByDefault
public enum TFGSlimeVariant implements StringRepresentable {
    PLANT(TFGCore.id("textures/entity/slime/latex.png"), Level.NETHER, TFGTags.Biomes.PlantSlimeHabitat, Items.SLIME_BALL),
    GLOWBERRY(TFGCore.id("textures/entity/slime/latex.png"), Level.NETHER, TFGTags.Biomes.GlowberrySlimeHabitat, Items.SLIME_BALL),
    SPRING(TFGCore.id("textures/entity/slime/latex.png"), Level.NETHER, TFGTags.Biomes.SpringSlimeHabitat, null),
    ICE(TFGCore.id("textures/entity/slime/latex.png"), Level.NETHER, TFGTags.Biomes.IceSlimeHabitat, null),
    LAVA(TFGCore.id("textures/entity/slime/latex.png"), Level.NETHER, TFGTags.Biomes.LavaSlimeHabitat, null),
    RESIN(TFGCore.id("textures/entity/slime/latex.png"), Level.NETHER, null, Items.SLIME_BALL),
    LATEX(TFGCore.id("textures/entity/slime/latex.png"), Level.NETHER, null, Items.SLIME_BALL);

    public static final TFGSlimeVariant[] VALUES = values();

    private final String name;
    @Getter
    private final ResourceLocation texture;
    @Getter
    private final ResourceKey<Level> dimension;
    @Getter
    private final TagKey<Biome> biome;
    @Getter
    private final Item item;

    TFGSlimeVariant(ResourceLocation texture, @Nullable ResourceKey<Level> dimension, @Nullable TagKey<Biome> biome, @Nullable Item item) {
        this.name = this.name().toLowerCase(Locale.ROOT);
        this.texture = texture;
        this.dimension = dimension;
        this.biome = biome;
        this.item = item;
    }

    // region Getters
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public static TFGSlimeVariant getByName(String name) {
        for (TFGSlimeVariant variant : VALUES) {
            if (variant.getSerializedName().equals(name))
                return variant;
        }
        return SPRING;
    }
    // endregion
}
