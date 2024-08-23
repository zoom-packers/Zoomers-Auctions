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
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return true;
    }

}
