package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.IDynamicModels;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.util.Tuple.Quartet;
import net.minecraft.block.BlockContainer;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockLoot extends BlockContainer implements IDynamicModels {

    private final BlockBakeFrame blockFrame = new BlockBakeFrame("block_steel");

    private static final AxisAlignedBB SLAB_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D);

    public BlockLoot(String name) {
        super(Material.IRON);
        this.setTranslationKey(name);
        this.setRegistryName(name);
        this.setHardness(1.0F);
        this.setResistance(5.0F);
        this.setLightOpacity(0);
        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SLAB_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return SLAB_AABB;
    }

    @Override
    public net.minecraft.util.EnumBlockRenderType getRenderType(IBlockState state) {
        return net.minecraft.util.EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        // noop
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityLoot) {
                TileEntityLoot loot = (TileEntityLoot) te;
                for (Quartet<ItemStack, Double, Double, Double> q : loot.items) {
                    ItemStack stack = q.getW();
                    if (stack == null || stack.isEmpty()) continue;
                    EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, stack.copy());
                    world.spawnEntity(item);
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        if (!player.isSneaking()) {
            world.setBlockToAir(pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityLoot();
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityLoot();
    }

    @AutoRegister
    public static class TileEntityLoot extends TileEntity {
        public final List<Quartet<ItemStack, Double, Double, Double>> items = new ArrayList<>();

        public TileEntityLoot addItem(ItemStack stack, double x, double y, double z) {
            items.add(new Quartet<>(stack, x, y, z));
            return this;
        }

        @Override
        public NBTTagCompound getUpdateTag() {
            return writeToNBT(super.getUpdateTag());
        }

        @Override
        public void handleUpdateTag(NBTTagCompound tag) {
            readFromNBT(tag);
        }

        @Nullable
        @Override
        public SPacketUpdateTileEntity getUpdatePacket() {
            NBTTagCompound nbt = new NBTTagCompound();
            writeToNBT(nbt);
            return new SPacketUpdateTileEntity(this.pos, 0, nbt);
        }

        @Override
        public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
            readFromNBT(pkt.getNbtCompound());
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            items.clear();
            int count = nbt.getInteger("count");
            for (int i = 0; i < count; i++) {
                NBTTagCompound stackTag = nbt.getCompoundTag("item" + i);
                ItemStack stack = stackTag.isEmpty() ? ItemStack.EMPTY : new ItemStack(stackTag);
                if (stack.isEmpty()) continue;
                double x = nbt.getDouble("x" + i);
                double y = nbt.getDouble("y" + i);
                double z = nbt.getDouble("z" + i);
                items.add(new Quartet<>(stack, x, y, z));
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setInteger("count", items.size());
            for (int i = 0; i < items.size(); i++) {
                Quartet<ItemStack, Double, Double, Double> q = items.get(i);
                if (q == null) continue;
                ItemStack st = q.getW();
                if (st == null || st.isEmpty()) continue;
                NBTTagCompound stackTag = new NBTTagCompound();
                st.writeToNBT(stackTag);
                nbt.setTag("item" + i, stackTag);
                nbt.setDouble("x" + i, q.getX());
                nbt.setDouble("y" + i, q.getY());
                nbt.setDouble("z" + i, q.getZ());
            }
            return nbt;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation(blockFrame.getBaseModel()));
            ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();
            blockFrame.putTextures(textureMap);
            IModel retexturedModel = baseModel.retexture(textureMap.build());
            IBakedModel model = retexturedModel.bake(
                    ModelRotation.getModelRotation(0, 0), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
            );
            ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "inventory");
            event.getModelRegistry().putObject(modelLocation, model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(Objects.requireNonNull(this.getRegistryName()), "inventory"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerSprite(TextureMap map) {
        blockFrame.registerBlockTextures(map);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(loc, "inventory");
            }
        };
    }
}
