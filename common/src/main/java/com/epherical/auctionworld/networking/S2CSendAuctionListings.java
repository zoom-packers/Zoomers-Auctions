package com.epherical.auctionworld.networking;

import com.epherical.auctionworld.client.AModClient;
import com.epherical.auctionworld.client.screen.BrowseAuctionScreen;
import com.epherical.auctionworld.object.AuctionItem;
import com.epherical.epherolib.networking.AbstractNetworking;
import net.minecraft.client.Minecraft;

import java.util.List;

public record S2CSendAuctionListings(List<AuctionItem> items, int maxPages) {

    public static void handle(S2CSendAuctionListings auctions, AbstractNetworking.Context<?> context) {
        Minecraft minecraft = Minecraft.getInstance();
        AModClient.maxPages = auctions.maxPages;
        minecraft.execute(() -> {
            if (minecraft.screen != null && minecraft.screen instanceof BrowseAuctionScreen screen) {
                screen.reset();
            }
        });
        // dumb code, handled it in AuctionTheWorldForge...
    }
}
