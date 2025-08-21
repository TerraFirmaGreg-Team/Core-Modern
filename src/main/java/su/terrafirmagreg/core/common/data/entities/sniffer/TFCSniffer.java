package su.terrafirmagreg.core.common.data.entities.sniffer;

import com.mojang.serialization.Dynamic;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.entities.EntityHelpers;
import net.dries007.tfc.common.entities.livestock.ProducingAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.config.animals.ProducingAnimalConfig;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.events.AnimalProductEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.terrafirmagreg.core.common.data.TFGEntityDataSerializers;
import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.capabilities.ILargeEgg;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.entities.glacianram.TFCGlacianRam;

import java.util.List;

public class TFCSniffer extends ProducingAnimal implements IForgeShearable {

    private static final EntityDataAccessor<TFCSniffer.State> DATA_STATE = SynchedEntityData.defineId(TFCSniffer.class, TFGEntityDataSerializers.SNIFFER_STATE.get());
    private static final EntityDataAccessor<Long> DATA_WOOL = SynchedEntityData.defineId(TFCSniffer.class, EntityHelpers.LONG_SERIALIZER);

    static double familiarityCap = 0.35;
    static int adulthoodDays = 80;
    static int uses = 200;
    static boolean eatsRottenFood = false;
    //Produce = eggs
    static int produceTicks = 96000;
    static int hatchDays = 20;
    static Item eggItem = TFGItems.SNIFFER_EGG.get();
    static int woolProduceTicks = 48000;
    static int maxWool = 8;
    static Item woolItem = TFGItems.SNIFFER_WOOL.get();
    static double produceFamiliarity = 0.15;

    public final AnimationState scentingAnimationState = new AnimationState();
    public final AnimationState sniffingAnimationState = new AnimationState();

    public TFCSniffer(EntityType<? extends TFCAnimal> type, Level level, TFCSounds.EntitySound sounds, ProducingAnimalConfig config) {
        super(type, level, sounds, config);
        this.entityData.define(DATA_STATE, TFCSniffer.State.IDLING);
    }

