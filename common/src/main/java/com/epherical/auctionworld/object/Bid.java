package com.epherical.auctionworld.object;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record Bid(UUID user, int bidAmount) {


    public static Bid deserialize(CompoundTag tag) {
        return new Bid(tag.getUUID("user"), tag.getInt("amt"));
    }

    public static CompoundTag serialize(Bid bid) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("user", bid.user);
        tag.putInt("amt", bid.bidAmount);
        return tag;
    }

}
