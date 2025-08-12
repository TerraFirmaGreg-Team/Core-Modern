package su.terrafirmagreg.core.common.data.entities;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.livestock.*;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.config.animals.ProducingAnimalConfig;
import net.dries007.tfc.config.animals.ProducingMammalConfig;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.events.AnimalProductEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.data.TFGTags;

import java.util.List;

public class TFCGlacianRam extends ProducingMammal implements IForgeShearable {

    static double familiarityCap = 0.35;
    static int adulthoodDays = 40;
    static int uses = 100;
    static boolean eatsRottenFood = false;
    static int produceTicks = 96000;
    static double produceFamiliarity = 0.15;
    static int childCount = 1;
    static long gestationDays = 30;

    public TFCGlacianRam(EntityType<? extends ProducingMammal> type, Level level, TFCSounds.EntitySound sounds, ProducingMammalConfig config) {
        super(type, level, sounds, config);
    }

    public static TFCGlacianRam makeTFCGlacianRam(EntityType<? extends ProducingMammal> type, Level level){

        return new TFCGlacianRam(type, level, TFCSounds.SHEEP ,TFCConfig.SERVER.sheepConfig);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double)32.0F).add(Attributes.MOVEMENT_SPEED, (double)0.2F);
    }


    //This region is for bypasing config
    @Override
    public float getAdultFamiliarityCap() {
        return (float) familiarityCap;
    }

    @Override
    public int getDaysToAdulthood() {
        return adulthoodDays;
    }

    @Override
    public int getUsesToElderly() {
        return uses;
    }

    @Override
    public boolean eatsRottenFood() {
        return eatsRottenFood;
    }

    @Override
    public boolean isReadyForAnimalProduct()
    {
        return getFamiliarity() > produceFamiliarity && hasProduct();
    }

    @Override
    public long getProductsCooldown()
    {
        return Math.max(0, produceTicks + getProducedTick() - Calendars.get(level()).getTicks());
    }

    @Override
    public int getChildCount() { return childCount; }

    @Override
    public long getGestationDays() {return gestationDays; }
    //End of config override


    @Override
    public TagKey<Item> getFoodTag() {
        return TFGTags.Items.GlacianRamFood;
    }

    @Override
    public boolean hasProduct()
    {
        return (getProducedTick() <= 0 || getProductsCooldown() <= 0) && getAgeType() == Age.ADULT;
    }

    //Stuff from IForgeShearable
    @Override
    public boolean isShearable(@NotNull ItemStack item, Level level, BlockPos pos) {
        return isReadyForAnimalProduct();
    }

    @Override
    public @NotNull List<ItemStack> onSheared(@Nullable Player player, @NotNull ItemStack item, Level level, BlockPos pos, int fortune) {

        setProductsCooldown();
        playSound(SoundEvents.SHEEP_SHEAR, 1.0f, 1.0f);

        // if the event was not cancelled
        AnimalProductEvent event = new AnimalProductEvent(level, pos, player, this, getWoolItem(), item, 1);
        if (!MinecraftForge.EVENT_BUS.post(event))
        {
            addUses(event.getUses());
        }
        return List.of(event.getProduct());
    }

    public ItemStack getWoolItem()
    {
        final int amount = getFamiliarity() > 0.99f ? 2 : 1;
        return new ItemStack(TFGItems.GLACIAN_WOOL.get(), amount);
    }

    public boolean hasWool(){
        long cooldown = getProductsCooldown();
        if (cooldown == 0){
            return true;
        } else{ return false; }
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other){

        if (other != this && this.getGender() == Gender.FEMALE && other instanceof TFCGlacianRam otherFertile && !isFertilized())
        {
            this.onFertilized(otherFertile);
        }
        else if (other == this)
        {
            final Entity baby = getEntityTypeForBaby().create(level);
            if (baby instanceof TFCAnimalProperties properties && baby instanceof AgeableMob ageable)
            {
                setBabyTraits(properties);
                return ageable;
            }
        }

        return null;
    }

    public void onFertilized(TFCGlacianRam male) {
        male.setFertilized(true);
        male.setLastFed(getLastFed()-1);

        setLastFed(getLastFed()-1);
        addUses(5);

        male.setPregnantTime(getCalendar().getTotalDays());

        CompoundTag genes = new CompoundTag();
        male.createGenes(genes, this);
        male.setGenes(genes.isEmpty() ? null : genes);
    }


    public void createGenes(CompoundTag tag, TFCGlacianRam mate)
    {
        tag.putInt("size", mate.getGeneticSize() + getGeneticSize());
        tag.putBoolean("runt", getEntity().getRandom().nextInt(20) == 0);
    }

    @Override
    public MutableComponent getProductReadyName()
    {
        return Component.translatable("tfc.jade.product.wool");
    }

}
