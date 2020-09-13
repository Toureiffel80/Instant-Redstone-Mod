package toureiffel.inst_red.init;

import java.util.function.Supplier;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import toureiffel.inst_red.InstRedMod;

public final class ModItemGroups {

	private static class ModItemGroup extends ItemGroup {
		
		private final Supplier<ItemStack> m_iconSupplier;

		public ModItemGroup(final String name, final Supplier<ItemStack> iconSupplier) {
			super(name);
			m_iconSupplier = iconSupplier;
		}

		@Override
		public ItemStack createIcon() {
			return m_iconSupplier.get();
		}

	}
	
	public static final ItemGroup MOD_ITEM_GROUP = new ModItemGroup(InstRedMod.MODID, () -> new ItemStack(InstRedMod.ICON));
	
	
}
