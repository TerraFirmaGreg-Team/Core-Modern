package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.dries007.tfc.client.model.entity.DogCollarLayer;
import net.dries007.tfc.client.model.entity.DogModel;
import net.dries007.tfc.client.render.entity.DogRenderer;
import net.dries007.tfc.common.entities.livestock.pet.Dog;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.entity.animals.tfcdog.TFCDog;
import su.terrafirmagreg.core.common.entity.animals.tfcdog.TFCDogVariant;

@Mixin(value = DogRenderer.class)
public class TFCDogRendererMixin extends MobRenderer<Dog, DogModel> {
    @Unique
    private static final ResourceLocation TEXTURE_DEFAULT = TFGCore.id("textures/entity/animal/dog/default.png");
    @Unique
    private static final ResourceLocation TEXTURE_ASHEN = TFGCore.id("textures/entity/animal/dog/ashen.png");
    @Unique
    private static final ResourceLocation TEXTURE_BLACK = TFGCore.id("textures/entity/animal/dog/black.png");
    @Unique
    private static final ResourceLocation TEXTURE_CHESTNUT = TFGCore.id("textures/entity/animal/dog/chestnut.png");
    @Unique
    private static final ResourceLocation TEXTURE_RUSTY = TFGCore.id("textures/entity/animal/dog/rusty.png");
    @Unique
    private static final ResourceLocation TEXTURE_SNOWY = TFGCore.id("textures/entity/animal/dog/snowy.png");
    @Unique
    private static final ResourceLocation TEXTURE_SPOTTED = TFGCore.id("textures/entity/animal/dog/spotted.png");
    @Unique
    private static final ResourceLocation TEXTURE_STRIPED = TFGCore.id("textures/entity/animal/dog/striped.png");
    @Unique
    private static final ResourceLocation TEXTURE_WOODS = TFGCore.id("textures/entity/animal/dog/woods.png");

    public TFCDogRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx, new DogModel(ctx.bakeLayer(ModelLayers.WOLF)), 0.5F);
        this.addLayer(new DogCollarLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Dog entity) {
        TFCDogVariant variant = ((TFCDog) entity).tfg$getVariant();

        return switch (variant) {
            case ASHEN -> TEXTURE_ASHEN;
            case BLACK -> TEXTURE_BLACK;
            case CHESTNUT -> TEXTURE_CHESTNUT;
            case RUSTY -> TEXTURE_RUSTY;
            case SNOWY -> TEXTURE_SNOWY;
            case SPOTTED -> TEXTURE_SPOTTED;
            case STRIPED -> TEXTURE_STRIPED;
            case WOODS -> TEXTURE_WOODS;
            default -> TEXTURE_DEFAULT;
        };
    }
}
