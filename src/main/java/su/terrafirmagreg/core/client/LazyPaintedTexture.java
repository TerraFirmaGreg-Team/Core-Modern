package su.terrafirmagreg.core.client;

import com.alekiponi.alekiships.client.render.texture.PaintedTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.TFGCore;

import java.io.IOException;

public class LazyPaintedTexture extends AbstractTexture {
    private final PaintedTexture deferredTexture;
    private ResourceManager resourceManager;
    private boolean loaded = false;

    public LazyPaintedTexture(PaintedTexture deferredTexture) {
        this.deferredTexture = deferredTexture;
    }

    @Override
    public void load(@NotNull ResourceManager resourceManager) throws IOException {
        this.resourceManager = resourceManager;
    }

    @Override
    public int getId() {
        if (!loaded) {
            try {
                TFGCore.LOGGER.debug("Lazy paint loading texture: {}", deferredTexture);
                deferredTexture.load(resourceManager);
                loaded = true;
            } catch (IOException e) {
                TFGCore.LOGGER.error("Lazy paint failed", e);
            }
        } else {
            TFGCore.LOGGER.debug("Lazy paint texture already loaded: {}", deferredTexture);
        }
        return deferredTexture.getId();
    }

    public void releaseId() {
        deferredTexture.releaseId();
    }
}
