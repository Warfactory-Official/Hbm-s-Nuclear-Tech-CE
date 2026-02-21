package com.hbm.blocks.fluid;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.ArmorUtil;
import com.hbm.lib.ModDamageSource;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import java.util.Random;

public class MudBlock extends BlockFluidClassic implements IFluidFog {

	public static DamageSource damageSource;
	private final Random rand = new Random();

	public MudBlock(Fluid fluid, Material material, DamageSource d, String s) {
		super(fluid, material);
		damageSource = d;
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setQuantaPerBlock(4);
		this.setCreativeTab(null);
		displacements.put(this, false);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}
	
	@Override
	public boolean canDisplace(IBlockAccess world, BlockPos pos) {
		if (world.getBlockState(pos).getMaterial().isLiquid()) {
			return false;
		}
		return super.canDisplace(world, pos);
	}

	@Override
	public boolean displaceIfPossible(World world, BlockPos pos) {
		if (world.getBlockState(pos).getMaterial().isLiquid()) {
			return false;
		}
		return super.displaceIfPossible(world, pos);
	}
	
	@Override
	public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entity) {
		entity.setInWeb();

		if (!(entity instanceof EntityPlayer && ArmorUtil.checkForHazmat((EntityPlayer) entity))) {
			entity.attackEntityFrom(ModDamageSource.mudPoisoning, 8);
		}
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		reactToBlocks2(world, pos.east());
		reactToBlocks2(world, pos.west());
		reactToBlocks2(world, pos.up());
		reactToBlocks2(world, pos.down());
		reactToBlocks2(world, pos.south());
		reactToBlocks2(world, pos.north());
		super.updateTick(world, pos, state, rand);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighbourPos) {
		reactToBlocks(world, pos.east());
		reactToBlocks(world, pos.west());
		reactToBlocks(world, pos.up());
		reactToBlocks(world, pos.down());
		reactToBlocks(world, pos.south());
		reactToBlocks(world, pos.north());
		super.neighborChanged(state, world, pos, neighborBlock, neighbourPos);
	}

	public void reactToBlocks(World world, BlockPos pos) {
		if (world.getBlockState(pos).getMaterial() != ModBlocks.fluidmud) {
			IBlockState blockState = world.getBlockState(pos);
			if (blockState.getMaterial().isLiquid()) {
				world.setBlockToAir(pos);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void reactToBlocks2(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getMaterial() == ModBlocks.fluidmud) {
			return;
		}

		Block block = state.getBlock();
		ResourceLocation rl = block.getRegistryName();
		String blockName = (rl != null ? rl.getPath() : "");
		switch (blockName) {
			case "stone_brick_stairs", "stonebrick", "stone_slab", "stone" -> {
				if (rand.nextInt(20) == 0) {
					world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
				}
			}
			case "cobblestone" -> {
				if (rand.nextInt(15) == 0) {
					world.setBlockState(pos, Blocks.GRAVEL.getDefaultState());
				}
			}
			case "sandstone" -> {
				if (rand.nextInt(5) == 0) {
					world.setBlockState(pos, Blocks.SAND.getDefaultState());
				}
			}
			case "hardened_clay", "stained_hardened_clay" -> {
				if (rand.nextInt(10) == 0) {
					world.setBlockState(pos, Blocks.CLAY.getDefaultState());
				}
			}
			default -> {
				if (state.getMaterial() == Material.WOOD) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.CACTUS) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.CAKE) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.CIRCUITS) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.CLOTH) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.CORAL) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.CRAFTED_SNOW) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.GLASS) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.GOURD) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.ICE) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.LEAVES) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.PACKED_ICE) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.PISTON) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.PLANTS) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.PORTAL) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.REDSTONE_LIGHT) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.SNOW) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.SPONGE) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.VINE) {
					world.setBlockToAir(pos);
				} else if (state.getMaterial() == Material.WEB) {
					world.setBlockToAir(pos);
				} else if (block.getExplosionResistance(null) < 1.2F) {
					world.setBlockToAir(pos);
				}
			}
		}
	}

	@Override
	public int tickRate(World world) {
		return 15;
	}

    @Override
    public float getFogDensity() {
        return 2.0F;
    }

    @Override
    public int getFogColor() {
        return 0x895129;
    }
}
