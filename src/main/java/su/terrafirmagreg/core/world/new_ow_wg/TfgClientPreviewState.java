package su.terrafirmagreg.core.world.new_ow_wg;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLEnvironment;

import lombok.Getter;
import lombok.Setter;

/**
 * Tracks TFCGenViewer preview sessions on the logical client.
 * {@link WorldgenVersionData#OVERWORLD_VERSION} is set on the server JVM; multiplayer clients often
 * see {@code 0} here until synced, while TFCGenViewer responses append the overworld gen version
 * (see {@code ViewerResponsePacket} mixins) — use {@link #setPendingPacketOverworldVersion(int)}
 * paired with preview {@link #enter()}.
 * <p>
 * The create-world {@code PreviewGenerationScreen} runs {@link net.dries007.tfc.world.region.RegionGenerator} locally
 * without a {@link com.notenoughmail.tfcgenviewer.network.packets.ViewerResponsePacket}, while {@link WorldgenVersionData#OVERWORLD_VERSION} stays {@code 0}
 * until {@link WorldgenVersionData#OVERWORLD_SESSION_VERSION_RESOLVED}. During that gap we deliberately assume the
 * 1.21-backport overworld pipeline for preview only . Server-side resolution in
 * {@link su.terrafirmagreg.core.common.event.WorldgenVersionEvents} still decides the real saved version once a logical
 * server loads. 
 */
public final class TfgClientPreviewState {

    /** Server appends overworld version to {@link com.notenoughmail.tfcgenviewer.network.packets.ViewerResponsePacket} after decode reads the vanilla tail. */
    @Setter
    private static volatile int pendingPacketOverworldVersion;

    @Getter
    private static volatile boolean active;

    /** Effective overworld gen version for this TFCGenViewer preview session (from decoded packet tail). */
    private static volatile int previewSessionOverworldVersion;

    private TfgClientPreviewState() {
    }

    /**
     * Call when opening ViewWorldScreen; copies {@link #pendingPacketOverworldVersion} produced by decode
     * for this viewer response packet.
     */
    public static void enter() {
        active = true;
        previewSessionOverworldVersion = pendingPacketOverworldVersion;
        pendingPacketOverworldVersion = 0;
    }

    /**
     * Ends any previous preview session but keeps {@link #pendingPacketOverworldVersion} from the latest
     * decoded {@code ViewerResponsePacket} so {@link #enter()} can still apply it (e.g. create-world UI has no player).
     */
    public static void resetSessionPreserveDecodedTail() {
        active = false;
        previewSessionOverworldVersion = 0;
    }

    /**
     * Full reset (aborted handoff / error paths). Clears decoded tail — use {@link #resetSessionPreserveDecodedTail()} when
     * {@link #enter()} follows on the success path.
     */
    public static void leave() {
        resetSessionPreserveDecodedTail();
        pendingPacketOverworldVersion = 0;
    }

    /**
     * Use the 1.21-backport TFG overworld pipeline (TFGLayers, generators, climate).
     * <p>
     * While a viewer response is in flight or the preview session is active, the packet tail (see mixins on
     * {@code ViewerResponsePacket}) is authoritative — not {@link WorldgenVersionData#OVERWORLD_VERSION},
     * which often stays wrong on multiplayer clients or can disagree with the save being inspected.
     * Outside that, the static session version matches integrated singleplayer in-world generation.
     * <p>
     * When the logical server has not yet resolved a session (create-world preview before {@code ServerAboutToStart}),
     * we always use the backport pipeline for preview so the UI matches the default new-world path without reading config
     * from disk.
     */
    public static boolean useTfgOverworldPipeline() {
        final int backport = WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT;
        if (WorldgenVersionData.OVERWORLD_VERSION == backport) {
            return true;
        }
        final boolean viewerRelatedContext = active || pendingPacketOverworldVersion != 0;
        if (viewerRelatedContext) {
            return pendingPacketOverworldVersion == backport || (active && previewSessionOverworldVersion == backport);
        }
        if (mayInferOverworldVersionWhenSessionNotResolved()) {
            return true;
        }
        return false;
    }

    private static boolean mayInferOverworldVersionWhenSessionNotResolved() {
        if (WorldgenVersionData.OVERWORLD_SESSION_VERSION_RESOLVED) {
            return false;
        }
        if (WorldgenVersionData.OVERWORLD_VERSION != 0) {
            return false;
        }
        if (!FMLEnvironment.dist.isClient()) {
            return false;
        }
        final Minecraft mc = Minecraft.getInstance();
        return mc.level == null || mc.getConnection() == null || mc.hasSingleplayerServer();
    }

}
