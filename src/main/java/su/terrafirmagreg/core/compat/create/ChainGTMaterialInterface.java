package su.terrafirmagreg.core.compat.create;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

public interface ChainGTMaterialInterface {
    void addConnectionMaterial(BlockPos connection, Material chainMat);

    Material getConnectionMaterial(BlockPos connection);

    Item getConnectionChainItem(BlockPos connection);
}
