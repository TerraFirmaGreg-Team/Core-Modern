/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util;

import java.util.HashSet;
import java.util.Set;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.recipes.ingredients.BlockIngredient;
import net.dries007.tfc.network.DataManagerSyncPacket;
import net.dries007.tfc.util.collections.IndirectHashCollection;
import su.terrafirmagreg.core.common.perf.SupportCache;

public final class Support
{
    public static final DataManager<Support> MANAGER = new DataManager<>(Helpers.identifier("supports"), "support", Support::new, Support::new, Support::encode, Packet::new);
    public static final IndirectHashCollection<Block, Support> CACHE = IndirectHashCollection.create(s -> s.ingredient.blocks(), MANAGER::getValues);

    /**
     * The maximum range of all supports, used for support radius checks.
     */
    private static SupportRange RANGE = new SupportRange(0, 0, 0);

    /**
     * Exposed for addons. See comment for {@link Support#RANGE}.
     */
    public static SupportRange getSupportCheckRange()
    {
        return RANGE;
    }

	/**
	 * @author Mqrius
	 * Replace brute-force block scan with cache-based AABB intersection.
	 */
    public static Set<BlockPos> findUnsupportedPositions(BlockGetter worldIn, BlockPos from, BlockPos to)
    {
		if (worldIn instanceof Level level) {
			return SupportCache.forLevel(level).findUnsupportedPositions(level, from, to);
		}
		return Set.of();
    }

    public static boolean isSupported(BlockGetter world, BlockPos pos)
    {
		// Check our support cache to see if this position is supported by a HorizontalSupportBlock.
		if (world instanceof Level level) {
			return SupportCache.forLevel(level).isSupported(level, pos);
		}

        for (BlockPos supportPos : getMaximumSupportedAreaAround(pos, pos))
        {
            final BlockState supportState = world.getBlockState(supportPos);
            final Support support = get(supportState);
            if (support != null && support.canSupport(supportPos, pos))
            {
                return true;
            }
        }
        return false;
    }

    public static Iterable<BlockPos> getMaximumSupportedAreaAround(BlockPos minPoint, BlockPos maxPoint)
    {
        return BlockPos.betweenClosed(minPoint.offset(-RANGE.horizontal(), -RANGE.down(), -RANGE.horizontal()), maxPoint.offset(RANGE.horizontal(), RANGE.up(), RANGE.horizontal()));
    }

    @Nullable
    public static Support get(BlockState state)
    {
        for (Support support : CACHE.getAll(state.getBlock()))
        {
            if (support.matches(state))
            {
                return support;
            }
        }
        return null;
    }

    public static void updateMaximumSupportRange()
    {
        // Re-calculate maximum support range
        int up = 0, down = 0, horizontal = 0;
        for (Support support : MANAGER.getValues())
        {
            up = Math.max(support.getSupportUp(), up);
            down = Math.max(support.getSupportDown(), down);
            horizontal = Math.max(support.getSupportHorizontal(), horizontal);
        }

        RANGE = new SupportRange(up, down, horizontal);
    }

    private final ResourceLocation id;
    private final BlockIngredient ingredient;
    private final int supportUp, supportDown, supportHorizontal;

    public Support(ResourceLocation id, JsonObject json)
    {
        this.id = id;

        this.ingredient = BlockIngredient.fromJson(JsonHelpers.get(json, "ingredient"));
        this.supportUp = GsonHelper.getAsInt(json, "support_up", 0);
        this.supportDown = GsonHelper.getAsInt(json, "support_down", 0);
        this.supportHorizontal = GsonHelper.getAsInt(json, "support_horizontal", 0);

        if (supportUp < 0 || supportDown < 0 || supportHorizontal < 0)
        {
            throw new JsonParseException("Support values must be nonnegative.");
        }
    }

    private Support(ResourceLocation id, FriendlyByteBuf buffer)
    {
        this.id = id;

        this.ingredient = BlockIngredient.fromNetwork(buffer);
        this.supportUp = buffer.readVarInt();
        this.supportDown = buffer.readVarInt();
        this.supportHorizontal = buffer.readVarInt();
    }

    public ResourceLocation getId()
    {
        return id;
    }

    public int getSupportUp()
    {
        return supportUp;
    }

    public int getSupportDown()
    {
        return supportDown;
    }

    public int getSupportHorizontal()
    {
        return supportHorizontal;
    }

    public boolean matches(BlockState state)
    {
        return ingredient.test(state);
    }

    public boolean canSupport(BlockPos supportPos, BlockPos testPos)
    {
        BlockPos diff = supportPos.subtract(testPos);
        return Math.abs(diff.getX()) <= supportHorizontal && -supportDown <= diff.getY() && diff.getY() <= supportUp && Math.abs(diff.getZ()) <= supportHorizontal;
    }

    public Iterable<BlockPos> getSupportedArea(BlockPos center)
    {
        return BlockPos.betweenClosed(center.offset(-supportHorizontal, -supportDown, -supportHorizontal), center.offset(supportHorizontal, supportUp, supportHorizontal));
    }

    private void encode(FriendlyByteBuf buffer)
    {
        ingredient.toNetwork(buffer);

        buffer.writeVarInt(supportUp);
        buffer.writeVarInt(supportDown);
        buffer.writeVarInt(supportHorizontal);
    }

    public record SupportRange(int up, int down, int horizontal) {}

    public static class Packet extends DataManagerSyncPacket<Support> {}
}