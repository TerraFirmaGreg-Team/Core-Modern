package su.terrafirmagreg.core.common.block;

import com.cake.struts.content.StrutModelType;
import com.cake.struts.content.block.StrutBlock;
import com.cake.struts.content.block.StrutBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class TFGStrutBlock extends StrutBlock {
    public TFGStrutBlock(Properties properties, StrutModelType modelType) {
        super(properties, modelType);
    }

    @Override
    protected BlockEntityType<? extends StrutBlockEntity> getStrutBlockEntityType() {
        return TFGBlockEntities.STRUT.get();
    }
}
