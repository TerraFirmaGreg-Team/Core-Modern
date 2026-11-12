/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.glass;

import java.util.Map;
import java.util.function.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.items.Powder;
import net.dries007.tfc.common.items.TFCItems;

public enum GlassOperation
{
    // Blowpipe
    BLOW,
    ROLL,
    STRETCH,
    PINCH,
    FLATTEN,
    SAW,
    // Powder Bowl
    AMETHYST,
    SODA_ASH,
    SULFUR,
    IRON,
    RUBY,
    LAPIS_LAZULI,
    PYRITE,
    SAPPHIRE,
    GOLD,
    GRAPHITE,
    COPPER,
    NICKEL,
    TIN,
    SILVER,
    // Pours
    TABLE_POUR,
    BASIN_POUR,
    ;

    public static final GlassOperation[] VALUES = values();

    public static final Supplier<Map<Item, GlassOperation>> POWDERS = Suppliers.memoize(() -> {
            ImmutableMap.Builder<Item, GlassOperation> builder = ImmutableMap.builder();
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_soda_ash_dust")), SODA_ASH);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_sulfur_dust")), SULFUR);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_graphite_dust")), GRAPHITE);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_hematite_dust")), IRON);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_yellow_limonite_dust")), IRON);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_magnetite_dust")), IRON);
			builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_goethite_dust")), IRON);
			builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_iron_dust")), IRON);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_gold_dust")), GOLD);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_copper_dust")), COPPER);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_malachite_dust")), COPPER);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_tetrahedrite_dust")), COPPER);
			builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_chalcopyrite_dust")), COPPER);
			builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_chalcocite_dust")), COPPER);
			builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_bornite_dust")), COPPER);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_cassiterite_dust")), TIN);
			builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_tin_dust")), TIN);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_garnierite_dust")), NICKEL);
			builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_nickel_dust")), NICKEL);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_silver_dust")), SILVER);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_amethyst_dust")), AMETHYST);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_ruby_dust")), RUBY);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_lapis_dust")), LAPIS_LAZULI);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_pyrite_dust")), PYRITE);
            builder.put(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GTCEu.MOD_ID, "small_sapphire_dust")), SAPPHIRE);
            return builder.build();
        }
    );

    @Nullable
    public static GlassOperation byIndex(int id)
    {
        return id >= 0 && id < VALUES.length ? VALUES[id] : null;
    }

    @Nullable
    public static GlassOperation get(ItemStack stack, Player player)
    {
        if (stack.isEmpty())
        {
            return player.getLookAngle().y < -0.95 ? STRETCH : BLOW;
        }
        if (stack.getItem() instanceof IGlassworkingTool tool)
        {
            return tool.getOperation();
        }
        return null;
    }

    @Nullable
    public static GlassOperation getByPowder(ItemStack stack)
    {
        return POWDERS.get().get(stack.getItem());
    }

    public SoundEvent getSound()
    {
        return this == BLOW ? TFCSounds.BELLOWS_BLOW.get() : SoundEvents.ANVIL_USE;
    }

    public boolean hasRequiredTemperature(ItemStack stack)
    {
        return this == SAW
            || HeatCapability.getTemperature(stack) > Heat.FAINT_RED.getMin();
    }
}
