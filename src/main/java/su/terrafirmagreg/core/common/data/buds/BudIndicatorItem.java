package su.terrafirmagreg.core.common.data.buds;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class BudIndicatorItem extends BlockItem {

	private final Material material;

	public BudIndicatorItem(BudIndicator block, Properties props, Material mat) {
		super(block, props);
		this.material = mat;
	}

	public static BudIndicatorItem create(BudIndicator block, Properties props, Material mat) {
		return new BudIndicatorItem(block, props, mat);
	}

	@Override
	public BudIndicator getBlock() {
		return (BudIndicator) super.getBlock();
	}

	@Override
	public Component getDescription() {
		return this.getBlock().getName();
	}

	@Override
	public Component getName(ItemStack stack) {
		return getDescription();
	}

	public Material getMaterial() {
		return material;
	}
}
