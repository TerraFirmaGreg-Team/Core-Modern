package su.terrafirmagreg.core.mixins.common.tfc;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataCapability;
import net.dries007.tfc.world.chunkdata.ChunkDataGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.LazyOptional;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.world.IChunkData;

/**
 * ChunkData.get(LevelChunk) is called from ChunkDataProvider.get(ChunkAccess), which is used during world gen only.
 * The comment there says that this call to ChunkData.get(LevelChunk) "should generally not happen".
 * Add some logging so that crashes caused by corrupted chunks using this path are easier to debug.
 */
@Mixin(value = ChunkData.class, remap = false)
@SuppressWarnings("deprecation")
public class ChunkDataMixin implements IChunkData {
    @Unique
    private final byte[] shuffledBlockPositions = getShuffledByteArray();
    @Unique
    private long lastRandomTick;
    @Unique
    private byte nextSnowPosition;

    /**
     * Add logging in case of chunks with initialized caps where the cap data is empty.
     * This causes NPE, but we don't try to silently pass the error since that could cause players to play on
     * corrupted chunks rather than fix the issue.
     *
     * @author Redeix, Mqrius
     * @reason Log chunk info when capability data is missing
     */
    @Overwrite(remap = false)
    public static ChunkData get(LevelChunk chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return ChunkData.EMPTY;
        }

        ChunkPos pos = chunk.getPos();
        LazyOptional<ChunkDataCapability> cap = chunk.getCapability(ChunkDataCapability.CAPABILITY);

        if (!cap.isPresent()) {
            tfg$corruptChunkLogger(pos, "TFC capability missing");
            return ChunkData.EMPTY;
        }

        try {
            ChunkData data = cap.map(c -> ((IChunkDataCapabilityAccessor) c).tfg$getData()).orElse(ChunkData.EMPTY);

            if (data == ChunkData.EMPTY) {
                tfg$corruptChunkLogger(pos, "ChunkData is EMPTY for populated chunk");
            }

            return data;
        } catch (NullPointerException e) {
            tfg$corruptChunkLogger(pos, "ChunkDataCapability exists but ChunkDataCapability.getData() is null");
            throw new IllegalStateException("Corrupt TFC chunk at " + pos, e);
        }
    }

    @Unique
    private static void tfg$corruptChunkLogger(ChunkPos pos, String reason) {
        TFGCore.LOGGER.error("Possibly corrupt chunk detected");
        TFGCore.LOGGER.error("Chunk: ({}, {})", pos.x, pos.z);
        TFGCore.LOGGER.error("Region: r.{}.{}.mca", pos.getRegionX(), pos.getRegionZ());
        TFGCore.LOGGER.error("Coords: {},{} to {},{}", pos.getMinBlockX(), pos.getMinBlockZ(), pos.getMaxBlockX(), pos.getMaxBlockZ());
        TFGCore.LOGGER.error("Reason: {}", reason);
    }

    @Override
    public long tfg$getLastRandomTick() {
        return lastRandomTick;
    }

    @Override
    public void tfg$setLastRandomTick(ChunkAccess chunk, long lastRandomTick) {
        this.lastRandomTick = lastRandomTick;
        chunk.setUnsaved(true); // Flag the chunk, since we need to re-save the data
    }

    @Override
    public BlockPos tfg$getNextSnowPos(ChunkPos chunkPos) {
        // Convert byte into local coordinates x, z = [0, 15]
        byte b = shuffledBlockPositions[nextSnowPosition - Byte.MIN_VALUE];
        final byte mask = 15; // 0000 1111
        int x = b & mask;
        int z = b >> 4 & mask;

        return new BlockPos(chunkPos.getMinBlockX() + x, 0, chunkPos.getMinBlockZ() + z);
    }

    @Override
    public void tfg$iterateSnowPos(ChunkAccess chunk) {
        // Iterate to the next snow position
        nextSnowPosition++;
        chunk.setUnsaved(true); // Flag the chunk, since we need to re-save the data
    }

    @Unique
    private byte[] getShuffledByteArray() {
        byte[] arr = new byte[256];
        for (int i = 0; i < 256; i++) {
            arr[i] = (byte) (i - 128); // -128 to 127
        }
        // Fisher-Yates shuffle
        Random rand = new Random();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            byte tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
        return arr;
    }

    @Shadow
    private ChunkData.Status status;

    @Inject(method = "serializeNBT", at = @At(value = "TAIL"))
    public void tfg$SerializeInject(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag nbt = cir.getReturnValue();
        if (this.status == ChunkData.Status.FULL || this.status == ChunkData.Status.PARTIAL) {
            nbt.putLong("lastRandomTick", lastRandomTick);
            nbt.putByte("nextSnowPosition", nextSnowPosition);
        }
    }

    @Inject(method = "deserializeNBT", at = @At(value = "TAIL"))
    public void tfg$DeserializeInject(CompoundTag nbt, CallbackInfo ci) {
        if (this.status == ChunkData.Status.FULL || this.status == ChunkData.Status.PARTIAL) {
            lastRandomTick = nbt.getLong("lastRandomTick");
            nextSnowPosition = nbt.getByte("nextSnowPosition");
        }
    }

    @Inject(method = "<init>(Lnet/dries007/tfc/world/chunkdata/ChunkDataGenerator;Lnet/minecraft/world/level/ChunkPos;)V", at = @At("RETURN"))
    public void tfg$onChunkDataInit(ChunkDataGenerator generator, ChunkPos pos, CallbackInfo ci) {
        this.lastRandomTick = Integer.MIN_VALUE;
        this.nextSnowPosition = 0;
    }
}
