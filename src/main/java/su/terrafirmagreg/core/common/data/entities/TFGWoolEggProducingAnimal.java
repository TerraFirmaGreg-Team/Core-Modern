package su.terrafirmagreg.core.common.data.entities;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.entities.EntityHelpers;
import net.dries007.tfc.common.entities.livestock.ProducingAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.config.animals.ProducingAnimalConfig;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class TFGWoolEggProducingAnimal extends ProducingAnimal {

    private static final EntityDataAccessor<Long> DATA_WOOL = SynchedEntityData.defineId(TFGWoolEggProducingAnimal.class, EntityHelpers.LONG_SERIALIZER);



    public TFGWoolEggProducingAnimal(EntityType<? extends TFCAnimal> type, Level level, TFCSounds.EntitySound sounds, ProducingAnimalConfig config) {
        super(type, level, sounds, config);
    }


    public ItemStack getWoolItem(Item woolItem, int maxWool)
    {
        final int amount = (int) Math.ceil(getFamiliarity() * maxWool);
        return new ItemStack(woolItem, amount);
    }

    public boolean hasWool(int woolProduceTicks){
        long cooldown = getWoolCooldown(woolProduceTicks);
        if (cooldown == 0){
            return true;
        } else{ return false; }
    }

    //Adds separate produce info for wool
    @Override
    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.putLong("producedWool", getWoolProducedTick());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        setWoolProducedTick(nbt.getLong("producedWool"));
    }

    @Override
    public void defineSynchedData()
    {
        super.defineSynchedData();
        entityData.define(DATA_WOOL, 0L);
    }

    public long getWoolProducedTick()
    {
        return entityData.get(DATA_WOOL);
    }

    public void setWoolProducedTick(long producedTick)
    {
        entityData.set(DATA_WOOL, producedTick);
    }

    public void setWoolCooldown()
    {
        setWoolProducedTick(Calendars.get(level()).getTicks());
    }

    public long getWoolCooldown(int woolProduceTicks)
    {
        return Math.max(0, woolProduceTicks + getWoolProducedTick() - Calendars.get(level()).getTicks());
    }

    public boolean hasWoolProduct(int woolProduceTicks)
    {
        return(getWoolProducedTick() <= 0 || getWoolCooldown(woolProduceTicks) <= 0) && getAgeType() == Age.ADULT;
    }

    public MutableComponent getWoolReadyName()
    {
        return Component.translatable("tfc.jade.product.wool");
    }
}
