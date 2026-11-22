package su.terrafirmagreg.core.common.data;

import java.util.Optional;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import com.alekiponi.alekiships.util.BoatMaterial;
import com.alekiponi.firmaciv.common.entity.vehicle.CanoeEntity;
import com.alekiponi.firmaciv.util.CanoeMaterial;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import su.terrafirmagreg.core.TFGCore;

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

    public RegistryWood getWood() {
        return tfgWood;
    }

    @Override
    public Item getRailing() {
        //TODO handle railing stuff
        ResourceLocation lumberLocation = TFGCore.id("wood/lumber/" + getSerializedName());
        return ForgeRegistries.ITEMS.getValue(lumberLocation);
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
        return switch (boatType) {
            case ROWBOAT -> Optional.of(TFGEntities.ROWBOATS.get(this).get());
            case SLOOP -> Optional.of(TFGEntities.SLOOPS.get(this).get());
            case CONSTRUCTION_SLOOP -> Optional.of(TFGEntities.SLOOPS_UNDER_CONSTRUCTION.get(this).get());
        };
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
