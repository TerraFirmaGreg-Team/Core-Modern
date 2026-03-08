package su.terrafirmagreg.core.mixins.common.kubejs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraftforge.data.loading.DatagenModLoader;

import dev.latvian.mods.kubejs.platform.forge.MiscForgeHelper;

@Mixin(value = MiscForgeHelper.class, remap = false)
public class MiscForgeHelperMixin {

    /**
     * @author Mqrius
     * @reason Fix Forge bug where ModLoader.isDataGenRunning() always returns false;
     * DatagenModLoader.isRunningDataGen() works fine.
     * In non-datagen contexts kjs will make a threadpool that exits when minecraft exits.
     * For datagen minecraft never exits properly so the thread stays around and the task never exits.
     */
    @Overwrite
    public boolean isDataGen() {
        return DatagenModLoader.isRunningDataGen();
    }
}
