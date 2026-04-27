package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.machine.ItemScraps;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.Compat;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Random;

public class BlockDynamicSlag extends BlockContainer {

	private HashMap<NTMMaterial,TextureAtlasSprite> iconMap = new HashMap();

	public BlockDynamicSlag(String s) {
		super(Material.IRON);
		this.useNeighborBrightness = true;
		setTranslationKey(s);
		setRegistryName(s);
		setHarvestLevel("pickaxe", 0);
		setCreativeTab(MainRegistry.controlTab);
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySlag();
	}

	/*@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		super.registerBlockIcons(reg);

		if(reg instanceof TextureMap) {
			TextureMap map = (TextureMap) reg;

			for(NTMMaterial mat : Mats.orderedList) {
				if(mat.solidColorLight != mat.solidColorDark) {
					String placeholderName = this.getTextureName() + "-" + mat.names[0];
					TextureAtlasSpriteMutatable mutableIcon = new TextureAtlasSpriteMutatable(placeholderName, new RGBMutatorInterpolatedComponentRemap(0xFFFFFF, 0x505050, mat.solidColorLight, mat.solidColorDark)).setBlockAtlas();
					map.setTextureEntry(placeholderName, mutableIcon);
					iconMap.put(mat, mutableIcon);
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {

		TileEntitySlag tile = (TileEntitySlag) world.getTileEntity(x, y, z);

		if(tile != null && tile.mat != null) {
			IIcon override = iconMap.get(tile.mat);
			if(override != null) {
				return override;
			}
		}

		return this.blockIcon;
	}*/

	/*@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {

		TileEntitySlag tile = (TileEntitySlag) world.getTileEntity(x, y, z);

		if(tile != null && tile.mat != null) {
			if(!iconMap.containsKey(tile.mat)) {
				return tile.mat.moltenColor;
			}
		}

		return 0xffffff;
	}*/

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	/*@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		TileEntitySlag tile = (TileEntitySlag) world.getTileEntity(x, y, z);
		if(tile != null) {
			this.setBlockBounds(0F, 0F, 0F, 1F, (float) tile.amount / (float) TileEntitySlag.maxAmount, 1F);
		}
	}*/

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {
		TileEntitySlag tile = (TileEntitySlag)world.getTileEntity(pos);
		if(tile != null) {
			return new AxisAlignedBB(0F, 0F, 0F, 1F, (float) tile.amount / (float) TileEntitySlag.maxAmount, 1F);
		}
		return super.getBoundingBox(state,world,pos);
	}

	/*@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		TileEntitySlag tile = (TileEntitySlag) world.getTileEntity(x, y, z);
		if(tile != null) {
			this.setBlockBounds(0F, 0F, 0F, 1F, (float) tile.amount / (float) TileEntitySlag.maxAmount, 1F);
		}
		return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
	}*/

	@Override
	public void updateTick(World world,BlockPos pos,IBlockState state,Random rand) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		TileEntity s = Compat.getTileStandard(world, x, y, z);
		TileEntity b = Compat.getTileStandard(world, x, y - 1, z);

		/* Error here, delete the block */
		if(s == null || !(s instanceof TileEntitySlag)) {
			world.setBlockToAir(new BlockPos(x, y, z));
			return;
		}

		TileEntitySlag self = (TileEntitySlag) s;

		self.networkPackNT(60);

