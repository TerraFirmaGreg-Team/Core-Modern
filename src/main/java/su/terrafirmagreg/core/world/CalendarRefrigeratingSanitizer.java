package su.terrafirmagreg.core.world;

import java.util.*;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import su.terrafirmagreg.core.common.data.TFGFoodTraits;
import su.terrafirmagreg.core.common.data.tfgt.machine.electric.FoodRefrigeratorMachine;
import su.terrafirmagreg.core.mixins.common.minecraft.AccessorChunkMap;
import su.terrafirmagreg.core.mixins.common.minecraft.AccessorServerChunkCache;

/**
 * This tool removes the Refrigerating trait from food items during calendar ticks,
 * so long as they are not in an active refrigerator.
 * This prevents food from retaining infinite refrigeration when taken out of refrigerators
 * using non extraction methods like AE2 or breaking the fridge.
 */
@Mod.EventBusSubscriber(modid = "tfg", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CalendarRefrigeratingSanitizer {

    private static long LAST_SERVER_TICK = Long.MIN_VALUE;
    private static final int SANITIZE_INTERVAL_TICKS = 300;
    private static final int LARGE_JUMP_THRESHOLD = 20;
    private static long ACCUMULATED_CAL_TICKS = 0L;

    private static final Map<ServerLevel, Boolean> SANITIZING = new WeakHashMap<>();

    /**
     * onServerTick
     * <p>
     * - Reads calendar ticks from 'Calendars.SERVER' and computes the difference since the last run.
     * - Accumulates calendar ticks and triggers sanitation when the accumulator >= 'SANITIZE_INTERVAL_TICKS'
     * or when a large jump (>= 'LARGE_JUMP_THRESHOLD') is detected.
     * - When triggered it iterates all server levels and uses 'sanitizeLevel()' to remove the
     * REFRIGERATING trait from food items that are not inside active refrigerators.
     *
     * @param event TickEvent.ServerTickEvent
     */
    @SubscribeEvent
    public static void onServerTick(@NotNull TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        final long now = Calendars.SERVER.getTicks();
        final long last = LAST_SERVER_TICK;

        if (last == Long.MIN_VALUE) {
            LAST_SERVER_TICK = now;
            return;
        }

        long delta = now - last;
        if (delta <= 0) {
            LAST_SERVER_TICK = now;
            return;
        }

        ACCUMULATED_CAL_TICKS += delta;

        boolean largeJump = delta >= LARGE_JUMP_THRESHOLD;
        boolean intervalHit = ACCUMULATED_CAL_TICKS >= SANITIZE_INTERVAL_TICKS;

        if (largeJump || intervalHit) {
            ACCUMULATED_CAL_TICKS = 0;

            final MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (ServerLevel level : server.getAllLevels()) {
                    sanitizeLevel(level);
                }
            }
        }

        LAST_SERVER_TICK = now;
    }

    private static void sanitizeLevel(@NotNull ServerLevel level) {
        if (Boolean.TRUE.equals(SANITIZING.put(level, Boolean.TRUE)))
            return;
        try {
            final ChunkMap map = ((AccessorServerChunkCache) level.getChunkSource()).tfg$getChunkMap();
            final Set<UUID> seen = new HashSet<>();

            for (ChunkHolder holder : ((AccessorChunkMap) map).tfg$invokeGetChunks()) {
                final LevelChunk chunk = holder.getTickingChunk();
                if (chunk == null)
                    continue;

                // Block entities.
                // Cache to avoid ConcurrentModificationException.
                for (BlockEntity be : new ArrayList<>(chunk.getBlockEntities().values())) {
                    if (isActiveFridge(be))
                        continue;
                    be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
                        if (handler instanceof IItemHandlerModifiable mod)
                            sanitizeHandler(mod);
                        else
                            sanitizeHandlerReadOnly(handler);
                    });
                }

                // Item entities.
                sanitizeItemEntitiesInChunk(level, chunk, seen);
            }

            // Player inventories.
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                if (player.level() != level)
                    continue;
                Inventory inv = player.getInventory();
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack s = inv.getItem(i);
                    IFood food = FoodCapability.get(s);
                    if (food == null || !food.hasTrait(TFGFoodTraits.REFRIGERATING))
                        continue;

                    ItemStack sanitized = sanitizeRefrigeratingInPlace(s.copy());
                    if (!ItemStack.isSameItemSameTags(s, sanitized) || s.getCount() != sanitized.getCount()) {
                        inv.setItem(i, sanitized);
                    }
                }
            }
        } finally {
            SANITIZING.remove(level);
        }
    }

    private static void sanitizeHandler(IItemHandlerModifiable mod) {
        for (int i = 0; i < mod.getSlots(); i++) {
            ItemStack s = mod.getStackInSlot(i);
            if (s.isEmpty())
                continue;

            IFood food = FoodCapability.get(s);
            if (food == null || !food.hasTrait(TFGFoodTraits.REFRIGERATING))
                continue;

            ItemStack sanitized = sanitizeRefrigeratingInPlace(s.copy());
            if (!ItemStack.isSameItemSameTags(s, sanitized) || s.getCount() != sanitized.getCount()) {
                mod.setStackInSlot(i, sanitized);
            }
        }
    }

    private static void sanitizeHandlerReadOnly(IItemHandler handler) {
        // Read only.
    }

    private static void sanitizeItemEntitiesInChunk(ServerLevel level, LevelChunk chunk, Set<UUID> seen) {
        final ChunkPos pos = chunk.getPos();
        final int minX = pos.getMinBlockX();
        final int minZ = pos.getMinBlockZ();
        final int maxX = minX + 15;
        final int maxZ = minZ + 15;
        final int minY = level.getMinBuildHeight();
        final int maxY = level.getMaxBuildHeight() - 1;

        final AABB aabb = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, aabb)) {
            if (!seen.add(entity.getUUID()))
                continue;

            final ItemStack s = entity.getItem();
            if (s.isEmpty())
                continue;

            final IFood food = FoodCapability.get(s);
            if (food == null || !food.hasTrait(TFGFoodTraits.REFRIGERATING))
                continue;

            final ItemStack sanitized = sanitizeRefrigeratingInPlace(s.copy());
            if (!ItemStack.isSameItemSameTags(s, sanitized) || s.getCount() != sanitized.getCount()) {
                entity.setItem(sanitized);
            }
        }
    }

    private static boolean isActiveFridge(@NotNull BlockEntity be) {
        if (be instanceof IMachineBlockEntity mbe) {
            MetaMachine mm = mbe.getMetaMachine();
            if (mm instanceof FoodRefrigeratorMachine fridge) {
                return fridge.isActivelyRefrigerating();
            }
        }
        return false;
    }

    // Remove trait and clamp rounded creation date.
    private static ItemStack sanitizeRefrigeratingInPlace(@NotNull ItemStack stack) {
        FoodCapability.removeTrait(stack, TFGFoodTraits.REFRIGERATING);
        IFood food = FoodCapability.get(stack);
        if (food != null) {
            long orig = food.getCreationDate();
            long rounded = FoodCapability.getRoundedCreationDate(orig);
            food.setCreationDate(Math.min(orig, rounded));
        }
        return stack;
    }

    private CalendarRefrigeratingSanitizer() {
    }
}
