package com.hbm.blocks.machine;

import com.hbm.main.MainRegistry;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.render.block.RotatableStateMapper;
import com.hbm.tileentity.machine.TileEntityReactorControl;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class MachineReactorControl extends BlockContainerBakeable {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public MachineReactorControl(String name) {
        super(Material.IRON, name, BlockBakeFrame.cube("machine_controller_top", "machine_controller_top", "machine_controller_back", "machine_controller", "machine_controller_side", "machine_controller_side"));
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public @NotNull IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withProperty(FACING, mirrorIn.mirror(state.getValue(FACING)));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel() {
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(this),
                    facing.getHorizontalIndex(),
                    new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "facing=" + facing.getName())
            );
        }
        ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(this),
                0,
                new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory")
        );
    }

    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new RotatableStateMapper(loc);
    }

    @Override
    public @NotNull IBlockState getStateForPlacement(World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, @NotNull BlockPos pos, IBlockState state, EntityLivingBase placer, @NotNull ItemStack stack) {
        if(stack.hasDisplayName())
        {
            ((TileEntityReactorControl)worldIn.getTileEntity(pos)).setCustomName(stack.getDisplayName());
        }
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityReactorControl();
    }

    @NotNull
    @Override
    public Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null) {
            IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); ++i) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        float f = worldIn.rand.nextFloat() * 0.8F + 0.1F;
                        float f1 = worldIn.rand.nextFloat() * 0.8F + 0.1F;
                        float f2 = worldIn.rand.nextFloat() * 0.8F + 0.1F;

                        ItemStack stackCopy = stack.copy();
                        while (!stackCopy.isEmpty()) {
                            int j1 = worldIn.rand.nextInt(21) + 10;
                            if (j1 > stackCopy.getCount()) {
                                j1 = stackCopy.getCount();
                            }

                            ItemStack dropStack = stackCopy.splitStack(j1);
                            EntityItem entityitem = new EntityItem(
                                    worldIn,
                                    pos.getX() + f,
                                    pos.getY() + f1,
                                    pos.getZ() + f2,
                                    dropStack
                            );

                            float f3 = 0.05F;
                            entityitem.motionX = worldIn.rand.nextGaussian() * f3;
                            entityitem.motionY = worldIn.rand.nextGaussian() * f3 + 0.2F;
                            entityitem.motionZ = worldIn.rand.nextGaussian() * f3;

                            if (!worldIn.isRemote) {
                                worldIn.spawnEntity(entityitem);
                            }
                        }
                    }
                }
                worldIn.updateComparatorOutputLevel(pos, this);
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(world.isRemote)
        {
            return true;
        } else if(!player.isSneaking())
        {
            TileEntityReactorControl entity = (TileEntityReactorControl) world.getTileEntity(pos);
            if(entity != null)
            {
                FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World worldIn, BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityReactorControl) {
            TileEntityReactorControl entity = (TileEntityReactorControl) te;
            return (int) Math.ceil((double) entity.heat * 15D / 50000D);
        }
        return 0;
    }
}
