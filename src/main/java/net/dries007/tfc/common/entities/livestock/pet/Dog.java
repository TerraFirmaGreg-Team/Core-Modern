/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.entities.livestock.pet;

import net.dries007.tfc.common.entities.livestock.MammalProperties;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfVariant;

public class Dog extends TamableMammal
{
	private static final EntityDataAccessor<Integer> DATA_VARIANT;

    private float interestedAngle;
    private float interestedAngleO;

	static {
		DATA_VARIANT = SynchedEntityData.defineId(Dog.class, EntityDataSerializers.INT);
	}

    public Dog(EntityType<? extends TFCAnimal> animal, Level level)
    {
        super(animal, level, TFCSounds.DOG, TFCConfig.SERVER.dogConfig);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (isAlive())
        {
            this.interestedAngleO = this.interestedAngle;
            interestedAngle += 0.4f * (isInterested() ? (1f - interestedAngle) : (0f - interestedAngle));
        }
    }

    public float getHeadRollAngle(float partialTick)
    {
        return Mth.lerp(partialTick, this.interestedAngleO, this.interestedAngle) * 0.15F * Mth.PI;
    }

    @Override
    public TagKey<Item> getFoodTag()
    {
        return TFCTags.Items.DOG_FOOD;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions size)
    {
        return size.height * 0.8F;
    }

    @Override
    public boolean canAttack(LivingEntity entity)
    {
        return super.canAttack(entity) && (Helpers.isEntity(entity, TFCTags.Entities.HUNTED_BY_DOGS) || entity instanceof Monster);
    }

	public TFCWolfVariant getVariant() {
		if (!this.entityData.hasItem(DATA_VARIANT)) {
			return TFCWolfVariant.DEFAULT;
		}
		return TFCWolfVariant.byId(this.entityData.get(DATA_VARIANT));
	}

	public void setVariant(TFCWolfVariant id) {
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
		tag.putInt("TFCWolfVariant", this.getVariant().id);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setVariant(TFCWolfVariant.byId(tag.getInt("TFCWolfVariant")));
	}

	public void createGenes(CompoundTag tag, TFCAnimalProperties male) {
		super.createGenes(tag, male);
		if (male instanceof Dog maleDog) {
			TFCWolfVariant variant = this.random.nextBoolean() ? maleDog.getVariant() : this.getVariant();
			tag.putInt("TFCWolfVariant", variant.id);
		}
	}

	public void applyGenes(CompoundTag tag, MammalProperties baby) {
		super.applyGenes(tag, baby);
		if (baby instanceof Dog dog) {
			int id = tag.getInt("TFCWolfVariant");

			TFCWolfVariant variant = TFCWolfVariant.byId(id);

			dog.setVariant(variant);
		}
	}
}
