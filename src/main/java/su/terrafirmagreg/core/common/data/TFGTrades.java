package su.terrafirmagreg.core.common.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.alekiponi.firmaciv.common.item.FirmacivItems;
import com.eerussianguy.beneath.common.items.BeneathItems;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.items.FLFood;
import com.eerussianguy.firmalife.common.items.FLItems;
import com.github.talrey.createdeco.ItemRegistry;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.ninni.species.registry.SpeciesItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.therighthon.afc.common.blocks.AFCBlocks;

import net.dries007.tfc.common.blocks.GroundcoverBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.HideItemType;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Metal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import earth.terrarium.adastra.common.entities.mob.lunarians.LunarianMerchantOffer;
import earth.terrarium.adastra.common.registry.ModItems;
import lombok.SneakyThrows;
import mod.traister101.sns.common.items.SNSItems;
import tfchotornot.common.items.HNItems;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;
import su.terrafirmagreg.core.common.data.items.TFGItems;
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
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCBlocks.CANDLE.get(), 4), 3, 4));

        for (var entry : DyeColor.values()) {
            earthTrades.add(new TFGItemListing("Copper", new ItemStack(ForgeRegistries.BLOCKS.getValue(TFGCore.id("decorative_vase/" + entry.getName())).asItem(), 1), 3, 4));
            earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCBlocks.DYED_CANDLE.get(entry).get().asItem(), 4), 3, 4));
            earthTrades.add(
                    new TFGItemListing("Iron", new ItemStack(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("comforts", "sleeping_bag_" + entry.getName())).asItem(), 1), 9, 1));
            earthTrades
                    .add(new TFGItemListing("Iron", new ItemStack(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("comforts", "hammock_" + entry.getName())).asItem(), 1), 9, 1));
        }
        for (var entry : TFGBlocks_Earth.PLANTS.values()) {
            // Not all groundcover has an item
            var item = entry.get().asItem();
            if (entry.get() != Blocks.AIR && item != Items.AIR) {
                earthTrades.add(new TFGItemListing("Copper", new ItemStack(item, 4), 1, 4));
            }
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
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(Items.FEATHER, 8), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(Items.FLINT, 8), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(Items.CLAY_BALL, 20), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCItems.BLUBBER.get(), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCItems.GOAT_HORN.get(), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.MOLLUSK).get().asItem(), 4), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.MUSSEL).get().asItem(), 4), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.PINECONE).get().asItem(), 4), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.PUMICE).get().asItem(), 4), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.SEAWEED).get().asItem(), 4), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.SEA_URCHIN).get().asItem(), 4), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.GUANO).get().asItem(), 4), 1, 4));

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
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(Items.PAINTING, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(FLItems.FOODS.get(FLFood.HARDTACK).get(), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(Items.FLINT_AND_STEEL, 1), 6, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(GTItems.DYNAMITE, 4), 6, 4));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(GTItems.TOOL_MATCHBOX, 1), 1, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(GTItems.TOOL_MATCHES, 4), 1, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(GTItems.FERTILIZER, 4), 1, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(GTItems.FLUID_CELL_GLASS_VIAL, 4), 1, 4));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFCItems.COMPOST.get(), 1), 1, 4));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(GTItems.FACE_MASK, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(GTItems.RUBBER_GLOVES, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCItems.EMPTY_JAR_WITH_LID.get(), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCItems.LAMP_GLASS.get(), 2), 1, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCItems.LENS.get(), 1), 4, 2));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFCItems.JACKS.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFCItems.BLOWPIPE.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFCItems.GEM_SAW.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCItems.PADDLE.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(TFCItems.JAR_LID.get(), 2), 1, 4));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems.SNOWSHOES, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems.SNORKEL.get(), 1), 6, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFGItems.FLIPPERS.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(SNSItems.QUIVER.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(SNSItems.LEATHER_SACK.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(SNSItems.BURLAP_SACK.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(SNSItems.ORE_SACK.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(SNSItems.SEED_POUCH.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(SpeciesItems.SMOKE_BOMB.get(), 1), 1, 4));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(SpeciesItems.CRANKTRAP.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(SpeciesItems.WICKED_TREAT.get(), 1), 4, 4));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(SpeciesItems.WICKED_MASK.get(), 1), 8, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(FLItems.CHEESECLOTH.get(), 1), 4, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(FLItems.RAW_HONEY.get(), 1), 4, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(FLItems.BEESWAX.get(), 1), 4, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(FLItems.PIE_PAN.get(), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(FLItems.RENNET.get(), 1), 1, 4));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(FLItems.SEED_BALL.get(), 1), 1, 4));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(FLItems.GUANO_JAR.get(), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(FLItems.HOLLOW_SHELL.get(), 4), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCItems.JUG.get(), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(FLItems.WATERING_CAN.get(), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCBlocks.CERAMIC_BOWL.get().asItem(), 2), 1, 4));
        earthTrades.add(new TFGItemListing("Copper", new ItemStack(TFCBlocks.WOODEN_BOWL.get().asItem(), 2), 1, 4));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(TFGItems.TROWEL.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(FLItems.WINE_GLASS.get(), 1), 2, 4));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(GTItems.BOTTLE_PURPLE_DRINK, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(Items.SPYGLASS, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(Items.ITEM_FRAME, 1), 4, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(Items.ARMOR_STAND, 1), 2, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(Items.GLOW_ITEM_FRAME, 1), 4, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(Items.GUNPOWDER, 4), 1, 10));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(BeneathItems.JUICER.get(), 1), 2, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(FirmacivItems.BAROMETER.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(FirmacivItems.FIRMACIV_COMPASS.get(), 1), 1, 1));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(FirmacivItems.NAV_CLOCK.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(FirmacivItems.ROPE_COIL.get(), 1), 4, 4));
        earthTrades.add(new TFGItemListing("Gold", new ItemStack(FirmacivItems.SEXTANT.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFCBlocks.CALENDAR_CLOCK.get().asItem(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFCBlocks.THERMOMETER.get().asItem(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFCBlocks.VANE.get().asItem(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFCBlocks.ANEMOMETER.get().asItem(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFCBlocks.BRASS_BELL.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(TFCBlocks.BRONZE_BELL.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(Blocks.BELL, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Industrial Iron", new ItemStack(Blocks.CAKE, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(AllBlocks.CUCKOO_CLOCK, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(AllBlocks.MYSTERIOUS_CUCKOO_CLOCK, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(AllBlocks.DESK_BELL, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(AllBlocks.CLIPBOARD, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(AllItems.HONEYED_APPLE, 2), 1, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(AllItems.CRAFTING_BLUEPRINT, 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(HNItems.MITTENS.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(HNItems.BURLAP_POTHOLDER.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(HNItems.SILK_POTHOLDER.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(HNItems.WOOL_POTHOLDER.get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(HNItems.METAL_TONGS.get(Metal.Default.ZINC).get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(HNItems.METAL_TONGS.get(Metal.Default.TIN).get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(HNItems.METAL_TONGS.get(Metal.Default.BISMUTH).get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(HNItems.METAL_TONGS.get(Metal.Default.BRASS).get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(HNItems.METAL_TONGS.get(Metal.Default.SILVER).get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(HNItems.METAL_TONGS.get(Metal.Default.NICKEL).get(), 1), 4, 1));
        earthTrades.add(new TFGItemListing("Iron", new ItemStack(HNItems.METAL_TONGS.get(Metal.Default.STERLING_SILVER).get(), 1), 4, 1));

        var cowBucket = new ItemStack(TFCItems.WOODEN_BUCKET.get());
        cowBucket.setTag(NbtUtils.snbtToStructure("{fluid: {Amount: 1000, FluidName: \"minecraft:milk\"}}"));
        earthTrades.add(new TFGItemListing("Iron", cowBucket, 5, 4));

        var yakBucket = new ItemStack(TFCItems.WOODEN_BUCKET.get());
        yakBucket.setTag(NbtUtils.snbtToStructure("{fluid: {Amount: 1000, FluidName: \"firmalife:yak_milk\"}}"));
        earthTrades.add(new TFGItemListing("Iron", yakBucket, 5, 4));

        var goatBucket = new ItemStack(TFCItems.WOODEN_BUCKET.get());
        goatBucket.setTag(NbtUtils.snbtToStructure("{fluid: {Amount: 1000, FluidName: \"firmalife:goat_milk\"}}"));
        earthTrades.add(new TFGItemListing("Iron", goatBucket, 5, 4));

        var jellieBucket = new ItemStack(TFCItems.WOODEN_BUCKET.get());
        jellieBucket.setTag(NbtUtils.snbtToStructure("{fluid: {Amount: 1000, FluidName: \"tfg:jellie_milk\"}}"));
        earthTrades.add(new TFGItemListing("Iron", jellieBucket, 5, 4));

        var invarLighter = new ItemStack(GTItems.TOOL_LIGHTER_INVAR, 1);
        invarLighter.setTag(NbtUtils.snbtToStructure("{Fluid:{Amount:100,FluidName:\"gtceu:propane\"},lighterOpen:0b}"));
        earthTrades.add(new TFGItemListing("Iron", invarLighter, 4, 1));

        var platLighter = new ItemStack(GTItems.TOOL_LIGHTER_PLATINUM, 1);
        platLighter.setTag(NbtUtils.snbtToStructure("{Fluid:{Amount:1000,FluidName:\"gtceu:butane\"},lighterOpen:0b}"));
        earthTrades.add(new TFGItemListing("Industrial Iron", platLighter, 4, 1));

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

        // These have their durability doubled in KJS, and we want to sell these at 10% durability remaining
        var helmet = new ItemStack(ModItems.SPACE_HELMET.get());
        helmet.setDamageValue((int) (helmet.getMaxDamage() * 1.9));
        spaceTrades.add(new TFGItemListing("Netherite", helmet, 18, 1));
        var suit = new ItemStack(ModItems.SPACE_SUIT.get());
        suit.setDamageValue((int) (suit.getMaxDamage() * 1.9));
        spaceTrades.add(new TFGItemListing("Netherite", suit, 18, 1));
        var pants = new ItemStack(ModItems.SPACE_PANTS.get());
        pants.setDamageValue((int) (pants.getMaxDamage() * 1.9));
        spaceTrades.add(new TFGItemListing("Netherite", pants, 18, 1));
        var boots = new ItemStack(ModItems.SPACE_BOOTS.get());
        boots.setDamageValue((int) (boots.getMaxDamage() * 1.9));
        spaceTrades.add(new TFGItemListing("Netherite", boots, 18, 1));
        spaceTrades.add(new TFGItemListing("Netherite", new ItemStack(ModItems.LAUNCH_PAD.get()), 9, 1));
        spaceTrades.add(new TFGItemListing("Netherite", new ItemStack(ForgeRegistries.ITEMS.getValue(TFGCore.id("space_suit_fabric")), 1), 3, 4));

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
                .filter(item -> item.get() != Items.AIR)
                .map(item -> new TFGItemListing(coinMaterial, new ItemStack(item.get(), outputCount), coinCount, 4))
                .toList());
    }

    private static void addBlockTrades(List<VillagerTrades.ItemListing> trades, Collection<RegistryObject<Block>> blocks, String coinMaterial, int coinCount, int outputCount) {
        trades.addAll(blocks.stream()
                .filter(block -> block.get().asItem() != Items.AIR && block.get() != Blocks.AIR)
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
