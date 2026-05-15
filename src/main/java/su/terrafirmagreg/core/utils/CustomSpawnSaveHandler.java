package su.terrafirmagreg.core.utils;

import com.teamresourceful.resourcefullib.common.utils.SaveHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;

/// Manages a single GlobalPos that represents the servers world spawn
public class CustomSpawnSaveHandler extends SaveHandler {

    private GlobalPos data = GlobalPos.of(ServerLevel.OVERWORLD, BlockPos.ZERO);

    @Override
    public void loadData(CompoundTag compoundTag) {
        GlobalPos.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("spawn")).result().ifPresent(globalPos -> data = globalPos);
    }

    @Override
    public void saveData(CompoundTag compoundTag) {
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, data).result().ifPresent(encodedPos -> {
            compoundTag.put("spawn", encodedPos);
        });
    }

    public static GlobalPos getSpawnPos(ServerLevel level) {
        return read(level).data;
    }

    public static void setSpawnPos(ServerLevel level, GlobalPos globalPos) {
        CustomSpawnSaveHandler handler = read(level);
        handler.data = globalPos;
        handler.setDirty();
    }

    public static CustomSpawnSaveHandler read(ServerLevel level) {
        return (CustomSpawnSaveHandler) read(level.getDataStorage(), CustomSpawnSaveHandler::new, "tfg_spawn_position_data");
    }

}
