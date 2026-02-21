package com.hbm.items.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.IModelRegister;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class ItemMold extends Item implements IModelRegister {

    public static List<Mold> molds = new ArrayList<>(); //molds in "pretty" order, variable between versions
    public static HashMap<Integer, Mold> moldById = new HashMap<>(); //molds by their static ID -> stack item damage

    public HashMap<NTMMaterial, ItemStack> blockOverrides = new HashMap<>();

    public ItemMold(String s) {

        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(MainRegistry.templateTab);

        blockOverrides.put(Mats.MAT_STONE,		new ItemStack(Blocks.STONE));
        blockOverrides.put(Mats.MAT_OBSIDIAN,	new ItemStack(Blocks.OBSIDIAN));

        int S = 0;
        int L = 1;
        registerMold(new MoldShape(		0, S, "nugget", MaterialShapes.NUGGET));
        registerMold(new MoldShape(		1, S, "billet", MaterialShapes.BILLET));
        registerMold(new MoldShape(		2, S, "ingot", MaterialShapes.INGOT));
        registerMold(new MoldShape(		3, S, "plate", MaterialShapes.PLATE));
        registerMold(new MoldShape(		4, S, "wire", MaterialShapes.WIRE, 8));

        registerMold(new MoldMulti(5, S, "blade", MaterialShapes.INGOT.q(3),
                Mats.MAT_TITANIUM, (Supplier<ItemStack>) () -> new ItemStack(ModItems.blade_titanium),
                Mats.MAT_TUNGSTEN, (Supplier<ItemStack>) () -> new ItemStack(ModItems.blade_tungsten)));

        registerMold(new MoldMulti(6, S, "blades", MaterialShapes.INGOT.q(4),
                Mats.MAT_STEEL,         (Supplier<ItemStack>) () -> new ItemStack(ModItems.blades_steel),
                Mats.MAT_TITANIUM,      (Supplier<ItemStack>) () -> new ItemStack(ModItems.blades_titanium),
                Mats.MAT_ALLOY,         (Supplier<ItemStack>) () -> new ItemStack(ModItems.blades_advanced_alloy)));

        registerMold(new MoldMulti(7, S, "stamp", MaterialShapes.INGOT.q(4),
                Mats.MAT_STONE,         (Supplier<ItemStack>) () -> new ItemStack(ModItems.stamp_stone_flat),
                Mats.MAT_IRON,          (Supplier<ItemStack>) () -> new ItemStack(ModItems.stamp_iron_flat),
                Mats.MAT_STEEL,         (Supplier<ItemStack>) () -> new ItemStack(ModItems.stamp_steel_flat),
                Mats.MAT_TITANIUM,      (Supplier<ItemStack>) () -> new ItemStack(ModItems.stamp_titanium_flat),
                Mats.MAT_OBSIDIAN,      (Supplier<ItemStack>) () -> new ItemStack(ModItems.stamp_obsidian_flat)));

        registerMold(new MoldShape(		8, S, "shell", MaterialShapes.SHELL));
        registerMold(new MoldShape(		9, S, "pipe", MaterialShapes.PIPE));

        registerMold(new MoldShape(		10, L, "ingots", MaterialShapes.INGOT, 9));
        registerMold(new MoldShape(		11, L, "plates", MaterialShapes.PLATE, 9));
        registerMold(new MoldBlock(		12, L, "block", MaterialShapes.BLOCK));
        registerMold(new MoldSingle(13, L, "pipes",
                () -> new ItemStack(ModItems.pipes_steel),
                Mats.MAT_STEEL, MaterialShapes.BLOCK.q(3)));

        registerMold(new MoldMulti(16, S, "c9", MaterialShapes.PLATE.q(1, 4),
                Mats.MAT_GUNMETAL,      (Supplier<ItemStack>) () -> OreDictManager.DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.SMALL),
                Mats.MAT_WEAPONSTEEL,   (Supplier<ItemStack>) () -> OreDictManager.DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.SMALL_STEEL)));

        registerMold(new MoldMulti(17, S, "c50", MaterialShapes.PLATE.q(1, 2),
                Mats.MAT_GUNMETAL,      (Supplier<ItemStack>) () -> OreDictManager.DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.LARGE),
                Mats.MAT_WEAPONSTEEL,   (Supplier<ItemStack>) () -> OreDictManager.DictFrame.fromOne(ModItems.casing, ItemEnums.EnumCasingType.LARGE_STEEL)));

        registerMold(new MoldShape(		19, S, "plate_cast", MaterialShapes.CASTPLATE));
        registerMold(new MoldShape(		20, S, "wire_dense", MaterialShapes.DENSEWIRE));
        registerMold(new MoldShape(		21, L, "wires_dense", MaterialShapes.DENSEWIRE, 9));

        registerMold(new MoldShape(		22, S, "barrel_light", MaterialShapes.LIGHTBARREL));
        registerMold(new MoldShape(		23, S, "barrel_heavy", MaterialShapes.HEAVYBARREL));
        registerMold(new MoldShape(		24, S, "receiver_light", MaterialShapes.LIGHTRECEIVER));
        registerMold(new MoldShape(		25, S, "receiver_heavy", MaterialShapes.HEAVYRECEIVER));
        registerMold(new MoldShape(		26, S, "mechanism", MaterialShapes.MECHANISM));
        registerMold(new MoldShape(		27, S, "stock", MaterialShapes.STOCK));
        registerMold(new MoldShape(		28, S, "grip", MaterialShapes.GRIP));

        ModItems.ALL_ITEMS.add(this);
    }

    public void registerMold(Mold mold) {
        this.molds.add(mold);
        this.moldById.put(mold.id, mold);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (Mold mold : molds) {
                items.add(new ItemStack(this, 1, mold.id));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (Mold mold : molds) {
            ModelLoader.setCustomModelResourceLocation(this, mold.id, new ModelResourceLocation(Tags.MODID + ":items/mold_" + mold.name, "inventory"));
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerSprites(TextureMap map){
        for (Mold mold : molds) {
            ResourceLocation loc = new ResourceLocation(Tags.MODID + ":items/mold_" + mold.name);
            map.registerSprite(loc);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void bakeModels(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
            for (Mold mold : molds) {
                ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, "items/mold_" + mold.name);
                IModel retexturedModel = baseModel.retexture(
                        ImmutableMap.of(
                                "layer0", spriteLoc.toString()
                        )
                );
                IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                ModelResourceLocation bakedModelLocation = new ModelResourceLocation(new ResourceLocation(Tags.MODID, "items/mold_" + mold.name), "inventory");
                event.getModelRegistry().putObject(bakedModelLocation, bakedModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        Mold mold = getMold(stack);
        list.add(TextFormatting.YELLOW + mold.getTitle());

        if(mold.size == 0) list.add(TextFormatting.GOLD + I18nUtil.resolveKey(ModBlocks.foundry_mold.getTranslationKey() + ".name"));
        if(mold.size == 1) list.add(TextFormatting.RED + I18nUtil.resolveKey(ModBlocks.foundry_basin.getTranslationKey() + ".name"));
    }

    public Mold getMold(ItemStack stack) {
        Mold mold = moldById.get(stack.getItemDamage());
        return mold != null ? mold : molds.get(0);
    }

    public static int nextOrder = 0;

    public abstract class Mold {
        public int order;
        public int id;
        public int size;
        public String name;

        public Mold(int id, int size, String name) {
            this.order = nextOrder++;
            this.id = id;
            this.size = size;
            this.name = name;
        }

        @NotNull //FOR FUCKS SAKE IF OUTPUT IS ITEMSTACK, IT SHOULD NEVER BE NULL
        public abstract ItemStack getOutput(NTMMaterial mat);
        public abstract int getCost();
        public abstract String getTitle();
    }

    public class MoldShape extends Mold {

        public MaterialShapes shape;
        public int amount;

        public MoldShape(int id, int size, String name, MaterialShapes shape) {
            this(id, size, name, shape, 1);
        }

        public MoldShape(int id, int size, String name, MaterialShapes shape, int amount) {
            super(id, size, name);
            this.shape = shape;
            this.amount = amount;
        }

        @Override
        public @NotNull ItemStack getOutput(NTMMaterial mat) {

            for(String name : mat.names) {
                String od = shape.name() + name;
                List<ItemStack> ores = OreDictionary.getOres(od);
                if(!ores.isEmpty()) {
                    //prioritize NTM items
                    for(ItemStack ore : ores) {
                        if(Item.REGISTRY.getNameForObject(ore.getItem()).toString().startsWith(Tags.MODID)) {
                            ItemStack copy = ore.copy();
                            copy.setCount(this.amount);
                            return copy;
                        }
                    }
                    //...then try whatever comes first
                    ItemStack copy = ores.get(0).copy();
                    copy.setCount(this.amount);
                    return copy;
                }
            }

            return ItemStack.EMPTY;
        }

        @Override
        public int getCost() {
            return shape.q(amount);
        }

        @Override
        public String getTitle() {
            return I18nUtil.resolveKey("shape." + shape.name()) + " x" + amount;
        }
    }

    public class MoldBlock extends MoldShape {

        public MoldBlock(int id, int size, String name, MaterialShapes shape) {
            super(id, size, name, shape);
        }

        @Override
        public @NotNull ItemStack getOutput(NTMMaterial mat) {

            ItemStack override = blockOverrides.get(mat);

            if(override != null)
                return override.copy();

            return super.getOutput(mat);
        }
    }


    /* because why not */
    public class MoldSingle extends Mold {

        private final Supplier<ItemStack> outSupplier;
        public NTMMaterial mat;
        public int amount;

        public MoldSingle(int id, int size, String name,
                          Supplier<ItemStack> outSupplier,
                          NTMMaterial mat, int amount) {
            super(id, size, name);
            this.outSupplier = outSupplier;
            this.mat = mat;
            this.amount = amount;
        }

        @Override
        public @NotNull ItemStack getOutput(NTMMaterial mat) {
            if (this.mat != mat) return ItemStack.EMPTY;
            ItemStack s = outSupplier.get();
            return s.isEmpty() ? ItemStack.EMPTY : s.copy();
        }

        @Override
        public int getCost() {
            return amount;
        }

        @Override
        public String getTitle() {
            ItemStack s = outSupplier.get();
            if (s.isEmpty()) return I18nUtil.resolveKey("shape." + name) + " x0";
            return s.getDisplayName() + " x" + s.getCount();
        }
    }

    /* not so graceful but it does the job and it does it well */
    public class MoldMulti extends Mold {

        public HashMap<NTMMaterial, Supplier<ItemStack>> map = new HashMap<>();
        public int amount;
        public int stacksize;

        public MoldMulti(int id, int size, String name, int amount, Object... inputs) {
            super(id, size, name);
            this.amount = amount;

            for (int i = 0; i < inputs.length; i += 2) {
                NTMMaterial m = (NTMMaterial) inputs[i];
                Object outObj = inputs[i + 1];

                Supplier<ItemStack> sup;

                if (outObj instanceof Supplier) {
                    sup = (Supplier<ItemStack>) outObj;
                } else if (outObj instanceof ItemStack) {
                    ItemStack preset = (ItemStack) outObj;
                    final int count = preset.getCount() <= 0 ? 1 : preset.getCount();
                    final int meta = preset.getMetadata();
                    final Item item = preset.getItem();
                    final ResourceLocation rl = item.getRegistryName();

                    sup = () -> {
                        if (rl == null) return ItemStack.EMPTY;
                        Item resolved = ForgeRegistries.ITEMS.getValue(rl);
                        return resolved != null ? new ItemStack(resolved, count, meta) : ItemStack.EMPTY;
                    };
                } else {
                    throw new IllegalArgumentException("Expected ItemStack or Supplier<ItemStack> at position " + (i + 1));
                }

                map.put(m, sup);

                if (i == 0) {
                    ItemStack tmp = sup.get();
                    stacksize = tmp.isEmpty() ? 1 : tmp.getCount();
                }
            }
        }

        @Override
        public @NotNull ItemStack getOutput(NTMMaterial mat) {
            Supplier<ItemStack> sup = this.map.get(mat);
            if (sup == null) return ItemStack.EMPTY;
            ItemStack out = sup.get();
            return out.isEmpty() ? ItemStack.EMPTY : out.copy();
        }

        @Override
        public int getCost() {
            return amount;
        }

        @Override
        public String getTitle() {
            if (stacksize <= 0 && !map.isEmpty()) {
                Supplier<ItemStack> first = map.values().iterator().next();
                ItemStack s = first.get();
                stacksize = s.isEmpty() ? 1 : s.getCount();
            }
            return I18nUtil.resolveKey("shape." + name) + " x" + stacksize;
        }
    }
}
