package com.epherical.auctionworld.client.screen;

import com.epherical.auctionworld.menu.AuctionOwnerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AuctionOwnerScreen extends AuctionScreen<AuctionOwnerMenu> {

    public AuctionOwnerScreen(AuctionOwnerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
