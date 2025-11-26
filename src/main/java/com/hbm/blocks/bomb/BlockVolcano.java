package com.hbm.blocks.bomb;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNT.ExAttrib;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.IDynamicModels;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlockVolcano extends Block implements IDynamicModels {

	public static final PropertyInteger META = BlockDummyable.META;
	
	public BlockVolcano(String s) {
		super(Material.IRON);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
        INSTANCES.add(this);
	}
	
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items){
		if(tab == CreativeTabs.SEARCH || tab == this.getCreativeTab())
			for(int i = 0; i < 4; ++i) {
				items.add(new ItemStack(this, 1, i));
			}
	}
	
	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced){
		int meta = stack.getItemDamage();

		tooltip.add(BlockVolcano.isGrowing(meta) ? (TextFormatting.RED + "DOES GROW") : (TextFormatting.DARK_GRAY + "DOES NOT GROW"));
		tooltip.add(BlockVolcano.isExtinguishing(meta) ? (TextFormatting.RED + "DOES EXTINGUISH") : (TextFormatting.DARK_GRAY + "DOES NOT EXTINGUISH"));
	}
	
	@Override
	public int tickRate(World world) {
		return 5;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state){
		if(!world.isRemote)
			world.scheduleUpdate(pos, this, this.tickRate(world));
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand){
		if(!world.isRemote) {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			
			int meta = world.getBlockState(pos).getValue(META);
			blastMagmaChannel(world, x, y, z, rand);
			raiseMagma(world, x, y, z, rand);
			spawnBlobs(world, x, y, z, rand);
			spawnSmoke(world, x, y, z, rand);
			
			updateVolcano(world, x, y, z, rand, meta);
		}
	}

	private void blastMagmaChannel(World world, int x, int y, int z, Random rand) {
		
		List<ExAttrib> attribs = Arrays.asList(new ExAttrib[] {ExAttrib.NODROP, ExAttrib.LAVA_V, ExAttrib.NOSOUND, ExAttrib.ALLMOD, ExAttrib.NOHURT});
		
		ExplosionNT explosion = new ExplosionNT(world, null, x + 0.5, y + rand.nextInt(15) + 1.5, z + 0.5, 7);
		explosion.addAllAttrib(attribs);
		explosion.explode();
		
		ExplosionNT explosion2 = new ExplosionNT(world, null, x + 0.5 + rand.nextGaussian() * 3, rand.nextInt(y + 1), z + 0.5 + rand.nextGaussian() * 3, 10);
		explosion2.addAllAttrib(attribs);
		explosion2.explode();
	}
	
	private void raiseMagma(World world, int x, int y, int z, Random rand) {

		int rX = x - 10 + rand.nextInt(21);
		int rY = y + rand.nextInt(11);
		int rZ = z - 10 + rand.nextInt(21);
		BlockPos pos = new BlockPos(rX, rY, rZ);
		
		if(world.getBlockState(pos).getBlock() == Blocks.AIR && world.getBlockState(pos.down()).getBlock() == ModBlocks.volcanic_lava_block)
			world.setBlockState(pos, ModBlocks.volcanic_lava_block.getDefaultState());
	}
	
	private void spawnBlobs(World world, int x, int y, int z, Random rand) {
		
		for(int i = 0; i < 3; i++) {
			EntityShrapnel frag = new EntityShrapnel(world);
			frag.setLocationAndAngles(x + 0.5, y + 1.5, z + 0.5, 0.0F, 0.0F);
			frag.motionY = 1D + rand.nextDouble();
			frag.motionX = rand.nextGaussian() * 0.2D;
			frag.motionZ = rand.nextGaussian() * 0.2D;
			frag.setVolcano(true);
			world.spawnEntity(frag);
		}
	}
	
	/*
	 * I SEE SMOKE, AND WHERE THERE'S SMOKE THERE'S FIRE!
	 */
	private void spawnSmoke(World world, int x, int y, int z, Random rand) {
		NBTTagCompound dPart = new NBTTagCompound();
		dPart.setString("type", "vanillaExt");
		dPart.setString("mode", "volcano");
		PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(dPart, x + 0.5, y + 10, z + 0.5), new TargetPoint(world.provider.getDimension(), x + 0.5, y + 10, z + 0.5, 250));
	}
	
	private void updateVolcano(World world, int x, int y, int z, Random rand, int meta) {
		BlockPos pos = new BlockPos(x, y, z);
		if(rand.nextDouble() < this.getProgressChance(world, x, y, z, rand, meta)) {
			
			//if there's progress, check if the volcano can grow or not
			if(shouldGrow(world, x, y, z, rand, meta)) {
				
				//raise the level for growing volcanos, spawn lava, schedule update at the new position
				y++;
				world.scheduleUpdate(pos, this, this.tickRate(world));
				
				for(int i = -1; i <= 1; i++) {
					for(int j = -1; j <= 1; j++) {
						for(int k = -1; k <= 1; k++) {
							
							if(i + j + k == 0) {
								world.setBlockState(pos, this.getDefaultState().withProperty(META, meta), 3);
							} else {
								world.setBlockState(pos.add(i, j, k), ModBlocks.volcanic_lava_block.getDefaultState());
							}
						}
					}
				}
				
			//a progressing volcano that can't grow will extinguish
			} else if(isExtinguishing(meta)) {
				world.setBlockState(pos, ModBlocks.volcanic_lava_block.getDefaultState());
			}
			
		//if there's no progress, schedule an update on the current position
		}
		
		world.scheduleUpdate(pos, this, this.tickRate(world));
	}

	public static final int META_STATIC_ACTIVE = 0;
	public static final int META_STATIC_EXTINGUISHING = 1;
	public static final int META_GROWING_ACTIVE = 2;
	public static final int META_GROWING_EXTINGUISHING = 3;
	
	public static boolean isGrowing(int meta) {
		return meta == META_GROWING_ACTIVE || meta == META_GROWING_EXTINGUISHING;
	}
	
	public static boolean isExtinguishing(int meta) {
		return meta == META_STATIC_EXTINGUISHING || meta == META_GROWING_EXTINGUISHING;
	}
	
	private boolean shouldGrow(World world, int x, int y, int z, Random rand, int meta) {
		
		//non-growing volcanoes should extinguish
		if(!isGrowing(meta))
			return false;
		
		//growing volcanoes extinguish when exceeding 200 blocks
		return y < 200;
	}
	
	private double getProgressChance(World world, int x, int y, int z, Random rand, int meta) {

		if(meta == META_STATIC_EXTINGUISHING)
			return 0.00003D; //about once every hour
		
		if(isGrowing(meta)) {
			
			if(meta != META_GROWING_ACTIVE || y < 199)
				return 0.007D; //about 250x an hour
		}
		
		return 0;
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
		return this.getDefaultState().withProperty(META, meta);
	}

    @Override
    public void bakeModel(ModelBakeEvent event) {

    }

    @Override
    public void registerModel() {
        var item = Item.getItemFromBlock(this);

        for (int i = 0; i < 4; i++) {
            ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    @Override
    public void registerSprite(TextureMap map) {

    }
}