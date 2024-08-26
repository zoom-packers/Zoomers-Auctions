package com.epherical.auctionworld.object;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record Bid(UUID user, String username, int bidAmount) {


    public static Bid deserialize(CompoundTag tag) {
        return new Bid(tag.getUUID("user"), tag.getString("name"), tag.getInt("amt"));
    }

    public static CompoundTag serialize(Bid bid) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("user", bid.user);
        tag.putString("name", bid.username);
        tag.putInt("amt", bid.bidAmount);
        return tag;
    }

}
