package com.epherical.auctionworld.command;

import com.epherical.auctionworld.AuctionManager;
import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.object.ClaimedItem;
import com.epherical.auctionworld.object.User;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ClaimCommand {

    public static void registerCommand(CommandDispatcher<CommandSourceStack> stack) {
        stack.register(Commands.literal("atw")
                .then(Commands.literal("claim")
                        .executes(ClaimCommand::listClaims)
                        .then(Commands.argument("claim", IntegerArgumentType.integer(1))
                                .executes(ClaimCommand::claimItem)))
                .then(Commands.literal("gen_auctions")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .executes(ClaimCommand::generateAuction)));
    }

    private static int generateAuction(CommandContext<CommandSourceStack> context) {
        AuctionTheWorldAbstract mod = AuctionTheWorldAbstract.getInstance();
        AuctionManager manager = mod.getAuctionManager();
        for (int i = 0; i < 10; i++) {
            try {
                RandomSource random = context.getSource().getPlayerOrException().getRandom();
                Optional<Holder.Reference<Item>> random1 = BuiltInRegistries.ITEM.getRandom(random);
                ItemStack stack = new ItemStack(random1.get().value(), 64);
                int randomPrice = random.nextInt(100);
                int randomBuyout = randomPrice + 30;
                User seller = mod.getUserManager().getUserByID(Util.NIL_UUID);
                manager.addAuctionItem(List.of(stack), Instant.now(), 120, randomPrice, randomBuyout, seller.getName(), seller.getUuid());
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return 1;
    }

    private static int claimItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int claim = IntegerArgumentType.getInteger(context, "claim") - 1;
        ServerPlayer player = context.getSource().getPlayerOrException();
        AuctionTheWorldAbstract mod = AuctionTheWorldAbstract.getInstance();
        User user = mod.getUserManager().getUserByID(player.getUUID());
        NonNullList<ClaimedItem> items = user.getClaimedItems();
        if (items.size() > claim) {
            ClaimedItem claimedItem = user.getClaimedItems().get(claim);
            // todo; write and validate

        }

        return 1;
    }

    private static int listClaims(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        AuctionTheWorldAbstract mod = AuctionTheWorldAbstract.getInstance();
        User userByID = mod.getUserManager().getUserByID(player.getUUID());
        Component component = Component.literal("Unclaimed Items");
        player.sendSystemMessage(component);
        int itemInc= 1;
        for (ClaimedItem claimedItem : userByID.getClaimedItems()) {
            MutableComponent claim = (Component.translatable("[%s]", itemInc++)
                    .withStyle(Style.EMPTY.withColor(claimedItem.type().getColor())
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(claimedItem.itemStack())))
                            ));
            claim.append(Component.literal(" "));
        }
        return 1;
    }
}
