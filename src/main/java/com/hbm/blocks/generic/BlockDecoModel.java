package com.hbm.blocks.generic;

import com.hbm.Tags;
import com.hbm.blocks.BlockEnumMeta;
import com.hbm.blocks.ModBlocks;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.BlockDecoBakedModel;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BlockDecoModel<E extends Enum<E>> extends BlockEnumMeta<E> implements INBTBlockTransformable {

    private float mnX = 0.0F;
    private float mnY = 0.0F;
    private float mnZ = 0.0F;
    private float mxX = 1.0F;
    private float mxY = 1.0F;
    private float mxZ = 1.0F;

    private ResourceLocation objModelLocation;
    public BlockDecoModel(Material mat, SoundType type, String registryName,
                          E[] blockEnum, boolean multiName, boolean multiTexture,
                          ResourceLocation objModelLocation) {
        super(mat, type, registryName, blockEnum, multiName, multiTexture);
        this.objModelLocation = objModelLocation;
    }

    public BlockDecoModel(Material mat, SoundType type, String registryName,
                          E[] blockEnum, boolean multiName, boolean multiTexture) {
        super(mat, type, registryName, blockEnum, multiName, multiTexture);
    }


    //FIXME: This is a hack, the mapping for items should be flat (first model occupies 0-3, seconds occupies 3-7, etc)
    @Override
    public @NotNull ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        if(state.getBlock() == ModBlocks.filing_cabinet)
            return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(META) & 3);
        if(state.getBlock() == ModBlocks.deco_computer)
            return new ItemStack(Item.getItemFromBlock(this), 1, 0);
        else
            return super.getPickBlock(state, target, world, pos, player);
    }

    public BlockDecoModel setBlockBoundsTo(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        mnX = minX;
        mnY = minY;
        mnZ = minZ;
        mxX = maxX;
        mxY = maxY;
        mxZ = maxZ;
        return this;
    }

    @Override
    public @NotNull BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public @NotNull IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                                     float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer, @NotNull EnumHand hand) {
        int i = MathHelper.floor(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int orient;

        if((i & 1) != 1)
            orient = i >> 1;
        else {
            if(i == 3) orient = 2;
            else orient = 3;
        }

        int finalMeta = (orient << 2) | (meta & 3);
        return this.getDefaultState().withProperty(META, finalMeta);
    }

    private AxisAlignedBB getBoxFor(int orient) {
        return switch (orient) {
            case 0 -> new AxisAlignedBB(1.0F - mxX, mnY, 1.0F - mxZ, 1.0F - mnX, mxY, 1.0F - mnZ);
            case 1 -> new AxisAlignedBB(mnX, mnY, mnZ, mxX, mxY, mxZ);
            case 2 -> new AxisAlignedBB(1.0F - mxZ, mnY, mnX, 1.0F - mnZ, mxY, mxX);
            case 3 -> new AxisAlignedBB(mnZ, mnY, 1.0F - mxX, mxZ, mxY, 1.0F - mnX);
            default -> FULL_BLOCK_AABB;
        };
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        int meta = state.getValue(META);
        int orient = (meta >> 2) & 3;
        return getBoxFor(orient);
    }

    @Override
    public void addCollisionBoxToList(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos,
                                      @NotNull AxisAlignedBB entityBox, @NotNull List<AxisAlignedBB> collidingBoxes,
                                      @Nullable Entity entityIn, boolean isActualState) {
        AxisAlignedBB bb = getBoundingBox(state, worldIn, pos);
        Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int meta = state.getValue(META) & 3;
        return Collections.singletonList(new ItemStack(Item.getItemFromBlock(this), 1, meta));
    }

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaDecoModelHigh(meta, coordBaseMode);
    }

    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/deco_computer"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        HFRWavefrontObject wavefront = new HFRWavefrontObject(objModelLocation);
        TextureAtlasSprite sprite = Minecraft.getMinecraft()
                .getTextureMapBlocks()
                .getAtlasSprite(new ResourceLocation("hbm", "blocks/deco_computer").toString());
        IBakedModel baked = BlockDecoBakedModel.forBlock(wavefront, sprite);

        for (int m = 0; m < 16; m++) {
            ModelResourceLocation mrl = new ModelResourceLocation(getRegistryName(), "meta=" + m);
            event.getModelRegistry().putObject(mrl, baked);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
                return new ModelResourceLocation(loc, "meta=" + state.getValue(META));
            }
        };
    }
}