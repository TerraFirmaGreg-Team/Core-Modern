package su.terrafirmagreg.core.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData;

@OnlyIn(Dist.CLIENT)
public final class ClientWorldgenVersion {

    private ClientWorldgenVersion() {
    }

    public static void apply(int overworldVersion) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            return;
        }
        WorldgenVersionData.OVERWORLD_VERSION = overworldVersion;
        WorldgenVersionData.OVERWORLD_SESSION_VERSION_RESOLVED = true;
    }

    public static void clear() {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            return;
        }
        WorldgenVersionData.OVERWORLD_VERSION = 0;
        WorldgenVersionData.OVERWORLD_SESSION_VERSION_RESOLVED = false;
    }
}
