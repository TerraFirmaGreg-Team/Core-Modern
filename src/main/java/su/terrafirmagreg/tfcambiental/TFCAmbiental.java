package su.terrafirmagreg.tfcambiental;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.dries007.tfc.util.Helpers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import top.theillusivec4.curios.api.SlotTypeMessage;

import su.terrafirmagreg.tfcambiental.capability.TemperaturePacket;
import su.terrafirmagreg.tfcambiental.event.ClientEventHandler;
import su.terrafirmagreg.tfcambiental.item.ClothesItem;
import su.terrafirmagreg.tfcambiental.item.TFCAmbientalItems;
import su.terrafirmagreg.tfcambiental.item.material.TemperatureAlteringMaterial;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;

@Mod(TFCAmbiental.MOD_ID)
public class TFCAmbiental {
    public static final String MOD_ID = "tfcambiental";
    public static final String CURIOS_ID = "curios";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation HEAD_SLOT = new ResourceLocation("curios:slot/clothes_hat");
    public static final ResourceLocation CHEST_SLOT = new ResourceLocation("curios:slot/clothes_torso");
    public static final ResourceLocation LEGS_SLOT = new ResourceLocation("curios:slot/clothes_pants");
    public static final ResourceLocation FEET_SLOT = new ResourceLocation("curios:slot/clothes_socks");

    public static final TagKey<Item> SUNBLOCKING_APPAREL = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "sunblocking_apparel"));
    public static final TagKey<Item> HOT_INGOTS = TagKey.create(Registries.ITEM, new ResourceLocation("forge:hot_ingots"));
    public static final TagKey<Block> WARM_STUFF = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "warm_stuff"));
    public static final TagKey<Block> HOT_STUFF = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "hot_stuff"));
    public static final TagKey<Block> COLD_STUFF = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "cold_stuff"));
    public static final TagKey<Fluid> SPRING_WATER = TagKey.create(Registries.FLUID, Helpers.identifier("spring_water"));
    public static final TagKey<EntityType<?>> HOT_ENTITIES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(MOD_ID, "hot_entities"));
    public static final TagKey<EntityType<?>> COLD_ENTITIES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(MOD_ID, "cold_entities"));

    public static final ResourceKey<DamageType> HOT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(TFCAmbiental.MOD_ID, "heatstroke"));
    public static final ResourceKey<DamageType> FREEZE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(TFCAmbiental.MOD_ID, "frostbite"));

    public static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Helpers.identifier("tfcambiental"), () -> VERSION, VERSION::equals, VERSION::equals);

    public TFCAmbiental() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.addListener(this::addTooltips);
        eventBus.addListener(this::onInterModComms);

        TFCAmbientalConfig.init();
        ClientEventHandler.init(eventBus);
        register(
                0,
                TemperaturePacket.class,
                TemperaturePacket::encode,
                TemperaturePacket::new,
                TemperaturePacket::handle);
        TFCAmbientalItems.ITEMS.register(eventBus);
    }

    private String formatAttribute(float attribute) {
        if (attribute != 0) {
            if (attribute > 0) {
                return attribute % 1 == 0 ? "+" + ((int) attribute) : "+" + attribute;
            } else {
                return attribute % 1 == 0 ? "" + ((int) attribute) : "" + attribute;
            }
        } else {
            return "";
        }
    }

    private void onInterModComms(InterModEnqueueEvent event) {
        InterModComms.sendTo(CURIOS_ID, SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("clothes_hat")
                .icon(HEAD_SLOT)
                .priority(90)
                .build());
        InterModComms.sendTo(CURIOS_ID, SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("clothes_torso")
                .icon(CHEST_SLOT)
                .priority(91)
                .build());
        InterModComms.sendTo(CURIOS_ID, SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("clothes_pants")
                .icon(LEGS_SLOT)
                .priority(92)
                .build());
        InterModComms.sendTo(CURIOS_ID, SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("clothes_socks")
                .icon(FEET_SLOT)
                .priority(93)
                .build());
        InterModComms.sendTo(CURIOS_ID, SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("feet")
                .icon(FEET_SLOT)
                .priority(94)
                .build());
    }

    private void addTooltips(ItemTooltipEvent event) {
        if (event.getEntity() != null && !event.getEntity().level().isClientSide())
            return;
        float warmth = 0;
        float insulation = 0;
        if (event.getItemStack().getItem() instanceof ClothesItem clothesItem) {
            if (clothesItem.getMaterial() instanceof TemperatureAlteringMaterial tempMaterial) {
                TempModifier modifier = tempMaterial.getTempModifier(event.getItemStack());
                warmth = (modifier.getChange());
                insulation = (modifier.getPotency() / 0.1f);
            }
            if (clothesItem.getType().equals(ArmorItem.Type.HELMET)) {
                event.getToolTip().add(Component.translatable("tfcambiental.tooltip.sun_protection").withStyle(ChatFormatting.GRAY));
            }
        }
        warmth = ((float) Math.floor(warmth / 0.25f)) * 0.25f;
        insulation = ((float) Math.floor(insulation / 0.25f)) * -0.25f;

        if (warmth != 0) {
            event.getToolTip().add(Component.translatable("tfcambiental.tooltip.warmth", formatAttribute(warmth)).withStyle(ChatFormatting.GRAY));
        }
        if (insulation != 0) {
            event.getToolTip().add(Component.translatable("tfcambiental.tooltip.insulation", formatAttribute(insulation)).withStyle(ChatFormatting.GRAY));
        }
    }

    private static <T> void register(int id, Class<T> cls, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, NetworkEvent.Context> handler) {
        CHANNEL.registerMessage(id, cls, encoder, decoder, (packet, context) -> {
            context.get().setPacketHandled(true);
            handler.accept(packet, context.get());
        });
    }
}
