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
    ARAUCARIA(TFGWood.ARAUCARIA, TFGWatercraftMaterial.WoodKind.SOFT),
    GINKGO(TFGWood.GINKGO, WoodKind.SOFT),
    //Hardwoods
    BEECH(TFGWood.BEECH, WoodKind.HARD),
    MAHOE(TFGWood.MAHOE, WoodKind.HARD),
    GLACIAN(TFGWood.GLACIAN, WoodKind.HARD),
    //None, Aeronos and Strophar don't have RotatedPillarBlocks and as a result they can't be used to make boats, sad!
    AERONOS(TFGWood.AERONOS, WoodKind.NONE),
    STROPHAR(TFGWood.STROPHAR, WoodKind.NONE),
    ;

    public enum WoodKind {
        //Makes canoes
        SOFT(),
        //Makes Rowboats and Sloops
        HARD(),
        //Cant be used for boats
        NONE();

        WoodKind() {

        }
    }

    TFGWatercraftMaterial(TFGWood tfgWood, WoodKind woodKind) {
        this.tfgWood = tfgWood;
        this.woodKind = woodKind;
    }

    private final TFGWood tfgWood;
    public final WoodKind woodKind;

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
        return Optional.of(TFGEntities.CANOES.get(this).get());
    }

    @Override
    public String getSerializedName() {
        return tfgWood.getSerializedName();
    }
}
