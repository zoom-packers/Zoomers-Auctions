package com.epherical.auctionworld;

import com.epherical.auctionworld.command.ATWCommands;
import com.epherical.auctionworld.data.FlatAuctionStorage;
import com.epherical.auctionworld.data.FlatPlayerStorage;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.world.level.storage.LevelResource;

public class Events {

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> {
            ATWCommands.registerCommand(dispatcher);
        });
        LifecycleEvent.SERVER_STARTED.register(server -> {
            AuctionTheWorldAbstract.auctionStorage = new FlatAuctionStorage(LevelResource.ROOT, server, "epherical/auctiontw");
            AuctionTheWorldAbstract.playerStorage = new FlatPlayerStorage(LevelResource.ROOT, server, "epherical/auctiontw/players");
            AuctionTheWorldAbstract.userManager = new UserManager(AuctionTheWorldAbstract.playerStorage);
            AuctionTheWorldAbstract.serverAuctionManager = new AuctionManager(AuctionTheWorldAbstract.auctionStorage, false, AuctionTheWorldAbstract.userManager);
            AuctionTheWorldAbstract.userManager.loadPlayers();
        });

        LifecycleEvent.SERVER_STOPPING.register(server -> {
            AuctionTheWorldAbstract.serverAuctionManager.saveAuctionItems();
            AuctionTheWorldAbstract.serverAuctionManager.stop();
        });

        LifecycleEvent.SERVER_STOPPED.register(server -> {
            if (AuctionTheWorldAbstract.client) {
                AuctionTheWorldAbstract.serverAuctionManager = new AuctionManager(null, false, null); // just in case for client players playing in SP then joining MP later?
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
