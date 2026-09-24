package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.InventoryHelper;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.machine.TileEntityFurnaceBrick;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Random;

public class MachineBrickFurnace extends BlockContainerBakeable {
    private final boolean isActive;
    private static boolean keepInventory;

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public MachineBrickFurnace(String s, boolean blockState) {
        super(Material.IRON, s, BlockBakeFrame.cube("machine_furnace_brick_top", "machine_furnace_brick_bottom",
                "machine_furnace_brick_side", blockState ? "machine_furnace_brick_front_on" : "machine_furnace_brick_front_off",
                "machine_furnace_brick_side", "machine_furnace_brick_side"));
        isActive = blockState;
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityFurnaceBrick();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(ModBlocks.machine_furnace_brick_off));
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.machine_furnace_brick_off);
    }
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (stack.hasDisplayName() && te instanceof TileEntityFurnaceBrick) {
            ((TileEntityFurnaceBrick) te).setCustomName(stack.getDisplayName());
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else if (!playerIn.isSneaking()) {
            TileEntityFurnaceBrick entity = (TileEntityFurnaceBrick) worldIn.getTileEntity(pos);
            if (entity != null) {
                playerIn.openGui(MainRegistry.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        } else {
            return false;
        }
    }

    public static void updateBlockState(boolean isProcessing, World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        TileEntity entity = world.getTileEntity(pos);
        keepInventory = true;
        IBlockState newState = Library.changeBlockState(ModBlocks.machine_furnace_brick_on, ModBlocks.machine_furnace_brick_off, state, FACING, isProcessing);
        if (newState != null) world.setBlockState(pos, newState, 3);
        keepInventory = false;

        if (entity != null) {
            entity.validate();
            world.setTileEntity(pos, entity);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!keepInventory) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te != null) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (handler == null && te instanceof TileEntityFurnaceBrick) {
                    handler = ((TileEntityFurnaceBrick) te).inventory;
                }
                if (handler != null) {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            ItemStack drop = stack.copy();
                            InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), drop);
                            if (handler instanceof IItemHandlerModifiable) {
                                ((IItemHandlerModifiable) handler).setStackInSlot(i, ItemStack.EMPTY);
                            }
                        }
                    }
                    worldIn.updateComparatorOutputLevel(pos, this);
                }
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (isActive) {
            EnumFacing facing = stateIn.getValue(FACING);
            double cX = pos.getX() + 0.5D;
            double cY = pos.getY() + rand.nextDouble() * 0.375D;
            double cZ = pos.getZ() + 0.5D;
            double off = 0.52D;
            double var = rand.nextDouble() * 0.6D - 0.3D;

            if (facing == EnumFacing.WEST) {
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, cX - off, cY, cZ + var, 0.0D, 0.0D, 0.0D);
                worldIn.spawnParticle(EnumParticleTypes.FLAME, cX - off, cY, cZ + var, 0.0D, 0.0D, 0.0D);
            } else if (facing == EnumFacing.EAST) {
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, cX + off, cY, cZ + var, 0.0D, 0.0D, 0.0D);
                worldIn.spawnParticle(EnumParticleTypes.FLAME, cX + off, cY, cZ + var, 0.0D, 0.0D, 0.0D);
            } else if (facing == EnumFacing.NORTH) {
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, cX + var, cY, cZ - off, 0.0D, 0.0D, 0.0D);
                worldIn.spawnParticle(EnumParticleTypes.FLAME, cX + var, cY, cZ - off, 0.0D, 0.0D, 0.0D);
            } else if (facing == EnumFacing.SOUTH) {
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, cX + var, cY, cZ + off, 0.0D, 0.0D, 0.0D);
                worldIn.spawnParticle(EnumParticleTypes.FLAME, cX + var, cY, cZ + off, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }
}
