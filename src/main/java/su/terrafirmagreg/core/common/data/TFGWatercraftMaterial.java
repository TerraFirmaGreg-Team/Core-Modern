package su.terrafirmagreg.core.common.data;

import java.util.Optional;
import java.util.function.Supplier;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import com.alekiponi.alekiships.util.BoatMaterial;
import com.alekiponi.firmaciv.common.entity.vehicle.CanoeEntity;
import com.alekiponi.firmaciv.util.CanoeMaterial;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.items.TFGItems;

public enum TFGWatercraftMaterial implements BoatMaterial, CanoeMaterial {
    //Softwoods
    ARAUCARIA(TFGWood.ARAUCARIA, true),
    AERONOS(TFGWood.AERONOS, true),
    STROPHAR(TFGWood.STROPHAR, true),
    GINKGO(TFGWood.GINKGO, true),
    //Hardwoods
    BEECH(TFGWood.BEECH, false),
    MAHOE(TFGWood.MAHOE, false),
    GLACIAN(TFGWood.GLACIAN, false),
    ;

    TFGWatercraftMaterial(TFGWood tfgWood, boolean isSoftWood) {
        this.tfgWood = tfgWood;
        this.isSoftWood = isSoftWood;
    }

    private final TFGWood tfgWood;
    public final boolean isSoftWood;

    public Supplier<Block> getWoodBlockType(Wood.BlockType blockType) {
        //If we're generating the wood blocks, get it directly
        return tfgWood.getBlock(blockType);
    }

    public RegistryWood getWood() {
        return tfgWood;
    }

    @Override
    public Item getRailing() {
        //TODO handle railing stuff
        return TFGItems.FISH_ROE.get();
    }

    @Override
    public Item getStrippedLog() {
        return tfgWood.getBlock(Wood.BlockType.STRIPPED_LOG).get().asItem();
    }

    @Override
    public boolean withstandsLava() {
        return false;
    }

    @Override
    public BlockState getDeckBlock() {
        return tfgWood.getBlock(Wood.BlockType.PLANKS).get().defaultBlockState();
    }

    @Override
    public Optional<EntityType<? extends AbstractVehicle>> getEntityType(BoatType boatType) {
        return Optional.empty();
    }

    @Override
    public Optional<EntityType<? extends CanoeEntity>> getCanoeType() {
        return Optional.empty();
    }

    @Override
    public String getSerializedName() {
        return tfgWood.getSerializedName();
    }
}
