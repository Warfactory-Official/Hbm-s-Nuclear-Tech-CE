package com.hbm.blocks.gas;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class BlockGasBase extends Block {

    float red;
    float green;
    float blue;

    public BlockGasBase(float r, float g, float b, String s) {
        super(ModBlocks.materialGas);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHardness(0.0F);
        this.setResistance(0.0F);
        this.setTickRandomly(true);
        this.lightOpacity = 0;
        this.red = r;
        this.green = g;
        this.blue = b;

        ModBlocks.ALL_BLOCKS.add(this);
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
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return false;
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return Collections.emptyList();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        // no-op
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote) {
            if (world.rand.nextInt(2) == 0) {
                if (!tryMove(world, pos.getX(), pos.getY(), pos.getZ(), getFirstDirection(world, pos.getX(), pos.getY(), pos.getZ())))
                    tryMove(world, pos.getX(), pos.getY(), pos.getZ(), getSecondDirection(world, pos.getX(), pos.getY(), pos.getZ()));
            }
        }
    }

    public abstract ForgeDirection getFirstDirection(World world, int x, int y, int z);

    public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
        return getFirstDirection(world, x, y, z);
    }

    public boolean tryMove(World world, int x, int y, int z, ForgeDirection dir) {
        BlockPos newPos = new BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);

        if (!world.isBlockLoaded(newPos)) {
            return false;
        } else if (world.isAirBlock(newPos)) {
            world.setBlockToAir(new BlockPos(x, y, z));
            world.setBlockState(newPos, this.getDefaultState());
            return true;
        }

        return false;
    }

    public int getDelay(World world) {
        return 20;
    }

    public ForgeDirection randomHorizontal(World world) {
        return ForgeDirection.getOrientation(world.rand.nextInt(4) + 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);
        EntityPlayer p = MainRegistry.proxy.me();
        if (ArmorUtil.checkArmorPiece(p, ModItems.ashglasses, 3)) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "vanillaExt");
            data.setString("mode", "cloud");
            data.setDouble("posX", pos.getX() + 0.5);
            data.setDouble("posY", pos.getY() + 0.5);
            data.setDouble("posZ", pos.getZ() + 0.5);
            data.setFloat("r", red);
            data.setFloat("g", green);
            data.setFloat("b", blue);
            MainRegistry.proxy.effectNT(data);
        }
    }
}