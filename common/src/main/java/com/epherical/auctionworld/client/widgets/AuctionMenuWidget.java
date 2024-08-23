package com.epherical.auctionworld.client.widgets;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.client.screen.AuctionScreen;
import com.epherical.auctionworld.networking.OpenCreateAuction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AuctionMenuWidget {
    private Inventory inventory;
    private Component title;

    private Button createAuction;
    private Button browseAuction;
    private Button walletManagement;
    private Button auctionParticipation;
    private Button auctionOwner;
    private Button titleButton;


    public AuctionMenuWidget(Inventory inventory, Component title) {
        this.inventory = inventory;
        this.title = title;
    }

    public void init(AuctionScreen screen, int leftPos, int topPos, Font font) {
        createAuction = createButton(screen, leftPos, topPos, 0, "Create Auction", "Create a new auction listing", button -> {
            AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new OpenCreateAuction(CurrentScreen.CREATE_AUCTION));
        });

        browseAuction = createButton(screen, leftPos, topPos, 1, "Browse Auctions", "Browse all available auctions", button -> {
            AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new OpenCreateAuction(CurrentScreen.BROWSE_AUCTIONS));
        });

        walletManagement = createButton(screen, leftPos, topPos, 2, "Wallet", "See your wallet", button -> {
            AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new OpenCreateAuction(CurrentScreen.WALLET_MANAGEMENT));
        });

//        auctionParticipation = createButton(screen, leftPos, topPos, 3, "Auction Participation", "Participate in auctions", button -> {
//            AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new OpenCreateAuction(CurrentScreen.AUCTION_PARTICIAPTION));
//        });
//
//        auctionOwner = createButton(screen, leftPos, topPos, 4, "Auction Owner", "Manage your auctions", button -> {
//            AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new OpenCreateAuction(CurrentScreen.AUCTION_OWNER));
//        });
    }

    private Button createButton(AuctionScreen screen, int leftPos, int topPos, int index, String text, String tooltip, Button.OnPress onClick) {
        var button = createAuction = Button.builder(Component.literal(text), onClick)
                .pos(leftPos + 2, topPos + 2 + (index * 24))
                .size(120, 20)
                .tooltip(Tooltip.create(Component.literal(tooltip)))
                .build();
        screen.addRenderableWidgetExternal(button);
        return button;
    }



    public enum CurrentScreen {
        CREATE_AUCTION,
        BROWSE_AUCTIONS,
        WALLET_MANAGEMENT,
        AUCTION_PARTICIAPTION,
        AUCTION_OWNER,
    }
}
