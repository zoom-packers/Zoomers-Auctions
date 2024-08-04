package com.epherical.auctionworld.container;

import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class DenseSimpleContainer implements Container, StackedContentsCompatible {

    private final int size;
    private final NonNullList<ItemStack> items;
    private List<ContainerListener> listeners;

    public DenseSimpleContainer(int size) {
        this.size = size;
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public DenseSimpleContainer(ItemStack... stacks) {
        this.size = stacks.length;
        this.items = NonNullList.of(ItemStack.EMPTY, stacks);
    }

    public void addListener(ContainerListener listener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }

        this.listeners.add(listener);
    }

    public void removeListener(ContainerListener listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }

    }

    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < this.items.size() ? this.items.get(slot) : ItemStack.EMPTY;
    }

    public List<ItemStack> removeAllItems() {
        List<ItemStack> list = this.items.stream().filter((item) -> {
            return !item.isEmpty();
        }).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    public ItemStack removeItem(int slot, int count) {
        ItemStack itemstack = ContainerHelper.removeItem(this.items, slot, count);
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    public ItemStack removeItemType(Item item, int count) {
        ItemStack itemstack = new ItemStack(item, 0);

        for(int i = this.size - 1; i >= 0; --i) {
            ItemStack itemstack1 = this.getItem(i);
            if (itemstack1.getItem().equals(item)) {
                int j = count - itemstack.getCount();
                ItemStack itemstack2 = itemstack1.split(j);
                itemstack.grow(itemstack2.getCount());
                if (itemstack.getCount() == count) {
                    break;
                }
            }
        }

        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    public ItemStack addItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemstack = stack.copy();
            this.moveItemToOccupiedSlotsWithSameType(itemstack);
            if (itemstack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                this.moveItemToEmptySlots(itemstack);
                return itemstack.isEmpty() ? ItemStack.EMPTY : itemstack;
            }
        }
    }

    public boolean canAddItem(ItemStack stack) {
        boolean flag = false;

        for(ItemStack itemstack : this.items) {
            if (itemstack.isEmpty() || ItemStack.isSameItemSameTags(itemstack, stack) && itemstack.getCount() < itemstack.getMaxStackSize()) {
                flag = true;
                break;
            }
        }

        return flag;
    }

    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack itemstack = this.items.get(slot);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.items.set(slot, ItemStack.EMPTY);
            return itemstack;
        }
    }

    public void setItem(int slot, ItemStack item) {
        this.items.set(slot, item);
        this.setChanged();
    }

    public int getContainerSize() {
        return this.size;
    }

    public boolean isEmpty() {
        for(ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void setChanged() {
        if (this.listeners != null) {
            for(ContainerListener containerlistener : this.listeners) {
                containerlistener.containerChanged(this);
            }
        }

    }

    public boolean stillValid(Player p_19167_) {
        return true;
    }

    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    public void fillStackedContents(StackedContents contents) {
        for(ItemStack itemstack : this.items) {
            contents.accountStack(itemstack);
        }

    }

    public String toString() {
        return this.items.stream().filter((itemStack) -> {
            return !itemStack.isEmpty();
        }).collect(Collectors.toList()).toString();
    }

    private void moveItemToEmptySlots(ItemStack stack) {
        for(int i = 0; i < this.size; ++i) {
            ItemStack itemstack = this.getItem(i);
            if (itemstack.isEmpty()) {
                this.setItem(i, stack.copyAndClear());
                return;
            }
        }

    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack stack) {
        for(int i = 0; i < this.size; ++i) {
            ItemStack itemstack = this.getItem(i);
            if (ItemStack.isSameItemSameTags(itemstack, stack)) {
                this.moveItemsBetweenStacks(stack, itemstack);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }

    }

    private void moveItemsBetweenStacks(ItemStack stack, ItemStack other) {
        int i = Math.min(this.getMaxStackSize(), other.getMaxStackSize());
        int j = Math.min(stack.getCount(), i - other.getCount());
        if (j > 0) {
            other.grow(j);
            stack.shrink(j);
            this.setChanged();
        }

    }

    public void fromTag(ListTag list) {
        this.clearContent();

        for(int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = ItemStack.of(list.getCompound(i));
            if (!itemstack.isEmpty()) {
                this.addItem(itemstack);
            }
        }

    }

    public ListTag createTag() {
        ListTag listtag = new ListTag();

        for(int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemstack = this.getItem(i);
            if (!itemstack.isEmpty()) {
                listtag.add(itemstack.save(new CompoundTag()));
            }
        }

        return listtag;
    }

}
