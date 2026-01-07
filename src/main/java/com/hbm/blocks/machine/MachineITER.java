package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityITER;
import com.hbm.tileentity.machine.TileEntityITERStruct;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@Deprecated
public class MachineITER extends BlockDummyable {

	public static boolean drop = true;
	
	public MachineITER(String s) {
		super(Material.IRON, s);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if(meta >= 12)
			return new TileEntityITER();
		if(meta >= 6)
			return new TileEntityProxyCombo(true, true, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		//because we'll implement our own gnarly behavior here
		return new int[] { 0, 0, 0, 0, 0, 0 };
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos1, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			int[] pos = this.findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());

			if(pos == null)
				return false;

			TileEntityITER entity = (TileEntityITER) world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
			if(entity != null)
			{
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public static final int height = 2;
	
	@Override
	public void onBlockPlacedBy(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase player, @NotNull ItemStack itemStack) {
		if(!(player instanceof EntityPlayer))
			return;
		EnumHand hand = player.getHeldItemMainhand() == itemStack ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		EntityPlayer pl = (EntityPlayer) player;

		int o = getOffset();

		ForgeDirection dir = ForgeDirection.NORTH;

		if(i == 0)
		{
			dir = ForgeDirection.getOrientation(2);
		}
		if(i == 1)
		{
			dir = ForgeDirection.getOrientation(5);
		}
		if(i == 2)
		{
			dir = ForgeDirection.getOrientation(3);
		}
		if(i == 3)
		{
			dir = ForgeDirection.getOrientation(4);
		}

		dir = dir.getOpposite();

		world.setBlockToAir(pos);

		if(!checkRequirement(world, x, y, z, dir, o)) {

			if(!pl.capabilities.isCreativeMode) {
				ItemStack stack = pl.getHeldItem(hand);
				Item item = Item.getItemFromBlock(this);

				if(stack == null || stack.isEmpty()) {
					pl.setHeldItem(hand, new ItemStack(this));
				} else {
					if(stack.getItem() != item || stack.getCount() == stack.getMaxStackSize()) {
						pl.inventory.addItemStackToInventory(new ItemStack(this));
					} else {
						pl.getHeldItem(hand).grow(1);
					}
				}
			}

			return;
		}

		pl.getHeldItem(hand).shrink(1);

		world.setBlockState(new BlockPos(x + dir.offsetX * o , y + dir.offsetY * o + height, z + dir.offsetZ * o), this.getDefaultState().withProperty(META, dir.ordinal() + offset), 3);
		safeRem = true;
		fillSpace(world, x, y, z, dir, o);
		safeRem = false;
		world.scheduleUpdate(pos, this, 1);
		world.scheduleUpdate(pos, this, 2);
		
		super.onBlockPlacedBy(world, pos, state, player, itemStack);
	}
    //had to comment this out due to half of these blocks being non-existent now - SilentYeti
    /*
	@Override
	public void breakBlock(@NotNull World world, @NotNull BlockPos pos, IBlockState state) {
		int i = state.getValue(META);
		if(i >= 12 && drop) {

            for(int l = 0; l < 4; l++)
            	world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModBlocks.fusion_conductor, 64)));

        	world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModBlocks.fusion_conductor, 36)));
        	world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModBlocks.fusion_center, 64)));
        	world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModBlocks.fusion_motor, 4)));
        	world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModBlocks.reinforced_glass, 8)));
        	world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModBlocks.struct_iter_core, 1)));
    	}

		super.breakBlock(world, pos, state);
	}
	*/

	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;

		int[][][] layout = TileEntityITERStruct.collisionMask;

		for(int iy = 0; iy < 5; iy++) {

			int l = iy > 2 ? 4 - iy : iy;
			int[][] layer = layout[l];

			for(int ix = 0; ix < layer.length; ix++) {

				for(int iz = 0; iz < layer.length; iz++) {

					int ex = ix - layer.length / 2;
					int ez = iz - layer.length / 2;

					if(ex == 0 && y == 2 && ez == 0)
						continue;

					if(!world.getBlockState(new BlockPos(x + ex, y + iy, z + ez)).getBlock().canPlaceBlockAt(world, new BlockPos(x + ex, y + iy, z + ez))) {
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;

		int[][][] layout = TileEntityITERStruct.collisionMask;

		for(int iy = 0; iy < 5; iy++) {

			int l = iy > 2 ? 4 - iy : iy;
			int[][] layer = layout[l];

			for(int ix = 0; ix < layer.length; ix++) {

				for(int iz = 0; iz < layer[0].length; iz++) {

					int ex = ix - layer.length / 2;
					int ez = iz - layer.length / 2;

					int meta = 0;

					if(iy < 2) {
						meta = ForgeDirection.DOWN.ordinal();
					} else if(iy > 2) {
						meta = ForgeDirection.UP.ordinal();
					} else if(ex < 0) {
						meta = ForgeDirection.WEST.ordinal();
					} else if(ex > 0) {
						meta = ForgeDirection.EAST.ordinal();
					} else if(ez < 0) {
						meta = ForgeDirection.NORTH.ordinal();
					} else if(ez > 0) {
						meta = ForgeDirection.SOUTH.ordinal();
					} else {
						continue;
					}

					if(layout[l][ix][iz] > 0)
						world.setBlockState(new BlockPos(x + ex, y + iy, z + ez), this.getDefaultState().withProperty(META, meta), 3);
				}
			}
		}
		this.makeExtra(world, x, y, z);
		this.makeExtra(world, x, y + 4, z);

		Vec3d vec = new Vec3d(5.75, 0, 0);

		for(int i = 0; i < 16; i++) {
			vec = vec.rotateYaw((float) (Math.PI / 8));
			this.makeExtra(world, x + (int)vec.x, y, z + (int)vec.z);
			this.makeExtra(world, x + (int)vec.x, y + 4, z + (int)vec.z);
		}
	}
	
	@Override
	public int getOffset() {
		return 7;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.AIR;
	}

}
