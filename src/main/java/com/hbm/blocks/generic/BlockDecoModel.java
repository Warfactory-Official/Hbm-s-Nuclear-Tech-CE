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
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        if(state.getBlock() == ModBlocks.filing_cabinet)
            return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(META)%2);
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    private static int orientationFromYaw(EntityLivingBase player) {
        int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        if ((i & 1) != 1) {
            return i >> 1; // North(0) and South(1)
        } else {
            return (i == 3) ? 2 : 3; // West(2) or East(3)
        }
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        int orient = orientationFromYaw(placer) & 3;
        int finalMeta = ((orient << 2) | (meta & 3)) & 15;
        return this.getDefaultState().withProperty(META, finalMeta);
    }

    private AxisAlignedBB getBoxFor(int orient) {
        return switch (orient) {
            case 0 -> // North
                    new AxisAlignedBB(1.0F - mxX, mnY, 1.0F - mxZ, 1.0F - mnX, mxY, 1.0F - mnZ);
            case 1 -> // South
                    new AxisAlignedBB(mnX, mnY, mnZ, mxX, mxY, mxZ);
            case 2 -> // West
                    new AxisAlignedBB(1.0F - mxZ, mnY, mnX, 1.0F - mnZ, mxY, mxX);
            case 3 -> // East
                    new AxisAlignedBB(mnZ, mnY, 1.0F - mxX, mxZ, mxY, 1.0F - mnX);
            default -> FULL_BLOCK_AABB;
        };
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        int meta = state.getValue(META);
        int orient = (meta >> 2) & 3;
        return getBoxFor(orient);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos,
                                      AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
                                      @Nullable Entity entityIn, boolean isActualState) {
        AxisAlignedBB bb = getBoundingBox(state, worldIn, pos);
        Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int meta = state.getValue(META) & 3;
        return Collections.singletonList(new ItemStack(Item.getItemFromBlock(this), 1, meta));
    }

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        if(coordBaseMode == 0) return meta;
        //N: 0b00, S: 0b01, W: 0b10, E: 0b11
        int rot = meta >> 2;
        int type = meta & 3;

        switch (coordBaseMode) {
            default -> {
            } //South
            case 1 -> { //West
                if ((rot & 3) < 2) //N & S can just have bits toggled
                    rot = rot ^ 3;
                else //W & E can just have first bit set to 0
                    rot = rot ^ 2;
            }
            case 2 -> //North
                    rot = rot ^ 1; //N, W, E & S can just have first bit toggled
            case 3 -> { //East
                if ((rot & 3) < 2)//N & S can just have second bit set to 1
                    rot = rot ^ 2;
                else //W & E can just have bits toggled
                    rot = rot ^ 3;
            }
        }
        //genuinely like. why did i do that
        return (rot << 2) | type; //To accommodate for BlockDecoModel's shift in the rotation bits; otherwise, simply bit-shift right and or any non-rotation meta after
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
        for (int m = 0; m < 4; m++) {
            ModelResourceLocation mrl = new ModelResourceLocation(getRegistryName(), "meta=" + m);
            event.getModelRegistry().putObject(mrl, baked);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                int meta = state.getValue(META) & 3;
                return new ModelResourceLocation(loc, "meta=" + meta);
            }
        };
    }
}