		/* Flow down */
		if(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock().isReplaceable(world, new BlockPos(x, y - 1, z)) && y > 0) {
			world.setBlockState(new BlockPos(x, y - 1, z), ModBlocks.slag.getDefaultState());
			TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, x, y - 1, z);
			tile.mat = self.mat;
			tile.amount = self.amount;
			//world.markBlockForUpdate(x, y - 1, z);
			world.scheduleUpdate(new BlockPos(x, y - 1, z),this,1);
			world.setBlockToAir(new BlockPos(x, y, z));
			return;
		} else if(b instanceof TileEntitySlag) {

			TileEntitySlag below = (TileEntitySlag) b;

			if(below.mat == self.mat && below.amount < TileEntitySlag.maxAmount) {
				int transfer = Math.min(TileEntitySlag.maxAmount - below.amount, self.amount);
				below.amount += transfer;
				self.amount -= transfer;

				if(self.amount <= 0){
					world.setBlockToAir(new BlockPos(x, y, z));
				} else {
					//world.markBlockForUpdate(x, y, z);
					world.scheduleUpdate(new BlockPos(x, y, z),this,1);
				}

				//world.markBlockForUpdate(x, y - 1, z);
				//world.scheduleBlockUpdate(x, y - 1, z, ModBlocks.slag, 1);
				world.scheduleUpdate(new BlockPos(x, y - 1, z), ModBlocks.slag, 1);
				return;
			}
		}

		/* Flow sideways, no neighbors */
		ForgeDirection[] sides = new ForgeDirection[] { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST };
		int count = 0;
		for(ForgeDirection dir : sides) {
			int iX = x + dir.offsetX;
			int iZ = z + dir.offsetZ;

			if(world.getBlockState(new BlockPos(iX, y, iZ)).getBlock().isReplaceable(world, new BlockPos(iX, y, iZ))) {
				count++;
			}
		}

		if(self.amount >= self.maxAmount / 5 && count > 0) {
			int toSpread = Math.max(self.amount / (count * 2), 1);

			for(ForgeDirection dir : sides) {
				int iX = x + dir.offsetX;
				int iZ = z + dir.offsetZ;

				if(world.getBlockState(new BlockPos(iX, y, iZ)).getBlock().isReplaceable(world, new BlockPos(iX, y, iZ))) {
					world.setBlockState(new BlockPos(iX, y, iZ), ModBlocks.slag.getDefaultState());
					TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, iX, y, iZ);
					// world.markBlockForUpdate(iX, y, iZ);
					//world.scheduleBlockUpdate(iX, y, iZ, ModBlocks.slag, 1);
					world.scheduleUpdate(new BlockPos(iX,y,iZ),this,1);
					tile.mat = self.mat;
					tile.amount = toSpread;
					self.amount -= toSpread;
					// world.markBlockForUpdate(x, y, z);
					world.scheduleUpdate(new BlockPos(x,y,z),this,1);
				}
			}
		}
	}

	@Override
	public void onBlockHarvested(World world,BlockPos pos,IBlockState state,EntityPlayer player) {
		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			this.dropBlockAsItem(world, pos, state,  0);
			harvesters.set(null);
		}
	}

	@Override
	public void harvestBlock(World worldIn,EntityPlayer player,BlockPos pos,IBlockState state,@Nullable TileEntity te,ItemStack stack) {
		//player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
		// idk what the hell is this ^
		player.addExhaustion(0.025F);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops,IBlockAccess world,BlockPos pos,IBlockState state,int fortune) {
		TileEntitySlag tile = (TileEntitySlag) world.getTileEntity(pos);
		if(tile != null && tile.mat != null && tile.amount > 0)
			drops.add(ItemScraps.create(new MaterialStack(tile.mat, tile.amount)));
	}

	@Override
	public ItemStack getPickBlock(IBlockState state,RayTraceResult target,World world,BlockPos pos,EntityPlayer player) {
		TileEntitySlag tile = (TileEntitySlag) world.getTileEntity(pos);

		if(tile != null) {
			return ItemScraps.create(new MaterialStack(tile.mat, tile.amount));
		}

		return super.getPickBlock(state,target,world,pos,player);
	}

	// CE addition
	@Override
	public EnumPushReaction getPushReaction(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}
	// CE addition end

	@AutoRegister
	public static class TileEntitySlag extends TileEntityLoadedBase {

		public NTMMaterial mat;
		public int amount;
		public static int maxAmount = MaterialShapes.BLOCK.q(16);

		@Override
		public void serialize(ByteBuf buf) {
			buf.writeInt(mat.id);
			buf.writeInt(amount);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			mat = Mats.matById.get(buf.readInt());
			amount = buf.readInt();
		}
		/*
		@Override
		public Packet getDescriptionPacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
		}

		@Override
		public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.func_148857_g());
		}*/

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.mat = Mats.matById.get(nbt.getInteger("mat"));
			this.amount = nbt.getInteger("amount");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			if(this.mat != null) nbt.setInteger("mat", this.mat.id);
			nbt.setInteger("amount", this.amount);
			return super.writeToNBT(nbt);
		}
	}
}
