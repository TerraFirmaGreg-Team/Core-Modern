package su.terrafirmagreg.core.common.data.entities;

import com.mojang.serialization.Codec;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.livestock.MammalProperties;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.prey.TFCRabbit;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.config.animals.MammalConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.IntFunction;

public class MoonRabbit extends TFCRabbit {

	private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(MoonRabbit.class, EntityDataSerializers.INT);

	public static MoonRabbit makeMoonRabbit(EntityType<? extends TFCRabbit> type, Level level)
	{
		return new MoonRabbit(type, level, TFCConfig.SERVER.rabbitConfig);
	}

	public static boolean spawnRules(EntityType<? extends TFCRabbit> type, LevelAccessor level, MobSpawnType spawn, BlockPos pos, RandomSource rand)
	{
		return level.getBlockState(pos).isAir();
	}

	public MoonRabbit(EntityType<? extends Rabbit> type, Level level, MammalConfig config) {
		super(type, level, config);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_TYPE_ID, MoonVariant.PINK.id);
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("MoonRabbitType", this.getMoonVariant().id);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setVariant(MoonVariant.byId(tag.getInt("MoonRabbitType")));
	}

	@Override
	public float getWalkTargetValue(@NotNull BlockPos pos, LevelReader level)
	{
		return level.getBlockState(pos).is(TFCTags.Blocks.PLANTS) ? 10.0F : level.getPathfindingCostFromLightLevels(pos);
	}

	public MoonVariant getMoonVariant() {
		return MoonVariant.byId(this.entityData.get(DATA_TYPE_ID));
	}

	public void setVariant(MoonVariant variant)
	{
		this.entityData.set(DATA_TYPE_ID, variant.id);
	}

	@Override
	public void createGenes(@NotNull CompoundTag tag, @NotNull TFCAnimalProperties male)
	{
		super.createGenes(tag, male);
		tag.putString("variant1", getMoonVariant().getSerializedName());
		if (male instanceof MoonRabbit rabbit)
		{
			tag.putString("variant2", getMoonVariant().getSerializedName());
		}
	}

	@Override
	public void applyGenes(@NotNull CompoundTag tag, @NotNull MammalProperties baby)
	{
		super.applyGenes(tag, baby);
		if (baby instanceof MoonRabbit rabbit)
		{
			if (tag.contains("variant2", Tag.TAG_INT) && random.nextInt(10) != 0)
			{
				rabbit.setVariant(MoonVariant.byId(random.nextBoolean() ? tag.getInt("variant1") : tag.getInt("variant2")));
			}
			else if (level() instanceof ServerLevelAccessor server)
			{
				rabbit.setVariant(getRandomRabbitType());
			}
		}
	}

	@Override
	public MoonRabbit getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob other)
	{
		final AgeableMob mob = super.getBreedOffspring(level, other);
		if (mob instanceof MoonRabbit rabbit) {
			rabbit.setVariant(getRandomRabbitType());
			return rabbit;
		}
		return null;
	}

	@Override
	public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
										@NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag)
	{
		spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
		if (reason != MobSpawnType.BREEDING)
		{
			initCommonAnimalData(level, difficulty, reason);
			this.setVariant(getRandomRabbitType());
		}
		setPregnantTime(-1L);
		return spawnData;
	}

	private MoonVariant getRandomRabbitType()
	{
		return MoonVariant.byId(random.nextInt(6));
	}


	public static enum MoonVariant implements StringRepresentable {
		PINK(0, "pink"),
		WHITE(1, "white"),
		GREY(2, "grey"),
		CYAN(3, "cyan"),
		PURPLE(4, "purple"),
		SOFU(5, "sofu");

		private static final IntFunction<MoonVariant> BY_ID = ByIdMap.sparse(MoonVariant::id, values(), PINK);
		public static final Codec<MoonVariant> CODEC = StringRepresentable.fromEnum(MoonVariant::values);
		final int id;
		private final String name;

		private MoonVariant(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public @NotNull String getSerializedName() {
			return this.name;
		}

		public int id() {
			return this.id;
		}

		public static MoonVariant byId(int id) {
			return (MoonVariant) BY_ID.apply(id);
		}
	}
}

