package com.epherical.auctionworld.object;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.config.Config;
import com.epherical.auctionworld.util.UUIDUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class AuctionItem implements TooltipComponent {

    private UUID auctionID;

    private List<ItemStack> auctionItems;
    private transient int countOfItems;
    private Instant auctionStarted;
    private long timeLeft;
    private String currency;
    private int currentPrice;
    private final int buyoutPrice;
    private String seller;
    private UUID sellerID;
    private int minBidIncrement;
    private Stack<Bid> bidStack;
    private boolean boughtOut;



    public AuctionItem(UUID auctionID, String currency, List<ItemStack> auctionItems, Instant auctionStarted, long timeLeft, int currentPrice, int buyoutPrice,
                       String seller, UUID sellerID, Stack<Bid> bids) {
        this.auctionID = auctionID;
        this.currency = currency;
        this.auctionItems = auctionItems;
        this.auctionStarted = auctionStarted;
        this.timeLeft = timeLeft;
        this.currentPrice = currentPrice;
        this.buyoutPrice = buyoutPrice;
        this.seller = seller;
        this.sellerID = sellerID;
        this.bidStack = bids;
        //this.minBidIncrement = minBidIncrement;
    }

    public List<ItemStack> getAuctionItems() {
        return auctionItems;
    }

    public void setAuctionItems(List<ItemStack> auctionItems) {
        this.auctionItems = auctionItems;
    }

    public int getCountOfItems() {
        return countOfItems = auctionItems.stream().mapToInt(ItemStack::getCount).sum();
    }

    public void setCountOfItems(int countOfItems) {
        this.countOfItems = countOfItems;
    }

    public boolean isExpired() {
        // todo; check if it is expired.
        return timeLeft <= 0;
    }

    public String formatTimeLeft() {
        // todo; may not work
        long until = timeLeft;
        return String.format("%02dH:%02dM:%02dS",
                TimeUnit.SECONDS.toHours(until),
                TimeUnit.SECONDS.toMinutes(until) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(until)),
                TimeUnit.SECONDS.toSeconds(until) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(until)));
    }

    public Instant getAuctionStarted() {
        return auctionStarted;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public int getBuyoutPrice() {
        return buyoutPrice;
    }

    public String getSeller() {
        return seller;
    }

    public int getMinBidIncrement() {
        return minBidIncrement;
    }

    public UUID getAuctionID() {
        return auctionID;
    }

    public UUID getSellerID() {
        return sellerID;
    }

    public Stack<Bid> getBidStack() {
        return bidStack;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public boolean isBoughtOut() {
        return boughtOut;
    }

    public void addTime(long timeToAdd) {
        this.timeLeft += timeToAdd;
    }

    public void decrementTime() {
        this.timeLeft--;
    }

    public int getCurrentBidPrice() {
        return bidStack.isEmpty() ? currentPrice : bidStack.peek().bidAmount();
    }

    public void addBid(Bid bid) {
        bidStack.push(bid);
    }

    public void finishAuctionWithBuyOut(User user) {
        user.addWinnings(this.auctionItems, ClaimedItem.ClaimType.WON_LISTING);
        user.sendPlayerMessageIfOnline(Component.literal("You have bought out the auction for " + buyoutPrice + " " + Config.getAlias(currency)));
        rewardBackOtherPlayers(user);
        rewardAuctionSeller(buyoutPrice);
        timeLeft = 0;
        boughtOut = true;
        bidStack.clear();
    }

    public void finishAuction(Function<UUID, User> userGetter) {
        if (bidStack.isEmpty()) {
            User owner = userGetter.apply(this.sellerID);
            owner.addWinnings(this.auctionItems, ClaimedItem.ClaimType.EXPIRED_LISTING);
            owner.sendPlayerMessageIfOnline(Component.translatable("No one bid on your listing, so it has been returned to you."));
            return;
        }

        User winner = userGetter.apply(bidStack.peek().user());
        winner.addWinnings(this.auctionItems, ClaimedItem.ClaimType.WON_LISTING);
        winner.sendPlayerMessageIfOnline(Component.literal("You have won an auction for " + getCurrentBidPrice() + " " + Config.getAlias(currency)));
        rewardBackOtherPlayers(winner);
        rewardAuctionSeller(currentPrice);
    }

    private void rewardBackOtherPlayers(User winner) {
        List<User> awardedUsers = new ArrayList<>();
        awardedUsers.add(winner);
        for (Bid bid : bidStack) {
            User user = AuctionTheWorldAbstract.userManager.getUserByID(bid.user());
            if (awardedUsers.contains(user)) {
                continue;
            }
            user.addCurrency(currency, bid.bidAmount());
            var player = user.getPlayer();
            if (player != null) {
                user.sendPlayerMessageIfOnline(Component.literal("You have been refunded " + bid.bidAmount() + " " + Config.getAlias(currency) + " for the auction you bid on."));
            }
            awardedUsers.add(user);
        }
    }

    private void rewardAuctionSeller(int reward) {
        var seller = AuctionTheWorldAbstract.userManager.getUserByID(sellerID);
        seller.addCurrency(currency, reward);
        var player = seller.getPlayer();
        if (player != null) {
            seller.sendPlayerMessageIfOnline(Component.literal("Your auction has ended and you have been rewarded with " + reward + " " + Config.getAlias(currency)));
        }
    }


    public static Map<UUID, AuctionItem> loadAuctions(CompoundTag tag) {
        ListTag auctions = tag.getList("auctions", 10);
        Map<UUID, AuctionItem> auctionItems = new HashMap<>();
        for (Tag a : auctions) {
            CompoundTag auction = (CompoundTag) a;
            ListTag bidders = auction.getList("bids", 10);

            Stack<Bid> bids = new Stack<>();
            for (int i = 0; i < bidders.size(); i++) {
                CompoundTag compound = bidders.getCompound(i);
                bids.push(Bid.deserialize(compound));
            }

            AuctionItem auctionItem = new AuctionItem(
                    auction.getUUID("auctionId"),
                    auction.getString("currency"),
                    loadAllItems(auction),
                    Instant.ofEpochMilli(auction.getLong("startTime")),
                    auction.getLong("timeLeft"),
                    auction.getInt("currentPrice"),
                    auction.getInt("buyoutPrice"),
                    auction.getString("seller"),
                    UUIDUtils.loadUUID(auction.get("sellerId")),
                    bids);
            auctionItems.put(auctionItem.getAuctionID(), auctionItem);
        }

        return auctionItems;
    }

    public static CompoundTag saveAuctions(Map<UUID, AuctionItem> auctionItems) {
        ListTag auctions = new ListTag();
        CompoundTag allTag = new CompoundTag();
        for (AuctionItem auction : auctionItems.values()) {
            CompoundTag single = new CompoundTag();
            saveAllItems(single, auction.auctionItems);
            saveAllBids(single, auction.bidStack);
            single.putUUID("auctionId", auction.auctionID);
            single.putString("currency", auction.currency);
            single.putLong("startTime", auction.auctionStarted.toEpochMilli());
            single.putLong("timeLeft", auction.timeLeft);
            single.putInt("currentPrice", auction.currentPrice);
            single.putInt("buyoutPrice", auction.buyoutPrice);
            single.putString("seller", auction.seller);
            single.putUUID("sellerId", auction.sellerID);
            single.putUUID("auctionId", auction.auctionID);

            auctions.add(single);
        }
        allTag.put("auctions", auctions);
        return allTag;
    }

    private static CompoundTag saveAllBids(CompoundTag tag, Stack<Bid> bids) {
        ListTag bidList = new ListTag();
        bids.iterator().forEachRemaining(bid -> bidList.add(Bid.serialize(bid)));
        return tag;
    }

//    private static ListTag reverseListTag(ListTag listTag) {
//        ListTag reversed = new ListTag();
//        for (int i = listTag.size() - 1; i >= 0; i--) {
//            reversed.add(listTag.get(i));
//        }
//        return reversed;
//    }

    private static CompoundTag saveAllItems(CompoundTag tag, List<ItemStack> list) {
        ListTag listOfItems = new ListTag();

        for (ItemStack itemStack : list) {
            if (!itemStack.isEmpty()) {
                CompoundTag slottedItem = new CompoundTag();
                itemStack.save(slottedItem);
                listOfItems.add(slottedItem);
            }
        }
        tag.put("Items", listOfItems);

        return tag;
    }

    public static List<ItemStack> loadAllItems(CompoundTag compoundTag) {
        ListTag items = compoundTag.getList("Items", 10);
        List<ItemStack> itemStacks = new ArrayList<>();

        for (int i = 0; i < items.size(); ++i) {
            CompoundTag slottedItem = items.getCompound(i);

            ItemStack stack = ItemStack.of(slottedItem);
            itemStacks.add(stack);
        }

        return itemStacks;
    }

    public void networkSerialize(FriendlyByteBuf buf) {
        buf.writeUUID(auctionID);
        buf.writeUtf(currency);
        buf.writeInt(auctionItems.size());
        for (ItemStack item : auctionItems) {
            buf.writeItem(item);
        }
        buf.writeInstant(auctionStarted);
        buf.writeLong(timeLeft);
        buf.writeInt(getCurrentBidPrice());
        buf.writeInt(buyoutPrice);
        buf.writeUtf(seller);
        buf.writeUUID(sellerID);
        buf.writeInt(getCountOfItems());
        buf.writeInt(bidStack.size());
        for (int i = 0; i < bidStack.size(); i++) {
            buf.writeUUID(bidStack.get(i).user());
            buf.writeInt(bidStack.get(i).bidAmount());
        }
    }

    public static AuctionItem networkDeserialize(FriendlyByteBuf buf) {
        var auctionId = buf.readUUID();
        var currency = buf.readUtf();
        var itemCount = buf.readInt();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            items.add(buf.readItem());
        }
        var auctionStarted = buf.readInstant();
        var timeLeft = buf.readLong();
        var currentPrice = buf.readInt();
        var buyoutPrice = buf.readInt();
        var seller = buf.readUtf();
        var sellerId = buf.readUUID();
        var countOfItems = buf.readInt();
        var bidSize = buf.readInt();
        Stack<Bid> bids = new Stack<>();
        for (int i = 0; i < bidSize; i++) {
            bids.push(new Bid(buf.readUUID(), buf.readInt()));
        }
        AuctionItem auctionItem = new AuctionItem(auctionId, currency, items, auctionStarted, timeLeft, currentPrice, buyoutPrice, seller, sellerId, bids);
        auctionItem.setCountOfItems(countOfItems);
        return auctionItem;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuctionItem that = (AuctionItem) o;

        return Objects.equals(auctionID, that.auctionID);
    }

    @Override
    public int hashCode() {
        return auctionID != null ? auctionID.hashCode() : 0;
    }

    public String getCurrency() {
        return currency;
    }
}
