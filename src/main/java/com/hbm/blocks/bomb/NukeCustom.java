package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.effect.EntityCloudSolinium;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.grenade.EntityGrenadeZOMG;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.projectile.EntityFallingNuke;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityNukeCustom;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

import java.util.List;

public class NukeCustom extends BlockContainer implements IBomb {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public NukeCustom(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityNukeCustom();
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		InventoryHelper.dropInventoryItems(worldIn, pos, worldIn.getTileEntity(pos));
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()));
		if (worldIn.getTileEntity(pos) instanceof TileEntityNukeCustom nukeCustom && placer instanceof EntityPlayerMP playerMP)
			nukeCustom.placerID = playerMP.getUniqueID();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
			
		} else if (!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
			
		} else {
			return false;
		}
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (world.isBlockPowered(pos) && !world.isRemote) {
			this.explode(world, pos, null);
		}
	}
	
	public static void explodeCustom(World world, Entity detonator, double xCoord, double yCoord, double zCoord, float tnt, float nuke, float hydro, float bale, float dirty, float schrab, float sol, float euph) {
		
		dirty = Math.min(dirty, BombConfig.maxCustomDirtyRadius);
		
		/// EUPHEMIUM ///
		if(euph > 0) {
			
			euph = Math.min(euph, BombConfig.maxCustomEuphLvl);
			EntityGrenadeZOMG zomg = new EntityGrenadeZOMG(world, xCoord, yCoord, zCoord);
			ExplosionChaos.zomg(world, xCoord, yCoord, zCoord, (int)(100 * euph), detonator, zomg);

		// SOLINIUM ///
		} else if(sol > 0) {

			sol += schrab / 2 + bale / 4 + hydro / 8 + nuke / 16 + tnt / 32;
			sol = Math.min(sol, BombConfig.maxCustomSolRadius);

			EntityNukeExplosionMK3 entity = new EntityNukeExplosionMK3(world);
			entity.setPosition(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
    		entity.destructionRange = (int) sol;
    		entity.speed = BombConfig.blastSpeed;
    		entity.coefficient = 1.0F;
    		entity.waste = false;
    		entity.extType = 1;
			entity.setDetonator(detonator);
    		world.spawnEntity(entity);

    		EntityCloudSolinium cloud = new EntityCloudSolinium(world, (int)sol);
    		cloud.setPosition(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
    		world.spawnEntity(cloud);

		// SCHRABIDIUM ///
		} else if(schrab > 0) {
			
			schrab += bale / 2 + hydro / 4 + nuke / 8 + tnt / 16;
			schrab = Math.min(schrab, BombConfig.maxCustomSchrabRadius);

			EntityNukeExplosionMK3 ex = EntityNukeExplosionMK3.statFacFleija(world, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, (int) schrab);
			ex.setDetonator(detonator);
			if (!ex.isDead) {
				world.spawnEntity(ex);

				EntityCloudFleija cloud = new EntityCloudFleija(world, (int) schrab);
				cloud.setPosition(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
				world.spawnEntity(cloud);
			}
    		
    	/// ANTIMATTER ///
		} else if(bale > 0) {

			bale += hydro / 2 + nuke / 4 + tnt / 8;
			bale = Math.min(bale, BombConfig.maxCustomBaleRadius);

			EntityBalefire bf = new EntityBalefire(world);
    		bf.setPosition(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
			bf.destructionRange = (int) bale;
			bf.setDetonator(detonator);
			world.spawnEntity(bf);
			if(BombConfig.enableNukeClouds) {
				EntityNukeTorex.statFacBale(world, xCoord + 0.5, yCoord + 5, zCoord + 0.5, bale);
			}
			
		/// HYDROGEN ///
		} else if(hydro > 0) {

			hydro += nuke / 2 + tnt / 4;
			hydro = Math.min(hydro, BombConfig.maxCustomHydroRadius);
			dirty *= 0.25F;

			world.spawnEntity(EntityNukeExplosionMK5.statFac(world, (int)hydro, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5).moreFallout((int)dirty).setDetonator(detonator));
			if(BombConfig.enableNukeClouds) {
				EntityNukeTorex.statFac(world, xCoord + 0.5, yCoord + 5, zCoord + 0.5, hydro);
			}
			
		/// NUCLEAR ///
		} else if(nuke > 0) {
			
			nuke += tnt / 2;
			nuke = Math.min(nuke, BombConfig.maxCustomNukeRadius);

			world.spawnEntity(EntityNukeExplosionMK5.statFac(world, (int)nuke, xCoord + 0.5, yCoord + 5, zCoord + 0.5).moreFallout((int)dirty).setDetonator(detonator));
			if(BombConfig.enableNukeClouds) {
				EntityNukeTorex.statFac(world, xCoord + 0.5, yCoord + 5, zCoord + 0.5, nuke);
			}
			
		/// NON-NUCLEAR ///
		} else if(tnt >= 75) {

			tnt = Math.min(tnt, BombConfig.maxCustomTNTRadius);

			world.spawnEntity(EntityNukeExplosionMK5.statFacNoRad(world, (int)tnt, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5).setDetonator(detonator));
			if(BombConfig.enableNukeClouds) {
				EntityNukeTorex.statFac(world, xCoord + 0.5, yCoord + 5, zCoord + 0.5, tnt);
			}
		} else if(tnt > 0) {
			
			ExplosionLarge.explode(world, detonator, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, tnt, true, true, true);
		}
	}
	
	@Override
	public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {
		if(!world.isRemote) {
			TileEntityNukeCustom entity = (TileEntityNukeCustom) world.getTileEntity(pos);
			if (detonator == null && entity.placerID != null)
				detonator = world.getMinecraftServer().getPlayerList().getPlayerByUUID(entity.placerID); // could fail if the placer logged out
			if (!entity.isFalling()) {
				entity.clearSlots();
				world.destroyBlock(pos, false);
				NukeCustom.explodeCustom(world, detonator, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, entity.tnt, entity.nuke, entity.hydro, entity.bale, entity.dirty, entity.schrab, entity.sol, entity.euph);
				return BombReturnCode.TRIGGERED;

			} else {

				EntityFallingNuke bomb = new EntityFallingNuke(world, detonator, entity.tnt, entity.nuke, entity.hydro, entity.bale, entity.dirty, entity.schrab, entity.sol, entity.euph);
				bomb.getDataManager().set(EntityFallingNuke.FACING, world.getBlockState(pos).getValue(FACING));
				bomb.setPositionAndRotation(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
				entity.clearSlots();
				world.setBlockToAir(pos);
				world.spawnEntity(bomb);
				return BombReturnCode.TRIGGERED;
			}
		}

		return BombReturnCode.UNDEFINED;
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
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing) state.getValue(FACING)).getIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta);

		if(enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add("§d["+ I18nUtil.resolveKey("trait.modularbomb")+"]§r");
	}
}
