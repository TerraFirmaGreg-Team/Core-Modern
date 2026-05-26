package su.terrafirmagreg.core.common.entity.slime;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.CatVariant;

public record SlimeVariant(String id, ResourceLocation texture) {
    public static final SlimeVariant GREEN = new SlimeVariant("green", ResourceLocation.withDefaultNamespace("textures/entity/slime/green.png"));
}
