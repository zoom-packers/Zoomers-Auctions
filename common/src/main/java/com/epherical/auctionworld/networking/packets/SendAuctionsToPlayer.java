package com.epherical.auctionworld.networking.packets;

import com.epherical.epherolib.networking.AbstractNetworking;

public record SendAuctionsToPlayer() {



    public static void handle(SendAuctionsToPlayer auctions, AbstractNetworking.Context<?> context) {

    }
}