    public static TFCSniffer makeTFCSniffer(EntityType<? extends ProducingAnimal> type, Level level){

        return new TFCSniffer(type, level, TFCSounds.MUSK_OX , TFCConfig.SERVER.duckConfig.inner());
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double)75.0F).add(Attributes.MOVEMENT_SPEED, (double)0.1F);
    }

    public static boolean spawnRules(EntityType<? extends TFCSniffer> type, LevelAccessor level, MobSpawnType spawn, BlockPos pos, RandomSource rand)
    {
        return level.getBlockState(pos).isAir();
    }

    //Config Bypass
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
    //End Region

    @Override
    public TagKey<Item> getFoodTag() {
        return TFGTags.Items.SnifferFood;
    }

    @Override
    public boolean hasProduct()
    {
        return (getProducedTick() <= 0 || getProductsCooldown() <= 0)
                && getAgeType() == Age.ADULT
                && (getGender() == Gender.FEMALE || (getGender() == Gender.MALE && isFertilized()));
    }

    //Egg Stuff
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other){

        if (other != this && this.getGender() == Gender.FEMALE && other instanceof TFCSniffer otherFertile && !isFertilized()) {
            this.onFertilized(otherFertile);
            otherFertile.setProducedTick(0);
            this.setProductsCooldown();
        }

        return null;
    }

    public void onFertilized(TFCSniffer male) {
        male.setFertilized(true);
        male.setLastFed(getLastFed()-1);

        setLastFed(getLastFed()-1);
        male.addUses(5);
        addUses(5);
    }

    public ItemStack makeEgg()
    {
        final ItemStack stack = new ItemStack(eggItem);
        if (isFertilized())
        {
            final @Nullable ILargeEgg egg = LargeEggCapability.get(stack);
            if (egg != null)
            {
                final TFCSniffer baby = ((EntityType<TFCSniffer>) getType()).create(level());
                if (baby != null)
                {
                    baby.setGender(Gender.valueOf(random.nextBoolean()));
                    baby.setBirthDay(Calendars.SERVER.getTotalDays());
                    baby.setFamiliarity(getFamiliarity() < 0.9F ? getFamiliarity() / 2.0F : getFamiliarity() * 0.9F);
                    egg.setFertilized(baby, Calendars.SERVER.getTotalDays() + hatchDays);
                }
            }
        }
        AnimalProductEvent event = new AnimalProductEvent(level(), blockPosition(), null, this, stack, ItemStack.EMPTY, 1);
        if (!MinecraftForge.EVENT_BUS.post(event))
        {
            addUses(event.getUses());
        }
        return event.getProduct();
    }

    @Override
    public MutableComponent getProductReadyName() { return Component.translatable("tfc.jade.product.eggs"); }

    //Stuff from IForgeShearable
    @Override
    public boolean isShearable(@NotNull ItemStack item, Level level, BlockPos pos) {
        return isReadyForWoolProduct();
    }

    @Override
    public @NotNull List<ItemStack> onSheared(@Nullable Player player, @NotNull ItemStack item, Level level, BlockPos pos, int fortune) {

        setWoolCooldown();
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
        final int amount = (int) Math.ceil(getFamiliarity() * maxWool);
        return new ItemStack(woolItem, amount);
    }

    public boolean hasWool(){
        long cooldown = getWoolCooldown();
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

    public long getWoolCooldown()
    {
        return Math.max(0, woolProduceTicks + getWoolProducedTick() - Calendars.get(level()).getTicks());
    }

    public boolean hasWoolProduct()
    {
        return(getWoolProducedTick() <= 0 || getWoolCooldown() <= 0) && getAgeType() == Age.ADULT;
    }

    public MutableComponent getWoolReadyName()
    {
        return Component.translatable("tfc.jade.product.wool");
    }

    public boolean isReadyForWoolProduct()
    {
        return getFamiliarity() > produceFamiliarity && hasWoolProduct();
    }


    //Sound Handlers
    protected void playStepSound(BlockPos pPos, BlockState pState) {
        this.playSound(SoundEvents.SNIFFER_STEP, 0.15F, 1.0F);
    }

    public SoundEvent getEatingSound(ItemStack pStack) {
        return SoundEvents.SNIFFER_EAT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.SNIFFER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SNIFFER_DEATH;
    }

    protected SoundEvent getAmbientSound() {return SoundEvents.SNIFFER_IDLE; }

    private TFCSniffer onScentingStart() {
        this.playSound(SoundEvents.SNIFFER_SCENTING, 2.0F, this.isBaby() ? 1.3F : 1.0F);
        return this;
    }

    //AI Handlers
    @Override
    protected Brain.Provider<? extends TFCSniffer> brainProvider()
    {
        return Brain.provider(TFCSnifferAi.MEMORY_TYPES, TFCSnifferAi.SENSOR_TYPES);
    }
    @Override
    public Brain<?> makeBrain(Dynamic<?> dynamic)
    {
        return TFCSnifferAi.makeSniffBrain(brainProvider().makeBrain(dynamic));
    }

    public boolean isTempted() {
        return this.brain.getMemory(MemoryModuleType.IS_TEMPTED).orElse(false);
    }

    //Animation Handlers
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data)
    {
        if (DATA_STATE.equals(data)) {
            TFCSniffer.State sniffer$state = this.getState();
            this.resetAnimations();
            switch (sniffer$state) {
                case SCENTING:
                    this.scentingAnimationState.startIfStopped(this.tickCount);
                    break;
                case SNIFFING:
                    this.sniffingAnimationState.startIfStopped(this.tickCount);
            }
            this.refreshDimensions();

        }

        super.onSyncedDataUpdated(data);
    }

    private TFCSniffer.State getState() {
        return this.entityData.get(DATA_STATE);
    }

    private TFCSniffer setState(TFCSniffer.State pState) {
        this.entityData.set(DATA_STATE, pState);
        return this;
    }

    public TFCSniffer transitionTo(TFCSniffer.State pState) {
        switch (pState) {
            case SCENTING:
                this.setState(TFCSniffer.State.SCENTING).onScentingStart();
                break;
            case SNIFFING:
                this.playSound(SoundEvents.SNIFFER_SNIFFING, 1.0F, 1.0F);
                this.setState(TFCSniffer.State.SNIFFING);
                break;
            case IDLING:
                this.setState(TFCSniffer.State.IDLING);
                break;
        }
        return this;
    }

    public int getMaxHeadYRot() {
        return 50;
    }

    private void resetAnimations() {
        this.sniffingAnimationState.stop();
        this.scentingAnimationState.stop();
    }

    public enum State {
        IDLING,
        SCENTING,
        SNIFFING;

    }
}
