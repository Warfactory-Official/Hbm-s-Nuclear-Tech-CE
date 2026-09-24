package com.hbm.blocks.generic;

import com.hbm.Tags;
import com.hbm.blocks.machine.BlockContainerBakeable;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.render.icon.PaddedSpriteUtil;
import com.hbm.render.icon.PaddedSpriteUtil.TextureInfo;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.StaticMetaWavefrontBakedModel;
import com.hbm.render.model.StaticWavefrontItemBakedModel;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSkeletonHolder extends BlockContainerBakeable {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    private static final ResourceLocation MODEL = new ResourceLocation(Tags.MODID, "models/blocks/skeleton_holder.obj");
    private static final ResourceLocation SPRITE = new ResourceLocation(Tags.MODID, "particle/skeleton");
    private static final float[] YAWS_BY_META = {0.0F, 270.0F, 180.0F, 90.0F};

    public BlockSkeletonHolder(String regName, BlockBakeFrame blockFrame) {
        super(Material.ROCK, regName, blockFrame);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySkeletonHolder();
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntitySkeletonHolder();
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
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(meta & 3);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0,
                new ModelResourceLocation(this.getRegistryName(), "inventory"));
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

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        PaddedSpriteUtil.register(map, PaddedSpriteUtil.inspectTexture(SPRITE));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        HFRWavefrontObject wavefront = new HFRWavefrontObject(MODEL);
        TextureInfo texture = PaddedSpriteUtil.inspectTexture(SPRITE);
        TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite sprite = PaddedSpriteUtil.sprite(atlas, texture);

        IBakedModel worldModel = new StaticMetaWavefrontBakedModel(wavefront, sprite, YAWS_BY_META,
                new String[]{"Holder1"}, 0.0F, 0.0F, false, texture.uScale, texture.vScale,
                -0.5F, 0.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        IBakedModel itemModel = new StaticWavefrontItemBakedModel(wavefront, sprite, new String[]{"Holder1"}, 1.0F,
                0.0F, false, texture.uScale, texture.vScale, 0.0F, 0.0F, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D,
                -0.5F, 0.0F, -0.5F, 0.0F, 0.0F, 0.0F);

        event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName(), "normal"), worldModel);
        event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName(), "inventory"), itemModel);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
        int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        EnumFacing facing;
        switch (i) {
            default:
            case 0: facing = EnumFacing.EAST; break;
            case 1: facing = EnumFacing.SOUTH; break;
            case 2: facing = EnumFacing.WEST; break;
            case 3: facing = EnumFacing.NORTH; break;
        }
        world.setBlockState(pos, state.withProperty(FACING, facing), 2);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) return true;
        if (player.isSneaking()) return false;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntitySkeletonHolder)) return false;
        TileEntitySkeletonHolder pedestal = (TileEntitySkeletonHolder) te;

        ItemStack held = player.getHeldItem(hand);

        if (pedestal.item.isEmpty() && !held.isEmpty()) {
            if (world.isRemote) return true;
            pedestal.item = held.copy();
            player.setHeldItem(hand, ItemStack.EMPTY);
            pedestal.markDirty();
            world.notifyBlockUpdate(pos, state, state, 3);
            return true;
        } else if (!pedestal.item.isEmpty() && held.isEmpty()) {
            if (world.isRemote) return true;
            player.setHeldItem(hand, pedestal.item.copy());
            pedestal.item = ItemStack.EMPTY;
            pedestal.markDirty();
            world.notifyBlockUpdate(pos, state, state, 3);
            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntitySkeletonHolder) {
                TileEntitySkeletonHolder entity = (TileEntitySkeletonHolder) te;
                if (!entity.item.isEmpty()) {
                    EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, entity.item.copy());
                    world.spawnEntity(item);
                }
            }
        }
        super.breakBlock(world, pos, state);
    }
    @AutoRegister
    public static class TileEntitySkeletonHolder extends TileEntity {
        public ItemStack item = ItemStack.EMPTY;

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            if (!this.item.isEmpty()) {
                NBTTagCompound stack = new NBTTagCompound();
                this.item.writeToNBT(stack);
                nbt.setTag("item", stack);
            }
            return nbt;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            if (nbt.hasKey("item", 10)) {
                this.item = new ItemStack(nbt.getCompoundTag("item"));
            } else {
                this.item = ItemStack.EMPTY;
            }
        }

        @Override
        public SPacketUpdateTileEntity getUpdatePacket() {
            NBTTagCompound nbt = new NBTTagCompound();
            this.writeToNBT(nbt);
            return new SPacketUpdateTileEntity(this.pos, 0, nbt);
        }

        @Override
        public NBTTagCompound getUpdateTag() {
            NBTTagCompound nbt = super.getUpdateTag();
            this.writeToNBT(nbt);
            return nbt;
        }

        @Override
        public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
            this.readFromNBT(pkt.getNbtCompound());
        }
    }
}
