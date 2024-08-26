package com.epherical.auctionworld.networking;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.client.screen.BrowseAuctionScreen;
import com.epherical.auctionworld.object.AuctionItem;
import com.epherical.epherolib.networking.AbstractNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public record S2CAuctionUpdate(AuctionItem auctionItem) {


    public static void handle(S2CAuctionUpdate auctions, AbstractNetworking.Context<?> context) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            minecraft.player.sendSystemMessage(Component.literal("Auction " + auctions.auctionItem.getAuctionID() + " has been updated"));
            AuctionTheWorldAbstract.getInstance().getAuctionManager().updateAuctionItem(auctions.auctionItem);
            if (minecraft.screen != null && minecraft.screen instanceof BrowseAuctionScreen screen) {
                screen.reset();
            }
        });
        // dumb code, handled it in AuctionTheWorldForge...
    }

}
