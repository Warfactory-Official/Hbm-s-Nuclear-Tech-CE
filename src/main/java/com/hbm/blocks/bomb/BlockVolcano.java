package com.hbm.blocks.bomb;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNT.ExAttrib;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Arrays;
import java.util.List;

public class BlockVolcano extends BlockContainer implements ICustomBlockItem {

	public static final PropertyInteger META = BlockDummyable.META;

	public BlockVolcano(String s) {
		super(Material.IRON);
		setTranslationKey(s);
		setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityVolcanoCore();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items){
		if(tab == CreativeTabs.SEARCH || tab == getCreativeTab())
			for(int i = 0; i < 5; ++i) {
				items.add(new ItemStack(this, 1, i));
			}
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced){
		int meta = stack.getItemDamage();

		if(meta == META_SMOLDERING) {
			tooltip.add(TextFormatting.GOLD + "SHIELD VOLCANO");
			return;
		}

		tooltip.add(BlockVolcano.isGrowing(meta) ? (TextFormatting.RED + "DOES GROW") : (TextFormatting.DARK_GRAY + "DOES NOT GROW"));
		tooltip.add(BlockVolcano.isExtinguishing(meta) ? (TextFormatting.RED + "DOES EXTINGUISH") : (TextFormatting.DARK_GRAY + "DOES NOT EXTINGUISH"));
	}

	public static final int META_STATIC_ACTIVE = 0;
	public static final int META_STATIC_EXTINGUISHING = 1;
	public static final int META_GROWING_ACTIVE = 2;
	public static final int META_GROWING_EXTINGUISHING = 3;
	public static final int META_SMOLDERING = 4;

	public static boolean isGrowing(int meta) {
		return meta == META_GROWING_ACTIVE || meta == META_GROWING_EXTINGUISHING;
	}

