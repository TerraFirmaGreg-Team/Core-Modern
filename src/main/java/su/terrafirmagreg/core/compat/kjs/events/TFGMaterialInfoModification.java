package su.terrafirmagreg.core.compat.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import su.terrafirmagreg.core.TFGCore;

import java.util.Objects;

public class TFGMaterialInfoModification extends EventJS {

	/**
	 * Добавляет информацию о материале для указанного предмета.
	 *
	 * @param itemLike предмет, для которого добавляется информация о материале
	 * @param itemMaterialInfo информация о материале
	 */
	public void add(ItemLike itemLike, ItemMaterialInfo itemMaterialInfo) {
		ChemicalHelper.registerMaterialInfo(itemLike, itemMaterialInfo);
	}

	/**
	 * Добавляет информацию о материале для всех предметов в указанном теге.
	 *
	 * @param tagName имя тега
	 * @param itemMaterialInfo информация о материале
	 */
	public void add(String tagName, ItemMaterialInfo itemMaterialInfo) {
		ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
		var tagKey = tagManager.createTagKey(new ResourceLocation(tagName));
		var tag = tagManager.getTag(tagKey);
		tag.forEach(stack -> add(stack, itemMaterialInfo));
	}


	/**
	 * Удаляет информацию о материале для указанного предмета.
	 *
	 * @param itemLike предмет, для которого удаляется информация о материале
	 *
	 * @return true, если информация о материале была удалена, false в противном случае
	 */
	public boolean remove(ItemLike itemLike) {
		var itemResourceLocation = ForgeRegistries.ITEMS.getKey(itemLike.asItem());

		if (itemResourceLocation == null) {
			TFGCore.LOGGER.warn("Item not founded in item registry: {}", itemLike.asItem());
			return false;
		}

		var item = ChemicalHelper.ITEM_MATERIAL_INFO.keySet().stream()
		                                            .filter(el -> Objects.equals(ForgeRegistries.ITEMS.getKey(el.asItem()), itemResourceLocation))
		                                            .findFirst();

		if (item.isPresent()) {
			var removedValue = ChemicalHelper.ITEM_MATERIAL_INFO.remove(item.get());
			if (removedValue != null) {
				return true;
			}
			TFGCore.LOGGER.warn("Item has not been deleted from unification data: {}", itemResourceLocation);
			return false;
		}
		TFGCore.LOGGER.warn("No unification info for: {}", itemResourceLocation);
		return false;
	}
}

