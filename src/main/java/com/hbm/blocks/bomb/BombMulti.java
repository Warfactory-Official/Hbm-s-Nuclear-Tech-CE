package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityBombMulti;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class BombMulti extends BlockContainer implements IBomb {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final AxisAlignedBB MULTI_BB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);
	
	public final float explosionBaseValue = 8.0F;
	public float explosionValue = 0.0F;
	public int clusterCount = 0;
	public int fireRadius = 0;
	public int poisonRadius = 0;
	public int gasCloud = 0;
	
	public BombMulti(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityBombMulti();
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntityBombMulti entity = (TileEntityBombMulti) worldIn.getTileEntity(pos);
        if (worldIn.isBlockPowered(pos))
        {
        	if(/*entity.getExplosionType() != 0*/entity.isLoaded())
        	{
        		this.onPlayerDestroy(worldIn, pos, state);
            	igniteTestBomb(worldIn, null, pos.getX(), pos.getY(), pos.getZ());
        	}
        }
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		InventoryHelper.dropInventoryItems(worldIn, pos, worldIn.getTileEntity(pos));
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	public boolean igniteTestBomb(World world, Entity detonator, int x, int y, int z)
	{
		BlockPos pos = new BlockPos(x, y, z);
    	TileEntityBombMulti entity = (TileEntityBombMulti) world.getTileEntity(pos);
		if (!world.isRemote)
		{
        	if(entity.isLoaded())
        	{
        		this.explosionValue = this.explosionBaseValue;
        		switch(entity.return2type())
        		{
        		case 1:
        			this.explosionValue += 1.0F;
        			break;
        		case 2:
        			this.explosionValue += 4.0F;
        			break;
        		case 3:
        			this.clusterCount += 50;
        			break;
        		case 4:
        			this.fireRadius += 10;
        			break;
        		case 5:
        			this.poisonRadius += 15;
        			break;
        		case 6:
        			this.gasCloud += 50;
        		}
        		switch(entity.return5type())
        		{
        		case 1:
        			this.explosionValue += 1.0F;
        			break;
        		case 2:
        			this.explosionValue += 4.0F;
        			break;
        		case 3:
        			this.clusterCount += 50;
        			break;
        		case 4:
        			this.fireRadius += 10;
        			break;
        		case 5:
        			this.poisonRadius += 15;
        			break;
        		case 6:
        			this.gasCloud += 50;
        		}

        		entity.clearSlots();
            	world.setBlockToAir(pos);
            	//world.createExplosion(null, x , y , z , this.explosionValue, true);
            	ExplosionLarge.explode(world, detonator, x, y, z, explosionValue, true, true, true);
            	this.explosionValue = 0;
        		
        		if(this.clusterCount > 0)
        		{
                	ExplosionChaos.cluster(world, x, y, z, this.clusterCount, 0.5);
        		}
        		
        		if(this.fireRadius > 0)
        		{
                	ExplosionChaos.burn(world, null, pos, this.fireRadius);
        		}
        		
        		if(this.poisonRadius > 0)
        		{
                	ExplosionNukeGeneric.wasteNoSchrab(world, pos, this.poisonRadius);
        		}
        		
        		if(this.gasCloud > 0)
        		{
        			ExplosionChaos.spawnChlorine(world, x, y, z, this.gasCloud, this.gasCloud / 50, 0);
        		}
        		
        		this.clusterCount = 0;
        		this.fireRadius = 0;
        		this.poisonRadius = 0;
        		this.gasCloud = 0;
        		
        		
        	}
        }
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return MULTI_BB;
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {
		if(!world.isRemote) {
			TileEntityBombMulti entity = (TileEntityBombMulti) world.getTileEntity(pos);
			if (/*entity.getExplosionType() != 0*/entity.isLoaded()) {
				this.onPlayerDestroy(world, pos, world.getBlockState(pos));
				igniteTestBomb(world, detonator, pos.getX(), pos.getY(), pos.getZ());
			}
			return BombReturnCode.ERROR_MISSING_COMPONENT;
		}
		return BombReturnCode.UNDEFINED;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
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
		return new BlockStateContainer(this, new IProperty[]{FACING});
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
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
	}
	
	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
	{
	   return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
	}

}
