package su.terrafirmagreg.core.common.data.entities.astikorcarts;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import de.mennomax.astikorcarts.entity.AbstractDrawnInventoryEntity;
import de.mennomax.astikorcarts.inventory.container.CartContainer;

import su.terrafirmagreg.core.common.data.TFGContainers;

public final class RNRPlowContainer extends CartContainer {
    private static final TagKey<Item> ROAD_MATERIALS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("rnr", "road_materials"));
    private static final ResourceLocation CRUSHED_BASE_COURSE_ID = ResourceLocation.fromNamespaceAndPath("rnr", "crushed_base_course");

    private int randomModeClient = 0;

    public RNRPlowContainer(final int id, final Inventory playerInv, final AbstractDrawnInventoryEntity cart) {
        super(TFGContainers.RNR_PLOW_MENU.get(), id, cart);

        final Item crushedBaseCourse = ForgeRegistries.ITEMS.getValue(CRUSHED_BASE_COURSE_ID);

        // Upper section.
        for (int row = 0; row < 2; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new RoadMaterialsSlot(this.cartInv, col + row * 9, 8 + col * 18, 18 + row * 18, crushedBaseCourse));
            }
        }

        // Lower section.
        final int lowerStartIndex = 18;
        final int lowerStartY = 18 + 2 * 18 + 10;
        for (int row = 0; row < 2; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new CrushedBaseCourseSlot(
                        this.cartInv,
                        lowerStartIndex + col + row * 9,
                        8 + col * 18,
                        lowerStartY + row * 18,
                        crushedBaseCourse));
            }
        }

        // Player inventory (3 rows + hotbar).
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInv, l + k * 9 + 9, 8 + l * 18, 114 + k * 18));
            }
        }
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInv, x, 8 + x * 18, 172));
        }

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                if (RNRPlowContainer.this.cart instanceof RNRPlow plow) {
                    return plow.isRandomMode() ? 1 : 0;
                }
                return 0;
            }

            @Override
            public void set(int value) {
                randomModeClient = value & 1;
            }
        });
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        if (id == 0) {
            if (this.cart instanceof RNRPlow plow) {
                plow.setRandomMode(!plow.isRandomMode());
                return true;
            }
        }
        return super.clickMenuButton(player, id);
    }

    public boolean isRandomModeClient() {
        return this.randomModeClient != 0;
    }

    // Top inventory.
    private static final class RoadMaterialsSlot extends SlotItemHandler {
        private final Item crushed;

        public RoadMaterialsSlot(IItemHandler handler, int index, int x, int y, Item crushed) {
            super(handler, index, x, y);
            this.crushed = crushed;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (stack.isEmpty())
                return false;
            if (!stack.is(ROAD_MATERIALS))
                return false;
            return this.crushed == null || !stack.is(this.crushed);
        }
    }

    // Bottom inventory.
    private static final class CrushedBaseCourseSlot extends SlotItemHandler {
        private final Item crushed;

        public CrushedBaseCourseSlot(IItemHandler handler, int index, int x, int y, Item crushed) {
            super(handler, index, x, y);
            this.crushed = crushed;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (stack.isEmpty() || this.crushed == null)
                return false;
            return stack.is(this.crushed);
        }
    }
}
