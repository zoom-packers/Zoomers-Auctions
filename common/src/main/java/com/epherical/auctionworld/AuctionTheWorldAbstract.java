package com.epherical.auctionworld;

import com.epherical.auctionworld.client.widgets.AuctionMenuWidget;
import com.epherical.auctionworld.config.Config;
import com.epherical.auctionworld.data.AuctionStorage;
import com.epherical.auctionworld.data.PlayerStorage;
import com.epherical.auctionworld.integrations.dcm.DotCoinModIntegration;
import com.epherical.auctionworld.networking.*;
import com.epherical.auctionworld.registry.Registry;
import com.epherical.auctionworld.object.AuctionItem;
import com.epherical.epherolib.networking.AbstractNetworking;
import dev.architectury.platform.Platform;
import elocindev.necronomicon.api.config.v1.NecConfigAPI;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

//@Mod(Constants.MOD_ID)
public class AuctionTheWorldAbstract {

    public static final ResourceLocation MOD_CHANNEL = new ResourceLocation(Constants.MOD_ID, "packets");
    public static final Logger LOGGER = LoggerFactory.getLogger("auctionworld");

    public static boolean client = false;

    private static AuctionTheWorldAbstract mod;

    public static AbstractNetworking<?, ?> networking;
    public static AuctionStorage auctionStorage;
    public static PlayerStorage playerStorage;
    public static AuctionManager serverAuctionManager;
    public  static AuctionManager clientAuctionManager;
    public static UserManager userManager;
    public static PlayerWallet playerWallet;

    public static List<Runnable> auctionListeners = new ArrayList<>();

    public AuctionTheWorldAbstract(AbstractNetworking net) {
        mod = this;
        NecConfigAPI.registerConfig(Config.class);
        detectDotCoinAndEnableIntegration();
        networking = net;
        Registry.bootstrap();

        int id = 0;
        networking.registerClientToServer(id++, OpenCreateAuction.class,
                (createAuctionClick, friendlyByteBuf) -> {
                    friendlyByteBuf.writeInt(createAuctionClick.currentScreen().ordinal());
                },
                friendlyByteBuf -> new OpenCreateAuction((AuctionMenuWidget.CurrentScreen.values()[friendlyByteBuf.readInt()])), OpenCreateAuction::handle);
        networking.registerClientToServer(id++, CreateAuctionListing.class,
                (createAuctionListing, friendlyByteBuf) -> {
                    friendlyByteBuf.writeInt(createAuctionListing.timeInMinutes());
                    friendlyByteBuf.writeInt(createAuctionListing.startPrice());
                    friendlyByteBuf.writeInt(createAuctionListing.buyoutPrice());
                    friendlyByteBuf.writeUtf(createAuctionListing.currency());
                }, friendlyByteBuf -> new CreateAuctionListing(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readUtf()),
                CreateAuctionListing::handle);
        networking.registerClientToServer(id++, UserSubmitBid.class, (bid, friendlyByteBuf) -> {
            friendlyByteBuf.writeUUID(bid.listing());
            friendlyByteBuf.writeInt(bid.bidAmount());
        }, friendlyByteBuf -> new UserSubmitBid(friendlyByteBuf.readUUID(), friendlyByteBuf.readInt()), UserSubmitBid::handle);
        networking.registerClientToServer(id++, UserSubmitBuyout.class, (userSubmitBuyout, friendlyByteBuf) -> {
            friendlyByteBuf.writeUUID(userSubmitBuyout.listing());
        }, friendlyByteBuf -> new UserSubmitBuyout(friendlyByteBuf.readUUID()), UserSubmitBuyout::handle);
        networking.registerServerToClient(id++, S2CSendAuctionListings.class, (s2CSendAuctionListings, friendlyByteBuf) -> {
            serverAuctionManager.networkSerializeAuctions(friendlyByteBuf, s2CSendAuctionListings);
            friendlyByteBuf.writeInt(s2CSendAuctionListings.maxPages());
        }, friendlyByteBuf -> {
            List<AuctionItem> auctionItems = clientAuctionManager.networkDeserialize(friendlyByteBuf);
            int maxPages = friendlyByteBuf.readInt();
            return new S2CSendAuctionListings(auctionItems, maxPages);
        }, S2CSendAuctionListings::handle);
        networking.registerClientToServer(id++, C2SPageChange.class,
                (c2SPageChange, buf) -> buf.writeInt(c2SPageChange.newPage()),
                buf -> new C2SPageChange(buf.readInt()),
                C2SPageChange::handle);
        networking.registerServerToClient(id++, S2CAuctionUpdate.class,
                (s2CBidUpdate, buf) -> s2CBidUpdate.auctionItem().networkSerialize(buf),
                buf -> new S2CAuctionUpdate(AuctionItem.networkDeserialize(buf)),
                S2CAuctionUpdate::handle);
        networking.registerServerToClient(id++, S2CWalletUpdate.class,
                (s2CWalletUpdate, buf) -> s2CWalletUpdate.wallet().networkSerialize(buf),
                buf -> new S2CWalletUpdate(PlayerWallet.networkDeserialize(buf)),
                S2CWalletUpdate::handle);

        Events.register();
    }

    private void detectDotCoinAndEnableIntegration() {
        if (Platform.isModLoaded("dotcoinmod")) {
            DotCoinModIntegration.initDotcoinModIntegration();
        }
    }


    public static AuctionTheWorldAbstract getInstance() {
        return mod;
    }

    public AuctionManager getAuctionManager(boolean client) {
        return client ? clientAuctionManager : serverAuctionManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public AbstractNetworking<?, ?> getNetworking() {
        return networking;
    }

    public PlayerWallet getPlayerWallet() {
        return playerWallet;
    }

    public void setPlayerWallet(PlayerWallet playerWallet) {
        AuctionTheWorldAbstract.playerWallet = playerWallet;
    }
}
