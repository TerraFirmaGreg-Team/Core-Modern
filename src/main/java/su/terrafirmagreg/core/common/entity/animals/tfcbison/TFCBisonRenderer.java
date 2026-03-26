/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.entity.animals.tfcbison;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;

public class TFCBisonRenderer extends MobRenderer<TFCBison, TFCBisonModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TFGCore.MOD_ID, "textures/entity/animal/bison.png");

    public TFCBisonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TFCBisonModel(ctx.bakeLayer(TFCBisonModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TFCBison entity) {
        return TEXTURE;
    }
}
