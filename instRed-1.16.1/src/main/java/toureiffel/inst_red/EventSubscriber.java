package toureiffel.inst_red;

import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.WallOrFloorItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import toureiffel.inst_red.config.ConfigHelper;
import toureiffel.inst_red.config.ConfigHolder;
import toureiffel.inst_red.init.ModBlocks;
import toureiffel.inst_red.init.ModItemGroups;

@EventBusSubscriber(modid = InstRedMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class EventSubscriber {
	
	@SubscribeEvent
	public static void onRegisterItems(RegistryEvent.Register<Item> event) {
		
		InstRedMod.ICON = new WallOrFloorItem(ModBlocks.FAST_TORCH.get(), ModBlocks.FAST_WALL_TORCH.get(),
				new Item.Properties().group(ModItemGroups.MOD_ITEM_GROUP)).setRegistryName("fast_torch");
		
		event.getRegistry().register(InstRedMod.ICON);
		
		event.getRegistry().register(new BlockItem(ModBlocks.FAST_REPEATER.get(),
				new Item.Properties().group(ModItemGroups.MOD_ITEM_GROUP)).setRegistryName(ModBlocks.FAST_REPEATER.get().getRegistryName()));
		
	}
	
	@SubscribeEvent
	public static void setup(final FMLCommonSetupEvent event) {
		RenderTypeLookup.setRenderLayer(ModBlocks.FAST_TORCH.get(), RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(ModBlocks.FAST_WALL_TORCH.get(), RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(ModBlocks.FAST_REPEATER.get(), RenderType.getCutoutMipped());
	}
	
	@SubscribeEvent
	public static void onConfigEvent(final ModConfigEvent event) {
		
		if(event.getConfig().getSpec() == ConfigHolder.CLIENT_SPEC) {
			ConfigHelper.bakeClient(event.getConfig());
		} else if(event.getConfig().getSpec() == ConfigHolder.SERVER_SPEC) {
			ConfigHelper.bakeServer(event.getConfig());
		}
		
	}

}
