package net.airone.gandb;

import net.airone.gandb.EmptyLanternEntity;
import net.airone.gandb.GandB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;

public class EmptyLantern
extends BlockWithEntity
implements Waterloggable,
BlockEntityProvider {
    public static final BooleanProperty HANGING;
    public static final BooleanProperty WITH_MILK;
    public static final BooleanProperty FILLED;
    public static final BooleanProperty WATERLOGGED;
    protected static final VoxelShape STANDING_SHAPE;
    protected static final VoxelShape HANGING_SHAPE;

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(EmptyLantern::new);
    }

    public EmptyLantern(AbstractBlock.Settings settings) {
    super(settings);

    this.setDefaultState(
        this.getDefaultState()
            .with(HANGING, false)
            .with(WATERLOGGED, false)
            .with(FILLED, false)
            .with(WITH_MILK, false)
    );
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        Box box = new Box(pos).expand(10.0, 10.0, 10.0);
        List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
        StatusEffect effect = ((EmptyLanternEntity)world.getBlockEntity(pos)).getEffect();
        if (!state.isOf(newState.getBlock()) && effect != null) {
            for (PlayerEntity playerEnt : list) {
                playerEnt.removeStatusEffect(Registries.STATUS_EFFECT.getEntry(effect));
            }
            AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(world, (double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
            cloud.setDuration(100);
            cloud.setRadius(3.0f);
            cloud.addEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(effect), 60, 1));
            world.spawnEntity(cloud);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        RegistryEntry<StatusEffect>[] effectBlacklist = new RegistryEntry[]{StatusEffects.INSTANT_DAMAGE, StatusEffects.INSTANT_HEALTH};
        ItemStack item = player.getMainHandStack();

        if (
            item.isOf(Items.MILK_BUCKET)
            && !((Boolean)state.get(FILLED)).booleanValue()
            && !((Boolean)state.get(WITH_MILK)).booleanValue()
            ) {

            world.setBlockState(
                pos,
                state.with(WITH_MILK, true)
            );

            if (!player.isCreative()) {
                item.decrement(1);
                player.giveItemStack(new ItemStack(Items.BUCKET));
            }

            player.playSound(
                SoundEvents.ITEM_BUCKET_EMPTY,
                1.0f,
                1.0f
            );

            return ActionResult.success(world.isClient);
        }

        PotionContentsComponent potion = item.get(DataComponentTypes.POTION_CONTENTS);
        if (potion != null) {

            List<StatusEffectInstance> effects = new ArrayList<>();
            potion.getEffects().forEach(effects::add);

            if (!effects.isEmpty()) {

                StatusEffectInstance effect = effects.get(0);

                if (
                    item.isOf(Items.POTION)
                    && !((Boolean)state.get(FILLED)).booleanValue()
                    && !((Boolean)state.get(WITH_MILK)).booleanValue()
                    && !Arrays.stream(effectBlacklist).toList().contains(effect.getEffectType())
                ) {

                    world.setBlockState(pos, (BlockState)state.with(FILLED, Boolean.valueOf(true)));
                    world.updateListeners(pos, state, state, 3);

                    EmptyLanternEntity be =
                        (EmptyLanternEntity)world.getBlockEntity(pos);

                    be.setEffect(effect.getEffectType().value());
                    be.markDirty();

                    if (!player.isCreative()) {
                         item.decrement(1);
                        player.giveItemStack(new ItemStack(Items.GLASS_BOTTLE));
                    }

                    player.playSound(
                        SoundEvents.ENTITY_WITCH_DRINK,
                        1.0f,
                        1.0f
                    );

                    return ActionResult.success(world.isClient);
                }
            }

            if (
                item.isOf(Items.POTION)
                && effects.isEmpty()
                && (
                    ((Boolean)state.get(FILLED)).booleanValue()
                    || ((Boolean)state.get(WITH_MILK)).booleanValue()
                 )
            ) {

                 world.setBlockState(
                    pos,
                    (BlockState)state
                        .with(FILLED, Boolean.valueOf(false))
                        .with(WITH_MILK, Boolean.valueOf(false))
                );
                EmptyLanternEntity be =
                    (EmptyLanternEntity) world.getBlockEntity(pos);

                if (be != null) {
                    be.setEffect(null);
                }

                if (!player.isCreative()) {
                    item.decrement(1);
                    player.giveItemStack(new ItemStack(Items.GLASS_BOTTLE));
                }

                player.playSound(
                    SoundEvents.ENTITY_WITCH_DRINK,
                    1.0f,
                    1.0f
                );

                return ActionResult.success(world.isClient);
            }
        }
        return ActionResult.PASS;
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EmptyLanternEntity(pos, state);
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return type == GandB.EMPTY_LANTERN_ENTITY ? (BlockEntityTicker<T>)((world1, pos1, state1, be) -> EmptyLanternEntity.tick(world1, pos1, state1, (EmptyLanternEntity)be)) : null;
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        for (Direction direction : ctx.getPlacementDirections()) {
            BlockState blockState;
            if (direction.getAxis() != Direction.Axis.Y || !(blockState = this.getDefaultState().with(HANGING, Boolean.valueOf(direction == Direction.UP))).canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) continue;
            return blockState.with(WATERLOGGED, Boolean.valueOf(fluidState.getFluid() == Fluids.WATER));
        }
        return null;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return (Boolean)state.get(HANGING) != false ? HANGING_SHAPE : STANDING_SHAPE;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{HANGING, WATERLOGGED, FILLED, WITH_MILK});
    }

    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = EmptyLantern.attachedDirection(state).getOpposite();
        return Block.sideCoversSmallSquare(world, pos.offset(direction), direction.getOpposite());
    }

    protected static Direction attachedDirection(BlockState state) {
        return (Boolean)state.get(HANGING) != false ? Direction.DOWN : Direction.UP;
    }

    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (((Boolean)state.get(WATERLOGGED)).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return EmptyLantern.attachedDirection(state).getOpposite() == direction && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.get(WATERLOGGED) != false ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    static {
        WITH_MILK = BooleanProperty.of("with_milk");
        FILLED = BooleanProperty.of("filled");
        HANGING = Properties.HANGING;
        WATERLOGGED = Properties.WATERLOGGED;
        STANDING_SHAPE = VoxelShapes.union(Block.createCuboidShape((double)5.0, (double)0.0, (double)5.0, (double)11.0, (double)7.0, (double)11.0), Block.createCuboidShape((double)6.0, (double)7.0, (double)6.0, (double)10.0, (double)9.0, (double)10.0));
        HANGING_SHAPE = VoxelShapes.union(Block.createCuboidShape((double)5.0, (double)1.0, (double)5.0, (double)11.0, (double)8.0, (double)11.0), Block.createCuboidShape((double)6.0, (double)8.0, (double)6.0, (double)10.0, (double)10.0, (double)10.0));
    }
}

