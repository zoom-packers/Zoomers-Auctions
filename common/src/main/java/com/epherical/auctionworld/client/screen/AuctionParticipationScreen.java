package com.epherical.auctionworld.client.screen;

import com.epherical.auctionworld.menu.AuctionParticipationMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AuctionParticipationScreen extends AuctionScreen<AuctionParticipationMenu> {

    public AuctionParticipationScreen(AuctionParticipationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
