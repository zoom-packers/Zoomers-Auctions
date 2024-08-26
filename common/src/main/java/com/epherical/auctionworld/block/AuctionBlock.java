package com.epherical.auctionworld.block;

import com.epherical.auctionworld.AuctionManager;
import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.menu.BrowseAuctionMenu;
import com.epherical.auctionworld.networking.S2CSendAuctionListings;
import com.epherical.auctionworld.object.Page;
import com.epherical.auctionworld.object.User;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class AuctionBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static final VoxelShape SHAPE_BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    public static final VoxelShape SHAPE_POST = Block.box(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D);
    public static final VoxelShape SHAPE_COMMON = Shapes.or(SHAPE_BASE, SHAPE_POST);
    public static final VoxelShape SHAPE_TOP_PLATE = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 15.0D, 16.0D);
    public static final VoxelShape SHAPE_COLLISION = Shapes.or(SHAPE_COMMON, SHAPE_TOP_PLATE);
    public static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(3.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D), SHAPE_COMMON);
    public static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(0.0D, 12.0D, 3.0D, 16.0D, 16.0D, 16D), SHAPE_COMMON);
    public static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(0.0D, 12.0D, 0.0D, 13D, 16.0D, 16.0D), SHAPE_COMMON);
    public static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 13.0D), SHAPE_COMMON);
    private static final Component CONTAINER_TITLE = Component.translatable("BROWSE_AUCTIONS");

    public AuctionBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public VoxelShape getOcclusionShape(BlockState state, BlockGetter getter, BlockPos pos) {
        return SHAPE_COMMON;
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE_COLLISION;
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        ItemStack itemstack = context.getItemInHand();
        Player player = context.getPlayer();
        boolean flag = false;
        if (!level.isClientSide && player != null && player.canUseGameMasterBlocks()) {
            CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
            if (compoundtag != null && compoundtag.contains("Book")) {
                flag = true;
            }
        }

        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(level, pos));
            //player.awardStat(Stats.);
            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) -> {
            AuctionTheWorldAbstract instance = AuctionTheWorldAbstract.getInstance();
            AuctionManager manager = instance.getAuctionManager();
            User user = instance.getUserManager().getUserByID(player.getUUID());
            if (user.getLastReceivedAuctions() == null || user.getLastReceivedAuctions().isBefore(manager.getLastUpdated())) {
                instance.getNetworking().sendToClient(new S2CSendAuctionListings(manager.getAuctionItemsByPage(user.getCurrentPage()), manager.getMaxPages(user.getCurrentPage())), (ServerPlayer) player);
                user.setLastReceivedAuctions(Instant.now());
            }
            user.setCurrentPage(new Page(1, 10));
            return new BrowseAuctionMenu(id, inventory, user);
        }, CONTAINER_TITLE);
    }

    public VoxelShape getShape(BlockState p_54561_, BlockGetter p_54562_, BlockPos p_54563_, CollisionContext p_54564_) {
        return switch (p_54561_.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_COMMON;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
