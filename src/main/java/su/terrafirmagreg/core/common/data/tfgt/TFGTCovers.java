package su.terrafirmagreg.core.common.data.tfgt;

import java.util.function.Supplier;

import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;

import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.covers.RottenVoidCover;

/**
 * Registration of TFG GT covers.
 */
public class TFGTCovers {

    public static CoverDefinition ITEM_VOIDING_ROTTEN;

    public static void init() {
        registerAll();
    }

    private static CoverDefinition registerCover(String idPath, CoverDefinition.CoverBehaviourProvider provider, ResourceLocation texture) {
        ResourceLocation id = TFGCore.id(idPath);
        Supplier<Supplier<ICoverRenderer>> renderer = () -> () -> new SimpleCoverRenderer(texture);
        CoverDefinition def = new CoverDefinition(id, provider, renderer);
        GTRegistries.COVERS.register(id, def);
        return def;
    }

    private static void registerAll() {
        ITEM_VOIDING_ROTTEN = registerCover(
                "rotten_voiding_cover",
                RottenVoidCover::new,
                TFGCore.id("block/cover/rotten_voiding"));
    }
}
