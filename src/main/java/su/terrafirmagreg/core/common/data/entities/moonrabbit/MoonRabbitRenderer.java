package su.terrafirmagreg.core.common.data.entities.moonrabbit;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.render.entity.TFCRabbitRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Rabbit;

import su.terrafirmagreg.core.TFGCore;

public class MoonRabbitRenderer extends TFCRabbitRenderer {

    private static final ResourceLocation RABBIT_PINK_LOCATION = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID,
            "textures/entity/moon_rabbit/pink.png");
    private static final ResourceLocation RABBIT_WHITE_LOCATION = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID,
            "textures/entity/moon_rabbit/white.png");
    private static final ResourceLocation RABBIT_GREY_LOCATION = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID,
            "textures/entity/moon_rabbit/grey.png");
    private static final ResourceLocation RABBIT_CYAN_LOCATION = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID,
            "textures/entity/moon_rabbit/cyan.png");
    private static final ResourceLocation RABBIT_PURPLE_LOCATION = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID,
            "textures/entity/moon_rabbit/purple.png");
    private static final ResourceLocation RABBIT_SOFU_LOCATION = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID,
            "textures/entity/moon_rabbit/sofu.png");

    public MoonRabbitRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Rabbit rabbit) {
        if (rabbit instanceof MoonRabbit moonRabbit) {
            return switch (moonRabbit.getMoonVariant()) {
                case PINK -> RABBIT_PINK_LOCATION;
                case WHITE -> RABBIT_WHITE_LOCATION;
                case GREY -> RABBIT_GREY_LOCATION;
                case CYAN -> RABBIT_CYAN_LOCATION;
                case PURPLE -> RABBIT_PURPLE_LOCATION;
                case SOFU -> RABBIT_SOFU_LOCATION;
            };
        } else {
            return super.getTextureLocation(rabbit);
        }
    }
}
