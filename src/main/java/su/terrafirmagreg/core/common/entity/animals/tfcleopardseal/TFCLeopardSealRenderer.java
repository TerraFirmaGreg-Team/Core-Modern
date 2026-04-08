/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.entity.animals.tfcleopardseal;

import net.dries007.tfc.client.render.entity.SimpleMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;

public class TFCLeopardSealRenderer extends SimpleMobRenderer<TFCLeopardSeal, TFCLeopardSealModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TFGCore.MOD_ID, "textures/entity/animal/leopard_seal.png");

    public TFCLeopardSealRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TFCLeopardSealModel(ctx.bakeLayer(TFCLeopardSealModel.LAYER_LOCATION)), "leopard_seal",
                0.7F, false, 1.0f, false, false, x -> TEXTURE);
    }
}
