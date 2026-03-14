package com.hbm.blocks.machine;

import com.hbm.api.block.IToolable.ToolType;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.IDoor;
import com.hbm.interfaces.IKeypadHandler;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.items.tool.ItemTooling;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntitySlidingBlastDoorKeypad;
import com.hbm.tileentity.machine.TileEntitySlidingBlastDoor;
import com.hbm.util.I18nUtil;
import com.hbm.util.KeypadClient;
import micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Optional.InterfaceList({@Optional.Interface(iface = "micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock", modid = "galacticraftcore")})
public class BlockSlidingBlastDoor extends BlockDummyable implements IRadResistantBlock, IPartialSealableBlock {

	public BlockSlidingBlastDoor(Material materialIn, String s) {
		super(materialIn, s, true);
	}

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
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if(this == ModBlocks.sliding_blast_door_keypad) {
			return new TileEntitySlidingBlastDoorKeypad();
		}
		if(meta >= 12)
			return new TileEntitySlidingBlastDoor();
		return null;
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		float hardness = this.getExplosionResistance(null);
		tooltip.add("§2[" + I18nUtil.resolveKey("trait.radshield") + "]");
		if(hardness > 50){
			tooltip.add("§6" + I18nUtil.resolveKey("trait.blastres", hardness));
		}
		if(this == ModBlocks.sliding_blast_door){
			tooltip.add(I18nUtil.resolveKey("desc.varwin"));
		} else if(this == ModBlocks.sliding_blast_door_2){
			tooltip.add(I18nUtil.resolveKey("desc.varkey"));
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if(world.isRemote && te instanceof IKeypadHandler) {
			return handleClickClient(te, pos);
		}
		if(!world.isRemote && !playerIn.isSneaking()) {
			if(world.getBlockState(pos).getBlock() == ModBlocks.sliding_blast_door_keypad)
				return super.onBlockActivated(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
			int[] pos1 = findCore(world, pos.getX(), pos.getY(), pos.getZ());
			if(pos1 == null)
				return false;
			TileEntitySlidingBlastDoor door = (TileEntitySlidingBlastDoor) world.getTileEntity(new BlockPos(pos1[0], pos1[1], pos1[2]));

			if(door != null) {
                if (playerIn.getHeldItem(hand).getItem() instanceof ItemTooling tool && tool.getType() == ToolType.SCREWDRIVER) {
                    if (door.getConfiguredMode() == IDoor.Mode.TOOLABLE) {
                        if (!door.canToggleRedstone(playerIn)) {
                            return false;
                        }
                        door.toggleRedstoneMode();
                        return true;
                    }
                }

                if (door.isRedstoneOnly()) return false;

                return door.tryToggle(playerIn);
			}
		}
		return super.onBlockActivated(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
	@SideOnly(Side.CLIENT)
	public boolean handleClickClient(TileEntity te, BlockPos pos){
		KeypadClient pad = ((IKeypadHandler) te).getKeypad().client();
		if(pad.isPlayerMouseingOver(pos)) {
			return pad.client().playerClick(pos);
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if(world.isRemote && te instanceof IKeypadHandler) {
			KeypadClient pad = ((IKeypadHandler) te).getKeypad().client();
			AxisAlignedBB key = pad.rayTrace(pos);
			if(key != null) {
				return key;
			}
		}
		return super.getSelectedBoundingBox(state, world, pos);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull AxisAlignedBB entityBox, @NotNull List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
		AxisAlignedBB box = state.getCollisionBoundingBox(worldIn, pos);
		if(box.minY == 0 && box.maxY == 0)
			return;
		super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}

	@Override
	public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
		int meta = state.getValue(META);
		if(this == ModBlocks.sliding_blast_door_keypad)
			return FULL_BLOCK_AABB;
		if(hasExtra(meta)) {
			if(source.getBlockState(pos.up()).getBlock() == this) {
				return Library.EMPTY_AABB;
			}
			return new AxisAlignedBB(0, 0.5, 0, 1, 1, 1);
		}
		TileEntity te = source.getTileEntity(pos);
		if(te instanceof TileEntitySlidingBlastDoor && !((TileEntitySlidingBlastDoor) te).shouldUseBB) {
			return Library.EMPTY_AABB;
		}
		return FULL_BLOCK_AABB;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public int[] getDimensions() {
		return new int[] { 3, 0, 0, 0, 3, 3 };
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public boolean isOpaqueCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		if(world.getBlockState(new BlockPos(x, y, z)).getBlock() == ModBlocks.sliding_blast_door_2) {
			BlockPos pos = new BlockPos(x, y + 1, z).offset(dir.toEnumFacing().rotateY(), 3);
			BlockPos pos2 = new BlockPos(x, y + 1, z).offset(dir.toEnumFacing().rotateYCCW(), 3);
			int meta = world.getBlockState(pos).getValue(META);
			int meta2 = world.getBlockState(pos2).getValue(META);
			BlockDummyable.safeRem = true;
			world.setBlockState(pos, ModBlocks.sliding_blast_door_keypad.getDefaultState().withProperty(META, meta));
			world.setBlockState(pos2, ModBlocks.sliding_blast_door_keypad.getDefaultState().withProperty(META, meta2+extra));
			BlockDummyable.safeRem = false;
		}
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		RadiationSystemNT.markSectionForRebuild(worldIn, pos);
		super.onBlockAdded(worldIn, pos, state);
	}
	
	@Override
	public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, IBlockState state) {
		RadiationSystemNT.markSectionForRebuild(worldIn, pos);
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public boolean isRadResistant(World world, BlockPos blockPos){

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

}
