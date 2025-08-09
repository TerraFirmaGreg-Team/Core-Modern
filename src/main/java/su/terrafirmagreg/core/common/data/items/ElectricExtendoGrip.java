package su.terrafirmagreg.core.common.data.items;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.item.ProspectorScannerBehavior;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripInteractionPacket;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItemRenderer;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGItems;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class ElectricExtendoGrip extends ComponentItem{
    //public static final int MAX_DAMAGE = 200;

    public static final int COST = 8;
    //public static final int POWER_THRESHOLD = 100;
    public ElectricStats electricStats;

    public static final AttributeModifier singleRangeAttributeModifier =
            new AttributeModifier(UUID.fromString("604b1f96-e250-4674-9289-cca738a59460"), "Electric Extendo Grip Range modifier", 4,
                    AttributeModifier.Operation.ADDITION);
    public static final AttributeModifier doubleRangeAttributeModifier =
            new AttributeModifier(UUID.fromString("704b1f96-e250-4674-9289-cca738a59460"), "Duel Electric Extendo Grip Range modifier", 6,
                    AttributeModifier.Operation.ADDITION);

    private static final Supplier<Multimap<Attribute, AttributeModifier>> rangeModifier = Suppliers.memoize(() ->
            // Holding an ElectricExtendoGrip
            ImmutableMultimap.of(ForgeMod.BLOCK_REACH.get(), singleRangeAttributeModifier));
    private static final Supplier<Multimap<Attribute, AttributeModifier>> doubleRangeModifier = Suppliers.memoize(() ->
            // Holding two ElectricExtendoGrips o.O
            ImmutableMultimap.of(ForgeMod.BLOCK_REACH.get(), doubleRangeAttributeModifier));

    private static DamageSource lastActiveDamageSource;

    public ElectricExtendoGrip(Properties properties) {
        super(properties.defaultDurability(200));

        electricStats = ElectricStats.createElectricItem(1_000_000L, GTValues.MV);

        this.attachComponents(electricStats);
    }

    public static final String EXTENDO_MARKER = "electricExtendo";
    public static final String DUAL_EXTENDO_MARKER = "electricDualExtendo";

    @SubscribeEvent
    public static void holdingExtendoGripIncreasesRange(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        CompoundTag persistentData = player.getPersistentData();
        boolean inOff = player.getOffhandItem().is(TFGItems.ELECTRIC_EXTENDO_GRIP.get());
        boolean inMain = player.getMainHandItem().is(TFGItems.ELECTRIC_EXTENDO_GRIP.get());
        boolean holdingDualExtendo = inOff && inMain;
        boolean holdingExtendo = inOff ^ inMain;
        holdingExtendo &= !holdingDualExtendo;
        boolean wasHoldingExtendo = persistentData.contains(EXTENDO_MARKER);
        boolean wasHoldingDualExtendo = persistentData.contains(DUAL_EXTENDO_MARKER);


        //TFGCore.LOGGER.info("Charge is: {}", player.getMainHandItem().getOrCreateTag().getInt("Charge"));
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack currentHand = null;

        long currentCharge = 0;

        if (holdingExtendo) {
            currentHand = (mainHand.getItem() instanceof ElectricExtendoGrip) ? mainHand : offHand;
            IElectricItem currentElectric = GTCapabilityHelper.getElectricItem(currentHand);


            if (currentElectric != null) {
                currentElectric.discharge(COST, Integer.MAX_VALUE, true, false, false);
                currentCharge = currentElectric.getCharge();
                if (persistentData.contains(EXTENDO_MARKER) && currentCharge < COST) {
                    player.getAttributes()
                            .removeAttributeModifiers(rangeModifier.get());
                    persistentData.remove(EXTENDO_MARKER);

                }
            }
        }

        if (holdingExtendo != wasHoldingExtendo) {

            if (!holdingExtendo || currentCharge < COST) {
                player.getAttributes()
                        .removeAttributeModifiers(rangeModifier.get());
                persistentData.remove(EXTENDO_MARKER);

            } else {

                //AllAdvancements.EXTENDO_GRIP.awardTo(player);
                player.getAttributes()
                        .addTransientAttributeModifiers(rangeModifier.get());
                persistentData.putBoolean(EXTENDO_MARKER, true);
            }
        }

        if (holdingDualExtendo != wasHoldingDualExtendo) {
            if (!holdingDualExtendo ) {
                player.getAttributes()
                        .removeAttributeModifiers(doubleRangeModifier.get());
                persistentData.remove(DUAL_EXTENDO_MARKER);
            } else {
                //AllAdvancements.EXTENDO_GRIP_DUAL.awardTo(player);
                player.getAttributes()
                        .addTransientAttributeModifiers(doubleRangeModifier.get());
                persistentData.putBoolean(DUAL_EXTENDO_MARKER, true);
            }
        }



    }

    @SubscribeEvent
    public static void addReachToJoiningPlayersHoldingExtendo(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        CompoundTag persistentData = player.getPersistentData();

        if (persistentData.contains(DUAL_EXTENDO_MARKER))
            player.getAttributes()
                    .addTransientAttributeModifiers(doubleRangeModifier.get());
        else if (persistentData.contains(EXTENDO_MARKER))
            player.getAttributes()
                    .addTransientAttributeModifiers(rangeModifier.get());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void dontMissEntitiesWhenYouHaveHighReachDistance(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null)
            return;
        if (!isHoldingExtendoGrip(player))
            return;
        if (mc.hitResult instanceof BlockHitResult && mc.hitResult.getType() != HitResult.Type.MISS)
            return;

        // Modified version of GameRenderer#getMouseOver
        double d0 = player.getAttribute(ForgeMod.BLOCK_REACH.get())
                .getValue();
        if (!player.isCreative())
            d0 -= 0.5f;
        Vec3 Vector3d = player.getEyePosition(AnimationTickHolder.getPartialTicks());
        Vec3 Vector3d1 = player.getViewVector(1.0F);
        Vec3 Vector3d2 = Vector3d.add(Vector3d1.x * d0, Vector3d1.y * d0, Vector3d1.z * d0);
        AABB AABB = player.getBoundingBox()
                .expandTowards(Vector3d1.scale(d0))
                .inflate(1.0D, 1.0D, 1.0D);
        EntityHitResult entityraytraceresult =
                ProjectileUtil.getEntityHitResult(player, Vector3d, Vector3d2, AABB, (e) -> {
                    return !e.isSpectator() && e.isPickable();
                }, d0 * d0);
        if (entityraytraceresult != null) {
            Entity entity1 = entityraytraceresult.getEntity();
            Vec3 Vector3d3 = entityraytraceresult.getLocation();
            double d2 = Vector3d.distanceToSqr(Vector3d3);
            if (d2 < d0 * d0 || mc.hitResult == null || mc.hitResult.getType() == HitResult.Type.MISS) {
                mc.hitResult = entityraytraceresult;
                if (entity1 instanceof LivingEntity || entity1 instanceof ItemFrame)
                    mc.crosshairPickEntity = entity1;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void consumeDurabilityOnBlockBreak(BlockEvent.BreakEvent event) {
        //findAndDamageExtendoGrip(event.getPlayer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void consumeDurabilityOnPlace(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        //if (entity instanceof Player)
            //findAndDamageExtendoGrip((Player) entity);
    }

//	@SubscribeEvent(priority = EventPriority.LOWEST)
//	public static void consumeDurabilityOnPlace(PlayerInteractEvent event) {
//		findAndDamageExtendoGrip(event.getPlayer());
//	}

//    private static void findAndDamageExtendoGrip(Player player) {
//        if (player == null)
//            return;
//        if (player.level().isClientSide)
//            return;
//        InteractionHand hand = InteractionHand.MAIN_HAND;
//        ItemStack extendo = player.getMainHandItem();
//        if (!AllItems.EXTENDO_GRIP.isIn(extendo)) {
//            extendo = player.getOffhandItem();
//            hand = InteractionHand.OFF_HAND;
//        }
//        if (!AllItems.EXTENDO_GRIP.isIn(extendo))
//            return;
//        final InteractionHand h = hand;
//        if (!BacktankUtil.canAbsorbDamage(player, maxUses()))
//            extendo.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(h));
//    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return BacktankUtil.isBarVisible(stack, maxUses());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return BacktankUtil.getBarWidth(stack, maxUses());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BacktankUtil.getBarColor(stack, maxUses());
    }

    private static int maxUses() {
        return AllConfigs.server().equipment.maxExtendoGripActions.get();
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    @SubscribeEvent
    public static void bufferLivingAttackEvent(LivingAttackEvent event) {
        // Workaround for removed patch to get the attacking entity.
        lastActiveDamageSource = event.getSource();

        DamageSource source = event.getSource();
        if (source == null)
            return;
        Entity trueSource = source.getEntity();
        //if (trueSource instanceof Player)
            //findAndDamageExtendoGrip((Player) trueSource);
    }

    @SubscribeEvent
    public static void attacksByExtendoGripHaveMoreKnockback(LivingKnockBackEvent event) {
        if (lastActiveDamageSource == null)
            return;
        Entity entity = lastActiveDamageSource.getDirectEntity();
        lastActiveDamageSource = null;
        if (!(entity instanceof Player player))
            return;
        if (!isHoldingExtendoGrip(player))
            return;
        event.setStrength(event.getStrength() + 2);
    }

    private static boolean isUncaughtClientInteraction(Entity entity, Entity target) {
        // Server ignores entity interaction further than 6m
        if (entity.distanceToSqr(target) < 36)
            return false;
        if (!entity.level().isClientSide)
            return false;
        if (!(entity instanceof Player))
            return false;
        return true;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void notifyServerOfLongRangeAttacks(AttackEntityEvent event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();
        if (!isUncaughtClientInteraction(entity, target))
            return;
        Player player = (Player) entity;
        if (isHoldingExtendoGrip(player))
            AllPackets.getChannel().sendToServer(new ExtendoGripInteractionPacket(target));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void notifyServerOfLongRangeInteractions(PlayerInteractEvent.EntityInteract event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();
        if (!isUncaughtClientInteraction(entity, target))
            return;
        Player player = (Player) entity;
        if (isHoldingExtendoGrip(player))
            AllPackets.getChannel().sendToServer(new ExtendoGripInteractionPacket(target, event.getHand()));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void notifyServerOfLongRangeSpecificInteractions(PlayerInteractEvent.EntityInteractSpecific event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();
        if (!isUncaughtClientInteraction(entity, target))
            return;
        Player player = (Player) entity;
        if (isHoldingExtendoGrip(player))
            AllPackets.getChannel()
                    .sendToServer(new ExtendoGripInteractionPacket(target, event.getHand(), event.getLocalPos()));
    }

    public static boolean isHoldingExtendoGrip(Player player) {
        boolean inOff = player.getOffhandItem().is(TFGItems.ELECTRIC_EXTENDO_GRIP.get());
        boolean inMain = player.getMainHandItem().is(TFGItems.ELECTRIC_EXTENDO_GRIP.get());
        boolean holdingGrip = inOff || inMain;
        return holdingGrip;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new ExtendoGripItemRenderer()));
    }





}
