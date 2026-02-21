package com.hbm.items.special;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.inventory.gui.GUIBookLore;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.I18nUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Objects;

/*players can have a lil lore, as a treat.
 * nothing super complex like the guidebooks, just some NBT IDs, a bit of I18n and a centered textbox.
 * oh, and also different textures for both the book, the gui, and maybe the 'turn page' button based on what type of 'book' it is.
 * no metadata, i want it to be fairly flexible. probably like the assembly templates
 */
public class ItemBookLore extends Item implements IGUIProvider, IDynamicModels {

    private static final ResourceLocation TEX_COVER = new ResourceLocation(Tags.MODID, "items/book_cover");
    private static final ResourceLocation TEX_TITLE = new ResourceLocation(Tags.MODID, "items/book_title");

    public ItemBookLore(String s) {
        this.setMaxStackSize(1);
        this.setRegistryName(s);
        this.setTranslationKey(s);
        IDynamicModels.INSTANCES.add(this);
        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (world.isRemote) {
            player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
        if (!stack.hasTagCompound()) return;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return;
        String key = tag.getString("k");
        if (key.isEmpty()) return;

        String fullKey = "book_lore." + key + ".author";
        String loc = I18nUtil.resolveKey(fullKey);
        if (!loc.equals(fullKey)) {
            list.add(I18nUtil.resolveKey("book_lore.author", loc));
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if (!stack.hasTagCompound()) return "book_lore.test";
        NBTTagCompound tag = stack.getTagCompound();
        String key = tag == null ? "" : tag.getString("k");

        return "book_lore." + (key.isEmpty() ? "test" : key);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIBookLore(player);
    }

    public static ItemStack createBook(String key, int pages, int colorCov, int colorTit) {
        ItemStack book = new ItemStack(ModItems.book_lore);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("k", key);
        tag.setShort("p", (short) pages);
        tag.setInteger("cov_col", colorCov);
        tag.setInteger("tit_col", colorTit);

        book.setTagCompound(tag);
        return book;
    }

    public static void addArgs(ItemStack book, int page, String... args) {
        if (!book.hasTagCompound()) return;
        NBTTagCompound root = book.getTagCompound();
        if (root == null) return;

        NBTTagCompound data = new NBTTagCompound();
        for (int i = 0; i < args.length; i++) {
            data.setString("a" + (i + 1), args[i]);
        }

        root.setTag("p" + page, data);
    }

    // IDynamicModels

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel base = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:item/generated"));
            ImmutableMap.Builder<String, String> tex = ImmutableMap.builder();

            tex.put("layer0", TEX_COVER.toString());
            tex.put("layer1", TEX_TITLE.toString());
            tex.put("particle", TEX_COVER.toString());

            IModel retextured = base.retexture(tex.build());
            IBakedModel baked = retextured.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());

            ModelResourceLocation mrl = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory");
            event.getModelRegistry().putObject(mrl, baked);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(Objects.requireNonNull(this.getRegistryName()), "inventory"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        map.registerSprite(TEX_COVER);
        map.registerSprite(TEX_TITLE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IItemColor getItemColorHandler() {
        return (stack, tintIndex) -> {
            if (tintIndex == 0) { // cover
                if (stack.hasTagCompound()) {
                    NBTTagCompound tag = stack.getTagCompound();
                    if (tag != null) {
                        int color = tag.getInteger("cov_col");
                        if (color > 0) return color;
                    }
                }
                return 0x303030;
            } else if (tintIndex == 1) { // title
                if (stack.hasTagCompound()) {
                    NBTTagCompound tag = stack.getTagCompound();
                    if (tag != null) {
                        int color = tag.getInteger("tit_col");
                        if (color > 0) return color;
                    }
                }
                return 0xFFFFFF;
            }
            return 0xFFFFFF;
        };
    }
}
