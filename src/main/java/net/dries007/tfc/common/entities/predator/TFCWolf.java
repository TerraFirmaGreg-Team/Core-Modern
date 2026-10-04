package net.dries007.tfc.common.entities.predator;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.entities.ai.predator.PackPredator;
import net.dries007.tfc.common.entities.misc.IWolf;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;
import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfVariant;

public class TFCWolf extends PackPredator implements IWolf {

	@Unique
	private static final EntityDataAccessor<Integer> DATA_VARIANT;

	static {
		DATA_VARIANT = SynchedEntityData.defineId(TFCWolf.class, EntityDataSerializers.INT);
	}

	public TFCWolf(EntityType<? extends Predator> type, Level level, boolean diurnal, TFCSounds.EntitySound sounds, boolean tamable) {
		super(type, level, diurnal, sounds, tamable);
	}

	@Override
	public TFCWolfVariant getVariant() {
		if (!this.entityData.hasItem(DATA_VARIANT)) {
			return TFCWolfVariant.DEFAULT;
		}
		return TFCWolfVariant.byId(this.entityData.get(DATA_VARIANT));
	}

	@Override
	public void setVariant(TFCWolfVariant id) {
		this.entityData.set(DATA_VARIANT, id.id);
	}

	@Override
	public void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_VARIANT, TFCWolfVariant.DEFAULT.id);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("TFCWolfVariant", this.getVariant().id);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setVariant(TFCWolfVariant.byId(tag.getInt("TFCWolfVariant")));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
		data = super.finalizeSpawn(level, difficulty, type, data, tag);

		if (this.tamable) {
			BlockPos pos = this.blockPosition();
			ChunkData chunkData = ChunkDataProvider.get(level.getLevel()).get(level.getChunk(pos));
			ForestType forestType = chunkData.getForestType();
			boolean forestSpawn = switch (forestType) {
				case NONE, SPARSE -> false;
				default -> true;
			};
			float temperature = chunkData.getAverageTemp(pos);
			float rainfall = chunkData.getRainfall(pos);
			KoppenClimateClassification climate = KoppenClimateClassification.classify(temperature, rainfall);

			TFCWolfVariant variant = switch (climate) {
				case HUMID_SUBTROPICAL, TROPICAL_RAINFOREST -> TFCWolfVariant.RUSTY;
				case HUMID_SUBARCTIC, TUNDRA -> forestSpawn ? TFCWolfVariant.BLACK : TFCWolfVariant.CHESTNUT;
				case SUBTROPICAL, HOT_DESERT, TROPICAL_SAVANNA -> forestSpawn ? TFCWolfVariant.SPOTTED : TFCWolfVariant.STRIPED;
				case TEMPERATE -> forestSpawn ? TFCWolfVariant.WOODS : TFCWolfVariant.DEFAULT;
				case SUBARCTIC, ARCTIC -> forestSpawn ? TFCWolfVariant.ASHEN : TFCWolfVariant.SNOWY;
				default -> TFCWolfVariant.DEFAULT; // HUMID_OCEANIC, COLD_DESERT
			};

			this.setVariant(variant);
		} else {
			this.setVariant(TFCWolfVariant.DEFAULT);
		}

		return data;
	}
}