	public static boolean isExtinguishing(int meta) {
		return meta == META_STATIC_EXTINGUISHING || meta == META_GROWING_EXTINGUISHING;
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, META);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(META);
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(META, meta);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(META);
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
			float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(META, meta);
	}

	@Override
	public void registerItem() {
		ItemBlock itemBlock = new ItemBlockVolcano(this);
		itemBlock.setRegistryName(getRegistryName());
		ForgeRegistries.ITEMS.register(itemBlock);
	}

	public static class ItemBlockVolcano extends ItemBlock {
		public ItemBlockVolcano(Block block) {
			super(block);
			setHasSubtypes(true);
			canRepair = false;
		}

		@Override
		public int getMetadata(int damage) {
			return damage;
		}
	}

	@AutoRegister(name = "tileentity_volcano_core")
	public static class TileEntityVolcanoCore extends TileEntityLoadedBase implements ITickable {

		private static final List<ExAttrib> volcanoExplosion = Arrays.asList(ExAttrib.NODROP, ExAttrib.LAVA_V, ExAttrib.NOSOUND, ExAttrib.ALLMOD, ExAttrib.NOHURT);
		private static final List<ExAttrib> volcanoRadExplosion = Arrays.asList(ExAttrib.NODROP, ExAttrib.LAVA_R, ExAttrib.NOSOUND, ExAttrib.ALLMOD, ExAttrib.NOHURT);

		public int volcanoTimer;

		@Override
		public void update() {
			if(world.isRemote) return;

			volcanoTimer++;

			if(volcanoTimer % 10 == 0) {
				if(hasVerticalChannel()) {
					blastMagmaChannel();
					raiseMagma();
				}

				double magmaChamber = magmaChamberSize();
				if(magmaChamber > 0) blastMagmaChamber(magmaChamber);

				Object[] melting = surfaceMeltingParams();
				if(melting != null) meltSurface((int) melting[0], (double) melting[1], (double) melting[2]);

				if(isSpewing()) spawnBlobs();
				if(isSmoking()) spawnSmoke();

				surroundLava();
			}

			if(volcanoTimer >= getUpdateRate()) {
				volcanoTimer = 0;

				if(shouldGrow()) {
					int meta = getMeta();
					Block self = world.getBlockState(pos).getBlock();
					world.setBlockState(pos.up(), self.getDefaultState().withProperty(META, meta), 3);
					world.setBlockState(pos, getLava().getDefaultState());
					return;
				} else if(isExtinguishing()) {
					world.setBlockState(pos, getLava().getDefaultState());
					return;
				}
			}
		}

		private int getMeta() {
			IBlockState state = world.getBlockState(pos);
			if(!(state.getBlock() instanceof BlockVolcano)) return 0;
			return state.getValue(META);
		}

		public boolean isRadioacitve() {
			return world.getBlockState(pos).getBlock() == ModBlocks.volcano_rad_core;
		}

		protected Block getLava() {
			return isRadioacitve() ? ModBlocks.rad_lava_block : ModBlocks.volcanic_lava_block;
		}

		protected List<ExAttrib> getExpAttrb() {
			return isRadioacitve() ? volcanoRadExplosion : volcanoExplosion;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			volcanoTimer = nbt.getInteger("timer");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("timer", volcanoTimer);
			return nbt;
		}

		private boolean shouldGrow() {
			return isGrowing() && pos.getY() < 200;
		}

		private boolean isGrowing() {
			int meta = getMeta();
			return meta == META_GROWING_ACTIVE || meta == META_GROWING_EXTINGUISHING;
		}

		private boolean isExtinguishing() {
			int meta = getMeta();
			return meta == META_STATIC_EXTINGUISHING || meta == META_GROWING_EXTINGUISHING;
		}

		private boolean isSmoking() {
			return getMeta() != META_SMOLDERING;
		}

		private boolean isSpewing() {
			return getMeta() != META_SMOLDERING;
		}

		private boolean hasVerticalChannel() {
			return getMeta() != META_SMOLDERING;
		}

		private double magmaChamberSize() {
			return getMeta() == META_SMOLDERING ? 15 : 0;
		}

		/* count per tick, radius, depth */
		private Object[] surfaceMeltingParams() {
			return getMeta() == META_SMOLDERING ? new Object[] {50, 50D, 10D} : null;
		}

		private int getUpdateRate() {
			switch(getMeta()) {
				case META_STATIC_EXTINGUISHING: return 60 * 60 * 20; //once per hour
				case META_GROWING_ACTIVE:
				case META_GROWING_EXTINGUISHING: return 60 * 60 * 20 / 250; //250x per hour
				default: return 10;
			}
		}

		/** Causes two magma explosions, one from bedrock to the core and one from the core to 15 blocks above. */
		private void blastMagmaChannel() {
			ExplosionNT explosion = new ExplosionNT(world, null, pos.getX() + 0.5, pos.getY() + world.rand.nextInt(15) + 1.5, pos.getZ() + 0.5, 7);
			explosion.addAllAttrib(getExpAttrb()).explode();
			ExplosionNT explosion2 = new ExplosionNT(world, null, pos.getX() + 0.5 + world.rand.nextGaussian() * 3, world.rand.nextInt(pos.getY() + 1), pos.getZ() + 0.5 + world.rand.nextGaussian() * 3, 10);
			explosion2.addAllAttrib(getExpAttrb()).explode();
		}

		/** Causes two magma explosions at a random position around the core, one at normal and one at half range. */
		private void blastMagmaChamber(double size) {
			for(int i = 0; i < 2; i++) {
				double dist = size / (double) (i + 1);
				ExplosionNT explosion = new ExplosionNT(world, null, pos.getX() + 0.5 + world.rand.nextGaussian() * dist, pos.getY() + 0.5 + world.rand.nextGaussian() * dist, pos.getZ() + 0.5 + world.rand.nextGaussian() * dist, 7);
				explosion.addAllAttrib(getExpAttrb()).explode();
			}
		}

		/** Randomly selects surface blocks and converts them into lava if solid or air if not solid. */
		private void meltSurface(int count, double radius, double depth) {
			for(int i = 0; i < count; i++) {
				int x = (int) Math.floor(pos.getX() + world.rand.nextGaussian() * radius);
				int z = (int) Math.floor(pos.getZ() + world.rand.nextGaussian() * radius);
				int y = world.getHeight(x, z) + 1 - (int) Math.floor(Math.abs(world.rand.nextGaussian() * depth));

				BlockPos targetPos = new BlockPos(x, y, z);
				IBlockState targetState = world.getBlockState(targetPos);
				Block b = targetState.getBlock();

				if(!b.isAir(targetState, world, targetPos) && b.getExplosionResistance(null) < Blocks.OBSIDIAN.getExplosionResistance(null)) {
					world.setBlockState(targetPos, b.isNormalCube(targetState, world, targetPos) ? getLava().getDefaultState() : Blocks.AIR.getDefaultState());
				}
			}
		}

		/** Increases the magma level in a small radius around the core. */
		private void raiseMagma() {
			int rX = pos.getX() - 10 + world.rand.nextInt(21);
			int rY = pos.getY() + world.rand.nextInt(11);
			int rZ = pos.getZ() - 10 + world.rand.nextInt(21);

			BlockPos rPos = new BlockPos(rX, rY, rZ);
			BlockPos rDown = rPos.down();

			if(world.getBlockState(rPos).getBlock() == Blocks.AIR && world.getBlockState(rDown).getBlock() == getLava())
				world.setBlockState(rPos, getLava().getDefaultState());
		}

		/** Creates a 3x3x3 lava sphere around the core. */
		private void surroundLava() {
			for(int i = -1; i <= 1; i++) {
				for(int j = -1; j <= 1; j++) {
					for(int k = -1; k <= 1; k++) {
						if(i != 0 || j != 0 || k != 0) {
							world.setBlockState(pos.add(i, j, k), getLava().getDefaultState());
						}
					}
				}
			}
		}

		/** Spews specially tagged shrapnels which create volcanic lava and monoxide clouds. */
		private void spawnBlobs() {
			for(int i = 0; i < 3; i++) {
				EntityShrapnel frag = new EntityShrapnel(world);
				frag.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 0.0F, 0.0F);
				frag.motionY = 1D + world.rand.nextDouble();
				frag.motionX = world.rand.nextGaussian() * 0.2D;
				frag.motionZ = world.rand.nextGaussian() * 0.2D;
				if(isRadioacitve()) {
					frag.setRadVolcano(true);
				} else {
					frag.setVolcano(true);
				}
				world.spawnEntity(frag);
			}
		}

		/** I SEE SMOKE, AND WHERE THERE'S SMOKE THERE'S FIRE! */
		private void spawnSmoke() {
			NBTTagCompound dPart = new NBTTagCompound();
			dPart.setString("type", "vanillaExt");
			dPart.setString("mode", "volcano");
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(dPart, pos.getX() + 0.5, pos.getY() + 10, pos.getZ() + 0.5), new TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 10, pos.getZ() + 0.5, 250));
		}
	}
}
