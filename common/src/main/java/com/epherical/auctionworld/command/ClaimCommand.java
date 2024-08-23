package com.epherical.auctionworld.command;

import com.epherical.auctionworld.AuctionManager;
import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.config.ConfigBasics;
import com.epherical.auctionworld.object.ClaimedItem;
import com.epherical.auctionworld.object.User;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ClaimCommand {

    public static void registerCommand(CommandDispatcher<CommandSourceStack> stack) {
        stack.register(Commands.literal("atw")
                .then(Commands.literal("wallet")
                        .then(Commands.literal("balance")
                                .executes(ClaimCommand::walletBalance))
                        .then(Commands.literal("withdraw")
                                .then(Commands.argument("currency", StringArgumentType.string())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ClaimCommand::withdrawBalance))))
                        .then(Commands.literal("deposit")
                                .executes(ClaimCommand::depositBalance)
                                .then(Commands.literal("claim")
                                        .executes(ClaimCommand::listClaims)
                                        .then(Commands.argument("claim", IntegerArgumentType.integer(1))
                                                .executes(ClaimCommand::claimItem)))
                                .then(Commands.literal("gen_auctions")
                                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                                        .executes(ClaimCommand::generateAuction))))
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
                manager.addAuctionItem("minecraft:raw_copper", List.of(stack), Instant.now(), 120, randomPrice, randomBuyout, seller.getName(), seller.getUuid());
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
        int itemInc = 1;
        for (ClaimedItem claimedItem : userByID.getClaimedItems()) {
            MutableComponent claim = (Component.translatable("[%s]", itemInc++)
                    .withStyle(Style.EMPTY.withColor(claimedItem.type().getColor())
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(claimedItem.itemStack())))
                    ));
            claim.append(Component.literal(" "));
        }
        return 1;
    }

    private static User getUser(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var mod = AuctionTheWorldAbstract.getInstance();
        return mod.getUserManager().getUserByID(player.getUUID());
    }

    private static int walletBalance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var user = getUser(context);
        var allCurrencies = ConfigBasics.INSTANCE.currencies;
        player.sendSystemMessage(Component.translatable("Wallet Balances:"));
        for (var currency : allCurrencies) {
            var balance = user.getCurrencyAmount(currency);
            player.sendSystemMessage(Component.translatable("%s: %s", currency, balance));
        }
        return 1;
    }

    private static int withdrawBalance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var user = getUser(context);
        var currency = context.getArgument("currency", String.class);
        var amount = context.getArgument("amount", Integer.class);

        var playerBalance = user.getCurrencyAmount(currency);
        if (playerBalance < amount) {
            player.sendSystemMessage(Component.translatable("You do not have enough %s to withdraw %s", currency, amount));
            player.sendSystemMessage(Component.translatable("You have %s %s", playerBalance, currency));
            return 1;
        }
        player.sendSystemMessage(Component.translatable("Withdrawing %s %s", amount, currency));
        user.takeCurrency(currency, amount);
        var item = ConfigBasics.getCurrencyItem(currency);
        var itemStack = new ItemStack(item, amount);
        giveItemToPlayer(context, itemStack);
        return 1;
    }

    private static int depositBalance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var user = getUser(context);
        var playerMainHand = player.getMainHandItem();
        var item = playerMainHand.getItem();
        var currency = ConfigBasics.getCurrencyForItem(item);
        if (!Arrays.stream(ConfigBasics.INSTANCE.currencies).toList().contains(currency)) {
            player.sendSystemMessage(Component.translatable("You cannot deposit this item as currency"));
            return 1;
        }
        var itemStack = new ItemStack(playerMainHand.getItemHolder(), playerMainHand.getCount());
        player.sendSystemMessage(Component.translatable("Depositing %s %s", playerMainHand.getCount(), currency));
        user.insertCurrency(itemStack);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return 1;
    }

    private static void giveItemToPlayer(CommandContext<CommandSourceStack> context, ItemStack stack) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
