package com.epherical.auctionworld.object;

import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

public record ClaimedItem(ClaimType type, ItemStack itemStack) {

    public enum ClaimType implements StringRepresentable {
        EXPIRED_LISTING("expired_listing", TextColor.parseColor("#f56642").getOrThrow()),
        WON_LISTING("won_listing", TextColor.parseColor("#a1f542").getOrThrow()),
        CURRENCY_CHANGE("currency_change", TextColor.parseColor("#43b5f7").getOrThrow());

        private final String type;
        private final TextColor color;

        ClaimType(String type, TextColor color) {
            this.type = type;
            this.color = color;
        }

        @Override
        public String getSerializedName() {
            return type;
        }


        public TextColor getColor() {
            return color;
        }
    }


}
