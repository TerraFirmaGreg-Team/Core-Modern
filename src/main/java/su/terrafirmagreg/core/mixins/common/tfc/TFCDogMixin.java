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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfInterface;
import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfVariant;

@Mixin(value = Dog.class, remap = false)
public class TFCDogMixin extends TamableMammal implements TFCWolfInterface {
    @Unique
    private static final EntityDataAccessor<Integer> DATA_VARIANT;

    static {
        DATA_VARIANT = SynchedEntityData.defineId(TFCDogMixin.class, EntityDataSerializers.INT);
    }

    @Unique
    public TFCWolfVariant tfg$getVariant() {
        if (!this.entityData.hasItem(DATA_VARIANT)) {
            return TFCWolfVariant.DEFAULT;
        }
        return TFCWolfVariant.byId((Integer) this.entityData.get(DATA_VARIANT));
    }

    @Unique
    public void tfg$setVariant(TFCWolfVariant id) {
        this.entityData.set(DATA_VARIANT, id.id);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, TFCWolfVariant.DEFAULT.id);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TFCWolfVariant", this.tfg$getVariant().id);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.tfg$setVariant(TFCWolfVariant.byId(tag.getInt("TFCWolfVariant")));
    }

    public void createGenes(CompoundTag tag, TFCAnimalProperties male) {
        super.createGenes(tag, male);
        if (male instanceof TFCDogMixin maleDog) {
            TFCWolfVariant variant = this.random.nextBoolean() ? maleDog.tfg$getVariant() : this.tfg$getVariant();
            tag.putInt("TFCWolfVariant", variant.id);
        }
    }

    public void applyGenes(CompoundTag tag, MammalProperties baby) {
        super.applyGenes(tag, baby);
        if (baby instanceof TFCDogMixin dog) {
            int id = tag.getInt("TFCWolfVariant");

            TFCWolfVariant variant = TFCWolfVariant.byId(id);

            dog.tfg$setVariant(variant);
        }

    }

    public TFCDogMixin(EntityType<? extends TFCAnimal> animal, Level level) {
        super(animal, level, TFCSounds.DOG, TFCConfig.SERVER.dogConfig);
    }

    public TagKey<Item> getFoodTag() {
        return TFCTags.Items.DOG_FOOD;
    }
}
