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
            try {
                LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(livingEntity.getLootTable());
                LootParams.Builder builder = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.THIS_ENTITY, target)
                        .withParameter(LootContextParams.ORIGIN, target.position())
                        .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player));

                List<ItemStack> drops = lootTable.getRandomItems(builder.create(lootTable.getParamSet()));

                // Drop the items at the players position.
                for (ItemStack drop : drops) {
                    if (!drop.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), drop);
                        level.addFreshEntity(itemEntity);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to generate loot for entity " + target.getType() + ": " + e.getMessage());
            }
        }

        target.remove(Entity.RemovalReason.KILLED);

        player.swing(event.getHand(), true);

        if (!player.isCreative()) {
            heldItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(event.getHand()));
        }

        event.setCanceled(true);
    }
}
