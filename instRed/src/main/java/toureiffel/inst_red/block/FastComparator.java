package toureiffel.inst_red.block;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.tileentity.ComparatorTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class FastComparator extends ComparatorBlock {

	public FastComparator(Properties properties) {
		super(properties);
	}
	
	@Override
	protected int getDelay(BlockState state) {
		return 0;
	}
	
	private int calculateOutput(World worldIn, BlockPos pos, BlockState state) {
		return state.get(MODE) == ComparatorMode.SUBTRACT ? Math.max(this.calculateInputStrength(worldIn, pos, state) - this.getPowerOnSides(worldIn, pos, state), 0) : this.calculateInputStrength(worldIn, pos, state);
	}
	
	@Override
	protected void updateState(World worldIn, BlockPos pos, BlockState state) {
		if (!worldIn.getPendingBlockTicks().isTickPending(pos, this)) {
			 int i = this.calculateOutput(worldIn, pos, state);
			 TileEntity tileentity = worldIn.getTileEntity(pos);
			 int j = tileentity instanceof ComparatorTileEntity ? ((ComparatorTileEntity)tileentity).getOutputSignal() : 0;
			 if (i != j || state.get(POWERED) != this.shouldBePowered(worldIn, pos, state)) {
				onStateChange(worldIn, pos, state);
			 }
		}
	}

	private void onStateChange(World worldIn, BlockPos pos, BlockState state) {
		int i = this.calculateOutput(worldIn, pos, state);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		int j = 0;
		if (tileentity instanceof ComparatorTileEntity) {
			ComparatorTileEntity comparatortileentity = (ComparatorTileEntity)tileentity;
			j = comparatortileentity.getOutputSignal();
			comparatortileentity.setOutputSignal(i);
		}

		if (j != i || state.get(MODE) == ComparatorMode.COMPARE) {
			boolean flag1 = this.shouldBePowered(worldIn, pos, state);
			boolean flag = state.get(POWERED);
			if (flag && !flag1) {
				worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(false)), 2);
			} else if (!flag && flag1) {
				worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(true)), 2);
			}

			this.notifyNeighbors(worldIn, pos, state);
		}
	}
	
	@Override
	public void onNeighborChange(BlockState state, net.minecraft.world.IWorldReader world, BlockPos pos, BlockPos neighbor) {
		if (pos.getY() == neighbor.getY() && world instanceof World && !((World)world).isRemote()) {
			state.neighborChanged((World)world, pos, world.getBlockState(neighbor).getBlock(), neighbor, false);
		}
	}

}
