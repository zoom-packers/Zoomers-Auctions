package com.epherical.auctionworld.menu;

import com.epherical.auctionworld.registry.Registry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AuctionOwnerMenu extends AbstractContainerMenu {

    public AuctionOwnerMenu(int i, Inventory inventory) {
        super(Registry.AUCTION_OWNER_MENU, i);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
