package com.epherical.auctionworld.object;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.PlayerWallet;
import com.epherical.auctionworld.WalletEntry;
import com.epherical.auctionworld.config.Config;
import com.epherical.auctionworld.integrations.dcm.DotCoinModIntegration;
import com.epherical.auctionworld.networking.S2CWalletUpdate;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;

public class User implements DelegatedContainer {

    private Page currentPage = new Page(1, 10);

    private boolean saveData = true;
    private Instant lastReceivedAuctions;


    private final UUID uuid;
    private String name;
    @Nullable
    private transient ServerPlayer player;
    private Map<String, Integer> currencyMap = new HashMap<>();

    private NonNullList<ClaimedItem> claimedItems;

    public User(UUID uuid, String name) {
        this(uuid, name, NonNullList.create(), new HashMap<>(), Config.INSTANCE.startingCurrencies);
    }

    private User(UUID uuid, String name, NonNullList<ClaimedItem> items, Map<String, Integer> currencyMap, Integer[] startingCurrencies) {
        this.uuid = uuid;
        this.name = name;
        this.claimedItems = items;
        this.currencyMap = currencyMap;
        if (startingCurrencies != null) {
            for (int i = 0; i < startingCurrencies.length; i++) {
                this.currencyMap.put(Config.INSTANCE.currencies[i], startingCurrencies[i]);
            }
        }
    }


    public ServerPlayer getPlayer() {
        return player;
    }

    public void sendPlayerMessageIfOnline(Component component) {
        if (player != null) {
            player.sendSystemMessage(component);
        }
    }

    public void addWinnings(List<ItemStack> itemStacks, ClaimedItem.ClaimType claimType) {
        for (ItemStack itemStack : itemStacks) {
            ClaimedItem claimedItem = new ClaimedItem(claimType, itemStack);
            claimedItems.add(claimedItem);
        }
    }

    public String getName() {
        return name;
    }

    public static User loadUser(CompoundTag tag) {
        String name = tag.getString("name");
        UUID uuid = tag.getUUID("uuid");
        int currencies = tag.getInt("currencyCount");
        var currencyMap = new HashMap<String, Integer>();
        for (int i = 0; i < currencies; i++) {
            String currency = tag.getString("currency_" + i + "_id");
            int amount = tag.getInt("currency_" + i + "_amount");
            currencyMap.put(currency, amount);
        }
        NonNullList<ClaimedItem> items = NonNullList.create();
        loadAllItems(tag, items);

        // Remove unused keys
        var existingCurrenciesInWalletKeys = currencyMap.keySet();
        var keysToRemove = new ArrayList<String>();
        for (var key : existingCurrenciesInWalletKeys) {
            if (Arrays.stream(Config.INSTANCE.currencies).noneMatch(currency -> currency.equals(key))) {
                keysToRemove.add(key);
            }
        }
        for (var key : keysToRemove) {
            currencyMap.remove(key);
        }

        // Add new keys
        for (var currency: Config.INSTANCE.currencies) {
            if (!currencyMap.containsKey(currency)) {
                currencyMap.put(currency, 0);
            }
        }
        return new User(uuid, name, items, currencyMap, null);
    }

    public CompoundTag saveUser() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putUUID("uuid", uuid);
        tag.putInt("currencyCount", currencyMap.size());
        var index = 0;
        for (Map.Entry<String, Integer> entry : currencyMap.entrySet()) {
            tag.putString("currency_" + index + "_id", entry.getKey());
            tag.putInt("currency_" + index + "_amount", entry.getValue());
            index++;
        }

