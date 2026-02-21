package com.hbm.blocks.machine.rbmk;

import com.hbm.Tags;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemRBMKLid;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public abstract class RBMKBase extends BlockDummyable implements IToolable, ITooltipProvider {

	public static boolean dropLids = true;
	public static boolean digamma = false;
	public ResourceLocation coverTexture;
	public ResourceLocation columnTexture;
	
	public RBMKBase(String s, String columnTexture){
		super(Material.IRON, s);
		coverTexture = new ResourceLocation(Tags.MODID, "textures/blocks/rbmk/" + s + ".png");
		this.columnTexture = new ResourceLocation(Tags.MODID, "textures/blocks/rbmk/" + columnTexture +".png");
	}
	
	@Override
	public int[] getDimensions() {
		return new int[] {3, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        this.addStandardInfo(tooltip);
    }
	
	public boolean openInv(World world, int x, int y, int z, EntityPlayer player, EnumHand hand) {
		
		if(world.isRemote) {
			return true;
		}
		
		int[] pos = this.findCore(world, x, y, z);
		
		if(pos == null)
			return false;
		
		TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
		
		if(!(te instanceof TileEntityRBMKBase))
			return false;
		
		TileEntityRBMKBase rbmk = (TileEntityRBMKBase) te;
		
		if(player.getHeldItem(hand) != null && player.getHeldItem(hand).getItem() instanceof ItemRBMKLid) {
			
			if(!rbmk.hasLid())
				return false;
		}
		
		if(!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			return true;
		} else {
			return true;
		}
	}

	@Override
	public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos bpos){
		float height = 0.0F;
		
		int[] pos = this.findCore(source, bpos.getX(), bpos.getY(), bpos.getZ());
		
		if(pos != null) {
			TileEntity te = source.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
			
			if(te instanceof TileEntityRBMKBase) {
				
				TileEntityRBMKBase rbmk = (TileEntityRBMKBase) te;
				
				if(rbmk.hasLid()) {
					height += 0.25F;
				}
			}
		}
		return new AxisAlignedBB(0, 0, 0, 1, 1+height, 1);
	}
	
	/*
	 * NORTH: no cover
	 * EAST: concrete cover
	 * SOUTH: lead glass cover
	 * WEST: UNUSED
	 */

	public static final ForgeDirection DIR_NO_LID = ForgeDirection.NORTH;
	public static final ForgeDirection DIR_NORMAL_LID = ForgeDirection.EAST;
	public static final ForgeDirection DIR_GLASS_LID = ForgeDirection.SOUTH;
	
	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o){
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(world), this, dir);
		this.makeExtra(world, x, y + RBMKDials.getColumnHeight(world), z);
	}
	
	@Override
	public ForgeDirection getDirModified(ForgeDirection dir) {
		return DIR_NO_LID;
	}
	
	public int[] getDimensions(World world) {
		return new int[] {RBMKDials.getColumnHeight(world), 0, 0, 0, 0, 0};
	}
	
	@Override
	public void breakBlock(@NotNull World world, @NotNull BlockPos pos, IBlockState state){
		if(!world.isRemote && dropLids) {
			int i = state.getValue(META);
			if(i == DIR_NORMAL_LID.ordinal() + offset) {
				world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5 + RBMKDials.getColumnHeight(world), pos.getZ() + 0.5, new ItemStack(ModItems.rbmk_lid)));
			}
			if(i == DIR_GLASS_LID.ordinal() + offset) {
				world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5 + RBMKDials.getColumnHeight(world), pos.getZ() + 0.5, new ItemStack(ModItems.rbmk_lid_glass)));
			}
		}
		
		super.breakBlock(world, pos, state);
	}

    @Override
    @Nullable
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        final double px = pos.getX(), py = pos.getY(), pz = pos.getZ();
        final Vec3d s = start.subtract(px, py, pz);
        final Vec3d e = end.subtract(px, py, pz);

        final AxisAlignedBB bb = state.getBoundingBox(world, pos);
        RayTraceResult local = bb.calculateIntercept(s, e);
        if (local == null) return null;

        Vec3d hitWorld = local.hitVec.add(px, py, pz);

        final double EPS = 1.0e-7;
        final double hx = local.hitVec.x;
        final double hy = local.hitVec.y;
        final double hz = local.hitVec.z;
        EnumFacing face;
        if (Math.abs(hy - bb.maxY) <= EPS) {
            face = EnumFacing.UP;
        } else if (Math.abs(hy - bb.minY) <= EPS) {
            face = EnumFacing.DOWN;
        } else if (Math.abs(hx - bb.maxX) <= EPS) {
            face = EnumFacing.EAST;
        } else if (Math.abs(hx - bb.minX) <= EPS) {
            face = EnumFacing.WEST;
        } else if (Math.abs(hz - bb.maxZ) <= EPS) {
            face = EnumFacing.SOUTH;
        } else if (Math.abs(hz - bb.minZ) <= EPS) {
            face = EnumFacing.NORTH;
        } else {
            face = local.sideHit;
        }
        boolean goingDown = e.y < s.y;
        boolean insideTallCap = s.x > bb.minX - EPS && s.x < bb.maxX + EPS && s.z > bb.minZ - EPS && s.z < bb.maxZ + EPS && s.y >= 1.0 - EPS && s.y <= bb.maxY + EPS;

        if (face == EnumFacing.DOWN && goingDown && insideTallCap) {
            face = EnumFacing.UP;
            hitWorld = new Vec3d(hitWorld.x, py + Math.min(1.0, bb.maxY), hitWorld.z);
        }
        return new RayTraceResult(hitWorld, face, pos);
    }
	
	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool){
		if(tool != ToolType.SCREWDRIVER)
			return false;
		
		int[] pos = this.findCore(world, x, y, z);
		
		if(pos != null) {
			TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
			
			if(te instanceof TileEntityRBMKBase) {
				
				TileEntityRBMKBase rbmk = (TileEntityRBMKBase) te;
				int i = rbmk.getBlockMetadata();
				
				if(rbmk.hasLid() && rbmk.isLidRemovable()) {
					
					if(!world.isRemote) {
						if(i == DIR_NORMAL_LID.ordinal() + offset) {
							world.spawnEntity(new EntityItem(world, pos[0] + 0.5, pos[1] + 0.5 + RBMKDials.getColumnHeight(world), pos[2] + 0.5, new ItemStack(ModItems.rbmk_lid)));
						}
						if(i == DIR_GLASS_LID.ordinal() + offset) {
							world.spawnEntity(new EntityItem(world, pos[0] + 0.5, pos[1] + 0.5 + RBMKDials.getColumnHeight(world), pos[2] + 0.5, new ItemStack(ModItems.rbmk_lid_glass)));
						}
						
						world.setBlockState(new BlockPos(pos[0], pos[1], pos[2]), this.getDefaultState().withProperty(META, DIR_NO_LID.ordinal() + BlockDummyable.offset), 3);
						NBTTagCompound nbt = rbmk.writeToNBT(new NBTTagCompound());
						world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2])).readFromNBT(nbt);
					}
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		return meta;
	}
}
