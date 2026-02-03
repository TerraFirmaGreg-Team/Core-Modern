package su.terrafirmagreg.core.common.data.utils;

import java.util.*;

import com.teamresourceful.resourcefullib.common.utils.SaveHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;

public class LaunchPositionHandler extends SaveHandler {
    /**
     * data = Map<UUID, LaunchPositionHolder>
     * LaunchPositionHolder = Map<ResourceKey<Level>, List<List<Object>>>
     * List<Object> = (Boolean locked, GlobalPos pos, @Nullable String name)
     *
     * data holds all launch positions keyed with player uuid
     * LaunchPositionHolder holds launch positions keyed with dimension
     * LaunchPositionList holds launch positions info lists
     * Info lists holds the position, if the position is locked, and a nullable name
     * **/
    private final Map<UUID, LaunchPositionHolder> data = new HashMap<>();

    @Override
    public void loadData(CompoundTag compoundTag) {
        compoundTag.getAllKeys().forEach((key) -> {
            UUID uuid = UUID.fromString(key);

            //Effectively gets the LaunchPositionHolder
            CompoundTag allPlanetsTag = compoundTag.getCompound(key);
            Map<ResourceKey<Level>, List<List<Object>>> planetsMap = new HashMap<>();

            for (String planetKey : allPlanetsTag.getAllKeys()) {
                CompoundTag singlePlanetTag = allPlanetsTag.getCompound(planetKey);

                List<List<Object>> singlePlanetPositions = unpackagePlanetPosData(singlePlanetTag);

                planetsMap.put(ResourceKey.create(Registries.DIMENSION, Objects.requireNonNull(ResourceLocation.tryParse(planetKey))), singlePlanetPositions);
            }

            this.data.put(uuid, new LaunchPositionHolder(planetsMap));
        });
    }

    @Override
    public void saveData(CompoundTag compoundTag) {
        List<Object> posData = new ArrayList<>(List.of(
                GlobalPos.of(Planet.MOON, new BlockPos(20, 20, 20)), false, "test"));
        List<Object> posData2 = new ArrayList<>(List.of(
                GlobalPos.of(Planet.MOON, new BlockPos(40, 20, 40)), false, "test2"));
        List<List<Object>> tempList = new ArrayList<>(List.of(posData, posData2));
        Map<ResourceKey<Level>, List<List<Object>>> tempMap = new HashMap<>();
        tempMap.put(Planet.MOON, tempList);
        data.put(UUID.fromString("eebb8358-cda4-4cb6-9c8c-c7a17eaa58b3"), new LaunchPositionHolder(tempMap));

        this.data.forEach((uuid, launchPositions) -> {
            CompoundTag planetsTag = new CompoundTag();

            for (var planetSet : launchPositions.planets.entrySet()) {
                ResourceKey<Level> planetKey = planetSet.getKey();
                List<List<Object>> planetPosLists = planetSet.getValue();

                planetsTag.put(planetKey.location().toString(), packagePlanetPosData(planetPosLists));
            }
            compoundTag.put(uuid.toString(), planetsTag);
        });

    }

    public static CompoundTag packagePlanetPosData(List<List<Object>> planetList) {
        CompoundTag singlePlanetTag = new CompoundTag();

        int i = 0;
        for (List<Object> singlePosList : planetList) {
            GlobalPos pos = (GlobalPos) singlePosList.get(0);
            boolean locked = (boolean) singlePosList.get(1);
            String name = (String) singlePosList.get(2);

            CompoundTag posDataTag = new CompoundTag();

            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos)
                    .result().ifPresent((encodedPos) -> {
                        posDataTag.put("pos", encodedPos);
                        posDataTag.putBoolean("locked", locked);
                        posDataTag.putString("name", name);
                    });
            if (!posDataTag.isEmpty()) {
                singlePlanetTag.put(Integer.toString(i), posDataTag);
                i++;
            }
        }

        return singlePlanetTag;
    }

    public static List<List<Object>> unpackagePlanetPosData(CompoundTag singlePlanetTag) {
        List<List<Object>> singlePlanetPositions = new ArrayList<>();

        for (String posIndex : singlePlanetTag.getAllKeys()) {
            List<Object> singlePosList = new ArrayList<>();

            CompoundTag singlePosTag = singlePlanetTag.getCompound(posIndex);
            GlobalPos.CODEC.parse(NbtOps.INSTANCE, singlePosTag.getCompound("pos"))
                    .result().ifPresent((parsedPos) -> {
                        singlePosList.add(parsedPos);
                        singlePosList.add(singlePosTag.getBoolean("locked"));
                        singlePosList.add(singlePosTag.getString("name"));
                    });
            if (!singlePosList.isEmpty()) {
                singlePlanetPositions.add(singlePosList);
            }
        }

        return singlePlanetPositions;
    }

    private static LaunchPositionHolder getPlayerData(Player player, ServerLevel level) {
        Map<UUID, LaunchPositionHolder> newData = read(level).data;
        return newData.getOrDefault(player.getUUID(), null);
    }

    public static Optional<CompoundTag> getPosDataNBT(Player player, ServerLevel level, ResourceKey<Level> planet) {
        Optional<List<List<Object>>> playerPlanetPosList = getPosData(player, level, planet);
        return playerPlanetPosList.map(LaunchPositionHandler::packagePlanetPosData);
    }

    public static Optional<List<List<Object>>> getPosData(Player player, ServerLevel level, ResourceKey<Level> planet) {
        LaunchPositionHolder playerData = getPlayerData(player, level);
        if (playerData == null)
            return Optional.empty();

        List<List<Object>> playerPlanetPosList = playerData.planets.getOrDefault(planet, null);

        if (playerPlanetPosList == null)
            return Optional.empty();

        return Optional.of(playerPlanetPosList);
    }

    public static LaunchPositionHandler read(ServerLevel level) {
        return (LaunchPositionHandler) read(level.getDataStorage(), LaunchPositionHandler::new, "tfg_launch_position_data");
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    private static record LaunchPositionHolder(Map<ResourceKey<Level>, List<List<Object>>> planets) {
        public LaunchPositionHolder() {
            this(new HashMap<>());
        }
    }

    //Tangentally related methods
    public static Set<CompoundTag> getPlanetPosDataFromBuffer(FriendlyByteBuf buf) {
        System.out.println("getPlanetPosDataFromBuffer was called");
        Set<CompoundTag> locations = new HashSet<>();
        int locationCount = buf.readVarInt();
        System.out.println(locationCount);
        for (int i = 0; i < locationCount; ++i) {
            locations.add(buf.readAnySizeNbt());
        }

        System.out.println(locations);
        return Collections.unmodifiableSet(locations);
    }
}
