package com.hbm.blocks.machine;

import com.hbm.api.block.IToolable.ToolType;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.IDoor;
import com.hbm.interfaces.IMultiBlock;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemLock;
import com.hbm.items.tool.ItemTooling;
import com.hbm.tileentity.machine.TileEntityVaultDoor;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import java.util.List;

@Optional.InterfaceList({@Optional.Interface(iface = "micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock", modid = "galacticraftcore")})
public class VaultDoor extends BlockContainer implements IBomb, IMultiBlock, IRadResistantBlock, IPartialSealableBlock {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
	public VaultDoor(Material materialIn, String s) {
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
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityVaultDoor();
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {
		if(!world.isRemote) {
			TileEntityVaultDoor te = (TileEntityVaultDoor) world.getTileEntity(pos);

			if (!te.isLocked()) {
				te.tryToggle();
				return BombReturnCode.TRIGGERED;
			}

			return BombReturnCode.ERROR_INCOMPATIBLE;
		}

		return BombReturnCode.UNDEFINED;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntityVaultDoor te = (TileEntityVaultDoor) world.getTileEntity(pos);
		world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
		
		int i = MathHelper.floor(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		
		if(i == 0)
		{
			//frame
			if(!(te.placeDummy(x + 1, y, z) &&
				te.placeDummy(x + 2, y, z) &&
				te.placeDummy(x + 2, y + 1, z) &&
				te.placeDummy(x + 2, y + 2, z) &&
				te.placeDummy(x + 2, y + 3, z) &&
				te.placeDummy(x + 2, y + 4, z) &&
				te.placeDummy(x + 1, y + 4, z) &&
				te.placeDummy(x, y + 4, z) &&
				te.placeDummy(x - 1, y + 4, z) &&
				te.placeDummy(x - 2, y + 4, z) &&
				te.placeDummy(x - 2, y + 3, z) &&
				te.placeDummy(x - 2, y + 2, z) &&
				te.placeDummy(x - 2, y + 1, z) &&
				te.placeDummy(x - 2, y, z) &&
				te.placeDummy(x - 1, y, z) &&
				//cog
				te.placeDummy(x - 1, y + 1, z) &&
				te.placeDummy(x - 1, y + 2, z) &&
				te.placeDummy(x - 1, y + 3, z) &&
				te.placeDummy(x, y + 1, z) &&
				te.placeDummy(x, y + 2, z) &&
				te.placeDummy(x, y + 3, z) &&
				te.placeDummy(x + 1, y + 1, z) &&
				te.placeDummy(x + 1, y + 2, z) &&
				te.placeDummy(x + 1, y + 3, z) &&
				//teeth
				te.placeDummy(x + 2, y, z + 1) &&
				te.placeDummy(x + 1, y, z + 1) &&
				te.placeDummy(x, y, z + 1) &&
				te.placeDummy(x - 1, y, z + 1) &&
				te.placeDummy(x - 2, y, z + 1))) {
				
				world.destroyBlock(pos, true);
			}
		}
		if(i == 1)
		{
			
			//frame
			if(!(te.placeDummy(x, y, z + 1) &&
				te.placeDummy(x, y, z + 2) &&
				te.placeDummy(x, y + 1, z + 2) &&
				te.placeDummy(x, y + 2, z + 2) &&
				te.placeDummy(x, y + 3, z + 2) &&
				te.placeDummy(x, y + 4, z + 2) &&
				te.placeDummy(x, y + 4, z + 1) &&
				te.placeDummy(x, y + 4, z) &&
				te.placeDummy(x, y + 4, z - 1) &&
				te.placeDummy(x, y + 4, z - 2) &&
				te.placeDummy(x, y + 3, z - 2) &&
				te.placeDummy(x, y + 2, z - 2) &&
				te.placeDummy(x, y + 1, z - 2) &&
				te.placeDummy(x, y, z - 2) &&
				te.placeDummy(x, y, z - 1) &&
				//cog
				te.placeDummy(x, y + 1, z - 1) &&
				te.placeDummy(x, y + 2, z - 1) &&
				te.placeDummy(x, y + 3, z - 1) &&
				te.placeDummy(x, y + 1, z) &&
				te.placeDummy(x, y + 2, z) &&
				te.placeDummy(x, y + 3, z) &&
				te.placeDummy(x, y + 1, z + 1) &&
				te.placeDummy(x, y + 2, z + 1) &&
				te.placeDummy(x, y + 3, z + 1) &&
				//teeth
				te.placeDummy(x - 1, y, z + 2) &&
				te.placeDummy(x - 1, y, z + 1) &&
				te.placeDummy(x - 1, y, z) &&
				te.placeDummy(x - 1, y, z - 1) &&
				te.placeDummy(x - 1, y, z - 2))) {
				
				world.destroyBlock(pos, true);
			}
		}
		if(i == 2)
		{
			
			//frame
			if(!(te.placeDummy(x + 1, y, z) &&
				te.placeDummy(x + 2, y, z) &&
				te.placeDummy(x + 2, y + 1, z) &&
				te.placeDummy(x + 2, y + 2, z) &&
				te.placeDummy(x + 2, y + 3, z) &&
				te.placeDummy(x + 2, y + 4, z) &&
				te.placeDummy(x + 1, y + 4, z) &&
				te.placeDummy(x, y + 4, z) &&
				te.placeDummy(x - 1, y + 4, z) &&
				te.placeDummy(x - 2, y + 4, z) &&
				te.placeDummy(x - 2, y + 3, z) &&
				te.placeDummy(x - 2, y + 2, z) &&
				te.placeDummy(x - 2, y + 1, z) &&
				te.placeDummy(x - 2, y, z) &&
				te.placeDummy(x - 1, y, z) &&
				//cog
				te.placeDummy(x - 1, y + 1, z) &&
				te.placeDummy(x - 1, y + 2, z) &&
				te.placeDummy(x - 1, y + 3, z) &&
				te.placeDummy(x, y + 1, z) &&
				te.placeDummy(x, y + 2, z) &&
				te.placeDummy(x, y + 3, z) &&
				te.placeDummy(x + 1, y + 1, z) &&
				te.placeDummy(x + 1, y + 2, z) &&
				te.placeDummy(x + 1, y + 3, z) &&
				//teeth
				te.placeDummy(x + 2, y, z - 1) &&
				te.placeDummy(x + 1, y, z - 1) &&
				te.placeDummy(x, y, z - 1) &&
				te.placeDummy(x - 1, y, z - 1) &&
				te.placeDummy(x - 2, y, z - 1))) {
				
				world.destroyBlock(pos, true);
			}
		}
		if(i == 3)
		{
			
			//frame
			if(!(te.placeDummy(x, y, z + 1) &&
				te.placeDummy(x, y, z + 2) &&
				te.placeDummy(x, y + 1, z + 2) &&
				te.placeDummy(x, y + 2, z + 2) &&
				te.placeDummy(x, y + 3, z + 2) &&
				te.placeDummy(x, y + 4, z + 2) &&
				te.placeDummy(x, y + 4, z + 1) &&
				te.placeDummy(x, y + 4, z) &&
				te.placeDummy(x, y + 4, z - 1) &&
				te.placeDummy(x, y + 4, z - 2) &&
				te.placeDummy(x, y + 3, z - 2) &&
				te.placeDummy(x, y + 2, z - 2) &&
				te.placeDummy(x, y + 1, z - 2) &&
				te.placeDummy(x, y, z - 2) &&
				te.placeDummy(x, y, z - 1) &&
				//cog
				te.placeDummy(x, y + 1, z - 1) &&
				te.placeDummy(x, y + 2, z - 1) &&
				te.placeDummy(x, y + 3, z - 1) &&
				te.placeDummy(x, y + 1, z) &&
				te.placeDummy(x, y + 2, z) &&
				te.placeDummy(x, y + 3, z) &&
				te.placeDummy(x, y + 1, z + 1) &&
				te.placeDummy(x, y + 2, z + 1) &&
				te.placeDummy(x, y + 3, z + 1) &&
				//teeth
				te.placeDummy(x + 1, y, z + 2) &&
				te.placeDummy(x + 1, y, z + 1) &&
				te.placeDummy(x + 1, y, z) &&
				te.placeDummy(x + 1, y, z - 1) &&
				te.placeDummy(x + 1, y, z - 2))) {
				
    			world.destroyBlock(pos, true);
			}
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(player.getHeldItem(hand).getItem() instanceof ItemLock || player.getHeldItem(hand).getItem() == ModItems.key_kit) {
			return false;

        }
        TileEntityVaultDoor entity = (TileEntityVaultDoor) world.getTileEntity(pos);
        if (entity == null) return false;

        if (player.getHeldItem(hand).getItem() instanceof ItemTooling tool && tool.getType() == ToolType.SCREWDRIVER) {
            if (entity.getConfiguredMode() == IDoor.Mode.TOOLABLE) {
                if (!entity.canToggleRedstone(player)) {
                    return false;
                }
                entity.toggleRedstoneMode();
                return true;
            }
        }
        if (!player.isSneaking()) {
            if (entity.isRedstoneOnly()) {
                return false;
            }
            if (entity.canAccess(player)) {
                entity.tryToggle();
                return true;
            }
        } else {
            entity.type++;
            if (entity.type >= TileEntityVaultDoor.maxTypes)
                entity.type = 0;
        }
        return false;
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
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
			EnumFacing side) {
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
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		RadiationSystemNT.markSectionForRebuild(worldIn, pos);
		super.onBlockAdded(worldIn, pos, state);
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        RadiationSystemNT.markSectionForRebuild(worldIn, pos);
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public boolean isRadResistant(World worldIn, BlockPos blockPos) {
		// Door should be rad resistant only when closed
		if (worldIn != null)
		{
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
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		float hardness = this.getExplosionResistance(null);
		tooltip.add("§2[" + I18nUtil.resolveKey("trait.radshield") + "]");
		if(hardness > 50){
			tooltip.add("§6" + I18nUtil.resolveKey("trait.blastres", hardness));
		}
	}
}
