package su.terrafirmagreg.core.common.entity.slime;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import dev.ftb.mods.ftbquests.MethodsReturnNonnullByDefault;
import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.items.TFGItems_Slimes;

@MethodsReturnNonnullByDefault
public enum SlimeVariant implements StringRepresentable {
    PLANT(TFGCore.id("textures/entity/slime/plant.png"), Level.NETHER, TFGTags.Biomes.PlantSlimeHabitat, TFGItems_Slimes.PLANT_SLIME_BALL.asItem()),
    GLOWBERRY(TFGCore.id("textures/entity/slime/glowberry.png"), Level.NETHER, TFGTags.Biomes.GlowberrySlimeHabitat, TFGItems_Slimes.GLOWBERRY_SLIME_BALL.asItem()),
    SPRING(TFGCore.id("textures/entity/slime/spring.png"), Level.NETHER, TFGTags.Biomes.SpringSlimeHabitat, null),
    ICE(TFGCore.id("textures/entity/slime/ice.png"), Level.NETHER, TFGTags.Biomes.IceSlimeHabitat, null),
    LAVA(TFGCore.id("textures/entity/slime/lava.png"), Level.NETHER, TFGTags.Biomes.LavaSlimeHabitat, null),
    LATEX(TFGCore.id("textures/entity/slime/latex.png"), Level.NETHER, null, TFGItems_Slimes.LATEX_SLIME_BALL.asItem());

    private static final Map<String, SlimeVariant> variantNameMap = new HashMap<>();

    static {
        for (SlimeVariant variant : values()) {
            variantNameMap.put(variant.getSerializedName(), variant);
        }
    }

    private final String name;
    @Getter
    private final ResourceLocation texture;
    @Getter
    private final ResourceKey<Level> dimension;
    @Getter
    private final TagKey<Biome> biome;
    @Getter
    private final Item item;

    SlimeVariant(ResourceLocation texture, @Nullable ResourceKey<Level> dimension, @Nullable TagKey<Biome> biome, @Nullable Item item) {
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

    public static SlimeVariant getByName(String name) {
        SlimeVariant variant = variantNameMap.get(name);
        if (variant != null) {
            return variant;
        }

        return SPRING;
    }

    public static SlimeVariant getByHabitat(ResourceKey<Level> dimension, Holder<Biome> biome) {
        for (Map.Entry<String, SlimeVariant> entry : variantNameMap.entrySet()) {
            SlimeVariant variant = entry.getValue();

            if (variant.getBiome() != null &&
                    dimension.equals(variant.getDimension()) &&
                    biome.is(variant.getBiome())) {
                return variant;
            }
        }

        return SPRING;
    }
    // endregion
}
