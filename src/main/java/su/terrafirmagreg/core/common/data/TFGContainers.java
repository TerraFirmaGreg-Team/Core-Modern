package su.terrafirmagreg.core.common.data;

import java.util.function.Supplier;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import de.mennomax.astikorcarts.entity.AbstractDrawnInventoryEntity;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blockentity.LargeNestBoxBlockEntity;
import su.terrafirmagreg.core.common.data.container.LargeNestBoxContainer;
import su.terrafirmagreg.core.common.data.container.SmithingTableContainer;
import su.terrafirmagreg.core.common.data.entities.astikorcarts.RNRPlowContainer;

public class TFGContainers {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TFGCore.MOD_ID);

    public static final RegistryObject<MenuType<RNRPlowContainer>> RNR_PLOW_MENU = registerContainer("rnr_plow", (windowId, inv, buf) -> {
        final int entityId = buf.readInt();
        final Entity e = inv.player.level().getEntity(entityId);
        if (!(e instanceof AbstractDrawnInventoryEntity cart))
            return null;
        return new RNRPlowContainer(windowId, inv, cart);
    });

    public static final RegistryObject<MenuType<LargeNestBoxContainer>> LARGE_NEST_BOX = TFGContainers
            .<LargeNestBoxBlockEntity, LargeNestBoxContainer>registerBlockEntityContainer("large_nest_box",
                    TFGBlockEntities.LARGE_NEST_BOX, LargeNestBoxContainer::create);

    public static final RegistryObject<MenuType<SmithingTableContainer>> SMITHING_TABLE = TFGContainers
            .registerContainer("smithing_table", ((windowId, inv, data) -> SmithingTableContainer.create(inv, windowId, ContainerLevelAccess.NULL)));

    public static <T extends InventoryBlockEntity<?>, C extends BlockEntityContainer<T>> RegistryObject<MenuType<C>> registerBlockEntityContainer(
            String name, Supplier<BlockEntityType<T>> type, BlockEntityContainer.Factory<T, C> factory) {
        return registerContainer(name, (windowId, playerInventory, buffer) -> {
            final Level level = playerInventory.player.level();
            final BlockPos pos = buffer.readBlockPos();
            final T entity = level.getBlockEntity(pos, type.get()).orElseThrow();

            return factory.create(entity, playerInventory, windowId);
        });
    }

    public static <C extends AbstractContainerMenu> RegistryObject<MenuType<C>> registerContainer(String name,
            IContainerFactory<C> factory) {
        return CONTAINERS.register(name, () -> IForgeMenuType.create(factory));
    }
}
