package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.utils.GTUtil;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import su.terrafirmagreg.core.TFGCore;

@Mixin(value = GTUtil.class, remap = false)
public abstract class GTUtilMixin {

    /**
     * @author FiNiTe
     * @reason i think replacing it fully just makes sense
     * <br>
     * the EnvironmentHelpers thing I found from <a href="https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1.20.x/src/main/java/net/dries007/tfc/mixin/LevelMixin.java">https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1.20.x/src/main/java/net/dries007/tfc/mixin/LevelMixin.java</a>
     */
    @Overwrite
    public static boolean canSeeSunClearly(Level world, BlockPos blockPos) {
        //todo: i heard theres some ad astra weather stuff??
        if (!world.canSeeSky(blockPos.above())) {
            return false;
        } else {
            Biome biome = (Biome)world.getBiome(blockPos.above()).value();
            //for tfc overworld: EnvironmentHelpers.isRainingOrSnowing(world,blockPos) instead of world.isRaining()
            //just incase I left it how it was before for other dimensions
            if(world.dimension()==Level.OVERWORLD) {
                return world.isDay() && !EnvironmentHelpers.isRainingOrSnowing(world,blockPos);
            } else if (!world.isRaining() || !biome.warmEnoughToRain(blockPos.above()) && !biome.coldEnoughToSnow(blockPos.above())) {
                if (world.getBiome(blockPos.above()).is(BiomeTags.IS_END)) {
                    return false;
                } else {
                    return world.isDay();
                }
            } else {
                return false;
            }
        }
    }

    //i realised this doesnt give me coord
    /*@Redirect(method = "canSeeSunClearly", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isRaining()Z"), remap = false)
    private static boolean tfg$canSeeSunClearly$isRaining(Level instance) {
        TFGCore.LOGGER.info("bananaphone");
        return false;
    }*/
}
