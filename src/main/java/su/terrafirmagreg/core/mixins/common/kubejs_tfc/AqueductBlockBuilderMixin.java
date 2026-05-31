package su.terrafirmagreg.core.mixins.common.kubejs_tfc;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.notenoughmail.kubejs_tfc.block.AqueductBlockBuilder;

import net.dries007.tfc.common.blocks.rock.AqueductBlock;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import dev.latvian.mods.kubejs.block.custom.MultipartShapedBlockBuilder;

import su.terrafirmagreg.core.common.data.TFGBlockProperties;

@Mixin(value = AqueductBlockBuilder.class, remap = false)
public abstract class AqueductBlockBuilderMixin extends MultipartShapedBlockBuilder {

    // Make kubejs-generated aqueducts have the same fluid property as java-generated aqueducts

    public AqueductBlockBuilderMixin(ResourceLocation i, String... suffixes) {
        super(i, suffixes);
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lcom/notenoughmail/kubejs_tfc/block/AqueductBlockBuilder;fluidProperty:Lnet/dries007/tfc/common/fluids/FluidProperty;", opcode = Opcodes.PUTFIELD))
    private void tfg$init(AqueductBlockBuilder instance, FluidProperty value) {
        instance.fluidProperty = TFGBlockProperties.SPACE_WATER_AND_LAVA;
    }

    /**
     * @author Pyritie
     * @reason KJS-TFC's method includes overrides to getFluidProperty() which messes with our own
     * custom fluid-logging
     */
    @Overwrite
    public Block createObject() {
        return new AqueductBlock(createProperties());
    }
}
