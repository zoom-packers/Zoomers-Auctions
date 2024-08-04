package com.epherical.auctionworld.container;

import com.epherical.auctionworld.object.DelegatedContainer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DelegatedSimpleContainer implements Container {


    private DelegatedContainer container;

    public DelegatedSimpleContainer(DelegatedContainer container) {
        this.container = container;
        // TODO; fix the DelegatedContainer, it should REALLY just pull from a NonNullList
        //  and fill THESE methods out
    }


    @Override
    public int getContainerSize() {
        return container.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return container.getItem(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return container.removeItem(pSlot, pAmount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return container.removeItemNoUpdate(pSlot);
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        container.setItem(pSlot, pStack);
    }

    @Override
    public void setChanged() {
        container.setChanged();
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return container.stillValid(pPlayer);
    }

    @Override
    public void clearContent() {
        container.clearContent();
    }
}
