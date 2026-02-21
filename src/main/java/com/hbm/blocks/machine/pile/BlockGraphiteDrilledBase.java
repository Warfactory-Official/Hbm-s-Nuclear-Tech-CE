package com.hbm.blocks.machine.pile;

import com.hbm.Tags;
import com.hbm.api.block.IInsertable;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockFlammable;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemCell;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockGraphiteDrilledBase extends BlockFlammable implements IToolable, IInsertable {

	protected String sideTexture;
	protected String aluminumTexture;

	public BlockGraphiteDrilledBase(String s) {
		super(ModBlocks.block_graphite.getDefaultState().getMaterial(), s, ((BlockFlammable) ModBlocks.block_graphite).encouragement, ((BlockFlammable) ModBlocks.block_graphite).flammability);
		sideTexture = s;
		aluminumTexture = s + "_aluminum";
		this.setCreativeTab(null);
		this.setSoundType(SoundType.METAL);
		this.setHardness(5.0F);
		this.setResistance(10.0F);

		this.blockFrames = new BlockBakeFrame[16];
		for (int meta = 0; meta < 16; meta++) {
			boolean isAluminum = (meta & 4) != 0;
			String front = isAluminum ? aluminumTexture : sideTexture;
			this.blockFrames[meta] = new BlockBakeFrame(front, "block_graphite", front);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerSprite(TextureMap map) {
		map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/" + sideTexture));
		map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/" + aluminumTexture));
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		int meta = getMetaFromState(state);
		List<ItemStack> drops = new ArrayList<>();
		drops.add(new ItemStack(ModItems.ingot_graphite, 8));
		if ((meta & 4) == 4)
			drops.add(new ItemStack(ModItems.shell, 1, Mats.MAT_ALUMINIUM.id));
		if (getInsertedItem() != null)
			drops.add(getInsertedItem(meta));
		return drops;
	}
	
	protected static void ejectItem(World world, int x, int y, int z, EnumFacing dir, ItemStack stack) {
		
		EntityItem dust = new EntityItem(world, x + 0.5D + dir.getXOffset() * 0.75D, y + 0.5D + dir.getYOffset() * 0.75D, z + 0.5D + dir.getZOffset() * 0.75D, stack);
		dust.motionX = dir.getXOffset() * 0.25;
		dust.motionY = dir.getYOffset() * 0.25;
		dust.motionZ = dir.getZOffset() * 0.25;
		world.spawnEntity(dust);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune){
		return Items.AIR;
	}


	protected ItemStack getInsertedItem(int meta) {
		return getInsertedItem();
	}

	protected ItemStack getInsertedItem() {
		return ItemStack.EMPTY;
	}

	//Checks the relationship between specific items and placement.
	//kinda cringe but anything other than hardcoding would be overengineering this for no reason so
	//all of this is destined to be changed most likely anyway
	protected RecipesCommon.MetaBlock checkInteractions(ItemStack stack) {
		Item item = stack.getItem(); //temp
		if(item == ModItems.pile_rod_uranium) return new RecipesCommon.MetaBlock(ModBlocks.block_graphite_fuel);
		if(item == ModItems.pile_rod_pu239) return new RecipesCommon.MetaBlock(ModBlocks.block_graphite_fuel, 0b1000);
		if(item == ModItems.pile_rod_plutonium) return new RecipesCommon.MetaBlock(ModBlocks.block_graphite_plutonium);
		if(item == ModItems.pile_rod_source) return new RecipesCommon.MetaBlock(ModBlocks.block_graphite_source);
		if(item == ModItems.pile_rod_boron) return new RecipesCommon.MetaBlock(ModBlocks.block_graphite_rod);
		if(item == ModItems.pile_rod_lithium) return new RecipesCommon.MetaBlock(ModBlocks.block_graphite_lithium);
		if(stack == ItemCell.getFullCell(Fluids.TRITIUM)) return new RecipesCommon.MetaBlock(ModBlocks.block_graphite_tritium);
		if(item == ModItems.pile_rod_detector) return new RecipesCommon.MetaBlock(ModBlocks.block_graphite_detector);
		return null;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
		if (tool != ToolType.SCREWDRIVER)
			return false;

		if (!world.isRemote) {
			BlockPos pos = new BlockPos(x, y, z);
			int meta = getMetaFromState(world.getBlockState(pos));
			int cfg = meta & 3;
			int sideIdx = side.getIndex();
			if (sideIdx == cfg * 2 || sideIdx == cfg * 2 + 1) {
				world.setBlockState(pos, ModBlocks.block_graphite_drilled.getDefaultState().withProperty(BlockMeta.META, meta & 7), 3);
				ejectItem(world, x, y, z, side, getInsertedItem(meta));
			}
		}
		return true;
	}

	@Override
	public boolean insertItem(World world, int x, int y, int z, EnumFacing dir, ItemStack stack) {

		if(stack == null) return false;

		RecipesCommon.MetaBlock baseBlock = checkInteractions(stack);
		if(baseBlock == null) return false;

		final int side = dir.ordinal();
		int meta = getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
		int pureMeta = meta & 3; //in case it's shrouded in aluminum

		if(side == pureMeta * 2 || side == pureMeta * 2 + 1) {
			//first, make sure we can even push rods out
			for(int i = 0; true; i++) { //limited to 3 boyos
				int ix = x + dir.getXOffset() * i;
				int iy = y + dir.getYOffset() * i;
				int iz = z + dir.getZOffset() * i;
				BlockPos iPos = new BlockPos(ix, iy, iz);
				IBlockState iState = world.getBlockState(iPos);
				Block b = iState.getBlock();

				if(b instanceof BlockGraphiteDrilledBase) {
					int baseMeta = getMetaFromState(world.getBlockState(iPos));
					if((baseMeta & 3) != pureMeta) //wrong orientation
						return false;

					if(((BlockGraphiteDrilledBase)b).getInsertedItem(baseMeta) == null) //if there's nothing to push
						break;
					else if(i >= 3) //if there is stuff to push and we reach our limit
						return false;
				} else {
					if(b.isNormalCube(iState, world, iPos)) //obstructions
						return false;
					else //empty space? no need to search
						break;
				}
			}

			//TODO convert old methods to use itemstack for flexibility
			int oldMeta = pureMeta | baseBlock.meta; //metablocks are kinda inconvenient to work with so
			Block oldBlock = baseBlock.block;
			NBTTagCompound oldTag = new NBTTagCompound(); //In case of TEs
			oldTag.setInteger("x", x); //giving tags prevents issues and resets any lingering tes.
			oldTag.setInteger("y", y);
			oldTag.setInteger("z", z);

			//now actually make the change
			for(int i = 0; i <= 3; i++) { //yeah yeah we know it's safe but let's be *extra cautious* of infinite loops
				int ix = x + dir.getXOffset() * i;
				int iy = y + dir.getYOffset() * i;
				int iz = z + dir.getZOffset() * i;
				BlockPos iPos = new BlockPos(ix, iy, iz);
				IBlockState iState = world.getBlockState(iPos);
				Block newBlock = iState.getBlock();

				if(newBlock instanceof BlockGraphiteDrilledBase) {
					int newMeta = getMetaFromState(world.getBlockState(iPos));
					NBTTagCompound newTag = new NBTTagCompound();

					if(newBlock instanceof BlockGraphiteDrilledTE) {
						TileEntity te = world.getTileEntity(iPos);
						te.writeToNBT(newTag);
						newTag.setInteger("x", te.getPos().getX() + dir.getXOffset()); //malformed positions is very very bad and prevents the pile TEs from ticking
						newTag.setInteger("y", te.getPos().getY() + dir.getYOffset());
						newTag.setInteger("z", te.getPos().getZ() + dir.getZOffset());
					}

					world.setBlockState(iPos, oldBlock.getDefaultState().withProperty(BlockMeta.META, (oldMeta & ~0b100) | (newMeta & 0b100)), 0);

					if(oldBlock instanceof BlockGraphiteDrilledTE && !oldTag.isEmpty()) { //safety first
						TileEntity te = world.getTileEntity(iPos);
						te.readFromNBT(oldTag);
					}

					world.markAndNotifyBlock(iPos, world.getChunk(ix, iz), newBlock.getDefaultState(), oldBlock.getDefaultState(), 3); //in case setBlock returns false due to = meta / block

					oldMeta = newMeta;
					oldBlock = newBlock;
					oldTag = newTag;

					if(oldBlock == ModBlocks.block_graphite_drilled) //if there's no need to eject an item
						break;
				} else {
					ItemStack eject = ((BlockGraphiteDrilledBase) oldBlock).getInsertedItem(oldMeta);
					ejectItem(world, ix - dir.getXOffset(), iy - dir.getYOffset(), iz - dir.getZOffset(), dir, eject);
					world.playSound(null, ix + 0.5, iy + 0.5, iz + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.25F, 1.0F);

					break;
				}
			}

			return true;
		}

		return false;
	}
}
