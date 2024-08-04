package com.epherical.auctionworld.util;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;

import java.util.UUID;

public class UUIDUtils {

    public static UUID loadUUID(Tag tag) {
        int[] splitUUID;
        if (tag.getType() == ByteArrayTag.TYPE) {
           splitUUID = ((ByteArrayTag)tag).stream().mapToInt(ByteTag::getAsInt).toArray();
        } else {
            splitUUID = ((IntArrayTag)tag).getAsIntArray();
        }
        if (splitUUID.length != 4) {
            throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + splitUUID.length + ".");
        } else {
            return UUIDUtil.uuidFromIntArray(splitUUID);
        }
    }
}
