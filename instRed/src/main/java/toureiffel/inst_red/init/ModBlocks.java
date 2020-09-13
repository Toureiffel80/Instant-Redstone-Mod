package toureiffel.inst_red.init;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import toureiffel.inst_red.InstRedMod;
import toureiffel.inst_red.block.FastRepeater;
import toureiffel.inst_red.block.FastTorch;
import toureiffel.inst_red.block.FastWallTorch;

public class ModBlocks {
	
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, InstRedMod.MODID);
	
	public static final RegistryObject<Block> FAST_REPEATER = BLOCKS.register("fast_repeater",
			() -> new FastRepeater(Block.Properties.create(Material.MISCELLANEOUS)
					.hardnessAndResistance(0.0F, 0.0F).sound(SoundType.WOOD)));
	public static final RegistryObject<Block> FAST_TORCH = BLOCKS.register("fast_torch", 
			() -> new FastTorch(Block.Properties.create(Material.MISCELLANEOUS)
					.doesNotBlockMovement().hardnessAndResistance(0.0F).func_235838_a_((state) -> 7).sound(SoundType.WOOD)));
	public static final RegistryObject<Block> FAST_WALL_TORCH = BLOCKS.register("fast_wall_torch",
			() -> new FastWallTorch(Block.Properties.create(Material.MISCELLANEOUS)
					.doesNotBlockMovement().hardnessAndResistance(0.0F).func_235838_a_((state) -> 7).sound(SoundType.WOOD)));
}
