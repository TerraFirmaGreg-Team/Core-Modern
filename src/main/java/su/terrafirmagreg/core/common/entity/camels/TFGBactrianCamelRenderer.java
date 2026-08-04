package su.terrafirmagreg.core.common.entity.camels;

import net.dries007.tfc.client.model.entity.HierarchicalAnimatedModel;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.camel.Camel;

import su.terrafirmagreg.core.TFGCore;

public class TFGBactrianCamelRenderer<T extends Camel, M extends HierarchicalAnimatedModel<T>> extends MobRenderer<T, M> {
    private final ResourceLocation young;
    private final ResourceLocation old;
    private final ResourceLocation saddled;
    private final ResourceLocation old_saddled;

    public TFGBactrianCamelRenderer(EntityRendererProvider.Context ctx, M model, float shadow) {
        super(ctx, model, shadow);
        this.young = TFGCore.id("textures/entity/bactrian_camel/young.png");
        this.old = TFGCore.id("textures/entity/bactrian_camel/old.png");
        this.saddled = TFGCore.id("textures/entity/bactrian_camel/saddle.png");
        this.old_saddled = TFGCore.id("textures/entity/bactrian_camel/old_saddle.png");
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        if (entity instanceof TFGAbstractCamel camel) {
            if (camel.isSaddled()) {
                return camel.getAgeType() == TFCAnimalProperties.Age.OLD ? old_saddled : saddled;
            } else
                return camel.getAgeType() == TFCAnimalProperties.Age.OLD ? old : young;
        } else
            return young;
    }
}
