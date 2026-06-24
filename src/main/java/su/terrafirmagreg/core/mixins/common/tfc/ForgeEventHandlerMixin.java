package su.terrafirmagreg.core.mixins.common.tfc;

import static net.dries007.tfc.TerraFirmaCraft.LOGGER;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.dries007.tfc.ForgeEventHandler;
import net.dries007.tfc.common.capabilities.size.ItemSizeManager;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.common.capabilities.size.Weight;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.Units;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.common.food.nutrient.NutrientEffectsHandler;
import su.terrafirmagreg.core.utils.CustomSpawnHelper;
import su.terrafirmagreg.core.utils.CustomSpawnSaveHandler;

@Mixin(value = ForgeEventHandler.class, remap = false)
public class ForgeEventHandlerMixin {

    // Forcibly disable nether portals because there's some funky mod conflict going on with
    // settings overwriting each other

    @Inject(method = "onCreateNetherPortal", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$onCreateNetherPortal(BlockEvent.PortalSpawnEvent event, CallbackInfo ci) {
        event.setCanceled(true);
        ci.cancel();
    }

    @WrapOperation(method = "onPlayerTick(Lnet/minecraftforge/event/TickEvent$PlayerTickEvent;)V", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/Helpers;countOverburdened(Lnet/minecraft/world/Container;)I", remap = false), remap = false)
    private static int tfg$redirectCountOverburdened(Container container, Operation<Integer> original) {
        int count = tfg$countOverburdenedFull(container);
        // Protein nutrition >85%: allow 1 extra hugeHeavy item before overburdened effect applies.
        if (container instanceof Inventory inventory) {
            Player player = inventory.player;
            if (NutrientEffectsHandler.hasProteinHeavyItemBoost(player.getUUID())) {
                count = Math.max(0, count - 1);
            }
        }
        return Math.min(count, 2);
    }

    // Remake overburdened check because TFC did it weird.
    @Unique
    private static int tfg$countOverburdenedFull(Container container) {
        int count = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                var size = ItemSizeManager.get(stack);
                if (size.getWeight(stack) == Weight.VERY_HEAVY && size.getSize(stack) == Size.HUGE) {
                    count++;
                }
            }
        }
        return count;
    }

    // Don't create water source blocks when hot items melt the ice
    // target = ServerLevel.setBlockAndUpdate(BlockPos, BlockState)
    // There's a few of those in the method so have to check which blocks are being melted.
    @WrapOperation(method = "onItemExpire", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", remap = true), remap = false)
    private static boolean tfg$preventHotIceWater(ServerLevel level, BlockPos pos, BlockState newState, Operation<Boolean> original) {
        Block currentBlock = level.getBlockState(pos).getBlock();

        // Only intercept packed/blue ice melting
        if ((currentBlock == Blocks.PACKED_ICE || currentBlock == Blocks.BLUE_ICE) && newState.is(Blocks.WATER)) {
            level.destroyBlock(pos, false);
            return true;
        }

        // Otherwise do as usual
        return original.call(level, pos, newState);
    }

    @Inject(method = "onLivingSpawnCheck", at = @At("TAIL"), remap = false)
    private static void tfg$onLivingSpawnCheck(MobSpawnEvent.FinalizeSpawn event, CallbackInfo ci) {
        // Prevent surface slimes because TFC makes an exception for them for some reason
        final MobSpawnType spawn = event.getSpawnType();
        if (spawn == MobSpawnType.NATURAL || spawn == MobSpawnType.CHUNK_GENERATION) {
            final LivingEntity entity = event.getEntity();
            if (entity.getType() == EntityType.SLIME && event.getLevel().getRawBrightness(entity.blockPosition(), 0) != 0) {
                event.setSpawnCancelled(true);
                event.setCanceled(true);
            }
        }
    }

    /**
     * @author
     * @reason It was becoming too complicated to do as a normal mixin.
     */
    @Overwrite()
    public static void onCreateWorldSpawn(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel level && level.getChunkSource().getGenerator() instanceof ChunkGeneratorExtension extension) {
            final ChunkGenerator generator = extension.self();
            final ServerLevelData levelData = event.getSettings();

            ChunkPos chunkPos = null;
            RandomSource random = new XoroshiroRandomSource(level.getSeed());

            RegionGenerator regionGen = new RegionGenerator(extension.settings(), random);

            var condition = CustomSpawnHelper.getFromConfig();

            boolean climateMatch = false;
            int seedTicker = 0;

            var settingsMultiplier = CustomSpawnHelper.findSettingsMultipliers(extension);

            while (!climateMatch) {

                chunkPos = new ChunkPos(
                        CustomSpawnHelper.findSpawnBiome((int) (condition.spawnCenterX() * settingsMultiplier.get(1)), (int) (condition.spawnCenterZ() * settingsMultiplier.get(0)),
                                extension.settings().spawnDistance() * condition.spawnRadiusMultiplier(),
                                random, extension));
                Region.Point regionPoint = regionGen.getOrCreateRegionPoint(Units.blockToGrid(chunkPos.getMinBlockX()), Units.blockToGrid(chunkPos.getMinBlockZ()));

                //System.out.println("Testing chunkPos " + chunkPos.getWorldPosition());
                //System.out.println(regionPoint.temperature);
                //System.out.println(regionPoint.rainfall);
                if (CustomSpawnHelper.testWithinRanges(regionPoint.temperature, regionPoint.rainfall, condition)) {
                    climateMatch = true;
                } else {
                    ++seedTicker;
                    random = new XoroshiroRandomSource(level.getSeed() + seedTicker);
                }
            }

            BlockPos defaultPos = chunkPos.getWorldPosition().offset(8, generator.getSpawnHeight(level), 8);

            levelData.setSpawn(defaultPos, 0.0F);

            boolean foundExactSpawn = false;
            int x = 0, z = 0;
            int xStep = 0;
            int zStep = -1;

            GlobalPos globalSpawnPos = GlobalPos.of(ServerLevel.OVERWORLD, defaultPos);

            for (int tries = 0; tries < 1024; ++tries) {
                if (x > -16 && x <= 16 && z > -16 && z <= 16) {
                    final BlockPos spawnPos = PlayerRespawnLogic.getSpawnPosInChunk(level, new ChunkPos(chunkPos.x + x, chunkPos.z + z));
                    if (spawnPos != null) {
                        globalSpawnPos = GlobalPos.of(ServerLevel.OVERWORLD, spawnPos);
                        levelData.setSpawn(spawnPos, 0);
                        foundExactSpawn = true;
                        break;
                    }
                }

                if ((x == z) || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                    final int swap = xStep;
                    xStep = -zStep;
                    zStep = swap;
                }

                x += xStep;
                z += zStep;
            }

            if (!foundExactSpawn) {
                LOGGER.warn("Unable to find a suitable spawn location!");
            }

            if (level.getServer().getWorldData().worldGenOptions().generateBonusChest()) {
                LOGGER.warn("No bonus chest for you, you cheaty cheater!");
            }

            if (condition.dimension() == ServerLevel.OVERWORLD) {
                CustomSpawnSaveHandler.setSpawnPos(level, globalSpawnPos);
            } else if (condition.dimension() == ServerLevel.NETHER) {
                CustomSpawnSaveHandler.setSpawnPos(level, CustomSpawnHelper.BENEATH_PLACEHOLDER);
            } else if (condition.dimension() == Planet.MARS) {
                CustomSpawnSaveHandler.setSpawnPos(level, CustomSpawnHelper.MARS_PLACEHOLDER);
            }

            CustomSpawnHelper.resetConfigValue();
            event.setCanceled(true);
        }

    }

}
