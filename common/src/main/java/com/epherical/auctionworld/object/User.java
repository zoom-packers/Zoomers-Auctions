package com.epherical.auctionworld.object;

import com.epherical.auctionworld.config.ConfigBasics;
import com.epherical.auctionworld.util.ClaimedItemUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class User implements DelegatedContainer {

    public static final int CURRENCY_SLOT = 0;


    private Page currentPage = new Page(1, 10);

    private boolean saveData = true;
    private Instant lastReceivedAuctions;


    private final UUID uuid;
    private String name;
    private int currencyAmount;
    @Nullable
    private transient ServerPlayer player;

    private NonNullList<ClaimedItem> claimedItems;

    // We can take this last known currency item, and if the item changes in the config
    // we can withdraw all the deposited currency from the block into some other block for
    // the player to withdraw.
    private Item lastKnownCurrencyItem;

    public User(UUID uuid, String name, int currency) {
        this(uuid, name, currency, NonNullList.create(), ConfigBasics.CURRENCY);
    }

    private User(UUID uuid, String name, int currency, NonNullList<ClaimedItem> items, Item lastKnownCurrencyItem) {
        this.uuid = uuid;
        this.name = name;
        this.currencyAmount = currency;
        this.claimedItems = items;
        this.lastKnownCurrencyItem = lastKnownCurrencyItem;
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
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("lastKnownItem")));
        int amount = tag.getInt("currencyAmount");
        String name = tag.getString("name");
        UUID uuid = tag.getUUID("uuid"); // LMAO if the NILUUID gets saved, this will fail, because sure.
        NonNullList<ClaimedItem> items = NonNullList.create();
        loadAllItems(tag, items);
        return new User(uuid, name, amount, items, item);
    }

    public CompoundTag saveUser() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("currencyAmount", currencyAmount);
        tag.putString("lastKnownItem", BuiltInRegistries.ITEM.getKey(lastKnownCurrencyItem).toString());
        tag.putString("name", name);
        tag.putUUID("uuid", uuid);
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
        if (pSlot == 0) {
            return new ItemStack(lastKnownCurrencyItem, currencyAmount);
        }
        if (pSlot > 0) {
            pSlot -= 1;
        }

        if (pSlot < claimedItems.size()) {
            if (claimedItems.get(pSlot).itemStack().isEmpty()) {
                claimedItems.remove(pSlot);
                return ItemStack.EMPTY;
            }
            return claimedItems.get(pSlot).itemStack();
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        if (pSlot == 0)  {
            currencyAmount -= pAmount;
            return new ItemStack(lastKnownCurrencyItem, currencyAmount).split(pAmount);
        }
        if (pSlot > 0) {
            pSlot -= 1;
        }


        ItemStack itemStack = ClaimedItemUtil.removeItem(claimedItems, pSlot, pAmount);
        if (!itemStack.isEmpty()) {
            this.setChanged();
            if (claimedItems.get(pSlot).itemStack().isEmpty()) {
                claimedItems.remove(pSlot);
            }
            return itemStack;
        }
        claimedItems.remove(pSlot);
        return itemStack;
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

    public boolean emptyCurrency(ServerPlayer player) {
        int itemsToTake = Math.min(64, currencyAmount);
        ItemStack currency = new ItemStack(lastKnownCurrencyItem);
        currency.setCount(itemsToTake);
        currencyAmount -= itemsToTake;
        if (!player.addItem(currency)) {
            // re-add. This will take the remaining itemstack and put it back into storage.
            currencyAmount += currency.getCount();
            return false;
        }
        return true;
    }

    public int insertCurrency(ItemStack item) {
        if (!item.isEmpty()) {

            int itemsInserted = item.getCount();

            this.currencyAmount += itemsInserted;
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

    public boolean hasEnough(int needed)  {
        return currencyAmount >= needed;
    }

    public int getCurrencyAmount() {
        return currencyAmount;
    }

    public Item getLastKnownCurrencyItem() {
        return lastKnownCurrencyItem;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void takeCurrency(int amountToTake) {
        this.currencyAmount -= amountToTake;
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
}
