package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemLock;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Random;
import java.util.function.Supplier;

// mlbv: I can't believe it actually (originally) used tile.newInstance(). Jesus Christ.
public class BlockDecoContainer<E extends Enum<E>, T extends TileEntity> extends BlockDecoModel<E> implements ITileEntityProvider {

    private final Supplier<T> tile;

    public BlockDecoContainer(Material mat, SoundType type, String registryName, E[] blockEnum, boolean multiName, boolean multiTexture, Supplier<T> tile) {
        super(mat, type, registryName, blockEnum, multiName, multiTexture);
        this.tile = tile;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        try {
            return tile.get();
        } catch (Exception e) {
            MainRegistry.logger.error("BlockDecoContainer attempted to create a TE, but couldn't. How does that even happen?");
            return null;
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        try {
            return tile.get();
        } catch (Exception e) {
            MainRegistry.logger.error("BlockDecoContainer attempted to create a TE, but couldn't. How does that even happen?");
            return null;
        }
    }

    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int eventNo, int eventArg) {
        super.eventReceived(state, world, pos, eventNo, eventArg);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventNo, eventArg);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (world.isRemote) {
            return true;
        } else {
            TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityLockableBase) { //annoying accommodations for the filing cabinet, but whatever, could potentially be useful
                ItemStack held = player.getHeldItem(hand);
                if (!held.isEmpty() && (held.getItem() instanceof ItemLock || held.getItem() == ModItems.key_kit)) {
                    return false;
                } else if (!player.isSneaking() && ((TileEntityLockableBase) entity).canAccess(player)) {
                    player.openGui(MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
                    return true;
                }
            } else if (!player.isSneaking()) {
                player.openGui(MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }

        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        // mlbv: IInventory? seriously?
        IInventory inventory = te instanceof IInventory ? (IInventory) te : null;
        Random rand = world.rand;

        if (inventory != null) {
            for (int i1 = 0; i1 < inventory.getSizeInventory(); ++i1) {
                ItemStack itemstack = inventory.getStackInSlot(i1);

                if (!itemstack.isEmpty()) {
                    float f = rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = rand.nextFloat() * 0.8F + 0.1F;
                    float f2 = rand.nextFloat() * 0.8F + 0.1F;

                    while (itemstack.getCount() > 0) {
                        int j1 = rand.nextInt(21) + 10;

                        if (j1 > itemstack.getCount()) {
                            j1 = itemstack.getCount();
                        }

                        ItemStack drop = itemstack.splitStack(j1);
                        EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, drop);

                        float f3 = 0.05F;
                        entityitem.motionX = rand.nextGaussian() * f3;
                        entityitem.motionY = rand.nextGaussian() * f3 + 0.2F;
                        entityitem.motionZ = rand.nextGaussian() * f3;
                        world.spawnEntity(entityitem);
                    }
                }
            }
        } else if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            InventoryHelper.dropInventoryItems(world, pos, te);
        }
        world.updateComparatorOutputLevel(pos, this);
        super.breakBlock(world, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel blockBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("block/cube_all"));
            ImmutableMap<String, String> blockTextures = ImmutableMap.of("all", "hbm:blocks/block_steel");
            IModel blockRetextured = blockBaseModel.retexture(blockTextures);
            IBakedModel blockBaked = blockRetextured.bake(
                    ModelRotation.X0_Y0,
                    DefaultVertexFormats.BLOCK,
                    ModelLoader.defaultTextureGetter()
            );
            ModelResourceLocation worldLocation = new ModelResourceLocation(getRegistryName(), "normal");
            event.getModelRegistry().putObject(worldLocation, blockBaked);
            IModel itemBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("item/generated"));
            ImmutableMap<String, String> itemTextures = ImmutableMap.of("layer0", "hbm:blocks/" + getRegistryName().getPath());
            IModel itemRetextured = itemBaseModel.retexture(itemTextures);
            IBakedModel itemBaked = itemRetextured.bake(
                    ModelRotation.X0_Y0,
                    DefaultVertexFormats.ITEM,
                    ModelLoader.defaultTextureGetter()
            );
            ModelResourceLocation inventoryLocation = new ModelResourceLocation(getRegistryName(), "inventory");
            event.getModelRegistry().putObject(inventoryLocation, itemBaked);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        Item item = Item.getItemFromBlock(this);
        ModelResourceLocation inv = new ModelResourceLocation(this.getRegistryName(), "inventory");
        ModelLoader.setCustomModelResourceLocation(item, 0, inv);
        ModelLoader.setCustomModelResourceLocation(item, 1, inv);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(loc, "normal");
            }
        };
    }
}
