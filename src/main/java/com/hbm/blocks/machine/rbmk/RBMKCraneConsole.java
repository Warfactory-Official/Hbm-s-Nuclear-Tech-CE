package com.hbm.blocks.machine.rbmk;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKCraneConsole;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;


public class RBMKCraneConsole extends BlockDummyable implements IToolable {

	public RBMKCraneConsole(String s) {
		super(Material.IRON, s);
		this.setHardness(3F);
		this.setResistance(30F);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if(meta >= offset)
			return new TileEntityRBMKCraneConsole();
		return null;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isOpaqueCube(@NotNull IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isBlockNormalCube(@NotNull IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(@NotNull IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
		return false;
	}
	@Override
	public boolean shouldSideBeRendered(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos, @NotNull EnumFacing side) {
		return false;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 0, 0, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y, z + dir.offsetZ * o, new int[] {0, 0, 0, 1, 1, 1}, this, dir);
	}

	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {0, 0, 0, 1, 1, 1}, x, y, z, dir))
			return false;
		
		return super.checkRequirement(world, x, y, z, dir, o);
	}

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        if(tool == ToolType.SCREWDRIVER) {
            if(world.isRemote) return true;
            TileEntityRBMKCraneConsole tile = (TileEntityRBMKCraneConsole) findCoreTE(world, x, y, z);
            if(tile == null) return false;
            tile.cycleCraneRotation();
            tile.markDirty();
            return true;
        }
        return false;
    }
}
