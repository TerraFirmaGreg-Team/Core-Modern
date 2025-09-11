package su.terrafirmagreg.core.common.data.entities.wraptor;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Dynamic;
import com.ninni.species.registry.SpeciesSoundEvents;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.entities.livestock.ProducingAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.config.animals.ProducingAnimalConfig;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.events.AnimalProductEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.MinecraftForge;

import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.capabilities.ILargeEgg;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.entities.TFGWoolEggProducingAnimal;

public class TFCWraptor extends TFGWoolEggProducingAnimal implements IForgeShearable {

    static double familiarityCap = 0.35;
    static int adulthoodDays = 45;
    static int uses = 100;
    static boolean eatsRottenFood = false;
    // Produce = eggs
    static int produceTicks = 48000;
    static int hatchDays = 10;
    static Item eggItem = TFGItems.WRAPTOR_EGG.get();
    static int woolProduceTicks = 48000;
    static int maxWool = 12;
    static Item woolItem = TFGItems.WRAPTOR_WOOL.get();
    static double produceFamiliarity = 0.15;

    public final AnimationState roarAnimationState = new AnimationState();
    public final AnimationState fallingAnimationState = new AnimationState();

    public TFCWraptor(EntityType<? extends TFCAnimal> type, Level level, TFCSounds.EntitySound sounds,
            ProducingAnimalConfig config) {
        super(type, level, sounds, config);
    }

    public static TFCWraptor makeTFCWraptor(EntityType<? extends ProducingAnimal> type, Level level) {
        return new TFCWraptor(type, level, TFCSounds.CHICKEN, TFCConfig.SERVER.duckConfig.inner());
    }

    @NotNull
    public static AttributeSupplier.Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double) 35.0F)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.2F).add(Attributes.KNOCKBACK_RESISTANCE, (double) 0.25F);
    }

    public static boolean spawnRules(EntityType<? extends TFCWraptor> type, LevelAccessor level, MobSpawnType spawn,
            BlockPos pos, RandomSource rand) {
        return level.getBlockState(pos).isAir();
    }

    // Config Overrides
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
    public boolean isReadyForAnimalProduct() {
        return getFamiliarity() > produceFamiliarity && hasProduct();
    }

    @Override
    public long getProductsCooldown() {
        return Math.max(0, produceTicks + getProducedTick() - Calendars.get(level()).getTicks());
    }

    @Override
    public float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height * 0.95F;
    }

    @Override
    public TagKey<Item> getFoodTag() {
        return TFGTags.Items.WraptorFood;
    }

    // Sound Handlers
    protected SoundEvent getAmbientSound() {
        return SpeciesSoundEvents.WRAPTOR_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SpeciesSoundEvents.WRAPTOR_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return SpeciesSoundEvents.WRAPTOR_DEATH.get();
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SpeciesSoundEvents.WRAPTOR_STEP.get(), 0.15F, 1.0F);
    }

    // Egg Stuff
    @Override
    public boolean hasProduct() {
        return (getProducedTick() <= 0 || getProductsCooldown() <= 0)
                && getAgeType() == Age.ADULT
                && (getGender() == Gender.FEMALE || (getGender() == Gender.MALE && isFertilized()));
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {

        if (other != this && this.getGender() == Gender.FEMALE && other instanceof TFCWraptor otherFertile
                && !isFertilized()) {
            this.onFertilized(otherFertile);
            otherFertile.setProducedTick(0);
            this.setProductsCooldown();
        }

        return null;
    }

    public void onFertilized(TFCWraptor male) {
        male.setFertilized(true);
        male.setLastFed(getLastFed() - 1);

        setLastFed(getLastFed() - 1);
        male.addUses(5);
        addUses(5);
    }

    @Override
    public ItemStack makeEgg() {
        final ItemStack stack = new ItemStack(eggItem);
        if (isFertilized()) {
            final @Nullable ILargeEgg egg = LargeEggCapability.get(stack);
            if (egg != null) {
                final TFCWraptor baby = ((EntityType<TFCWraptor>) getType()).create(level());
                if (baby != null) {
                    baby.setGender(Gender.valueOf(random.nextBoolean()));
                    baby.setBirthDay(Calendars.SERVER.getTotalDays());
                    baby.setFamiliarity(getFamiliarity() < 0.9F ? getFamiliarity() / 2.0F : getFamiliarity() * 0.9F);
                    egg.setFertilized(baby, Calendars.SERVER.getTotalDays() + hatchDays);
                } else {
                    System.out.println("Cannot Create Child");
                }
            }
        }
        AnimalProductEvent event = new AnimalProductEvent(level(), blockPosition(), null, this, stack, ItemStack.EMPTY,
                1);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            addUses(event.getUses());
        }
        return event.getProduct();
    }

    @Override
    public MutableComponent getProductReadyName() {
        return Component.translatable("tfc.jade.product.eggs");
    }

    // Stuff from IForgeShearable
    @Override
    public boolean isShearable(@NotNull ItemStack item, Level level, BlockPos pos) {
        return isReadyForWoolProduct();
    }

    @Override
    public @NotNull List<ItemStack> onSheared(@Nullable Player player, @NotNull ItemStack item, Level level,
            BlockPos pos, int fortune) {

        setWoolCooldown();
        playSound(SoundEvents.SHEEP_SHEAR, 1.0f, 1.0f);

        // if the event was not cancelled
        AnimalProductEvent event = new AnimalProductEvent(level, pos, player, this, getWoolItem(woolItem, maxWool),
                item, 1);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            addUses(event.getUses());
        }
        return List.of(event.getProduct());
    }

    public int getFeatherStage() {
        int usesLeft = getUsesToElderly();
        return hasWoolProduct(woolProduceTicks) ? 6 * (usesLeft / uses) : 0;
    }

    public boolean isReadyForWoolProduct() {
        return getFamiliarity() > produceFamiliarity && hasWoolProduct(woolProduceTicks);
    }

    // AI Handlers
    @Override
    protected Brain.@NotNull Provider<? extends TFCWraptor> brainProvider() {
        return Brain.provider(TFCWraptorAi.MEMORY_TYPES, TFCWraptorAi.SENSOR_TYPES);
    }

    @Override
    public @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamic) {
        return TFCWraptorAi.makeWraptorBrain(brainProvider().makeBrain(dynamic));
    }

}
