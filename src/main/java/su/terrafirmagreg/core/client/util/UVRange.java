package su.terrafirmagreg.core.client.util;

// Taken from the mod Nether Skyboxes
// https://www.curseforge.com/minecraft/mc-mods/nether-skyboxes

public record UVRange(float minU, float minV, float maxU, float maxV) {

    public static UVRange of() {
        return new UVRange(0, 0, 1, 1);
    }
}
