package com.epherical.auctionworld.networking;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.UserManager;
import com.epherical.epherolib.networking.AbstractNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record UserSubmitBid(UUID listing, int bidAmount) {


    public static void handle(UserSubmitBid bid, AbstractNetworking.Context<?> context) {
        ServerPlayer player = context.getPlayer();
        if (player != null) {
            player.getServer().execute(() -> {
                AuctionTheWorldAbstract instance = AuctionTheWorldAbstract.getInstance();
                UserManager userManager = instance.getUserManager();
                instance.getAuctionManager(false).userBid(userManager.getUserByID(player.getUUID()), bid.listing, bid.bidAmount);
            });

        }
    }

}
