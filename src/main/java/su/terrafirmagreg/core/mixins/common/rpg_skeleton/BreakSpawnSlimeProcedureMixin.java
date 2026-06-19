package su.terrafirmagreg.core.mixins.common.rpg_skeleton;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

@Pseudo
@Mixin(targets = "net.mcreator.realmrpgskeletons.procedures.BreakSpawnSlimeProcedure", remap = false)
public class BreakSpawnSlimeProcedureMixin {

    /**
     * @author gamestar
     * @reason Disable this thing
     */
    @Overwrite
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
    }
}
