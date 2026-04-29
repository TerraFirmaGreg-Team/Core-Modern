package su.terrafirmagreg.core.world.new_ow_wg;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Tracks TFCGenViewer preview sessions on the logical client.
 * {@link WorldgenVersionData#OVERWORLD_VERSION} is set on the server JVM; multiplayer clients often
 * see {@code 0} here until synced, while TFCGenViewer responses append the overworld gen version
 * (see {@code ViewerResponsePacket} mixins) — use {@link #setPendingPacketOverworldVersion(int)}
 * paired with preview {@link #enter()}.
 * <p>
 * The create-world {@code PreviewGenerationScreen} runs {@link net.dries007.tfc.world.region.RegionGenerator} locally
 * without a {@link ViewerResponsePacket}, so {@link WorldgenVersionData#OVERWORLD_VERSION} stays 0 until
 * {@link WorldgenVersionData#OVERWORLD_SESSION_VERSION_RESOLVED}; in that window we infer the intended version from
 * {@link TFGConfig#SERVER} when loaded, otherwise from {@code defaultconfigs/tfg-server.toml} until a logical server loads
 * serverconfig (same template new worlds inherit).
 */
public final class TfgClientPreviewState {

    private static final String DEFAULT_SERVERCONFIG_NAME = "tfg-server.toml";
    private static final Object INFER_CACHE_LOCK = new Object();

    /**
     * Fingerprint of inputs to {@link #inferredOverworldVersionFromConfigWhenUnset()}: defaultconfigs mtime +
     * hash of server worldgenOverrides list. Invalidate via {@link #invalidateWorldgenInferCache()} when the server
     * config reloads (see {@link su.terrafirmagreg.core.config.TFGConfig#onLoad}).
     */
    private static volatile long inferCacheFingerprint = Long.MIN_VALUE;

    private static volatile int cachedInferOverworldVersion;

    /** Server appends overworld version to {@link com.notenoughmail.tfcgenviewer.network.packets.ViewerResponsePacket} after decode reads the vanilla tail. */
    private static volatile int pendingPacketOverworldVersion;

    private static volatile boolean active;

    /** Effective overworld gen version for this TFCGenViewer preview session (from decoded packet tail). */
    private static volatile int previewSessionOverworldVersion;

    private TfgClientPreviewState() {
    }

    public static void setPendingPacketOverworldVersion(int overworldGenVersionFromPacketTail) {
        pendingPacketOverworldVersion = overworldGenVersionFromPacketTail;
    }

    /** for myself: write doc
     * TODO: remove this method after testing 
     * 
     * 
     * 
     * 
     * else it still works */

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

    public static boolean isActive() {
        return active;
    }

    /**
     * Call when SERVER-type TFG config is (re)loaded so cached infer results pick up edited {@code worldgenOverrides}.
     */
    public static void invalidateWorldgenInferCache() {
        synchronized (INFER_CACHE_LOCK) {
            inferCacheFingerprint = Long.MIN_VALUE;
        }
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
     * we infer the version from server config so {@code PreviewGenerationScreen} matches the world that will be created.
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
            return inferredOverworldVersionFromConfigWhenUnset() == backport;
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
        if (mc == null) {
            return false;
        }
        if (mc.level != null && mc.getConnection() != null && !mc.hasSingleplayerServer()) {
            return false;
        }
        return true;
    }

    /**
     * Create-world preview happens before a new world's serverconfig exists. The template
     * {@code defaultconfigs/tfg-server.toml} is what Forge copies into {@code serverconfig} for a new save, so it wins
     * over any stale in-memory {@link TFGConfig#SERVER} from a previously loaded world when both define the overworld key.
     */
    private static int inferredOverworldVersionFromConfigWhenUnset() {
        final long fp = computeInferInputFingerprint();
        if (fp == inferCacheFingerprint) {
            return cachedInferOverworldVersion;
        }
        synchronized (INFER_CACHE_LOCK) {
            if (fp == inferCacheFingerprint) {
                return cachedInferOverworldVersion;
            }
            final ResourceLocation overworld = Level.OVERWORLD.location();
            Integer fromDefaultsTemplate = parseWorldgenOverridesFromDefaultconfigsToml().get(overworld);
            if (fromDefaultsTemplate != null) {
                cachedInferOverworldVersion = fromDefaultsTemplate;
                inferCacheFingerprint = fp;
                return cachedInferOverworldVersion;
            }
            final Map<ResourceLocation, Integer> fromServer = TFGConfig.SERVER.parsedWorldgenOverrides();
            if (fromServer.containsKey(overworld)) {
                cachedInferOverworldVersion = fromServer.get(overworld);
                inferCacheFingerprint = fp;
                return cachedInferOverworldVersion;
            }
            cachedInferOverworldVersion = WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT;
            inferCacheFingerprint = fp;
            return cachedInferOverworldVersion;
        }
    }

    private static Path defaultconfigsPath() {
        return FMLPaths.GAMEDIR.get().resolve("defaultconfigs").resolve(DEFAULT_SERVERCONFIG_NAME);
    }

    /**
     * Changes when {@code defaultconfigs/tfg-server.toml} is touched or {@link ServerConfig#worldgenOverrides} list
     * contents change identity/hash.
     */
    private static long computeInferInputFingerprint() {
        long mtime;
        try {
            final Path path = defaultconfigsPath();
            mtime = Files.isRegularFile(path) ? Files.getLastModifiedTime(path).toMillis() : Long.MIN_VALUE;
        } catch (IOException ignored) {
            mtime = Long.MIN_VALUE;
        }
        final int listHc = Objects.hashCode(TFGConfig.SERVER.worldgenOverrides.get());
        return mtime ^ ((long) listHc << 32);
    }

    private static Map<ResourceLocation, Integer> parseWorldgenOverridesFromDefaultconfigsToml() {
        final Path path = defaultconfigsPath();
        if (!Files.isRegularFile(path)) {
            return Map.of();
        }
        try {
            return parseWorldgenOverridesTomlBracketList(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException ignored) {
            return Map.of();
        }
    }

    /**
     * Reads {@code worldgenOverrides = [ "...=N", ... ]} from raw server.toml text (minimal parser; skips comments).
     */
    static Map<ResourceLocation, Integer> parseWorldgenOverridesTomlBracketList(final String raw) {
        final int keyIdx = raw.indexOf("worldgenOverrides");
        if (keyIdx < 0) {
            return Map.of();
        }
        final int bracketOpen = raw.indexOf('[', keyIdx);
        if (bracketOpen < 0) {
            return Map.of();
        }
        int depth = 1;
        int pos = bracketOpen + 1;
        final StringBuilder slice = new StringBuilder();
        for (; depth > 0 && pos < raw.length(); pos++) {
            final char c = raw.charAt(pos);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    break;
                }
            }
            if (depth > 0) {
                slice.append(c);
            }
        }
        final Map<ResourceLocation, Integer> out = new HashMap<>();
        for (String token : slice.toString().split(",")) {
            token = token.trim();
            if (token.isEmpty() || token.startsWith("#")) {
                continue;
            }
            if (token.startsWith("\"")) {
                token = token.substring(1);
                if (token.endsWith("\"")) {
                    token = token.substring(0, token.length() - 1);
                }
            } else if (token.endsWith(",")) {
                token = token.substring(0, token.length() - 1).trim();
            }
            token = token.replace("\\\"", "\"");
            final int eq = token.lastIndexOf('=');
            if (eq <= 0 || eq >= token.length() - 1) {
                continue;
            }
            final String dim = token.substring(0, eq).trim();
            final String ver = token.substring(eq + 1).trim();
            final ResourceLocation id = ResourceLocation.tryParse(dim);
            if (id == null) {
                continue;
            }
            try {
                out.put(id, Integer.parseInt(ver));
            } catch (NumberFormatException ignored) {
                // malformed entry — skip
            }
        }
        return out.isEmpty() ? Map.of() : Map.copyOf(out);
    }

}
