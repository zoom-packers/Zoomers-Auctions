package com.epherical.auctionworld.networking;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.menu.BrowseAuctionMenu;
import com.epherical.auctionworld.object.Action;
import com.epherical.auctionworld.object.User;
import com.epherical.epherolib.networking.AbstractNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public record SlotManipulation(int slot, Action action) {
    public static void handle(SlotManipulation slotManipulation, AbstractNetworking.Context<?> context) {
        ServerPlayer player = context.getPlayer();
        if (player != null) {
            player.getServer().execute(() -> {
                if (player.containerMenu instanceof BrowseAuctionMenu) {
                    User userByID = AuctionTheWorldAbstract.getInstance().getUserManager().getUserByID(player.getUUID());
                    switch (slotManipulation.action()) {
                        case INSERT_SLOT -> slotManipulation.insertSingleSlot(player, userByID);
                        case INSERT_ALL -> slotManipulation.insertAll(player, userByID);
                        case REMOVE_ALL -> slotManipulation.removeAllSlots(player, userByID);
                        case REMOVE_STACK -> slotManipulation.removeSingleSlot(player, userByID);
                    }
                }
            });
        }
    }

    private void insertSingleSlot(ServerPlayer player, User blockEntity) {
        insertSingleSlotAbstract(player, blockEntity);
    }

    private void insertSingleSlotAbstract(ServerPlayer player, User blockEntity) {
        if (this.slot == User.CURRENCY_SLOT) {
            // todo; config option for items.diamond
            int slotMatchingItem = player.getInventory().findSlotMatchingItem(new ItemStack(Items.DIAMOND));
            if (slotMatchingItem != -1) {
                blockEntity.insertCurrency(player.getInventory().getItem(slotMatchingItem));
            }
        }
    }

    private void insertAll(ServerPlayer player, User blockEntity) {
        abstractInsertAll(player, blockEntity);
    }

    private void abstractInsertAll(ServerPlayer player, User blockEntity) {
        if (this.slot == User.CURRENCY_SLOT) {
            for (ItemStack item : player.getInventory().items) {
                // todo; config option for items.diamond
                if (!item.isEmpty() && ItemStack.isSameItemSameTags(item, new ItemStack(Items.DIAMOND))) {
                    blockEntity.insertCurrency(item);
                }
            }
        }
    }

    private void removeSingleSlot(ServerPlayer player, User blockEntity) {
        if (this.slot == User.CURRENCY_SLOT) {
            blockEntity.emptyCurrency(player);
        }
    }

    private void removeAllSlots(ServerPlayer player, User blockEntity) {
        if (this.slot == User.CURRENCY_SLOT) {
            while (blockEntity.emptyCurrency(player));
        }
    }
}
