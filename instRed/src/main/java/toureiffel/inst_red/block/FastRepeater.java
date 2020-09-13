package toureiffel.inst_red.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import toureiffel.inst_red.InstRedMod;



public class FastRepeater extends HorizontalBlock {

	protected static final VoxelShape SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
	
	public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;
	public static final IntegerProperty DELAY = IntegerProperty.create("delay", 0, 3);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public FastRepeater(Properties properties) {
		super(properties);
		this.setDefaultState(stateContainer.getBaseState().with(HORIZONTAL_FACING, Direction.NORTH)
				.with(DELAY, Integer.valueOf(0)).with(LOCKED, Boolean.valueOf(false)).with(POWERED, Boolean.valueOf(false)));
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return hasSolidSideOnTop(worldIn, pos.down());
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity entity, Hand hand, BlockRayTraceResult result) {
		if (!entity.abilities.allowEdit) {
			return ActionResultType.PASS;
		} else {
			worldIn.setBlockState(pos, state.func_235896_a_(DELAY), 3);
			return ActionResultType.SUCCESS;
		}
	}
	

	protected int getDelay(BlockState state) {
		return state.get(DELAY);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState blockstate = this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
		return blockstate.with(LOCKED, Boolean.valueOf(this.isLocked(context.getWorld(), context.getPos(), blockstate)));
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (this.shouldBePowered(worldIn, pos, state)) {
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return !worldIn.isRemote() && facing.getAxis() != stateIn.get(HORIZONTAL_FACING).getAxis() ? stateIn.with(LOCKED, Boolean.valueOf(this.isLocked(worldIn, currentPos, stateIn))) : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
	
	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.getWeakPower(blockAccess, pos, side);
	}
	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (!blockState.get(POWERED)) {
			return 0;
		} else {
			return blockState.get(HORIZONTAL_FACING) == side ? this.getActiveSignal(blockAccess, pos, blockState) : 0;
		}
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if (state.isValidPosition(worldIn, pos)) {
			this.updateState(worldIn, pos, state);
		} else {
			TileEntity tileentity = state.hasTileEntity() ? worldIn.getTileEntity(pos) : null;
			spawnDrops(state, worldIn, pos, tileentity);
			worldIn.removeBlock(pos, false);

			for(Direction direction : Direction.values()) {
				worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
			}

		}
	}

