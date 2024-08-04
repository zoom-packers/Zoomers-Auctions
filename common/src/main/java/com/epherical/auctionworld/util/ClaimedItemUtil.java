package com.epherical.auctionworld.util;

import com.epherical.auctionworld.object.ClaimedItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ClaimedItemUtil {


    public static ItemStack removeItem(List<ClaimedItem> stacks, int index, int amount) {
        return index >= 0 && index < stacks.size() && !stacks.get(index).itemStack().isEmpty() && amount > 0 ? stacks.get(index).itemStack().split(amount) : ItemStack.EMPTY;
    }

}
