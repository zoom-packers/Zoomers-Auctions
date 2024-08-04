package com.epherical.auctionworld.menu;

import com.epherical.auctionworld.container.DenseSimpleContainer;
import com.epherical.auctionworld.registry.Registry;
import com.epherical.auctionworld.object.User;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BrowseAuctionMenu extends AbstractContainerMenu {


    public BrowseAuctionMenu(int id, Inventory inventory) {
        this(id, inventory, new DenseSimpleContainer(10));
    }

    public BrowseAuctionMenu(int id, Inventory inventory, Container container) {
        super(Registry.BROWSE_AUCTION_MENU, id);

        this.addSlot(new Slot(container, User.CURRENCY_SLOT, 362, 253) {
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player p_40228_) {
                return false;
            }
        });

        // 9 slots for auction winnings, expirations, commerce changes
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col){
                this.addSlot(new Slot(container, 1 + (col + row * 3), 13 + col * 18, 256 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack pStack) {
                        return false;
                    }
                });
            }
        }

        // player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int finalCol = col;
                int finalRow = row;
                this.addSlot(new Slot(inventory, finalCol + finalRow * 9 + 9, 176 + finalCol * 18, 258 + finalRow * 18) {

                    @Override
                    public boolean mayPickup(Player pPlayer) {
                        return false;
                    }
                });
            }
        }

        // player hotbar
        for (int row = 0; row < 9; ++row) {
            int finalRow = row;
            this.addSlot(new Slot(inventory, finalRow, 176 + row * 18, 316) {

                @Override
                public boolean mayPickup(Player pPlayer) {
                    return false;
                }
            });
        }

    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotP) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotP);
        if (slot != null && slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemstack = item.copy();
            if (slotP < 9) {
                if (!this.moveItemStackTo(item, 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(item, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (item.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return true;
    }

}
