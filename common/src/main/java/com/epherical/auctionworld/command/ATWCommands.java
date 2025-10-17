package com.epherical.auctionworld.command;

import com.epherical.auctionworld.AuctionManager;
import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.block.AuctionBlock;
import com.epherical.auctionworld.config.Config;
import com.epherical.auctionworld.menu.BrowseAuctionMenu;
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
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ATWCommands {

    public static void registerCommand(CommandDispatcher<CommandSourceStack> stack) {
        stack.register(Commands.literal("auctions")
                .then(Commands.literal("open")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(4) || !Config.INSTANCE.disableOpenGuiCommandForNonAdmins)
                    .executes(ATWCommands::openMenu))
                .then(Commands.literal("wallet")
                        .executes(ATWCommands::walletBalance)
                        .then(Commands.literal("balance")
                                .executes(ATWCommands::walletBalance))
                        .then(Commands.literal("withdraw")
                                .then(Commands.argument("currency", StringArgumentType.string())
                                        .then(Commands.argument("available", IntegerArgumentType.integer(1))
                                                .executes(ATWCommands::withdrawBalance))))
                        .then(Commands.literal("deposit")
                                .executes(ATWCommands::depositBalance)
                                .then(Commands.literal("claim")
                                        .executes(ATWCommands::listClaims)
                                        .then(Commands.argument("claim", IntegerArgumentType.integer(1))
                                                .executes(ATWCommands::claimItems)))
                                .then(Commands.literal("gen_auctions")
                                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                                        .executes(ATWCommands::generateAuction))))
                .then(Commands.literal("claim")
                        .executes(ATWCommands::claimItems))
                .then(Commands.literal("gen_auctions")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .executes(ATWCommands::generateAuction)));
    }

    private static int openMenu(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var mod = AuctionTheWorldAbstract.getInstance();
        var userManager = mod.getUserManager();
        var user = userManager.getUserByID(player.getUUID());
        user.sendWalletData();
        player.openMenu(AuctionBlock.getMenuProviderGlobal());
        return 1;
    }

    private static int generateAuction(CommandContext<CommandSourceStack> context) {
        AuctionTheWorldAbstract mod = AuctionTheWorldAbstract.getInstance();
        AuctionManager manager = AuctionTheWorldAbstract.serverAuctionManager;
        for (int i = 0; i < 10; i++) {
            try {
                RandomSource random = context.getSource().getPlayerOrException().getRandom();
                Optional<Holder.Reference<Item>> random1 = BuiltInRegistries.ITEM.getRandom(random);
                ItemStack stack = new ItemStack(random1.get().value(), 64);
                int randomPrice = random.nextInt(100);
                int randomBuyout = randomPrice + 30;
                User seller = mod.getUserManager().getUserByID(Util.NIL_UUID);
                manager.addAuctionItem(Config.INSTANCE.currencies[0], List.of(stack), Instant.now(), 120, randomPrice, randomBuyout, seller.getName(), seller.getUuid());
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return 1;
    }

    private static int claimItems(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        AuctionTheWorldAbstract mod = AuctionTheWorldAbstract.getInstance();
        User user = mod.getUserManager().getUserByID(player.getUUID());
        NonNullList<ClaimedItem> items = user.getClaimedItems();
        if (items.isEmpty()) {
            player.sendSystemMessage(Component.translatable("You have no items to claim from auctions"));
            return 1;
        }
        for (ClaimedItem item : items) {
            giveItemToPlayer(context, item.itemStack());
            player.sendSystemMessage(Component.translatable("Claiming %s from auctions", item.itemStack().getHoverName()));
        }
        items.clear();
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
        var allCurrencies = Config.INSTANCE.currencies;
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
        var currencyText = context.getArgument("currency", String.class).toLowerCase();
        var amount = context.getArgument("available", Integer.class);

        boolean foundCurrency = false;
        String currency = null;
        for (var c : Config.INSTANCE.currencies) {
            if (c.toLowerCase().equals(currencyText)) {
                foundCurrency = true;
                currency = c;
                break;
            }
            if (Config.getAlias(c).toLowerCase().equals(currencyText)) {
                foundCurrency = true;
                currency = c;
                break;
            }
        }

        if (!foundCurrency) {
            player.sendSystemMessage(Component.translatable("Invalid currency"));
            return 1;
        }

        var playerBalance = user.getCurrencyAmount(currency);
        if (playerBalance < amount) {
            player.sendSystemMessage(Component.translatable("You do not have enough %s to withdraw %s", currency, amount));
            player.sendSystemMessage(Component.translatable("You have %s %s", playerBalance, currency));
            return 1;
        }
        player.sendSystemMessage(Component.translatable("Withdrawing %s %s", amount, currency));
        user.removeCurrency(currency, amount);
        user.sendWalletData();
        var item = Config.getCurrencyItem(currency);
        var itemStack = new ItemStack(item, amount);
        giveItemToPlayer(context, itemStack);
        return 1;
    }

    private static int depositBalance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var user = getUser(context);
        var playerMainHand = player.getMainHandItem();
        var item = playerMainHand.getItem();
        var currency = Config.getCurrencyForItem(item);
        if (!Arrays.stream(Config.INSTANCE.currencies).toList().contains(currency)) {
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
