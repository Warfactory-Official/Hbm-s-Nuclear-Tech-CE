package com.hbm.blocks.fluid;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

import static com.hbm.blocks.BlockEnumMeta.stateFromEnum;
import static com.hbm.blocks.BlockEnums.EnumBasaltOreType.*;
import static com.hbm.blocks.ModBlocks.basalt_ore;

public class VolcanicBlock extends BlockFluidClassic implements IFluidFog {

	public VolcanicBlock(Fluid fluid, Material material, String s) {
		super(fluid, material);
		this.setTickRandomly(true);
		this.setQuantaPerBlock(4);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighbourPos){
		super.neighborChanged(state, world, pos, neighborBlock, neighbourPos);
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			Block b = getReaction(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ);
			
			if(b != null)
				world.setBlockState(new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ), b.getDefaultState());
		}
	}
	
	public Block getReaction(World world, int x, int y, int z) {
		
		IBlockState state = world.getBlockState(new BlockPos(x, y, z));
		Block b = state.getBlock();
		if(state.getMaterial() == Material.WATER) {
			return Blocks.STONE;
		}
		if(b instanceof BlockLog) {
			return ModBlocks.waste_log;
		}
		if(b == Blocks.PLANKS) {
			return ModBlocks.waste_planks;
		}
		if(b instanceof BlockLeaves) {
			return Blocks.FIRE;
		}
		return null;
	}

	@Override
	public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entity){
		entity.setInWeb();
		entity.setFire(3);
		entity.attackEntityFrom(ModDamageSource.radiation, 2F);
		
		if(entity instanceof EntityLivingBase)
			ContaminationUtil.contaminate((EntityLivingBase)entity, HazardType.RADIATION, ContaminationType.CREATIVE, 0.05F);
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand){
		super.updateTick(world, pos, state, rand);
		
		int lavaCount = 0;
		int basaltCount = 0;
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			Block b = world.getBlockState(new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ)).getBlock();
			
			if(b == this)
				lavaCount++;
			if(b == ModBlocks.basalt) {
				basaltCount++;
			}
		}
		
		if(!world.isRemote && ((!this.isSourceBlock(world, pos) && lavaCount < 2) || (rand.nextInt(5) == 0) && lavaCount < 5) && world.getBlockState(pos.down()).getBlock() != this) {
			
			int r = rand.nextInt(200);
			
			Block above = world.getBlockState(pos.up(10)).getBlock();
			boolean canMakeGem = lavaCount + basaltCount == 6 && lavaCount < 3 && (above == ModBlocks.basalt || above == ModBlocks.volcanic_lava_block);
			
			if(r < 2)
				world.setBlockState(pos, stateFromEnum(basalt_ore, SULFUR));

			else if(r == 2)
				world.setBlockState(pos, stateFromEnum(basalt_ore, ASBESTOS));
			else if(r == 3)
				world.setBlockState(pos, stateFromEnum(basalt_ore, FLUORITE));
			else if(r < 14 && canMakeGem)
				world.setBlockState(pos, stateFromEnum(basalt_ore, GEM));
			else
				world.setBlockState(pos, ModBlocks.basalt.getDefaultState());
		}
	}
	
	@Override
	public boolean canDisplace(IBlockAccess world, BlockPos pos){
		Block b = world.getBlockState(pos).getBlock();
		
		if(b.getFlammability(world, pos, null) > 0)
			return true;
		
		if(b.isReplaceable(world, pos))
			return true;
		
		return super.canDisplace(world, pos);
	}
	
	@Override
	public boolean displaceIfPossible(World world, BlockPos pos){
		return super.displaceIfPossible(world, pos) || canDisplace(world, pos);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand){
		double dx;
		double dy;
		double dz;

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if(world.getBlockState(pos.up()).getMaterial() == Material.AIR && !world.getBlockState(pos.up()).isOpaqueCube()) {
			if(rand.nextInt(100) == 0) {
				dx = (float) x + rand.nextFloat();
				dy = (double) y + this.getBlockLiquidHeight(world, pos, state, this.material);
				dz = (float) z + rand.nextFloat();
				world.spawnParticle(EnumParticleTypes.LAVA, dx, dy, dz, 0.0D, 0.0D, 0.0D);
				world.playSound(null, dx, dy, dz, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F);
			}

			if(rand.nextInt(200) == 0) {
				world.playSound(null, x, y, z, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F);
			}
		}

		if(rand.nextInt(10) == 0 && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP) && !world.getBlockState(pos.down(2)).getMaterial().blocksMovement()) {
			dx = (float) x + rand.nextFloat();
			dy = (double) y - 1.05D;
			dz = (float) z + rand.nextFloat();
			world.spawnParticle(EnumParticleTypes.DRIP_LAVA, dx, dy, dz, 0.0D, 0.0D, 0.0D);
		}
	}

    @Override
    public float getFogDensity() {
        return 2.0F;
    }

    @Override
    public int getFogColor() {
        return 0xCF340E;
    }
}