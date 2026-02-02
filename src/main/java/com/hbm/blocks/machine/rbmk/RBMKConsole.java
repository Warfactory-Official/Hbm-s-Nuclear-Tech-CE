package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.BossSpawnHandler;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemGuideBook.BookType;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RBMKConsole extends BlockDummyable implements ITooltipProvider {

	public RBMKConsole(String s) {
		super(Material.IRON, s);
		this.setHardness(3F);
		this.setResistance(30F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= offset)
			return new TileEntityRBMKConsole();
		return null;
	}

	@Override
	public void neighborChanged(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos) {
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		if(!world.isRemote){
			if(state.getValue(META) >= offset){
				int power = world.getRedstonePowerFromNeighbors(pos);
				if(power > 0 && power <= 15){
					TileEntityRBMKConsole console = (TileEntityRBMKConsole) world.getTileEntity(pos);
					NBTTagCompound control = new NBTTagCompound();
					control.setDouble("level", (15D-power)/14D);

					for(int j = 0; j < console.columns.length; j++) {
						if(console.columns[j] != null && console.columns[j].type == ColumnType.CONTROL)
							control.setInteger("sel_" + j, j);
					}

					console.receiveControl(control);
				}
			}
		}
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
	public boolean onBlockActivated(World world, BlockPos bpos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(!player.isSneaking()) {

			BossSpawnHandler.markFBI(player);

			int[] pos = this.findCore(world, bpos.getX(), bpos.getY(), bpos.getZ());

			if(pos == null)
				return true;

			TileEntityRBMKConsole entity = (TileEntityRBMKConsole) world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
			if(entity != null) {

				if(facing.ordinal() == 1) {
					Vec3d vec = new Vec3d(1.375, 0, 0.75);
					int angle = 0;

                    switch (entity.getBlockMetadata() - offset) {
                        case 2 -> angle = 90;
                        case 3 -> angle = 270;
                        case 4 -> angle = 180;
                    }

					vec = vec.rotateYaw((float) Math.toRadians(angle));

					float hX = bpos.getX() + hitX;
					float hZ = bpos.getZ() + hitZ;
					double rX = entity.getPos().getX() + 0.5D + vec.x;
					double rZ = entity.getPos().getZ() + 0.5D + vec.z;
					double size = 0.1875D;

					if(Math.abs(hX - rX) < size && Math.abs(hZ - rZ) < size && !player.inventory.hasItemStack(new ItemStack(ModItems.book_guide, 1, BookType.RBMK.ordinal()))) {
						player.inventory.addItemStackToInventory(new ItemStack(ModItems.book_guide, 1, BookType.RBMK.ordinal()));
						player.inventoryContainer.detectAndSendChanges();
						return true;
					}
				}

				if(world.isRemote)
					FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			}

        }
        return true;
    }

	@Override
	public int[] getDimensions() {
		return new int[] {3, 0, 0, 0, 2, 2};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y, z + dir.offsetZ * o, new int[] {0, 0, 0, 1, 2, 2}, this, dir);
	}

	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {0, 0, 0, 1, 2, 2}, x, y, z, dir))
			return false;

		return super.checkRequirement(world, x, y, z, dir, o);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		this.addStandardInfo(list);
		super.addInformation(stack, worldIn, list, flagIn);
	}
}
