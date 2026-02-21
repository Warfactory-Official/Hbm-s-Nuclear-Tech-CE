package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.tileentity.deco.TileEntityTrappedBrick;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class TrappedBrick extends BlockContainer {

	public static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 15);

	public TrappedBrick(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if(Trap.get(meta).type == TrapType.DETECTOR)
			return new TileEntityTrappedBrick();
		return null;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag advanced) {
		list.add(Trap.get(stack.getItemDamage()).toString());
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		int meta = world.getBlockState(pos).getValue(TYPE);

    	if(world.isRemote || Trap.get(meta).type != TrapType.ON_STEP || !(entity instanceof EntityPlayer)) {
    		return;
    	}

    	EntityPlayer player = (EntityPlayer)entity;

		switch(Trap.get(meta)) {
		case FIRE:
			if(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock().isReplaceable(world, new BlockPos(x, y + 1, z)))
				world.setBlockState(new BlockPos(x, y + 1, z), Blocks.FIRE.getDefaultState());
			break;
		case SPIKES:
			if(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock().isReplaceable(world, new BlockPos(x, y + 1, z)))
				world.setBlockState(new BlockPos(x, y + 1, z), ModBlocks.spikes.getDefaultState());
			List<Entity> targets = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(x, y + 1, z, x + 1, y + 2, z + 1));
			for(Entity e : targets)
				e.attackEntityFrom(ModDamageSource.spikes, 10);
			world.playSound(null, x + 0.5, y + 1.5, z + 0.5, HBMSoundHandler.slicer, SoundCategory.HOSTILE, 1.0F, 1.0F);
			break;
		case MINE:
			world.createExplosion(null, x + 0.5, y + 1.5, z + 0.5, 1F, false);
			break;
		case WEB:
			if(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock().isReplaceable(world, new BlockPos(x, y + 1, z)))
				world.setBlockState(new BlockPos(x, y + 1, z), Blocks.WEB.getDefaultState());
			break;
		case RAD_CONVERSION:
			for(int a = - 3; a <= 3; a++) {
				for(int b = - 3; b <= 3; b++) {
					for(int c = - 3; c <= 3; c++) {

						if(world.rand.nextBoolean())
							continue;

						Block bl = world.getBlockState(new BlockPos(x + a, y + b, z + c)).getBlock();
						if(bl == ModBlocks.brick_jungle || bl == ModBlocks.brick_jungle_cracked || bl == ModBlocks.brick_jungle_lava) {
							world.setBlockState(new BlockPos(x + a, y + b, z + c), ModBlocks.brick_jungle_ooze.getDefaultState());
						}
					}
				}
			}
			break;
		case MAGIC_CONVERSTION:
			for(int a = - 3; a <= 3; a++) {
				for(int b = - 3; b <= 3; b++) {
					for(int c = - 3; c <= 3; c++) {

						if(world.rand.nextBoolean())
							continue;

						Block bl = world.getBlockState(new BlockPos(x + a, y + b, z + c)).getBlock();
						if(bl == ModBlocks.brick_jungle || bl == ModBlocks.brick_jungle_cracked || bl == ModBlocks.brick_jungle_lava) {
							world.setBlockState(new BlockPos(x + a, y + b, z + c), ModBlocks.brick_jungle_mystic.getDefaultState());
						}
					}
				}
			}
			break;
		case SLOWNESS:
			player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 300, 2));
			break;
		case WEAKNESS:
			player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 300, 2));
			break;
		default:
			break;
		}

		world.playSound(null, x + 0.5D, y + 0.5D, z + 0.5D, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
		world.setBlockState(new BlockPos(x, y, z), ModBlocks.brick_jungle.getDefaultState());
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TYPE, meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
		if(tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH)
			for (int i = 0; i < Trap.VALUES.length; ++i) {
				items.add(new ItemStack(this, 1, i));
			}
	}

	public static enum TrapType {
		ON_STEP,
		DETECTOR
	}

	public static enum Trap {

		FALLING_ROCKS(TrapType.DETECTOR),
		FIRE(TrapType.ON_STEP),
		ARROW(TrapType.DETECTOR),
		SPIKES(TrapType.ON_STEP),
		MINE(TrapType.ON_STEP),
		WEB(TrapType.ON_STEP),
		FLAMING_ARROW(TrapType.DETECTOR),
		PILLAR(TrapType.DETECTOR),
		RAD_CONVERSION(TrapType.ON_STEP),
		MAGIC_CONVERSTION(TrapType.ON_STEP),
		SLOWNESS(TrapType.ON_STEP),
		WEAKNESS(TrapType.ON_STEP),
		POISON_DART(TrapType.DETECTOR),
		ZOMBIE(TrapType.DETECTOR),
		SPIDERS(TrapType.DETECTOR);
        public static final Trap[] VALUES = values();

		public TrapType type;

		private Trap(TrapType type) {
			this.type = type;
		}

		public static Trap get(int i) {

			if(i >= 0 && i < Trap.VALUES.length)
				return Trap.VALUES[i];

			return FIRE;
		}
	}

}
