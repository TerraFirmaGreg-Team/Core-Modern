package su.terrafirmagreg.core.common.data.entities.astikorcarts;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
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

/**
 * The container for the RNR Plow entity.
 * This container manages the inventory slots, data synchronization, and player interactions.
 */
public final class RNRPlowContainer extends CartContainer {
    private static final TagKey<Item> ROAD_MATERIALS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("rnr", "road_materials"));
    private static final ResourceLocation CRUSHED_BASE_COURSE_ID = ResourceLocation.fromNamespaceAndPath("rnr", "crushed_base_course");

    private int randomModeClient = 0;
    private int widthClient = 3;

    /**
     * Constructs the RNR Plow container.
     *
     * @param id        The container ID.
     * @param playerInv The player's inventory.
     * @param cart      The cart entity.
     */
    public RNRPlowContainer(final int id, final Inventory playerInv, final AbstractDrawnInventoryEntity cart) {
        super(TFGContainers.RNR_PLOW_MENU.get(), id, cart);

        final Item crushedBaseCourse = ForgeRegistries.ITEMS.getValue(CRUSHED_BASE_COURSE_ID);

        // Upper inventory section (road materials).
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new RoadMaterialsSlot(this.cartInv, col + row * 9, 8 + col * 18, 18 + row * 18, crushedBaseCourse));
            }
        }

        // Lower inventory section (crushed base course).
        final int lowerStartIndex = 27;
        final int lowerStartY = 18 + 3 * 18 + 10;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new CrushedBaseCourseSlot(this.cartInv, lowerStartIndex + col + row * 9, 8 + col * 18, lowerStartY + row * 18, crushedBaseCourse));
            }
        }

        // Player inventory slots (3 rows + hotbar).
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInv, l + k * 9 + 9, 8 + l * 18, 170 + k * 18));
            }
        }
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInv, x, 8 + x * 18, 228));
        }

        // Data slot for random mode.
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

        // Data slot for plow width.
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                if (RNRPlowContainer.this.cart instanceof RNRPlow plow) {
                    return plow.getPlowWidth();
                }
                return 3;
            }

            @Override
            public void set(int value) {
                widthClient = Mth.clamp(value, 1, 5);
            }
        });

        if (this.cart instanceof RNRPlow plow) {
            this.widthClient = plow.getPlowWidth();
            this.randomModeClient = plow.isRandomMode() ? 1 : 0;
        }
    }

    /**
     * Handles menu button clicks.
     *
     * @param player The player interacting with the menu.
     * @param id     The button ID.
     * @return True if the action was handled, false otherwise.
     */
    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        if (id == 0) {
            if (this.cart instanceof RNRPlow plow) {
                plow.setRandomMode(!plow.isRandomMode());
                return true;
            }
        }
        if (id >= 1 && id <= 5) {
            if (this.cart instanceof RNRPlow plow) {
                plow.setPlowWidth(id);
                return true;
            }
        }
        return super.clickMenuButton(player, id);
    }

    /**
     * Checks if random mode is enabled on the client.
     *
     * @return True if random mode is enabled, false otherwise.
     */
    public boolean isRandomModeClient() {
        return this.randomModeClient != 0;
    }

    /**
     * Retrieves the plow width on the client.
     *
     * @return The plow width.
     */
    public int getPlowWidthClient() {
        return this.widthClient;
    }

    /**
     * Represents a slot for road materials in the upper inventory.
     */
    private static final class RoadMaterialsSlot extends SlotItemHandler {
        private final Item crushed;

        /**
         * Constructs a RoadMaterialsSlot.
         *
         * @param handler The item handler for the slot.
         * @param index   The slot index.
         * @param x       The x-coordinate of the slot.
         * @param y       The y-coordinate of the slot.
         * @param crushed The crushed base course item.
         */
        public RoadMaterialsSlot(IItemHandler handler, int index, int x, int y, Item crushed) {
            super(handler, index, x, y);
            this.crushed = crushed;
        }

        /**
         * Determines if an item stack can be placed in this slot.
         *
         * @param stack The item stack to check.
         * @return True if the stack can be placed, false otherwise.
         */
        @Override
        public boolean mayPlace(ItemStack stack) {
            if (stack.isEmpty())
                return false;
            if (!stack.is(ROAD_MATERIALS))
                return false;
            return this.crushed == null || !stack.is(this.crushed);
        }
    }

    /**
     * Represents a slot for crushed base course in the lower inventory.
     */
    private static final class CrushedBaseCourseSlot extends SlotItemHandler {
        private final Item crushed;

        /**
         * Constructs a CrushedBaseCourseSlot.
         *
         * @param handler The item handler for the slot.
         * @param index   The slot index.
         * @param x       The x-coordinate of the slot.
         * @param y       The y-coordinate of the slot.
         * @param crushed The crushed base course item.
         */
        public CrushedBaseCourseSlot(IItemHandler handler, int index, int x, int y, Item crushed) {
            super(handler, index, x, y);
            this.crushed = crushed;
        }

        /**
         * Determines if an item stack can be placed in this slot.
         *
         * @param stack The item stack to check.
         * @return True if the stack can be placed, false otherwise.
         */
        @Override
        public boolean mayPlace(ItemStack stack) {
            if (stack.isEmpty() || this.crushed == null)
                return false;
            return stack.is(this.crushed);
        }
    }
}
