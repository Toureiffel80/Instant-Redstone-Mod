package toureiffel.inst_red.block;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import toureiffel.inst_red.InstRedMod;

public class FastTorch extends TorchBlock {
	
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	private static final Map<IBlockReader, List<Toggle>> BURNED_TORCHES = new WeakHashMap<>();
	
	private static final int IT_BEFORE_BURNED = 8;

	public FastTorch(Properties properties) {
		super(properties, InstRedMod.FAST_REDSTONE_DUST);
		this.setDefaultState(this.stateContainer.getBaseState().with(LIT, Boolean.valueOf(true)));
		
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		for(Direction direction : Direction.values()) {
			worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!isMoving) {
			for(Direction direction : Direction.values()) {
				worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
			}
		}
	}

	protected boolean shouldBeOff(World worldIn, BlockPos pos, BlockState state) {
		return worldIn.isSidePowered(pos.down(), Direction.DOWN);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		
		List<Toggle> list = BURNED_TORCHES.get(worldIn);
		while(list != null && !list.isEmpty() && (worldIn.getGameTime() - (list.get(0)).time) > 1L) {
			list.remove(0);
		}
		
		if (state.get(LIT) == shouldBeOff(worldIn, pos, state)) {
			if(state.get(LIT)) {
				if(isBurnedOut(worldIn, pos, true)) {
					worldIn.playEvent(1502, pos, 0);
				}
				worldIn.setBlockState(pos, state.with(LIT, Boolean.valueOf(false)), 3);
			} else if(!isBurnedOut(worldIn, pos, false)) {
				worldIn.setBlockState(pos, state.with(LIT, Boolean.valueOf(true)), 3);
			}
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (stateIn.get(LIT)) {
			double d0 = (double)pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d1 = (double)pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d2 = (double)pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			worldIn.addParticle(this.field_235607_e_, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return side == Direction.DOWN ? blockState.getWeakPower(blockAccess, pos, side) : 0;
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.get(LIT) && Direction.UP != side ? 15 : 0;
	}
	
	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}
	
	private boolean isBurnedOut(World world, BlockPos pos, boolean add) {
		
		List<Toggle> list = BURNED_TORCHES.computeIfAbsent(world, k -> {
				return Lists.newArrayList();
		});
		
		if(add) {
			list.add(new Toggle(pos.toImmutable(), world.getGameTime()));
		}
		
		
		int i = 0;
		
		for(Toggle toggle : list) {
			if(toggle.pos.equals(pos)) {
				++i;
				if(i >= IT_BEFORE_BURNED) {
					return true;
				}
			}
		}
		
		InstRedMod.LOGGER.debug("Toggle at " + world.getGameTime() + " ; " + i);
	
		
		return false;
	}

	private static class Toggle {
		private final BlockPos pos;
		private final long time;
		
		public Toggle(BlockPos pos, long time) {
			this.pos = pos;
			this.time = time;
		}
	}
}
