package com.epherical.auctionworld.client.screen;

import com.epherical.auctionworld.client.widgets.AuctionMenuBase;
import com.epherical.auctionworld.client.widgets.AuctionMenuWidget;
import com.epherical.auctionworld.menu.AuctionOwnerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AuctionOwnerScreen extends AuctionScreen<AuctionOwnerMenu> {

    public AuctionOwnerScreen(AuctionOwnerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
