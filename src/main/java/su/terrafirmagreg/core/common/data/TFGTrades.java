package su.terrafirmagreg.core.common.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.items.FLFood;
import com.eerussianguy.firmalife.common.items.FLItems;
import com.github.talrey.createdeco.ItemRegistry;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.therighthon.afc.common.blocks.AFCBlocks;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.HideItemType;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import earth.terrarium.adastra.common.entities.mob.lunarians.LunarianMerchantOffer;
import earth.terrarium.adastra.common.registry.ModItems;
import lombok.SneakyThrows;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;
import su.terrafirmagreg.core.common.data.items.TFGItems_Medicines;
import su.terrafirmagreg.tfc_textile.item.TFCTextileItems;

public class TFGTrades {

    @SneakyThrows
    public static void init() {

        // Earth wandering trader

        List<VillagerTrades.ItemListing> earthTrades = new ArrayList<>();
        addItemTrades(earthTrades, TFCItems.CROP_SEEDS.values(), "Copper", 2, 1);
        addBlockTrades(earthTrades, TFCBlocks.PLANTS.values(), "Copper", 1, 4);
		addBlockTrades(earthTrades, TFCBlocks.GLAZED_LARGE_VESSELS.values(), "Copper", 2, 1);
		addItemTrades(earthTrades, FLItems.SPICES.values(), "Copper", 3, 4);

		for (var entry : DyeColor.values()) {
			earthTrades.add(new TFGItemListing("Copper", new ItemStack(ForgeRegistries.BLOCKS.getValue(TFGCore.id("decorative_vase/" + entry.getName())).asItem(), 1), 3, 4));
		}
		for	(var entry : TFCBlocks.GROUNDCOVER.values()) {
			// Not all groundcover has an item
			var item = entry.get().asItem();
			if (item != Items.AIR) {
				earthTrades.add(new TFGItemListing("Copper", new ItemStack(item, 4), 1, 4));
			}
		}
		for (var entry : TFGBlocks_Earth.PLANTS.values()) {
			earthTrades.add(new TFGItemListing("Copper", new ItemStack(entry.get().asItem(), 4), 1, 4));
		}

        earthTrades.add(new TFGItemListing("Copper", new ItemStack(FLItems.RED_GRAPE_SEEDS.get(), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(FLItems.WHITE_GRAPE_SEEDS.get(), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(ForgeRegistries.ITEMS.getValue(TFGCore.id("flax_seeds")), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(ForgeRegistries.ITEMS.getValue(TFGCore.id("sunflower_seeds")), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(ForgeRegistries.ITEMS.getValue(TFGCore.id("rapeseed_seeds")), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(ForgeRegistries.ITEMS.getValue(TFGCore.id("cucumber_seeds")), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(ForgeRegistries.ITEMS.getValue(TFGCore.id("lentil_seeds")), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(ForgeRegistries.ITEMS.getValue(TFGCore.id("radish_seeds")), 1), 2, 4));

        earthTrades.add(new TFGItemListing("Copper", new ItemStack(Items.BOW, 1), 3, 1));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(Items.ARROW, 16), 1, 10));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(Items.LADDER, 16), 1, 10));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(Items.FLOWER_POT, 1), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(Items.BAMBOO, 1), 3, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(Items.BOOK, 1), 1, 4));
		earthTrades.add(new TFGItemListing("Copper", new ItemStack(Items.FEATHER, 4), 1, 4));
		earthTrades.add(new TFGItemListing("Copper", new ItemStack(Items.FLINT, 4), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCItems.BLUBBER.get(), 1), 2, 4));

        for (var entry : TFCBlocks.WOODS.entrySet()) {
            earthTrades.add(new TFGItemListing("Copper", new ItemStack(entry.getValue().get(Wood.BlockType.LOG).get(), 8), 1, 1));
            earthTrades.add(new TFGItemListing("Gold", new ItemStack(entry.getValue().get(Wood.BlockType.SAPLING).get()), 3, 1));
        }

        for (var entry : AFCBlocks.WOODS.entrySet()) {
            earthTrades.add(new TFGItemListing("Copper", new ItemStack(entry.getValue().get(Wood.BlockType.LOG).get(), 8), 1, 1));
            earthTrades.add(new TFGItemListing("Gold", new ItemStack(entry.getValue().get(Wood.BlockType.SAPLING).get()), 3, 1));
        }

        addBlockTrades(earthTrades, TFCBlocks.FRUIT_TREE_SAPLINGS.values(), "Gold", 6, 1);
        addBlockTrades(earthTrades, TFCBlocks.SPREADING_BUSHES.values(), "Gold", 3, 1);
        addBlockTrades(earthTrades, TFCBlocks.STATIONARY_BUSHES.values(), "Gold", 3, 1);
        addBlockTrades(earthTrades, FLBlocks.FRUIT_TREE_SAPLINGS.values(), "Gold", 6, 1);
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCBlocks.CRANBERRY_BUSH.get(), 1), 3, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCBlocks.BANANA_SAPLING.get(), 1), 3, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(ForgeRegistries.BLOCKS.getValue(TFGCore.id("palm_tree/coconut_sapling")), 1), 3, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(ForgeRegistries.BLOCKS.getValue(TFGCore.id("palm_tree/date_sapling")), 1), 3, 4));

        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCItems.HIDES.get(HideItemType.RAW).get(HideItemType.Size.SMALL).get(), 1), 1, 2));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCItems.HIDES.get(HideItemType.RAW).get(HideItemType.Size.MEDIUM).get(), 1), 2, 2));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCItems.HIDES.get(HideItemType.RAW).get(HideItemType.Size.LARGE).get(), 1), 3, 2));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCItems.HIDES.get(HideItemType.SHEEPSKIN).get(HideItemType.Size.SMALL).get(), 1), 2, 2));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCItems.HIDES.get(HideItemType.SHEEPSKIN).get(HideItemType.Size.MEDIUM).get(), 1), 3, 2));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCItems.HIDES.get(HideItemType.SHEEPSKIN).get(HideItemType.Size.LARGE).get(), 1), 4, 2));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFCTextileItems.BLACK_BEAR_FUR.get(), 1), 6, 1));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFCTextileItems.CARIBOU_FUR.get(), 1), 6, 1));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFCTextileItems.COUGAR_FUR.get(), 1), 6, 1));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFCTextileItems.DIREWOLF_FUR.get(), 1), 6, 1));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFCTextileItems.GRIZZLY_BEAR_FUR.get(), 1), 6, 1));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFCTextileItems.LION_FUR.get(), 1), 6, 1));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFCTextileItems.PANTHER_FUR.get(), 1), 6, 1));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFCTextileItems.POLAR_BEAR_FUR.get(), 1), 6, 1));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFCTextileItems.SABERTOOTH_FUR.get(), 1), 6, 1));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFCTextileItems.TIGER_FUR.get(), 1), 6, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(Items.PAINTING, 1), 6, 1));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(FLItems.FOODS.get(FLFood.HARDTACK).get(), 1), 2, 4));
		earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(Items.FLINT_AND_STEEL, 1), 6, 1));
		earthTrades.add(new TFGItemListing("Iron", new ItemStack(GTItems.DYNAMITE, 4), 6, 4));
		earthTrades.add(new TFGItemListing("Iron", new ItemStack(GTItems.TOOL_MATCHBOX, 1), 1, 1));
		earthTrades.add(new TFGItemListing("Iron", new ItemStack(GTItems.TOOL_LIGHTER_INVAR, 1), 4, 1));
		earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(GTItems.TOOL_LIGHTER_PLATINUM, 1), 4, 1));
		earthTrades.add(new TFGItemListing("Gold", new ItemStack(GTItems.TOOL_MATCHES, 4), 1, 4));

        List<VillagerTrades.ItemListing> medicineTrades = new ArrayList<>();

        medicineTrades.add(new TFGItemListing("Copper", new ItemStack(TFGItems_Medicines.COMBAT_POWDER, 1), 2, 4));
        medicineTrades.add(new TFGItemListing("Copper", new ItemStack(TFGItems_Medicines.RECOVERY_POWDER, 1), 3, 4));
        medicineTrades.add(new TFGItemListing("Copper", new ItemStack(TFGItems_Medicines.ACROBAT_POWDER, 1), 2, 4));
        medicineTrades.add(new TFGItemListing("Copper", new ItemStack(TFGItems_Medicines.MINING_POWDER, 1), 2, 4));

        medicineTrades.add(new TFGItemListing("Gold", new ItemStack(TFGItems_Medicines.ABSORPTION_SALVO, 1), 3, 4));
        medicineTrades.add(new TFGItemListing("Gold", new ItemStack(TFGItems_Medicines.INVISIBILITY_SALVO, 1), 2, 4));
        medicineTrades.add(new TFGItemListing("Gold", new ItemStack(TFGItems_Medicines.LUCK_SALVO, 1), 1, 4));
        medicineTrades.add(new TFGItemListing("Gold", new ItemStack(TFGItems_Medicines.INSTANT_HEALTH_SALVO, 1), 5, 4));
        medicineTrades.add(new TFGItemListing("Gold", new ItemStack(TFGItems_Medicines.FIRE_RESISTANCE_SALVO, 1), 3, 4));
        medicineTrades.add(new TFGItemListing("Gold", new ItemStack(TFGItems_Medicines.RESISTANCE_SALVO, 1), 3, 4));

        medicineTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems_Medicines.ANTIPOISON_PILL, 1), 1, 4));
        medicineTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems_Medicines.WATER_BREATHING_PILL, 1), 2, 4));
        medicineTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems_Medicines.POISON_PILL, 1), 1, 4));
        medicineTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems_Medicines.SLOWNESS_PILL, 1), 1, 4));
        medicineTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems_Medicines.WEAKNESS_PILL, 1), 1, 4));
        medicineTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems_Medicines.HASTE_PILL, 1), 2, 4));
        medicineTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems_Medicines.NIGHT_VISION_PILL, 1), 2, 4));
        medicineTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems_Medicines.REGENERATION_PILL, 1), 3, 4));
        medicineTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems_Medicines.SPEED_PILL, 1), 2, 4));

        medicineTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFGItems_Medicines.ANTIPOISON_TABLET, 1), 1, 4));
        medicineTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFGItems_Medicines.WATER_BREATHING_TABLET, 1), 2, 4));
        medicineTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFGItems_Medicines.POISON_TABLET, 1), 1, 4));
        medicineTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFGItems_Medicines.SLOWNESS_TABLET, 1), 1, 4));
        medicineTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFGItems_Medicines.WEAKNESS_TABLET, 1), 1, 4));
        medicineTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFGItems_Medicines.HASTE_TABLET, 1), 2, 4));
        medicineTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFGItems_Medicines.NIGHT_VISION_TABLET, 1), 2, 4));
        medicineTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFGItems_Medicines.REGENERATION_TABLET, 1), 3, 4));
        medicineTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFGItems_Medicines.SPEED_TABLET, 1), 2, 4));

        medicineTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems_Medicines.PARASITICIDE_CAPSULE, 1), 4, 4));
        medicineTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFGItems_Medicines.DETOX_CAPSULE, 1), 4, 4));
        medicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.NANOFILTRATION_CAPSULE, 1), 9, 4));
        medicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.PARACETAMOL_PILL, 1), 18, 4));
        medicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.RAD_AWAY_PILL, 1), 36, 4));

        CompoundTag fluidTag = new CompoundTag();
        CompoundTag waterTag = new CompoundTag();
        waterTag.putInt("Amount", 400);
        waterTag.putString("FluidName", "minecraft:water");
        fluidTag.put("fluid", waterTag);

        var hemaBottle = new ItemStack(TFCItems.HEMATITIC_GLASS_BOTTLE.get());
        hemaBottle.setTag(fluidTag);
        var siliBottle = new ItemStack(TFCItems.SILICA_GLASS_BOTTLE.get());
        siliBottle.setTag(fluidTag);
        var olivBottle = new ItemStack(TFCItems.OLIVINE_GLASS_BOTTLE.get());
        olivBottle.setTag(fluidTag);
        var volcBottle = new ItemStack(TFCItems.VOLCANIC_GLASS_BOTTLE.get());
        volcBottle.setTag(fluidTag);

        medicineTrades.add(new TFGItemListing("Gold", hemaBottle, 1, 2));
        medicineTrades.add(new TFGItemListing("Gold", siliBottle, 1, 2));
        medicineTrades.add(new TFGItemListing("Gold", olivBottle, 1, 2));
        medicineTrades.add(new TFGItemListing("Gold", volcBottle, 1, 2));

        VillagerTrades.WANDERING_TRADER_TRADES.clear();
        VillagerTrades.WANDERING_TRADER_TRADES.put(1, earthTrades.toArray(VillagerTrades.ItemListing[]::new));
        VillagerTrades.WANDERING_TRADER_TRADES.put(2, medicineTrades.toArray(VillagerTrades.ItemListing[]::new));

        // Space wandering trader - only wants netherite (blue steel) coins

        List<VillagerTrades.ItemListing> spaceTrades = new ArrayList<>();
        spaceTrades.add(new TFGItemListing("Netherite", new ItemStack(ModItems.CHEESE.get()), 4, 2));
        spaceTrades.add(new TFGItemListing("Netherite", new ItemStack(ModItems.SPACE_PAINTING.get()), 1, 2));
        spaceTrades.add(new TFGItemListing("Netherite", new ItemStack(ModItems.SPACE_HELMET.get()), 18, 1));
        spaceTrades.add(new TFGItemListing("Netherite", new ItemStack(ModItems.SPACE_SUIT.get()), 18, 1));
        spaceTrades.add(new TFGItemListing("Netherite", new ItemStack(ModItems.SPACE_PANTS.get()), 18, 1));
        spaceTrades.add(new TFGItemListing("Netherite", new ItemStack(ModItems.SPACE_BOOTS.get()), 18, 1));
        spaceTrades.add(new TFGItemListing("Netherite", new ItemStack(ModItems.LAUNCH_PAD.get()), 9, 1));

        var gasTank = new ItemStack(ModItems.GAS_TANK.get());
        gasTank.setTag(NbtUtils.snbtToStructure("{BotariumData: {StoredFluids: [{Amount: 4000, Fluid: \"tfg:compressed_heliox_3\"}]}}"));
        spaceTrades.add(new TFGItemListing("Netherite", gasTank, 36, 1));

        var rocketFuelCell = new ItemStack(GTItems.FLUID_CELL_LARGE_ALUMINIUM.get());
        rocketFuelCell.setTag(NbtUtils.snbtToStructure("{Fluid: {Amount: 3000, FluidName: \"gtceu:rocket_fuel\"}}"));
        spaceTrades.add(new TFGItemListing("Netherite", rocketFuelCell, 36, 1));

        spaceTrades.add(new TFGItemListing("Netherite", hemaBottle, 1, 2));
        spaceTrades.add(new TFGItemListing("Netherite", siliBottle, 1, 2));
        spaceTrades.add(new TFGItemListing("Netherite", olivBottle, 1, 2));
        spaceTrades.add(new TFGItemListing("Netherite", volcBottle, 1, 2));

        spaceTrades.add(new TFGItemListing("Netherite", new ItemStack(FLItems.FOODS.get(FLFood.HARDTACK).get(), 4), 1, 10));
        spaceTrades.add(new TFGItemListing("Netherite", new ItemStack(ForgeRegistries.ITEMS.getValue(TFGCore.id("food/calorie_paste")), 4), 2, 10));

        List<VillagerTrades.ItemListing> spaceMedicineTrades = new ArrayList<>();
        spaceMedicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.ANTIPOISON_TABLET, 1), 2, 4));
        spaceMedicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.HASTE_TABLET, 1), 4, 4));
        spaceMedicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.NIGHT_VISION_TABLET, 1), 4, 4));
        spaceMedicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.REGENERATION_TABLET, 1), 6, 4));
        spaceMedicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.SPEED_TABLET, 1), 4, 4));

        spaceMedicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.PARASITICIDE_CAPSULE, 1), 18, 4));
        spaceMedicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.DETOX_CAPSULE, 1), 18, 4));
        spaceMedicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.NANOFILTRATION_CAPSULE, 1), 18, 4));
        spaceMedicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.PARACETAMOL_PILL, 1), 36, 4));
        spaceMedicineTrades.add(new TFGItemListing("Netherite", new ItemStack(TFGItems_Medicines.RAD_AWAY_PILL, 1), 64, 4));

        LunarianMerchantOffer.WANDERING_TRADER_TRADES.clear();
        LunarianMerchantOffer.WANDERING_TRADER_TRADES.put(1, spaceTrades.toArray(VillagerTrades.ItemListing[]::new));
        LunarianMerchantOffer.WANDERING_TRADER_TRADES.put(2, spaceMedicineTrades.toArray(VillagerTrades.ItemListing[]::new));
    }

    private static void addItemTrades(List<VillagerTrades.ItemListing> trades, Collection<RegistryObject<Item>> items, String coinMaterial, int coinCount, int outputCount) {
        trades.addAll(items.stream()
                .map(item -> new TFGItemListing(coinMaterial, new ItemStack(item.get(), outputCount), coinCount, 4))
                .toList());
    }

    private static void addBlockTrades(List<VillagerTrades.ItemListing> trades, Collection<RegistryObject<Block>> blocks, String coinMaterial, int coinCount, int outputCount) {
        trades.addAll(blocks.stream()
                .map(block -> new TFGItemListing(coinMaterial, new ItemStack(block.get().asItem(), outputCount), coinCount, 4))
                .toList());
    }

    private record TFGItemListing(String coinMaterial, ItemStack purchase, int coinCost, int maxUses) implements VillagerTrades.ItemListing {

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemStack(ItemRegistry.COINSTACKS.get(coinMaterial).get(), coinCost), purchase, maxUses, 0, 0.05f);
        }
    }
}
