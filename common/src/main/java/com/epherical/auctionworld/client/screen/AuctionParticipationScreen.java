package com.epherical.auctionworld.client.screen;

import com.epherical.auctionworld.client.widgets.AuctionMenuBase;
import com.epherical.auctionworld.client.widgets.AuctionMenuWidget;
import com.epherical.auctionworld.menu.AuctionParticipationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AuctionParticipationScreen extends AuctionScreen<AuctionParticipationMenu> {

    public AuctionParticipationScreen(AuctionParticipationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
