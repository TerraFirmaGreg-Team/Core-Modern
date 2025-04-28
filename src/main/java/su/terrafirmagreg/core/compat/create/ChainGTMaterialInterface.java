package su.terrafirmagreg.core.compat.create;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

public interface ChainGTMaterialInterface {
    public void addConnectionMaterial(BlockPos connection, Material chainMat);
    public Material getConnectionMaterial(BlockPos connection);
    public Item getConnectionChainItem(BlockPos connection);
}
