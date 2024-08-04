package com.epherical.auctionworld.fabric;

import com.epherical.auctionworld.AuctionManager;
import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.UserManager;
import com.epherical.auctionworld.command.ClaimCommand;
import com.epherical.auctionworld.data.FlatAuctionStorage;
import com.epherical.auctionworld.data.FlatPlayerStorage;
import dev.architectury.event.events.common.PlayerEvent;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.level.storage.LevelResource;

public class Events {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ClaimCommand.registerCommand(dispatcher);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            AuctionTheWorldAbstract.auctionStorage = new FlatAuctionStorage(LevelResource.ROOT, server, "epherical/auctiontw");
            AuctionTheWorldAbstract.playerStorage = new FlatPlayerStorage(LevelResource.ROOT, server, "epherical/auctiontw/players");
            AuctionTheWorldAbstract.userManager = new UserManager(AuctionTheWorldAbstract.playerStorage);
            AuctionTheWorldAbstract.auctionManager = new AuctionManager(AuctionTheWorldAbstract.auctionStorage, false, AuctionTheWorldAbstract.userManager);
            AuctionTheWorldAbstract.userManager.loadPlayers();
        });
//        ServerLevelSaveEvent

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            AuctionTheWorldAbstract.auctionManager.saveAuctionItems();
            AuctionTheWorldAbstract.auctionManager.stop();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (AuctionTheWorldAbstract.client) {
                AuctionTheWorldAbstract.auctionManager = new AuctionManager(null, true, null); // just in case for client players playing in SP then joining MP later?
            }
        });

        PlayerEvent.PLAYER_JOIN.register((player) -> {
            AuctionTheWorldAbstract.userManager.playerJoined(player);
        });

        PlayerEvent.PLAYER_QUIT.register((player) -> {
            AuctionTheWorldAbstract.userManager.playerLeft(player);
        });

    }

}
