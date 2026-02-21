package com.hbm.blocks.bomb;

import com.hbm.blocks.BlockBase;
import com.hbm.entity.mob.EntityCreeperTainted;
import com.hbm.entity.mob.EntityTaintCrab;
import com.hbm.entity.mob.EntityTeslaCrab;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockTaint extends BlockBase {
    private static final AxisAlignedBB TAINT_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);
	public static final PropertyInteger TAINTAGE = PropertyInteger.create("taintage", 0, 15);
	
	public BlockTaint(Material m, String s) {
		super(m, s);
		this.setTickRandomly(true);
		this.setCreativeTab(MainRegistry.controlTab);
		this.setDefaultState(this.blockState.getBaseState().withProperty(TAINTAGE, 0));
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return MapColor.GRAY;
	}

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) return;
        final int age = state.getValue(TAINTAGE);
        if (age >= 15) return;
        final int baseX = pos.getX();
        final int baseY = pos.getY();
        final int baseZ = pos.getZ();
        final BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();
        final BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        for (int dx = -3; dx <= 3; dx++) for (int dy = -3; dy <= 3; dy++) for (int dz = -3; dz <= 3; dz++) {
            if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 4) continue;
            if (rand.nextFloat() > 0.25F) continue;
            final int tx = baseX + dx;
            final int ty = baseY + dy;
            final int tz = baseZ + dz;
            targetPos.setPos(tx, ty, tz);
            IBlockState targetState = world.getBlockState(targetPos);
            Block targetBlock = targetState.getBlock();
            if (targetBlock.isAir(targetState, world, targetPos) || targetBlock == Blocks.BEDROCK) continue;
            int targetAge = age + 1;
            boolean hasAir = false;
            for (EnumFacing f : EnumFacing.VALUES) {
                neighborPos.setPos(tx + f.getXOffset(), ty + f.getYOffset(), tz + f.getZOffset());
                IBlockState neighborState = world.getBlockState(neighborPos);
                if (neighborState.getBlock().isAir(neighborState, world, neighborPos)) {
                    hasAir = true;
                    break;
                }
            }
            if (!hasAir) targetAge = age + 3;
            if (targetAge > 15) continue;
            if (targetBlock == this && targetState.getValue(TAINTAGE) >= targetAge) continue;
            IBlockState newState = this.getDefaultState().withProperty(TAINTAGE, targetAge);
            world.setBlockState(targetPos, newState, 3);
            neighborPos.setPos(tx, ty - 1, tz);
            if (rand.nextFloat() < 0.25F && BlockFalling.canFallThrough(world.getBlockState(neighborPos))) {
                EntityFallingBlock falling = new EntityFallingBlock(world, tx + 0.5D, ty + 0.5D, tz + 0.5D, newState);
                world.spawnEntity(falling);
            }
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return TAINT_AABB;
    }

	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
		int meta = world.getBlockState(pos).getBlock().getMetaFromState(state);
		int level = 15 - meta;
		
    	List<ItemStack> list = new ArrayList<>();
    	PotionEffect effect = new PotionEffect(HbmPotion.taint, 15 * 20, level);
    	effect.setCurativeItems(list);
    	
    	if(entity instanceof EntityLivingBase) {
    		if(world.rand.nextInt(50) == 0) {
    			((EntityLivingBase)entity).addPotionEffect(effect);
    		}
    	}
    	
    	if(entity instanceof EntityCreeper && !(entity instanceof EntityCreeperTainted)) {
    		EntityCreeperTainted creep = new EntityCreeperTainted(world);
    		creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

    		if(!world.isRemote) {
    			entity.setDead();
    			world.spawnEntity(creep);
    		}
    	}

		if (entity instanceof EntityTeslaCrab) {
			EntityTaintCrab crab = new EntityTaintCrab(world);
			crab.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

			if (!world.isRemote) {
				entity.setDead();
				world.spawnEntity(crab);
			}
		}
	}
	
	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add("DO NOT TOUCH, BREATHE OR STARE AT.");
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TAINTAGE);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TAINTAGE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TAINTAGE, meta);
	}
}
