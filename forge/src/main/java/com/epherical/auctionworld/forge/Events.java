package com.epherical.auctionworld.forge;

import com.epherical.auctionworld.AuctionManager;
import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.UserManager;
import com.epherical.auctionworld.command.ClaimCommand;
import com.epherical.auctionworld.data.AuctionFilterManager;
import com.epherical.auctionworld.data.FlatAuctionStorage;
import com.epherical.auctionworld.data.FlatPlayerStorage;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Events {


    public static final AuctionFilterManager manager = new AuctionFilterManager();



    @SubscribeEvent
    public static void onTagUpdate(TagsUpdatedEvent event) {
        TagsUpdatedEvent.UpdateCause updateCause = event.getUpdateCause();
        if (updateCause == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            event.getRegistryAccess().registry(Registries.ITEM).ifPresent(manager::updateFilter);
        }
    }

    @SubscribeEvent
    public static void commandRegisterEvent(RegisterCommandsEvent event) {
        ClaimCommand.registerCommand(event.getDispatcher());
    }

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        AuctionTheWorldAbstract.auctionStorage = new FlatAuctionStorage(LevelResource.ROOT, event.getServer(), "epherical/auctiontw");
        AuctionTheWorldAbstract.playerStorage = new FlatPlayerStorage(LevelResource.ROOT, event.getServer(), "epherical/auctiontw/players");
        AuctionTheWorldAbstract.userManager = new UserManager(AuctionTheWorldAbstract.playerStorage);
        AuctionTheWorldAbstract.auctionManager = new AuctionManager(AuctionTheWorldAbstract.auctionStorage, false, AuctionTheWorldAbstract.userManager);
        AuctionTheWorldAbstract.userManager.loadPlayers();
    }

    @SubscribeEvent
    public static void serverLevelSaveEvent(LevelEvent.Save event) {
        MinecraftServer server = event.getLevel().getServer();
        ServerLevel overWorld = server.overworld();
        if (overWorld.equals(event.getLevel())) {
            AuctionTheWorldAbstract.auctionManager.saveAuctionItems();
            AuctionTheWorldAbstract.userManager.saveAllPlayers();
        }
    }

    @SubscribeEvent
    public static void serverStoppingEvent(ServerStoppingEvent event) {
        AuctionTheWorldAbstract.auctionManager.saveAuctionItems();
        AuctionTheWorldAbstract.auctionManager.stop();
    }

    @SubscribeEvent
    public static void serverStoppedEvent(ServerStoppedEvent event) {
        if (AuctionTheWorldAbstract.client) {
            AuctionTheWorldAbstract.auctionManager = new AuctionManager(null, true, null); // just in case for client players playing in SP then joining MP later?
        }
    }

    @SubscribeEvent
    public static void playerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        AuctionTheWorldAbstract.userManager.playerJoined((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void playerLeaveEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        AuctionTheWorldAbstract.userManager.playerLeft((ServerPlayer) event.getEntity());
    }
}
