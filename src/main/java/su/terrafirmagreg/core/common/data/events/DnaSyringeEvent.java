package su.terrafirmagreg.core.common.data.events;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.config.TFGConfig;

import java.util.List;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DnaSyringeEvent {

    /**
     * This event will give the player a filled dna syringe with the nbt data of the mob clicked. It checks if the entity is considered a "living entity"
     * so it wouldn't give a sample for something like a minecart. And then it will play some effects like particles, sound and hurting the animal for 0 damage.
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Level level = event.getLevel();

        if (level.isClientSide) return;

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        // Check for empty syringe.
        if (!stack.is(TFGTags.Items.EmptySyringe)) return;

        // Check if the entity is alive.
        if (!(event.getTarget() instanceof LivingEntity target)) return;

        // Check if the item is on cooldown.
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return;

        // Check the entity ID against the blacklist.
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        List<String> blacklist = List.copyOf(TFGConfig.SERVER.SYRINGE_BLACKLIST.get()); // Returns List<String>
        if (entityId == null || blacklist.contains(entityId.toString())) return;

        ItemStack filled = new ItemStack(TFGItems.FILLED_DNA_SYRINGE.get());
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());

        // Adds nbt tag "mob_type".
        if (id == null) return;
        filled.getOrCreateTag().putString("mob_type", id.toString());

        // Shrinks the stack if the player isn't in creative.
        if (!player.isCreative()) stack.shrink(1);

        // Give the filled syringe.
        if (!player.addItem(filled)) player.drop(filled, false);

        // Play a sound at the players' location.
        level.playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.BOTTLE_FILL,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 2.0f);

        // Play particles at the entities location.
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    10, 1.0, 1.0, 1.0, 0.0
            );
        }

        // "Hurt" the entity for 0 damage.
        target.hurt(player.damageSources().playerAttack(player), 0.0F);

        // Apply a 1 sec cooldown to prevent exploits.
        player.getCooldowns().addCooldown(stack.getItem(), 20);

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        player.swing(event.getHand());
    }


    /**
     * Events for the dirty syringe. If right-clicked or left-clicked on an entity or player, it will provide poison for 10sec.
     */
    // Right-click dirty syringe
    @SubscribeEvent
    public static void onEntityRightClick(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof LivingEntity target) {
            applyEffect(event.getEntity(), target, event.getItemStack(), event.getHand());
        }
    }

    //Left-click dirty syringe
    @SubscribeEvent
    public static void onEntityLeftClick(AttackEntityEvent event) {
        if (event.getTarget() instanceof LivingEntity target) {
            applyEffect(event.getEntity(), target, event.getEntity().getMainHandItem(), InteractionHand.MAIN_HAND);
        }
    }

    // Events to occur for the dirty syringe.
    private static void applyEffect(Player player, LivingEntity target, ItemStack held, InteractionHand hand) {
        if (held.isEmpty()) return;

        if (held.is(TFGItems.DIRTY_DNA_SYRINGE.get())) {

            // Check if the item is on cooldown.
            if (player.getCooldowns().isOnCooldown(held.getItem())) return;

            // Play sound.
            target.level().playSound(
                    null,
                    target.blockPosition(),
                    SoundEvents.TRIDENT_HIT,
                    SoundSource.PLAYERS,
                    1.0F,
                    2.0F
            );

            // Hurt for 0 damage.
            target.hurt(player.damageSources().playerAttack(player), 0.0F);

            // Apply Poison I for 10s.
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 10*20, 0));

            // Apply a 20 sec cooldown.
            player.getCooldowns().addCooldown(held.getItem(), 20*20);
            player.swing(hand, true);
        }
    }
}
