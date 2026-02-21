package com.hbm.blocks.machine;

import com.hbm.api.block.IToolable.ToolType;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.IDoor;
import com.hbm.interfaces.IMultiBlock;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemLock;
import com.hbm.items.tool.ItemTooling;
import com.hbm.tileentity.machine.TileEntityBlastDoor;
import com.hbm.util.I18nUtil;
import micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import java.util.List;

@Optional.InterfaceList({@Optional.Interface(iface = "micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock", modid = "galacticraftcore")})
public class BlastDoor extends BlockContainer implements IBomb, IMultiBlock, IPartialSealableBlock, IRadResistantBlock {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
	public BlastDoor(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	public boolean isSealed(World world, BlockPos blockPos, EnumFacing direction){
		if(world != null) {
			TileEntity entity = world.getTileEntity(blockPos);
			if (entity != null) {
				if (IDoor.class.isAssignableFrom(entity.getClass())) {
					// Doors should be rad sealed when closed
					return ((IDoor) entity).getState() == IDoor.DoorState.CLOSED;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isRadResistant(World worldIn, BlockPos blockPos) {
		// Door should be rad resistant only when closed
		if (worldIn != null) {
			TileEntity entity = worldIn.getTileEntity(blockPos);
			if (entity != null) {
				if (IDoor.class.isAssignableFrom(entity.getClass())) {
					return ((IDoor) entity).getState() == IDoor.DoorState.CLOSED;
				}
			}
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityBlastDoor();
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {

		if(!world.isRemote) {
			TileEntityBlastDoor entity = (TileEntityBlastDoor) world.getTileEntity(pos);
			if(entity != null) {

				if(!entity.isLocked()) {
					entity.tryToggle();
					return BombReturnCode.TRIGGERED;
				}

				return BombReturnCode.ERROR_INCOMPATIBLE;
			}
		}

		return BombReturnCode.UNDEFINED;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(player.getHeldItemMainhand().getItem() instanceof ItemLock || player.getHeldItemMainhand().getItem() == ModItems.key_kit) {
			return false;
			
		} if(!player.isSneaking()) {
			
			TileEntityBlastDoor entity = (TileEntityBlastDoor) world.getTileEntity(pos);
			if(entity != null) {

                if (player.getHeldItem(hand).getItem() instanceof ItemTooling tool && tool.getType() == ToolType.SCREWDRIVER) {
                    if (entity.getConfiguredMode() == IDoor.Mode.TOOLABLE) {
                        if (!entity.canToggleRedstone(player)) {
                            return false;
                        }
                        entity.toggleRedstoneMode();
                        return true;
                    }
                }

                if (entity.isRedstoneOnly()) {
                    return false;
                }

                if(entity.canAccess(player)){
					entity.tryToggle();
					return true;
				}	
			}
			return false;
		}
		
		return false;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntityBlastDoor te = (TileEntityBlastDoor) world.getTileEntity(pos);
		
		world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
		
		//frame
		if(!(te.placeDummy(pos.up(1)) &&
			te.placeDummy(pos.up(2)) &&
			te.placeDummy(pos.up(3)) &&
			te.placeDummy(pos.up(4)) &&
			te.placeDummy(pos.up(5)) &&
			te.placeDummy(pos.up(6))))
			world.destroyBlock(pos, true);
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
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
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing)state.getValue(FACING)).getIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		float hardness = this.getExplosionResistance(null);
		tooltip.add("§2[" + I18nUtil.resolveKey("trait.radshield") + "]");
		if(hardness > 50){
			tooltip.add("§6" + I18nUtil.resolveKey("trait.blastres", hardness));
		}
	}
}
