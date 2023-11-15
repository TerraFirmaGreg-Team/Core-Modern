package exception.terrafirmagreg.objects;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

import static com.gregtechceu.gtceu.common.data.GTItems.materialInfo;

public class TFGItems {

    public static final ItemEntry<?>[] EXTRUDER_SHAPES = new ItemEntry[12];

    public static ItemEntry<Item> SHAPE_EXTRUDER_MINING_HAMMER_HEAD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_SWORD_HEAD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_PICKAXE_HEAD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_SHOVEL_HEAD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_AXE_HEAD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_HOE_HEAD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_SCYTHE_HEAD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_FILE_HEAD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_HAMMER_HEAD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_SAW_HEAD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_KNIFE_HEAD;
    public static ItemEntry<Item> SHAPE_EXTRUDER_BUTCHERY_KNIFE_HEAD;

    static {
        EXTRUDER_SHAPES[0] = SHAPE_EXTRUDER_MINING_HAMMER_HEAD = GTRegistries.REGISTRATE.item("mining_hammer_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

        EXTRUDER_SHAPES[1] = SHAPE_EXTRUDER_SWORD_HEAD = GTRegistries.REGISTRATE.item("sword_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

        EXTRUDER_SHAPES[2] = SHAPE_EXTRUDER_PICKAXE_HEAD = GTRegistries.REGISTRATE.item("pickaxe_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

        EXTRUDER_SHAPES[3] = SHAPE_EXTRUDER_SHOVEL_HEAD = GTRegistries.REGISTRATE.item("shovel_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

        EXTRUDER_SHAPES[4] = SHAPE_EXTRUDER_AXE_HEAD = GTRegistries.REGISTRATE.item("axe_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

        EXTRUDER_SHAPES[5] = SHAPE_EXTRUDER_HOE_HEAD = GTRegistries.REGISTRATE.item("hoe_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

        EXTRUDER_SHAPES[6] = SHAPE_EXTRUDER_SCYTHE_HEAD = GTRegistries.REGISTRATE.item("scythe_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

        EXTRUDER_SHAPES[7] = SHAPE_EXTRUDER_FILE_HEAD = GTRegistries.REGISTRATE.item("file_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

        EXTRUDER_SHAPES[8] = SHAPE_EXTRUDER_HAMMER_HEAD = GTRegistries.REGISTRATE.item("hammer_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

        EXTRUDER_SHAPES[9] = SHAPE_EXTRUDER_SAW_HEAD = GTRegistries.REGISTRATE.item("saw_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

        EXTRUDER_SHAPES[10] = SHAPE_EXTRUDER_KNIFE_HEAD = GTRegistries.REGISTRATE.item("knife_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();

        EXTRUDER_SHAPES[11] = SHAPE_EXTRUDER_BUTCHERY_KNIFE_HEAD = GTRegistries.REGISTRATE.item("butchery_knife_head_mold", Item::new)
                .lang("Casting Mold (Mining Hammer)")
                .onRegister(materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, GTValues.M * 4)))).register();
    }

    public static void init() {}
}
