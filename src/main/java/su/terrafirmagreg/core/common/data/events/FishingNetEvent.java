package su.terrafirmagreg.core.common.data.events;

import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.TFGTags;

public class FishingNetEvent {

    // Tags for fishing nets.
    private static final TagKey<Item> FISHING_NETS_TAG = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(),
            ResourceLocation.parse("forge:tools/fishing_nets"));

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Level level = event.getLevel();

        if (level.isClientSide())
            return;

        Player player = event.getEntity();
        Entity target = event.getTarget();
        ItemStack heldItem = player.getItemInHand(event.getHand());

        if (target == null || !target.isAlive() || target.isRemoved())
            return;

        if (!heldItem.is(FISHING_NETS_TAG))
            return;

        if (!target.getType().is(TFGTags.Entities.FishingNetScoopable))
            return;

        ServerLevel serverLevel = (ServerLevel) level;

        serverLevel.sendParticles(
                ParticleTypes.BUBBLE_POP,
                target.getX(), target.getY(), target.getZ(),
                10, 0.5, 0.5, 0.5, 0.00001);

        level.playSound(
                null,
                target.blockPosition(),
                SoundEvents.PLAYER_SPLASH,
                SoundSource.PLAYERS,
                2.0f, 2.0f);

        // Get entity's loot table and generate drops.
        if (target instanceof net.minecraft.world.entity.LivingEntity livingEntity) {

            // Special handling for Starcatcher fish which use custom drop logic.
            if (target.getType().toString().equals("entity.starcatcher.fish")) {
                handleStarcatcherFish(target, player, level);
            } else {
                // Standard loot table handling for other entities.
                handleStandardLootTable(livingEntity, serverLevel, player, level);
            }
        }

        target.remove(Entity.RemovalReason.KILLED);

        player.swing(event.getHand(), true);

        if (!player.isCreative()) {
            heldItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(event.getHand()));
        }

        event.setCanceled(true);
    }

    private void handleStarcatcherFish(Entity target, Player player, Level level) {
        try {
            var fishItemMethod = target.getClass().getMethod("getFishItem");
            ItemStack fishItem = (ItemStack) fishItemMethod.invoke(target);

            if (fishItem.isEmpty()) {
                try {
                    var tickMethod = target.getClass().getMethod("tick");
                    tickMethod.invoke(target);

                    fishItem = (ItemStack) fishItemMethod.invoke(target);
                } catch (Exception tickException) {
                    // Tick failed.
                }
            }

            if (!fishItem.isEmpty()) {
                // Give the fish item to the player.
                ItemStack itemToGive = fishItem.copy();
                if (!player.getInventory().add(itemToGive)) {
                    // If inventory is full, drop at players location.
                    ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), itemToGive);
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }

        } catch (Exception e) {
            try {
                // Use reflection to get the FISH_ITEM from the Starcatcher fish entity.
                var entityClass = target.getClass();
                var entityDataField = entityClass.getDeclaredField("entityData");
                entityDataField.setAccessible(true);
                var entityData = entityDataField.get(target);
                var synchedEntityDataClass = entityData.getClass();
                var getMethod = synchedEntityDataClass.getMethod("get", net.minecraft.network.syncher.EntityDataAccessor.class);
                var fishItemField = entityClass.getDeclaredField("FISH_ITEM");
                fishItemField.setAccessible(true);
                var fishItemAccessor = fishItemField.get(null);

                ItemStack fishItem = (ItemStack) getMethod.invoke(entityData, fishItemAccessor);

                if (!fishItem.isEmpty()) {
                    // Give the fish item to the player.
                    ItemStack itemToGive = fishItem.copy();
                    if (!player.getInventory().add(itemToGive)) {
                        // If inventory is full, drop at players location.
                        ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), itemToGive);
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);
                    }
                }

            } catch (Exception reflectionError) {
                // All methods failed.
            }
        }
    }

    private void handleStandardLootTable(net.minecraft.world.entity.LivingEntity livingEntity, ServerLevel serverLevel, Player player, Level level) {
        try {
            LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(livingEntity.getLootTable());
            LootParams.Builder builder = new LootParams.Builder(serverLevel);

            var paramSet = lootTable.getParamSet();
            var allParams = paramSet.getAllowed();

            // Basic parameters.
            if (allParams.contains(LootContextParams.THIS_ENTITY)) {
                builder.withParameter(LootContextParams.THIS_ENTITY, livingEntity);
            }
            if (allParams.contains(LootContextParams.ORIGIN)) {
                builder.withParameter(LootContextParams.ORIGIN, livingEntity.position());
            }
            if (allParams.contains(LootContextParams.DAMAGE_SOURCE)) {
                builder.withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player));
            }

            // Optional parameters.
            if (allParams.contains(LootContextParams.KILLER_ENTITY)) {
                builder.withOptionalParameter(LootContextParams.KILLER_ENTITY, player);
            }
            if (allParams.contains(LootContextParams.DIRECT_KILLER_ENTITY)) {
                builder.withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, player);
            }
            if (allParams.contains(LootContextParams.LAST_DAMAGE_PLAYER)) {
                builder.withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, player);
            }

            builder.withLuck(player.getLuck());

            LootParams lootParams = builder.create(lootTable.getParamSet());
            List<ItemStack> drops = lootTable.getRandomItems(lootParams);

            // Give items directly to player.
            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    // Try to add to inventory first.
                    if (!player.getInventory().add(drop)) {
                        // If inventory is full, drop at players location.
                        ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), drop);
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);
                    }
                }
            }

        } catch (Exception lootException) {
            // Loot table generation failed.
        }
    }
}
