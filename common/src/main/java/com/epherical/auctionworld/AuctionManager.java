package com.epherical.auctionworld;

import com.epherical.auctionworld.config.Config;
import com.epherical.auctionworld.data.AuctionStorage;
import com.epherical.auctionworld.networking.S2CAuctionUpdate;
import com.epherical.auctionworld.networking.S2CSendAuctionListings;
import com.epherical.auctionworld.object.AuctionItem;
import com.epherical.auctionworld.object.Bid;
import com.epherical.auctionworld.object.Page;
import com.epherical.auctionworld.object.User;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AuctionManager {

    private final Logger LOGGER = LogUtils.getLogger();

    private Instant lastUpdated;


    private AuctionStorage storage;
    private UserManager userManager;
    private Map<UUID, AuctionItem> auctions;
    private List<AuctionItem> auctionList;

    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;


    public AuctionManager(AuctionStorage storage, boolean client, UserManager userManager) {
        this.storage = storage;
        this.userManager = userManager;
        this.auctionList =new ArrayList<>();
        if (client) {
            this.auctions = new HashMap<>();
            future = service.scheduleAtFixedRate(() -> {
                if (!auctions.isEmpty()) {
                    List<UUID> expiredAuctions = new ArrayList<>();
                    for (Map.Entry<UUID, AuctionItem> entry : auctions.entrySet()) {
                        AuctionItem auction = entry.getValue();
                        auction.decrementTime();
                        if (auction.isExpired()) {
                            expiredAuctions.add(entry.getKey());
                            auctionList.remove(entry.getValue());
                        }
                    }
                    for (UUID expiredAuction : expiredAuctions) {
                        auctions.remove(expiredAuction);
                    }
                    if (!expiredAuctions.isEmpty()) {
                        for (Runnable auctionListener : AuctionTheWorldAbstract.auctionListeners) {
                            auctionListener.run();
                        }
                    }

                }
            }, 1L, 1L, TimeUnit.SECONDS);
        } else {
            this.auctions = storage.loadAuctionItems();
            this.auctionList = new ArrayList<>(auctions.values());
            this.lastUpdated = Instant.now();
            future = service.scheduleAtFixedRate(() -> {
                if (!auctions.isEmpty()) {
                    Instant now = Instant.now();
                    List<UUID> expiredAuctions = new ArrayList<>();
                    for (Map.Entry<UUID, AuctionItem> entry : auctions.entrySet()) {
                        AuctionItem auction = entry.getValue();
                        auction.decrementTime();
                        if (auction.isExpired()) {
                            if (!auction.isBoughtOut()) {
                                auction.finishAuction(this.userManager::getUserByID);
                            }
                            expiredAuctions.add(entry.getKey());
                            auctionList.remove(entry.getValue());
                        }
                    }
                    for (UUID expiredAuction : expiredAuctions) {
                        auctions.remove(expiredAuction);
                    }
                    if (!expiredAuctions.isEmpty()) {
                        for (Runnable auctionListener : AuctionTheWorldAbstract.auctionListeners) {
                            auctionListener.run();
                        }
                    }

                }
            }, 1L, 1L, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        if (future != null) {
            future.cancel(false);
        }
    }

    public void networkSerializeAuctions(FriendlyByteBuf byteBuf, S2CSendAuctionListings listings) {
        byteBuf.writeInt(listings.items().size());
        listings.items().forEach(item -> item.networkSerialize(byteBuf));
    }

    public void updateAuctionItem(AuctionItem item) {
        if (auctions.containsKey(item.getAuctionID())) {
            auctions.put(item.getAuctionID(), item);
            auctionList.remove(item);
            auctionList.add(item);
        }
    }

    // This method is called on the client.
    public List<AuctionItem> networkDeserialize(FriendlyByteBuf buf) {
        auctions.clear();
        auctionList.clear();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            AuctionItem auctionItem = AuctionItem.networkDeserialize(buf);
            auctions.put(auctionItem.getAuctionID(), auctionItem);
            auctionList.add(auctionItem);
        }
        return auctionList;
    }

    public void userBuyOut(User user, UUID auctionId) {
        AuctionItem auctionItem = getAuctionItem(auctionId);
        if (auctionItem == null) {
            return;
        }
        if (auctionItem.getSellerID().equals(user.getUuid())) {
            user.sendPlayerMessageIfOnline(Component.translatable("You can not bid on your own auction"));
            return;
        }
        if (auctionItem.isExpired()) {
            user.sendPlayerMessageIfOnline(Component.translatable("This auction has already expired"));
            return;
        }

        var requiredAmount = getRequiredAmountForBid(user, auctionItem, auctionItem.getBuyoutPrice());
        var difference = requiredAmount - user.getCurrencyAmount(auctionItem.getCurrency());
        if (difference > 0) {
            user.sendPlayerMessageIfOnline(Component.translatable("You do not have enough currency for this bid, you need to deposit" + difference + " more."));
            return;
        }

        Bid bid = new Bid(user.getUuid(), user.getName(), auctionItem.getBuyoutPrice());
        auctionItem.addBid(bid);

        user.removeCurrency(auctionItem.getCurrency(), requiredAmount);
        auctionItem.finishAuctionWithBuyOut(user);
        lastUpdated = Instant.now();
        user.sendWalletData();

        for (Runnable auctionListener : AuctionTheWorldAbstract.auctionListeners) {
            auctionListener.run();
        }
    }

    public AuctionItem getAuctionItem(UUID auctionId) {
        return auctions.get(auctionId);
    }

    public void userBid(User user, UUID auctionId, int bidAmount) {
        AuctionItem auctionItem = getAuctionItem(auctionId);
        if (auctionItem == null) {
            return;
        }
        if (auctionItem.getSellerID().equals(user.getUuid())) {
            user.sendPlayerMessageIfOnline(Component.translatable("You can not bid on your auction listing."));
            return;
        }
        if (auctionItem.isExpired()) {
            user.sendPlayerMessageIfOnline(Component.translatable("This auction listing has already expired."));
            return;
        }
        if (bidAmount <= auctionItem.getCurrentBidPrice()) {
            user.sendPlayerMessageIfOnline(Component.translatable("You did not bid enough money on the auction listing."));
            return;
        }

        var requiredAmount = getRequiredAmountForBid(user, auctionItem, bidAmount);
        var difference = requiredAmount - user.getCurrencyAmount(auctionItem.getCurrency());
        if (difference > 0) {
            user.sendPlayerMessageIfOnline(Component.translatable("You do not have enough currency for this bid, you need to deposit" + difference + " more."));
            return;
        }

        Bid bid = new Bid(user.getUuid(), user.getName(), bidAmount);
        user.removeCurrency(auctionItem.getCurrency(), requiredAmount);
        auctionItem.addBid(bid);
        auctionItem.addTime(Config.INSTANCE.addTimeAfterBid > -1 ? Config.INSTANCE.addTimeAfterBid : 0);
        lastUpdated = Instant.now();
        user.sendWalletData();


        sendMessagesToOutbiddedPlayers(auctionItem);
        sendNetworkUpdates(auctionItem);
        // TODO: Send Wallet Update
    }

    private int getRequiredAmountForBid(User user, AuctionItem auctionItem, int bidAmount) {
        var allBids = auctionItem.getBidStack();
        var copy = new Stack<Bid>();
        var allUserBids = allBids.stream().filter(bid -> bid.user().equals(user.getUuid()));
        copy.addAll(allUserBids.toList());
        if (copy.isEmpty()) {
            return bidAmount;
        } else {
            return bidAmount - copy.pop().bidAmount();
        }
    }

    private static void sendNetworkUpdates(AuctionItem auctionItem) {
        var playerMappings = AuctionTheWorldAbstract.getInstance().getUserManager().getPlayers();
        for (var us : playerMappings.values()) {
            var player = us.getPlayer();
            if (player != null) {
                AuctionTheWorldAbstract.getInstance().getNetworking().sendToClient(new S2CAuctionUpdate(auctionItem), player);
            }
        }
    }

    private void sendMessagesToOutbiddedPlayers(AuctionItem auctionItem) {
        Set<UUID> sentMessages = new HashSet<>();
        for (Bid previousBids : auctionItem.getBidStack()) {
            UUID previousID = previousBids.user();
            if (!sentMessages.contains(previousID)) {
                sentMessages.add(previousID);
                sendPlayerMessageIfOnline(previousBids.user(), Component.translatable("Someone has outbid you for item, %s", "BINGUS"));
            }
        }
    }

    public void sendPlayerMessageIfOnline(UUID uuid, Component message) {
        userManager.getUserByID(uuid).sendPlayerMessageIfOnline(message);
    }


    public void saveAuctionItems() {
        if (storage.saveAuctionItems(auctions)) {
            LOGGER.info("Saved All Auction Items");
        }
    }

    /**
     * Add a new auction item to the list, checking for existing auctions with the same UUID. If they have the same UUID, call the method again
     * with another random UUID being generated.
     */
    public void addAuctionItem(String currency, List<ItemStack> auctionItems, Instant auctionStarted, long timeLeft, int currentPrice, int buyoutPrice,
                               String seller, UUID sellerID) {
        UUID uuid = UUID.randomUUID();
        if (!auctions.containsKey(uuid)) {
            AuctionItem item = new AuctionItem(uuid, currency, auctionItems, auctionStarted, timeLeft, currentPrice, buyoutPrice, seller, sellerID, new Stack<>());
            auctions.put(uuid, item);
            auctionList.add(item);
        } else {
            addAuctionItem(currency, auctionItems, auctionStarted, timeLeft, currentPrice, buyoutPrice, seller, sellerID);
        }
        lastUpdated = Instant.now();
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<AuctionItem> getAuctions() {
        return auctionList;
    }

    public List<AuctionItem> getAuctionItemsByPage(Page currentPage) {
        if (currentPage.getPageOffset() > auctionList.size()) {
            return List.of();
        }
        return auctionList.subList(currentPage.getPageOffset(), Math.min(currentPage.getPagedItems(), auctionList.size()));
    }

    public int getMaxPages(Page page) {
        return auctionList.isEmpty() ? 1 : page.getMaxPages(auctionList.size());
    }

    public void setAuctionItems(List<AuctionItem> items) {
        this.auctionList.clear();
        this.auctions.clear();
        for (AuctionItem item : items) {
            this.auctionList.add(item);
            auctions.put(item.getAuctionID(), item);
        }
    }
}
