package com.epherical.auctionworld.menu;

import com.epherical.auctionworld.registry.Registry;
import com.epherical.auctionworld.menu.slot.SelectableSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CreateAuctionMenu extends AbstractContainerMenu {

    private SelectableSlot firstSlot;

    public CreateAuctionMenu(int id, Inventory inventory) {
        super(Registry.CREATE_AUCTION_MENU, id);

        // player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(createSlot(inventory, col + row * 9 + 9, 176 + col * 18, 258 + row * 18));
            }
        }

        // player hotbar
        for (int row = 0; row < 9; ++row) {
            this.addSlot(createSlot(inventory, row, 176 + row * 18, 316));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public SelectableSlot getFirstSlot() {
        return firstSlot;
    }

    private SelectableSlot createSlot(Container container, int slot, int x, int y) {
        return new SelectableSlot(container, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack player) {
                boolean b = super.mayPlace(player);
                selectSlot();
                return b;
            }

            @Override
            public boolean mayPickup(Player player) {
                boolean b = super.mayPickup(player);
                selectSlot();
                return b;
            }

            private void selectSlot() {
                if (firstSlot == null) {
                    firstSlot = this;
                    setSelected(true);
                } else if (this.equals(firstSlot)) {
                    firstSlot = null;
                    setSelected(false);
                    for (Slot slot : slots) {
                        SelectableSlot mine = (SelectableSlot) slot;
                        mine.setSelected(false);
                    }
                } else {
                    ItemStack item = this.getItem();
                    if (ItemStack.isSameItemSameTags(item, firstSlot.getItem())) {
                        setSelected(!isSelected());
                    }
                }
            }
        };
    }
}
