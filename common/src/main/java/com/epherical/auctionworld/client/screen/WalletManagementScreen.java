package com.epherical.auctionworld.client.screen;

import com.epherical.auctionworld.client.widgets.AuctionMenuBase;
import com.epherical.auctionworld.client.widgets.AuctionMenuWidget;
import com.epherical.auctionworld.menu.WalletManagementMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WalletManagementScreen extends AuctionScreen<WalletManagementMenu> {

    public WalletManagementScreen(WalletManagementMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }
}
