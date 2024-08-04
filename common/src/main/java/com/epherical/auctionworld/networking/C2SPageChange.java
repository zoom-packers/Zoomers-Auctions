package com.epherical.auctionworld.networking;

import com.epherical.auctionworld.AuctionManager;
import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.object.Page;
import com.epherical.auctionworld.object.User;
import com.epherical.epherolib.networking.AbstractNetworking;
import net.minecraft.server.level.ServerPlayer;

public record C2SPageChange(int newPage) {

    public static void handle(C2SPageChange listing, AbstractNetworking.Context<?> context) {
        ServerPlayer player = context.getPlayer();
        player.getServer().execute(() -> {
            AuctionTheWorldAbstract mod = AuctionTheWorldAbstract.getInstance();
            User user = mod.getUserManager().getUserByID(player.getUUID());
            int page = listing.newPage;
            if (listing.newPage <= 0) {
                page = 1;
            }
            AuctionManager aucManager = mod.getAuctionManager();
            user.setCurrentPage(new Page(page, 10));
            if (!AuctionTheWorldAbstract.client) {
                mod.getNetworking().sendToClient(
                        new S2CSendAuctionListings(
                                aucManager.getAuctionItemsByPage(user.getCurrentPage()),
                                aucManager.getMaxPages(user.getCurrentPage())
                        ), player);
            }

        });
    }

}
