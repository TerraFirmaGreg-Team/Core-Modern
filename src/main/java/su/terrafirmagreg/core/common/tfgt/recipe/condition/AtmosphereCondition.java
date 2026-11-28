package su.terrafirmagreg.core.common.tfgt.recipe.condition;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.tfgt.TFGRecipeConditions;
import su.terrafirmagreg.core.common.environment.EnvironmentSystem;

/**
 * Recipe condition that requires the atmosphere fluid to be in a tag.
 */
public class AtmosphereCondition extends RecipeCondition<AtmosphereCondition> {

    public static final Codec<AtmosphereCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(ForgeRegistries.FLUIDS.getRegistryKey()).fieldOf("fluid_tag").forGetter(cond -> cond.fluidTag))
            .apply(instance, AtmosphereCondition::new));

    private final TagKey<Fluid> fluidTag;

    public AtmosphereCondition() {
        this(TFGTags.Fluids.BreathableAir);
    }

    public AtmosphereCondition(TagKey<Fluid> fluidTag) {
        this.fluidTag = fluidTag;
    }

    public static AtmosphereCondition breathable() {
        return new AtmosphereCondition(TFGTags.Fluids.BreathableAir);
    }

    public static TagKey<Fluid> fluidTag(String name) {
        String id = name.startsWith("#") ? name.substring(1) : name;
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            throw new IllegalArgumentException("Invalid fluid tag: " + name);
        }
        return TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), location);
    }

    @Override
    public RecipeConditionType<AtmosphereCondition> getType() {
        return TFGRecipeConditions.ATMOSPHERE;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    @Override
    public Component getTooltips() {
        ResourceLocation id = fluidTag.location();
        String path = id.getPath().replace('/', '.');
        String suffix = "tfg".equals(id.getNamespace()) ? path : id.getNamespace().replace('/', '.') + "." + path;
        return Component.translatable("tfg.tooltip.recipe_condition.atmosphere." + suffix, "#" + id);
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.getMachine();
        var level = machine.getLevel();
        if (!(level instanceof ServerLevel serverLevel))
            return false;

        BlockPos pos = machine.getBlockPos();
        // Check the machine position directly, walls are oxygenated too.
        Fluid atmosphere = EnvironmentSystem.getAtmosphere(serverLevel, pos);
        return atmosphere != null && atmosphere.is(fluidTag);
    }

    @Override
    public AtmosphereCondition createTemplate() {
        return new AtmosphereCondition();
    }
}
