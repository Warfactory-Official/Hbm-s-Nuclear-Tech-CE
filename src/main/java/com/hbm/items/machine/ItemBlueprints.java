package com.hbm.items.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemBlueprints extends ItemBakedBase {
    private final String baseTexturePath;

    // Model and sprite locations for variants
    @SideOnly(Side.CLIENT) private ModelResourceLocation mrlBase;
    @SideOnly(Side.CLIENT) private ModelResourceLocation mrlDiscover;
    @SideOnly(Side.CLIENT) private ModelResourceLocation mrlSecret;

    @SideOnly(Side.CLIENT) private ResourceLocation spriteBase;
    @SideOnly(Side.CLIENT) private ResourceLocation spriteDiscover;
    @SideOnly(Side.CLIENT) private ResourceLocation spriteSecret;


    public ItemBlueprints(String s) {
        super(s);
        this.baseTexturePath = s;
        this.setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if(tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH) {
            for (Map.Entry<String, List<String>> pool : GenericRecipes.blueprintPools.entrySet()) {
                String poolName = pool.getKey();
                if (!poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) items.add(make(poolName));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));

            spriteBase = new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath);
            spriteDiscover = new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath + "_discover");
            spriteSecret = new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath + "_secret");

            mrlBase = new ModelResourceLocation(spriteBase, "inventory");
            mrlDiscover = new ModelResourceLocation(spriteDiscover, "inventory");
            mrlSecret = new ModelResourceLocation(spriteSecret, "inventory");

            {
                IModel retextured = baseModel.retexture(ImmutableMap.of("layer0", spriteBase.toString()));
                IBakedModel baked = retextured.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                event.getModelRegistry().putObject(mrlBase, baked);
            }
            {
                IModel retextured = baseModel.retexture(ImmutableMap.of("layer0", spriteDiscover.toString()));
                IBakedModel baked = retextured.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                event.getModelRegistry().putObject(mrlDiscover, baked);
            }
            {
                IModel retextured = baseModel.retexture(ImmutableMap.of("layer0", spriteSecret.toString()));
                IBakedModel baked = retextured.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                event.getModelRegistry().putObject(mrlSecret, baked);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        spriteBase = new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath);
        spriteDiscover = new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath + "_discover");
        spriteSecret = new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath + "_secret");

        mrlBase = new ModelResourceLocation(spriteBase, "inventory");
        mrlDiscover = new ModelResourceLocation(spriteDiscover, "inventory");
        mrlSecret = new ModelResourceLocation(spriteSecret, "inventory");

        ModelBakery.registerItemVariants(this, mrlBase, mrlDiscover, mrlSecret);

        ModelLoader.setCustomMeshDefinition(this, stack -> {
            String pool = grabPool(stack);
            if (pool != null) {
                if (pool.startsWith(GenericRecipes.POOL_PREFIX_DISCOVER)) return mrlDiscover;
                if (pool.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) return mrlSecret;
            }
            return mrlBase;
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath));
        map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath + "_discover"));
        map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath + "_secret"));
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if(world.isRemote) return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        if(!stack.hasTagCompound()) return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));

        String poolName = stack.getTagCompound().getString("pool");

        if(poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        if(!player.inventory.hasItemStack(new ItemStack(Items.PAPER))) return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));

        player.inventory.clearMatchingItems(Items.PAPER, 0, 1, null);
        player.swingArm(hand);

        ItemStack copy = stack.copy();
        copy.setCount(1);

        if(!player.capabilities.isCreativeMode) {
            if(stack.getCount() < stack.getMaxStackSize()) {
                stack.grow(1);
                return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
            }

            if(!player.inventory.addItemStackToInventory(copy)) {
                copy = stack.copy();
                copy.setCount(1);
                player.dropItem(copy, false);
            }

            player.inventoryContainer.detectAndSendChanges();
        } else {
            player.dropItem(copy, false);
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {

        if(!stack.hasTagCompound()) {
            return;
        }

        String poolName = stack.getTagCompound().getString("pool");
        List<String> pool = GenericRecipes.blueprintPools.get(poolName);

        if(pool == null || pool.isEmpty()) {
            return;
        }
        if(poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) {
            list.add(TextFormatting.RED + "Cannot be copied!");
        } else {
            list.add(TextFormatting.YELLOW + "Right-click to copy (requires paper)");
        }

        for(String name : pool) {
            GenericRecipe recipe = GenericRecipes.pooledBlueprints.get(name);
            if(recipe != null) {
                list.add(recipe.getLocalizedName());
            }
        }
    }

    public static String grabPool(ItemStack stack) {
        if(stack == null) return null;
        if(stack.getItem() != ModItems.blueprints) return null;
        if(!stack.hasTagCompound()) return null;
        if(!stack.getTagCompound().hasKey("pool")) return null;
        return stack.getTagCompound().getString("pool");
    }

    public static ItemStack make(String pool) {
        ItemStack stack = new ItemStack(ModItems.blueprints);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("pool", pool);
        return stack;
    }
}
