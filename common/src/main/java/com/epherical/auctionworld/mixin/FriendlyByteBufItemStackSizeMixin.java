package com.epherical.auctionworld.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufItemStackSizeMixin {


    @Shadow public abstract short readShort();

    @Redirect(remap = false, method = "writeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeByte(I)Lio/netty/buffer/ByteBuf;"))
    public ByteBuf auctionTheWorld$changeByteToShort(FriendlyByteBuf instance, int count) {
        instance.writeShort(count);
        return instance;
    }

    @Redirect(remap = false, method = "readItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readByte()B"))
    public byte auctionTheWorld$ignoreByteRead(FriendlyByteBuf instance) {
        return 0;
    }

    @Redirect(method = "readItem", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/ItemLike;I)Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack auctionTheWorld$readShortInsteadOfByte(ItemLike item, int oldCount) {
        return new ItemStack(item, readShort());
    }
}
