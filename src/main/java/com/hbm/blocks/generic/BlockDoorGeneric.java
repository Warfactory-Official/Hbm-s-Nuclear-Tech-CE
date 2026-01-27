package com.hbm.blocks.generic;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.IDoor;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemLock;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.DoorDecl;
import com.hbm.tileentity.TileEntityDoorGeneric;
import com.hbm.util.I18nUtil;
import micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Optional.InterfaceList({@Optional.Interface(iface = "micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock", modid = "galacticraftcore")})
public class BlockDoorGeneric extends BlockDummyable implements IRadResistantBlock, IPartialSealableBlock, IBomb {

	public DoorDecl type;
	public final boolean isRadResistant;

	public BlockDoorGeneric(Material materialIn, DoorDecl type, boolean isRadResistant, String s){
		super(materialIn, s, false);
		this.type = type;
		this.isRadResistant = isRadResistant;
	}

	@Override
	public boolean isSealed(World world, BlockPos blockPos, EnumFacing direction){
		if (world != null) {
			int[] corePos = findCore(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
			if(corePos != null){
				TileEntity core = world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));
				if (core != null && IDoor.class.isAssignableFrom(core.getClass())) {
					// Doors should be sealed only when closed
					return ((IDoor) core).getState() == IDoor.DoorState.CLOSED;
				}
			}
		}

		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		if(meta >= 12)
			return new TileEntityDoorGeneric();
		return null;
	}

	@Override
	public int[] getDimensions(){
		return type.getDimensions();
	}

	@Override
	public int getOffset(){
		return type.getBlockOffset();
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(world.isRemote) {
			return true;
		} else if(player.getHeldItem(hand).getItem() instanceof ItemLock || player.getHeldItem(hand).getItem() == ModItems.key_kit) {
			return false;

		} if(!player.isSneaking()) {
			int[] pos1 = findCore(world, pos.getX(), pos.getY(), pos.getZ());
			if(pos1 == null)
				return false;
			TileEntityDoorGeneric door = (TileEntityDoorGeneric) world.getTileEntity(new BlockPos(pos1[0], pos1[1], pos1[2]));

			if(door != null && door.canAccess(player)) {
				return door.tryToggle(player);
			}
		}
		return false;
	}

	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity){
		TileEntity te = world.getTileEntity(pos);
		int meta = state.getValue(META);
		boolean open = hasExtra(meta) || (te instanceof TileEntityDoorGeneric && ((TileEntityDoorGeneric)te).shouldUseBB);
		return type.isLadder(open);
	}

	@Override
	public void addCollisionBoxToList(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull AxisAlignedBB entityBox, @NotNull List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState){
		AxisAlignedBB box = state.getCollisionBoundingBox(worldIn, pos);
		if(box!= null && (box.minY == 0 && box.maxY == 0))
			return;
		super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}

	@Override
	public void neighborChanged(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos){
		if(!world.isRemote){
			int[] corePos = findCore(world, pos.getX(), pos.getY(), pos.getZ());
			if(corePos != null){
				TileEntity core = world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));
				if(core instanceof TileEntityDoorGeneric door){
                    door.updateRedstonePower(pos);
				}
			}
		}
		super.neighborChanged(state, world, pos, blockIn, fromPos);
	}

	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}

	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos){
		int meta = state.getValue(META);
		TileEntity te = source.getTileEntity(pos);
		int[] core = this.findCore(source, pos.getX(), pos.getY(), pos.getZ());
		boolean open = hasExtra(meta) || (te instanceof TileEntityDoorGeneric && ((TileEntityDoorGeneric)te).shouldUseBB);
		if(core == null){
			return FULL_BLOCK_AABB;
		}
		TileEntity te2 = source.getTileEntity(new BlockPos(core[0], core[1], core[2]));
		ForgeDirection dir = ForgeDirection.getOrientation(te2.getBlockMetadata() - BlockDummyable.offset);
		AxisAlignedBB box = type.getBlockBound(pos.add(-core[0], -core[1], -core[2]).rotate(dir.getBlockRotation().add(Rotation.COUNTERCLOCKWISE_90)), open);
		//System.out.println(te2.getBlockMetadata()-offset);
		switch(te2.getBlockMetadata()-offset){
		case 2:
			return new AxisAlignedBB(1-box.minX, box.minY, 1-box.minZ, 1-box.maxX, box.maxY, 1-box.maxZ);
		case 4:
			return new AxisAlignedBB(1-box.minZ, box.minY, box.minX, 1-box.maxZ, box.maxY, box.maxX);
		case 3:
			return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
		case 5:
			return new AxisAlignedBB(box.minZ, box.minY, 1-box.minX, box.maxZ, box.maxY, 1-box.maxX);
		}
		return FULL_BLOCK_AABB;
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if(this.isRadResistant){
			RadiationSystemNT.markSectionForRebuild(worldIn, pos);
		}
		super.onBlockAdded(worldIn, pos, state);
	}

	@Override
	public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, IBlockState state) {
		if(this.isRadResistant){
			RadiationSystemNT.markSectionForRebuild(worldIn, pos);
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public boolean isRadResistant(World world, BlockPos blockPos){
		if (!this.isRadResistant)
			return false;

		if (world != null) {
			int[] corePos = findCore(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
			if(corePos != null){
				TileEntity core = world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));
				if (core != null && IDoor.class.isAssignableFrom(core.getClass())) {
					// Doors should be rad resistant only when closed
					return ((IDoor) core).getState() == IDoor.DoorState.CLOSED;
				}
			}
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		float hardness = this.getExplosionResistance(null);
		if(this.isRadResistant){
			tooltip.add("§2[" + I18nUtil.resolveKey("trait.radshield") + "]");
		}
		if(hardness > 50){
			tooltip.add("§6" + I18nUtil.resolveKey("trait.blastres", hardness));
		}
	}

    //Months later I found this joke again
    //I'm not even sorry

    //Drillgon200: Months later (probably) I found this joke and don't understand it. Probably another reference...
    @Override
    public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {
        TileEntity te = findCoreTE(world,pos);
        if(!(te instanceof TileEntityDoorGeneric door)) return BombReturnCode.ERROR_INCOMPATIBLE;
        DoorDecl decl = door.getDoorType();
        if(!decl.remoteControllable()) return BombReturnCode.ERROR_INCOMPATIBLE;
        if(door.tryToggle(null)) {
            return BombReturnCode.TRIGGERED;
        }

        return BombReturnCode.ERROR_INCOMPATIBLE;
    }
}
