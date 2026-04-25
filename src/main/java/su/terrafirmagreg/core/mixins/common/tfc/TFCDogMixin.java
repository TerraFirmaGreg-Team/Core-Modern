package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.livestock.MammalProperties;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.livestock.pet.Dog;
import net.dries007.tfc.common.entities.livestock.pet.TamableMammal;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import su.terrafirmagreg.core.common.entity.animals.tfcdog.TFCDog;
import su.terrafirmagreg.core.common.entity.animals.tfcdog.TFCDogVariant;

@Mixin(value = Dog.class)
public class TFCDogMixin extends TamableMammal implements TFCDog {
    @Unique
    private static final EntityDataAccessor<Integer> DATA_VARIANT;

    static {
        DATA_VARIANT = SynchedEntityData.defineId(TFCDogMixin.class, EntityDataSerializers.INT);
    }

    @Unique
    public TFCDogVariant tfg$getVariant() {
        return TFCDogVariant.byId((Integer) this.entityData.get(DATA_VARIANT));
    }

    @Unique
    public void tfg$setVariant(TFCDogVariant id) {
        this.entityData.set(DATA_VARIANT, id.id);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, TFCDogVariant.DEFAULT.id);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TFCDogVariant", this.tfg$getVariant().id);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.tfg$setVariant(TFCDogVariant.byId(tag.getInt("TFCDogVariant")));
    }

    public void createGenes(CompoundTag tag, TFCAnimalProperties male) {
        super.createGenes(tag, male);
        if (male instanceof TFCDogMixin maleDog) {
            TFCDogVariant variant = this.random.nextBoolean() ? maleDog.tfg$getVariant() : this.tfg$getVariant();
            tag.putInt("TFCDogVariant", variant.id);
        }
    }

    public void applyGenes(CompoundTag tag, MammalProperties baby) {
        super.applyGenes(tag, baby);
        if (baby instanceof TFCDogMixin dog) {
            int id = tag.getInt("TFCDogVariant");

            TFCDogVariant variant = TFCDogVariant.byId(id);

            dog.tfg$setVariant(variant);
        }

    }

    public TFCDogMixin(EntityType<? extends TFCAnimal> animal, Level level) {
        super(animal, level, TFCSounds.DOG, TFCConfig.SERVER.dogConfig);
    }

    public TagKey<Item> getFoodTag() {
        return TFCTags.Items.DOG_FOOD;
    }

    @Override
    public void initCommonAnimalData(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason) {
        super.initCommonAnimalData(level, difficulty, reason);

        BlockPos pos = this.blockPosition();
        float temperature = Climate.getAverageTemperature(level.getLevel(), pos);
        float rainfall = Climate.getRainfall(level.getLevel(), pos);
        KoppenClimateClassification climate = KoppenClimateClassification.classify(temperature, rainfall);

        TFCDogVariant variant = switch (climate) {
            case HUMID_SUBTROPICAL, TROPICAL_RAINFOREST -> TFCDogVariant.RUSTY;
            case HUMID_SUBARCTIC, TUNDRA -> this.random.nextBoolean() ? TFCDogVariant.BLACK : TFCDogVariant.CHESTNUT;
            case HUMID_OCEANIC, COLD_DESERT -> TFCDogVariant.DEFAULT;
            case SUBTROPICAL, HOT_DESERT, TROPICAL_SAVANNA -> this.random.nextBoolean() ? TFCDogVariant.SPOTTED : TFCDogVariant.STRIPED;
            case TEMPERATE -> this.random.nextBoolean() ? TFCDogVariant.WOODS : TFCDogVariant.DEFAULT;
            case SUBARCTIC, ARCTIC -> this.random.nextBoolean() ? TFCDogVariant.SNOWY : TFCDogVariant.ASHEN;
        };

        this.tfg$setVariant(variant);
    }
}