	protected void updateState(World worldIn, BlockPos pos, BlockState state) {
		if (!this.isLocked(worldIn, pos, state)) {
			boolean shouldBePowered = this.shouldBePowered(worldIn, pos, state);
			if (state.get(POWERED) != shouldBePowered) {
				if(state.get(DELAY) == 0) {
					worldIn.setBlockState(pos, state.with(POWERED, shouldBePowered), 2);
				} else {
					TickPriority tickpriority = TickPriority.HIGH;
					if (this.isFacingTowardsRepeater(worldIn, pos, state)) {
						tickpriority = TickPriority.EXTREMELY_HIGH;
					} else if (state.get(POWERED)) {
						tickpriority = TickPriority.VERY_HIGH;
					}

					worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.getDelay(state), tickpriority);
				}
			}

		}
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (!this.isLocked(worldIn, pos, state)) {
			boolean powered = state.get(POWERED);
			boolean shouldBePowered = this.shouldBePowered(worldIn, pos, state);
			if (powered && !shouldBePowered) {
				worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(false)), 2);
			} else if (!powered) {
				worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(true)), 2);
				if (!shouldBePowered) {
					worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.getDelay(state), TickPriority.VERY_HIGH);
				}
			}
		}
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		this.notifyNeighbors(worldIn, pos, state);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!isMoving && state.getBlock() != newState.getBlock()) {
			super.onReplaced(state, worldIn, pos, newState, isMoving);
			this.notifyNeighbors(worldIn, pos, state);
		}
	}

	protected void notifyNeighbors(World worldIn, BlockPos pos, BlockState state) {
		Direction direction = state.get(HORIZONTAL_FACING);
		BlockPos blockpos = pos.offset(direction.getOpposite());
		if (net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(worldIn, pos, worldIn.getBlockState(pos), java.util.EnumSet.of(direction.getOpposite()), false).isCanceled())
			return;
		worldIn.neighborChanged(blockpos, this, pos);
		worldIn.notifyNeighborsOfStateExcept(blockpos, this, direction);
	}

	public boolean isLocked(IWorldReader worldIn, BlockPos pos, BlockState state) {
		return getPowerOnSides(worldIn, pos, state) > 0;
	}

	protected boolean shouldBePowered(World worldIn, BlockPos pos, BlockState state) {
		return this.calculateInputStrength(worldIn, pos, state) > 0;
	}

	protected int calculateInputStrength(World worldIn, BlockPos pos, BlockState state) {
		Direction direction = state.get(HORIZONTAL_FACING);
		BlockPos blockpos = pos.offset(direction);
		int i = worldIn.getRedstonePower(blockpos, direction);
		if (i >= 15) {
			return i;
		} else {
			BlockState blockstate = worldIn.getBlockState(blockpos);
			return Math.max(i, blockstate.getBlock() == Blocks.REDSTONE_WIRE ? blockstate.get(RedstoneWireBlock.POWER) : 0);
		}
	}

	protected int getPowerOnSides(IWorldReader worldIn, BlockPos pos, BlockState state) {
		Direction direction = state.get(HORIZONTAL_FACING);
		Direction direction1 = direction.rotateY();
		Direction direction2 = direction.rotateYCCW();
		return Math.max(this.getPowerOnSide(worldIn, pos.offset(direction1), direction1), this.getPowerOnSide(worldIn, pos.offset(direction2), direction2));
	}

	protected int getPowerOnSide(IWorldReader worldIn, BlockPos pos, Direction side) {
		BlockState blockstate = worldIn.getBlockState(pos);
		Block block = blockstate.getBlock();
		if (this.isAlternateInput(blockstate)) {
			if (block == Blocks.REDSTONE_BLOCK) {
				return 15;
			} else {
				return block == Blocks.REDSTONE_WIRE ? blockstate.get(RedstoneWireBlock.POWER) : worldIn.getStrongPower(pos, side);
			}
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}
	
	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader worldIn, BlockPos pos, Direction direction) {
		return state.get(HORIZONTAL_FACING) == direction || state.get(HORIZONTAL_FACING).getOpposite() == direction;
	}

	protected boolean isAlternateInput(BlockState state) {
		return isDiode(state);
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, DELAY, LOCKED, POWERED);
	}

	protected int getActiveSignal(IBlockReader worldIn, BlockPos pos, BlockState state) {
		return 15;
	}

	public static boolean isDiode(BlockState state) {
		return state.getBlock() instanceof FastRepeater || state.getBlock() instanceof RedstoneDiodeBlock;
	}

	public boolean isFacingTowardsRepeater(IBlockReader worldIn, BlockPos pos, BlockState state) {
		Direction direction = state.get(HORIZONTAL_FACING).getOpposite();
		BlockState blockstate = worldIn.getBlockState(pos.offset(direction));
		return isDiode(blockstate) && blockstate.get(HORIZONTAL_FACING) != direction;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (stateIn.get(POWERED)) {
			Direction direction = stateIn.get(HORIZONTAL_FACING);
			double d0 = (double)((float)pos.getX() + 0.5F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
			double d1 = (double)((float)pos.getY() + 0.4F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
			double d2 = (double)((float)pos.getZ() + 0.5F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
			float f = -5.0F;
			if (rand.nextBoolean()) {
				f = (float)(stateIn.get(DELAY) * 2 - 1);
			}

			f = f / 16.0F;
			double d3 = (double)(f * (float)direction.getXOffset());
			double d4 = (double)(f * (float)direction.getZOffset());
			worldIn.addParticle(InstRedMod.FAST_REDSTONE_DUST, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
		}
	}
}
