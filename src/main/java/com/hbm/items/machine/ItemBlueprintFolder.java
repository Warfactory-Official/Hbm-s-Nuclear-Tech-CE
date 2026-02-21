package com.hbm.items.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ItemBakedBase;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemBlueprintFolder extends ItemBakedBase {

    private final String baseTexturePath;

    @SideOnly(Side.CLIENT) private ModelResourceLocation mrlBase;
    @SideOnly(Side.CLIENT) private ModelResourceLocation mrlDiscover;
    @SideOnly(Side.CLIENT) private ModelResourceLocation mrlSecret;

    @SideOnly(Side.CLIENT) private ResourceLocation spriteBase;
    @SideOnly(Side.CLIENT) private ResourceLocation spriteDiscover;
    @SideOnly(Side.CLIENT) private ResourceLocation spriteSecret;

    public ItemBlueprintFolder(String name) {
        super(name);
        this.baseTexturePath = name;
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
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

            // Base
            {
                IModel retextured = baseModel.retexture(ImmutableMap.of("layer0", spriteBase.toString()));
                IBakedModel baked = retextured.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                event.getModelRegistry().putObject(mrlBase, baked);
            }
            // Discover
            {
                IModel retextured = baseModel.retexture(ImmutableMap.of("layer0", spriteDiscover.toString()));
                IBakedModel baked = retextured.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                event.getModelRegistry().putObject(mrlDiscover, baked);
            }
            // Secret
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
            int meta = stack.getMetadata();
            if (meta == 1) return mrlDiscover;
            if (meta == 2) return mrlSecret;
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
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;
        for (int i = 0; i < 2; i++) items.add(new ItemStack(this, 1, i));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) return new ActionResult<>(EnumActionResult.PASS, stack);

        List<String> pools = new ArrayList<>();

        for (String pool : GenericRecipes.blueprintPools.keySet()) {
            if (stack.getMetadata() == 0 && pool.startsWith(GenericRecipes.POOL_PREFIX_ALT)) pools.add(pool);
            if (stack.getMetadata() == 1 && pool.startsWith(GenericRecipes.POOL_PREFIX_DISCOVER)) pools.add(pool);
            if (stack.getMetadata() == 2 && pool.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) pools.add(pool);
        }

        if (!pools.isEmpty()) {
            stack.shrink(1);

            String chosen = pools.get(player.getRNG().nextInt(pools.size()));
            ItemStack blueprint = ItemBlueprints.make(chosen);

            return new ActionResult<>(EnumActionResult.SUCCESS, blueprint);
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
}
