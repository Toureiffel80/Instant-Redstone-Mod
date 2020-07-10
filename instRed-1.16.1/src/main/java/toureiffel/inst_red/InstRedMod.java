package toureiffel.inst_red;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.item.Item;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import toureiffel.inst_red.config.ConfigHolder;
import toureiffel.inst_red.init.ModBlocks;
import toureiffel.inst_red.init.ModItems;

@Mod(InstRedMod.MODID)
public final class InstRedMod {
	
	public static final String MODID = "inst_red";
	
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public static final RedstoneParticleData FAST_REDSTONE_DUST = new RedstoneParticleData(0.0F, 0.0F, 1.0F, 1.0F);
	
	public static Item ICON;
	
	public InstRedMod() {
		
		LOGGER.debug("Constructing mod object");
		
		final ModLoadingContext modLoadingContext = ModLoadingContext.get();
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		ModBlocks.BLOCKS.register(modEventBus);
		ModItems.ITEMS.register(modEventBus);
		
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
		modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC);
	}

}
