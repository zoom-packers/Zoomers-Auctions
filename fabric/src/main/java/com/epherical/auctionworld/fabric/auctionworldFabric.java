package com.epherical.auctionworld.fabric;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.Events;
import com.epherical.epherolib.CommonPlatform;
import com.epherical.epherolib.FabricPlatform;
import com.epherical.epherolib.networking.FabricNetworking;
import net.fabricmc.api.ModInitializer;

public final class auctionworldFabric implements ModInitializer {

    public static AuctionTheWorldAbstract abs;

    @Override
    public void onInitialize() {
        var networking = new FabricNetworking(AuctionTheWorldAbstract.MOD_CHANNEL, true);
        abs = new AuctionTheWorldAbstract(networking);
        CommonPlatform.create(new FabricPlatform());
    }
}
