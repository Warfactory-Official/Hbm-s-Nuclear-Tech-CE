package com.hbm.blocks.network;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.tool.ItemTooling;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityCraneBase;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

//mlbv: i have zero fucking idea how the direction ids are mapped to EnumFacing..
//TODO: implement IBlockSideRotation
public abstract class BlockCraneBase extends BlockContainer implements IToolable, ITooltipProvider {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public BlockCraneBase(Material mat) {
        super(mat);
    }

    @Override
    public abstract TileEntityCraneBase createNewTileEntity(World worldIn, int meta);

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(playerIn.getHeldItem(hand).getItem() instanceof ItemTooling) {
            return false;
        } else if(worldIn.isRemote) {
            return true;
        } else if(!playerIn.isSneaking()) {
            playerIn.openGui(MainRegistry.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) return false;
        if (!(world.getTileEntity(new BlockPos(x, y, z)) instanceof TileEntityCraneBase craneTileEntity)) return false;
        if (player.isSneaking()) {
            craneTileEntity.setOutputOverride(player.getHorizontalFacing().getOpposite());
        } else {
            craneTileEntity.setInput(player.getHorizontalFacing().getOpposite());
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof IInventory) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory)tileentity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    private final Random rand = new Random();
    public void dropContents(World world, BlockPos pos, IBlockState state, int start, int end) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof ISidedInventory) {
            ISidedInventory tileentityfurnace = (ISidedInventory) tileEntity;
            if(tileentityfurnace != null) {

                for(int i1 = start; i1 < end; ++i1) {
                    ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

                    if(itemstack != null) {
                        float f = this.rand.nextFloat() * 0.8F + 0.1F;
                        float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
                        float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

                        while(itemstack.getCount() > 0) {
                            int j1 = this.rand.nextInt(21) + 10;

                            if(j1 > itemstack.getCount()) {
                                j1 = itemstack.getCount();
                            }

                            itemstack.shrink(j1);
                            EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));

                            if(itemstack.hasTagCompound()) {
                                entityitem.getItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
                            }

                            float f3 = 0.05F;
                            entityitem.motionX = (float) this.rand.nextGaussian() * f3;
                            entityitem.motionY = (float) this.rand.nextGaussian() * f3 + 0.2F;
                            entityitem.motionZ = (float) this.rand.nextGaussian() * f3;
                            world.spawnEntity(entityitem);
                        }
                    }
                }

                world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);
            }
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byHorizontalIndex(meta);
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
    }
}
