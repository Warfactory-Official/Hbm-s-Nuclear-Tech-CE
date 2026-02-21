package com.hbm.blocks.machine;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMicrowave;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MachineMicrowave extends BlockMachineBase {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public static final AxisAlignedBB BOX_WEST_AABB = new AxisAlignedBB(1.0D, 0.0D, 0.875D, 0.25D, 0.4375D, 0.125D);
    public static final AxisAlignedBB BOX_NORTH_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.25D, 0.875D, 0.4375D, 1.0D);
    public static final AxisAlignedBB BOX_EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.875D, 0.75D, 0.4375D, 0.125D);
    public static final AxisAlignedBB BOX_SOUTH_AABB = new AxisAlignedBB(0.875D, 0.0D, 0.75D, 0.125D, 0.4375D, 0.0D);

	public MachineMicrowave(Material materialIn, String s) {
		super(materialIn, 0, s);
	}

	@Override
	protected boolean rotatable() {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityMicrowave();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			return true;

		} else {
			return false;
		}
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
        EnumFacing te = state.getValue(FACING);
        switch (te) {
            case WEST -> addCollisionBoxToList(pos, entityBox, collidingBoxes, BOX_WEST_AABB);
            case NORTH -> addCollisionBoxToList(pos, entityBox, collidingBoxes, BOX_NORTH_AABB);
            case EAST -> addCollisionBoxToList(pos, entityBox, collidingBoxes, BOX_EAST_AABB);
            case SOUTH -> addCollisionBoxToList(pos, entityBox, collidingBoxes, BOX_SOUTH_AABB);
        };
    }

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing te = state.getValue(FACING);

        return switch (te) {
            case WEST -> BOX_WEST_AABB;
            case NORTH -> BOX_NORTH_AABB;
            case EAST -> BOX_EAST_AABB;
            case SOUTH -> BOX_SOUTH_AABB;
            default -> FULL_BLOCK_AABB;
        };
    }
}