        saveAllItems(tag, claimedItems);
        return tag;
    }

    private static CompoundTag saveAllItems(CompoundTag tag, NonNullList<ClaimedItem> list) {
        return saveAllItems(tag, list, true);
    }

    private static CompoundTag saveAllItems(CompoundTag tag, NonNullList<ClaimedItem> list, boolean force) {
        ListTag listOfItems = new ListTag();

        for (ClaimedItem claimedItem : list) {
            if (!claimedItem.itemStack().isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                claimedItem.itemStack().save(itemTag);
                itemTag.putString("type", claimedItem.type().getSerializedName());
                listOfItems.add(itemTag);
            }
        }

        if (!listOfItems.isEmpty() || force) {
            tag.put("Items", listOfItems);
        }

        return tag;
    }


    private static void loadAllItems(CompoundTag compoundTag, NonNullList<ClaimedItem> nonNullList) {
        ListTag items = compoundTag.getList("Items", 10);

        for(int i = 0; i < items.size(); ++i) {
            CompoundTag slottedItem = items.getCompound(i);
            //int slot = slottedItem.getByte("Slot") & 255;
            ClaimedItem stack = new ClaimedItem(ClaimedItem.ClaimType.valueOf(slottedItem.getString("type").toUpperCase()), ItemStack.of(slottedItem));
            nonNullList.add(stack);
        }
    }



    @Override
    public int getContainerSize() {
        return claimedItems.size();
    }

    @Override
    public boolean isEmpty() {
        return claimedItems.isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        ItemStack itemstack = claimedItems.get(pSlot).itemStack();
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.claimedItems.remove(pSlot);
            return itemstack;
        }
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        if (pSlot == 0) {
            // we don't need to set the item i don't think...
        }

    }

    @Override
    public void setChanged() {
        // maybe do something here in the future

    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {
        this.claimedItems.clear();
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    public boolean emptyCurrency(ServerPlayer player, String currency) {
        int itemsToTake = Math.min(64, this.currencyMap.get(currency));
        var item = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(currency)));
        item.setCount(itemsToTake);
        this.currencyMap.put(currency, this.currencyMap.get(currency) + itemsToTake);
        if (!player.addItem(item)) {
            // re-add. This will take the remaining itemstack and put it back into storag
            this.currencyMap.put(currency, this.currencyMap.get(currency) + item.getCount());
            return false;
        }
        return true;
    }

    public int insertCurrency(ItemStack item) {
        if (DotCoinModIntegration.enabled) {
            DotCoinModIntegration.addCurrency(player, Config.getCurrencyForItem(item.getItem()), item.getCount());
            return 0;
        }
        if (!item.isEmpty()) {

            int itemsInserted = item.getCount();

            var itemResourceLocation = BuiltInRegistries.ITEM.getKey(item.getItem());
            if (!this.currencyMap.containsKey(itemResourceLocation.toString())) {
                this.currencyMap.put(itemResourceLocation.toString(), 0);
            }
            this.currencyMap.put(itemResourceLocation.toString(), this.currencyMap.get(itemResourceLocation.toString()) + itemsInserted);
            item.shrink(itemsInserted);
            return itemsInserted;
        }
        return 0;
    }

    public void setCurrentPage(Page currentPage) {
        this.currentPage = currentPage;
    }

    public Page getCurrentPage() {
        return currentPage;
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    public int getCurrencyAmount(String currency) {
        if (DotCoinModIntegration.enabled) {
            return DotCoinModIntegration.getCurrencyAmount(player, currency);
        }
        if (!this.currencyMap.containsKey(currency)) {
            this.currencyMap.put(currency, 0);
        }
        return this.currencyMap.get(currency);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void removeCurrency(String currency, int amountToTake) {
        if (DotCoinModIntegration.enabled) {
            if (player != null) {
                DotCoinModIntegration.removeCurrency(player, currency, amountToTake);
            } else {
                if (!this.currencyMap.containsKey(currency)) {
                    this.currencyMap.put(currency, 0);
                }
                this.currencyMap.put(currency, this.currencyMap.get(currency) - amountToTake);
            }
            return;
        }
        this.currencyMap.put(currency, this.currencyMap.get(currency) - amountToTake);
    }

    public void addCurrency(String currency, int amountToAdd) {
        if (DotCoinModIntegration.enabled) {
            if (player != null) {
                DotCoinModIntegration.addCurrency(player, currency, amountToAdd);
            } else {
                if (!this.currencyMap.containsKey(currency)) {
                    this.currencyMap.put(currency, 0);
                }
                this.currencyMap.put(currency, this.currencyMap.get(currency) + amountToAdd);
            }
            return;
        }
        this.currencyMap.put(currency, this.currencyMap.get(currency) + amountToAdd);
    }

    public NonNullList<ClaimedItem> getClaimedItems() {
        return claimedItems;
    }

    public void setLastReceivedAuctions(Instant lastReceivedAuctions) {
        this.lastReceivedAuctions = lastReceivedAuctions;
    }

    public Instant getLastReceivedAuctions() {
        return lastReceivedAuctions;
    }


    public void setSaveData(boolean saveData) {
        this.saveData = saveData;
    }

    public boolean canBeSaved() {
        return saveData;
    }

    public int getCurrencyInAuctions(String currency) {
        var auctionManager = AuctionTheWorldAbstract.getInstance().getAuctionManager(false);
        var auctions = auctionManager.getAuctions();
        int amount = 0;
        for (AuctionItem auction : auctions) {
            if (auction.getCurrency().equals(currency) && !auction.isExpired() && !auction.isBoughtOut()) {
                var bids = auction.getBidStack();
                var bidList = new ArrayList<>(bids);
                Collections.reverse(bidList);
                for (Bid bid : bidList) {
                    if (bid.user().equals(uuid)) {
                        amount += bid.bidAmount();
                        break;
                    }
                }
            }
        }
        return amount;
    }

    public void removeClaimedItems(ClaimedItem item) {
        claimedItems.remove(item);
    }

    public void sendWalletData() {
        if (player == null) {
            return;
        }
        var networking = AuctionTheWorldAbstract.getInstance().getNetworking();
        var wallet = new PlayerWallet();
        for (Map.Entry<String, Integer> entry : currencyMap.entrySet()) {
            var walletEntry = new WalletEntry(entry.getKey(), getCurrencyAmount(entry.getKey()), getCurrencyInAuctions(entry.getKey()));
            wallet.walletEntries.add(walletEntry);
        }
        networking.sendToClient(new S2CWalletUpdate(wallet), player);
    }

    public void onPlayerJoin() {
        AuctionTheWorldAbstract.LOGGER.info("Handling player join for " + name);
        if (DotCoinModIntegration.enabled) {
            // Re-add the currency to the player's inventory
            for (Map.Entry<String, Integer> entry : currencyMap.entrySet()) {
                var currency = entry.getKey();
                var amount = entry.getValue();
                if (amount > 0) {
                    DotCoinModIntegration.addCurrency(player, currency, amount);
                    currencyMap.put(currency, 0);
                    AuctionTheWorldAbstract.LOGGER.info("Added " + amount + " " + currency + " to " + player.getName().getString());
                } else if (amount < 0) {
                    DotCoinModIntegration.removeCurrency(player, currency, -amount);
                    currencyMap.put(currency, 0);
                    AuctionTheWorldAbstract.LOGGER.info("Removed " + -amount + " " + currency + " from " + player.getName().getString());
                }
            }
        }

        // Send the wallet data to the player
        sendWalletData();
    }

    public void onPlayerLeave() {
        lastReceivedAuctions = null;;
    }
}
