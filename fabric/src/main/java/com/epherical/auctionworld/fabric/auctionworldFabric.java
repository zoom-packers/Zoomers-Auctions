package com.epherical.auctionworld.fabric;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.epherolib.CommonPlatform;
import com.epherical.epherolib.FabricPlatform;
import com.epherical.epherolib.networking.FabricNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class auctionworldFabric implements ModInitializer {

    public static AuctionTheWorldAbstract abs;

    @Override
    public void onInitialize() {
        var networking = new FabricNetworking(AuctionTheWorldAbstract.MOD_CHANNEL,  FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT);
        abs = new AuctionTheWorldAbstract(networking);
        CommonPlatform.create(new FabricPlatform());
    }
}
