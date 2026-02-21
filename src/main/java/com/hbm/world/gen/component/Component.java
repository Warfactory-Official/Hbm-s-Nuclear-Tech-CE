package com.hbm.world.gen.component;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.blocks.generic.BlockBobble.TileEntityBobble;
import com.hbm.config.StructureConfig;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLever;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

abstract public class Component extends StructureComponent {

	/** Average height (Presumably stands for height position) */
	protected int hpos = -1;

	protected Component() {
		super(0);
	}

	protected Component(int componentType) {
		super(componentType);
	}

	protected Component(Random rand, int minX, int minY, int minZ, int maxX, int maxY, int maxZ ) {
		super(0);
		this.setCoordBaseMode(EnumFacing.byIndex(rand.nextInt(4)));

		switch (this.getCoordMode()) {
			case 1, 3:
				this.boundingBox = new StructureBoundingBox(minX, minY, minZ, minX + maxZ, minY + maxY, minZ + maxX);
				break;
            default:
				this.boundingBox = new StructureBoundingBox(minX, minY, minZ, minX + maxX, minY + maxY, minZ + maxZ);

		}
	}

	/** Set to NBT */
	protected void setToNBT(NBTTagCompound nbt) {
		nbt.setInteger("HPos", this.hpos);
	}

	/** Get from NBT */
	protected void getFromNBT(NBTTagCompound nbt) {
		this.hpos = nbt.getInteger("HPos");
	}

	protected boolean setAverageHeight(World world, StructureBoundingBox box, int y) {

		int total = 0;
		int iterations = 0;

		for(int z = this.boundingBox.minZ; z <= this.boundingBox.maxZ; z++) {
			for(int x = this.boundingBox.minX; x <= this.boundingBox.maxX; x++) {
				if(box.isVecInside(new Vec3i(x, y, z))) {
					total += Math.max(world.getTopSolidOrLiquidBlock(new BlockPos(x, y, z)).getY(), world.provider.getAverageGroundLevel());
					iterations++;
				}
			}
		}

		if(iterations == 0)
			return false;

		this.hpos = total / iterations; //finds mean of every block in bounding box
		this.boundingBox.offset(0, this.hpos - this.boundingBox.minY, 0);
		return true;
	}

	protected static int getAverageHeight(World world, StructureBoundingBox area, StructureBoundingBox box, int y) {

		int total = 0;
		int iterations = 0;

		for(int z = area.minZ; z <= area.maxZ; z++) {
			for(int x = area.minX; x <= area.maxX; x++) {
				if(box.isVecInside(new Vec3i(x, y, z))) {
					total += Math.max(world.getTopSolidOrLiquidBlock(new BlockPos(x, y, z)).getY(), world.provider.getAverageGroundLevel());
					iterations++;
				}
			}
		}

		if(iterations == 0)
			return -1;

		return total / iterations;
	}

	public int getCoordMode() {
		return this.getCoordBaseMode() != null ? this.getCoordBaseMode().getIndex() : 0;
	}

	/** Metadata for Decoration Methods **/

	/**
	 * Gets metadata for rotatable pillars.
	 * @param metadata (First two digits are equal to block metadata, other two are equal to orientation
	 * @return metadata adjusted for random orientation
	 */
	protected int getPillarMeta(int metadata) {
		if(this.getCoordMode() % 2 != 0 && this.getCoordMode() != -1)
			metadata = metadata ^ 12;

		return metadata;
	}

	/**
	 * Gets metadata for rotatable DecoBlock
	 * honestly i don't remember how i did this and i'm scared to optimize it because i fail to see any reasonable patterns like the pillar
	 * should work for hoppers, just flip dir for N/S and W/E
	 * @param metadata (2 for facing South, 3 for facing North, 4 for facing East, 5 for facing West
	 */
	protected int getDecoMeta(int metadata) {
		switch(this.getCoordMode()) {
		case 0: //South
			switch(metadata) {
			case 2: return 2;
			case 3: return 3;
			case 4: return 4;
			case 5: return 5;
			}
		case 1: //West
			switch(metadata) {
			case 2: return 5;
			case 3: return 4;
			case 4: return 2;
			case 5: return 3;
			}
		case 2: //North
			switch(metadata) {
			case 2: return 3;
			case 3: return 2;
			case 4: return 5;
			case 5: return 4;
			}
		case 3: //East
			switch(metadata) {
			case 2: return 4;
			case 3: return 5;
			case 4: return 3;
			case 5: return 2;
			}
		}
		return 0;
	}

	/**
	 * Get orientation-offset metadata for BlockDecoModel; also suitable for trapdoors
	 * @param metadata (0 for facing North, 1 for facing South, 2 for facing West, 3 for facing East)
	 */
	protected int getDecoModelMeta(int metadata) {
		//N: 0b00, S: 0b01, W: 0b10, E: 0b11

		switch(this.getCoordMode()) {
            case 1: //West
			if((metadata & 3) < 2) //N & S can just have bits toggled
				metadata = metadata ^ 3;
			else //W & E can just have first bit set to 0
				metadata = metadata ^ 2;
			break;
		case 2: //North
			metadata = metadata ^ 1; //N, W, E & S can just have first bit toggled
			break;
		case 3: //East
			if((metadata & 3) < 2)//N & S can just have second bit set to 1
				metadata = metadata ^ 2;
			else //W & E can just have bits toggled
				metadata = metadata ^ 3;
			break;
            default: //South
                break;
        }
		//genuinely like. why did i do that
		return metadata << 2; //To accommodate for BlockDecoModel's shift in the rotation bits; otherwise, simply bit-shift right and or any non-rotation meta after
	}

	//works for crts, toasters, and anything that follows mc's cardinal dirs. S: 0, W: 1, N: 2, E: 3
	protected int getCRTMeta(int meta) {
		return (meta + this.getCoordMode()) % 4;
	}

	/**
	 * Gets orientation-adjusted meta for stairs.
	 * 0 = West, 1 = East, 2 = North, 3 = South
	 */
	protected int getStairMeta(int metadata) {
		switch (this.getCoordMode()) {
			case 1: //West
				if ((metadata & 3) < 2) //Flip second bit for E/W
					metadata = metadata ^ 2;
				else
					metadata = metadata ^ 3; //Flip both bits for N/S
				break;
			case 2: //North
				metadata = metadata ^ 1; //Flip first bit
				break;
			case 3: //East
				if ((metadata & 3) < 2) //Flip both bits for E/W
					metadata = metadata ^ 3;
				else //Flip second bit for N/S
					metadata = metadata ^ 2;
				break;
			default: //South
				break;
		}

		return metadata;
	}

	/*
	 * Assuming door is on opposite side of block from direction: East: 0, South: 1, West: 2, North: 3<br>
	 * Doors cleverly take advantage of the use of two blocks to get around the 16 value limit on metadata, with the top and bottom blocks essentially relying on eachother for everything.<br>
	 * <li>The 4th bit (0b1000 or 8) indicates whether it is the top block: on for yes, off for no.
	 * <li>When the 4th bit is on, the 1st bit indicates whether the door opens to the right or not: on (0b1001) for yes, off (0b1000) for no.
	 * <li>The bits 1 & 2 (0b0011 or 3) indicate the direction the door is facing.
	 * <li>When the 4th bit is off, the 3rd bit (0b0100 or 4) indicates whether the door is open or not: on for yes, off for no. Used for doors' interactions with redstone power.
	 * </li>
	 */
	protected void placeDoor(World world, StructureBoundingBox box, Block door, int dirMeta, boolean opensRight, boolean isOpen, int featureX, int featureY, int featureZ) { //isOpen for randomly opened doors
		int posX = this.getXWithOffset(featureX, featureZ);
		int posY = this.getYWithOffset(featureY);
		int posZ = this.getZWithOffset(featureX, featureZ);

		if(!box.isVecInside(new Vec3i(posX, posY, posZ))) return;

		switch(this.getCoordMode()) {
		case 1: //West
			dirMeta = (dirMeta + 1) % 4; break;
		case 2: //North
			dirMeta ^= 2; break; //Flip second bit
		case 3: //East
			dirMeta = (dirMeta + 3) % 4; break; //fuck you modulo
		default: //South
			break;
		}

		//hee hoo
		int metaTop = opensRight ? 0b1001 : 0b1000;
		int metaBottom = dirMeta | (isOpen ? 0b100 : 0);
		BlockPos pos = new BlockPos(posX, posY, posZ);

		if(doesBlockHaveSolidTopSurface(world, posX, posY - 1, posZ)) {
			world.setBlockState(pos, door.getStateFromMeta(metaBottom), 2);
			world.setBlockState(pos.up(), door.getStateFromMeta(metaTop), 2);
		}
	}

	public static boolean doesBlockHaveSolidTopSurface(IBlockAccess world, int x, int y, int z)
	{
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		return block.isSideSolid(state, world, new BlockPos(x, y, z), EnumFacing.UP);
	}
	/** 1 for west face, 2 for east face, 3 for north, 4 for south*/
	protected void placeLever(World world, StructureBoundingBox box, int dirMeta, boolean on, int featureX, int featureY, int featureZ) {
		int posX = this.getXWithOffset(featureX, featureZ);
		int posY = this.getYWithOffset(featureY);
		int posZ = this.getZWithOffset(featureX, featureZ);

		if(!box.isVecInside(new Vec3i(posX, posY, posZ))) return;

		if(dirMeta <= 0 || dirMeta >= 7) { //levers suck ass
			switch(this.getCoordMode()) {
			case 1: case 3: //west / east
				dirMeta ^= 0b111;
			}
		} else if(dirMeta >= 5) {
			switch(this.getCoordMode()) {
			case 1: case 3: //west / east
				dirMeta = (dirMeta + 1) % 2 + 5;
			}
		} else {
			dirMeta = getButtonMeta(dirMeta);
		}

		BlockPos pos = new BlockPos(posX, posY, posZ);
		IBlockState state = Blocks.LEVER.getDefaultState()
				.withProperty(BlockLever.FACING, BlockLever.EnumOrientation.byMetadata(dirMeta & 7))
				.withProperty(BlockLever.POWERED, on);
		world.setBlockState(pos, state, 2);
	}

	/** pain. works for side-facing levers as well */
	protected int getButtonMeta(int dirMeta) {
		switch (this.getCoordMode()) { //are you ready for the pain?
			case 1: //West
				if (dirMeta <= 2) return dirMeta + 2;
				else if (dirMeta < 4) return dirMeta - 1;
				else return dirMeta - 3;// this shit sucks ass
			case 2: //North
				return dirMeta + (dirMeta % 2 == 0 ? -1 : 1);
			case 3: //East
				if (dirMeta <= 1) return dirMeta + 3;
				else if (dirMeta == 2) return dirMeta + 1;
				else return dirMeta - 2;
			default: //South
				return dirMeta;
		}
	}

	/**N:0 W:1 S:2 E:3 */
	protected void placeBed(World world, StructureBoundingBox box, int meta, int featureX, int featureY, int featureZ) {
		int xOffset = 0;
		int zOffset = 0;

		switch (meta & 3) {
			case 1:
				xOffset = -1;
				break;
			case 2:
				zOffset = -1;
				break;
			case 3:
				xOffset = 1;
				break;
			default:
				zOffset = 1;
				break;
		}

		switch (this.getCoordMode()) {
			case 1: //W
				meta = (meta + 1) % 4;
				break;
			case 2: //N
				meta ^= 2;
				break;
			case 3: //E
				meta = (meta - 1) % 4;
				break;
			default: //S
				break;
		}

		IBlockState foot = Blocks.BED.getDefaultState()
				.withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT)
				.withProperty(BlockHorizontal.FACING, EnumFacing.byHorizontalIndex(meta & 3));
		IBlockState head = Blocks.BED.getDefaultState()
				.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD)
				.withProperty(BlockHorizontal.FACING, EnumFacing.byHorizontalIndex(meta & 3));

		setBlockState(world, foot, featureX, featureY, featureZ, box);
		setBlockState(world, head, featureX + xOffset, featureY, featureZ + zOffset, box);
	}

	/**Tripwire Hook: S:0 W:1 N:2 E:3 */
	protected int getTripwireMeta(int metadata) {
        return switch (this.getCoordMode()) {
            case 1 -> (metadata + 1) % 4;
            case 2 -> metadata ^ 2;
            case 3 -> (metadata - 1) % 4;
			default -> metadata;
        };
	}


	/** Loot Methods **/

	/**
	 * it feels disgusting to make a method with this many parameters but fuck it, it's easier
	 * @return TE having item capability/implementing IItemHandlerModifiable with randomized contents
	 */
	protected boolean generateInvContents(World world, StructureBoundingBox box, Random rand, Block block, int featureX, int featureY, int featureZ, WeightedRandomChestContentFrom1710[] content, int amount) {
		return generateInvContents(world, box, rand, block, 0, featureX, featureY, featureZ, content, amount);
	}

	//TODO: explore min / max item generations: e.g., between 3 and 5 separate items are generated
	protected boolean generateInvContents(World world, StructureBoundingBox box, Random rand, Block block, int meta, int featureX, int featureY, int featureZ, WeightedRandomChestContentFrom1710[] content, int amount) {
		int posX = this.getXWithOffset(featureX, featureZ);
		int posY = this.getYWithOffset(featureY);
		int posZ = this.getZWithOffset(featureX, featureZ);
		BlockPos pos = new BlockPos(posX, posY, posZ);

		if(!box.isVecInside(pos) || world.getBlockState(pos).getBlock() == block) //replacement for hasPlacedLoot checks
			return true;

		this.setBlockState(world, block.getStateFromMeta(meta), featureX, featureY, featureZ, box);
		TileEntity tile = world.getTileEntity(pos);

		if(tile != null) {
			amount = (int)Math.floor(amount * StructureConfig.lootAmountFactor);
			int rolls = Math.max(amount, 1);

			net.minecraftforge.items.IItemHandler handler = tile.getCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if (handler instanceof net.minecraftforge.items.IItemHandlerModifiable) {
				WeightedRandomChestContentFrom1710.generateChestContents(rand, content, (net.minecraftforge.items.IItemHandlerModifiable) handler, rolls);
				return true;
			} else if (tile instanceof net.minecraftforge.common.capabilities.ICapabilityProvider) {
				WeightedRandomChestContentFrom1710.generateChestContents(rand, content, (net.minecraftforge.common.capabilities.ICapabilityProvider) tile, rolls);
				return true;
			}
		}

		return false;
	}


	/**
	 * Block TE MUST extend TileEntityLockableBase, otherwise this will not work and crash!
	 * @return TE having item capability/implementing IItemHandlerModifiable and extending TileEntityLockableBase with randomized contents + lock
	 */
	protected boolean generateLockableContents(World world, StructureBoundingBox box, Random rand, Block block, int featureX, int featureY, int featureZ,
											   WeightedRandomChestContentFrom1710[] content, int amount, double mod) {
		return generateLockableContents(world, box, rand, block, 0, featureX, featureY, featureZ, content, amount, mod);
	}

	protected boolean generateLockableContents(World world, StructureBoundingBox box, Random rand, Block block, int meta, int featureX, int featureY, int featureZ,
											   WeightedRandomChestContentFrom1710[] content, int amount, double mod) {
		int posX = this.getXWithOffset(featureX, featureZ);
		int posY = this.getYWithOffset(featureY);
		int posZ = this.getZWithOffset(featureX, featureZ);
		BlockPos pos = new BlockPos(posX, posY, posZ);

		if(!box.isVecInside(pos) || world.getBlockState(pos).getBlock() == block) //replacement for hasPlacedLoot checks
			return false;

		this.setBlockState(world, block.getStateFromMeta(meta), featureX, featureY, featureZ, box);
		TileEntity tile = world.getTileEntity(pos);
		TileEntityLockableBase lock = (TileEntityLockableBase) tile;

		if(tile != null) {
			amount = (int)Math.floor(amount * StructureConfig.lootAmountFactor);
			int rolls = amount < 1 ? 1 : amount;

			boolean filled = false;
			IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if (handler instanceof IItemHandlerModifiable) {
				WeightedRandomChestContentFrom1710.generateChestContents(rand, content, (IItemHandlerModifiable) handler, rolls);
				filled = true;
			} else if (tile instanceof ICapabilityProvider) {
				WeightedRandomChestContentFrom1710.generateChestContents(rand, content, (ICapabilityProvider) tile, rolls);
				filled = true;
			}

			if (filled) {
				lock.setPins(rand.nextInt(999) + 1);
				lock.setMod(mod);
				lock.lock();
				return true;
			}
		}

		return false;
	}

	protected void generateLoreBook(World world, StructureBoundingBox box, int featureX, int featureY, int featureZ, int slot, ItemStack stack) {
		int posX = this.getXWithOffset(featureX, featureZ);
		int posY = this.getYWithOffset(featureY);
		int posZ = this.getZWithOffset(featureX, featureZ);
		BlockPos pos = new BlockPos(posX, posY, posZ);

		if(!box.isVecInside(pos)) return;

		TileEntity tile = world.getTileEntity(pos);

		if(tile != null) {
			IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if(handler instanceof IItemHandlerModifiable) {
				((IItemHandlerModifiable) handler).setStackInSlot(slot, stack);
			} else if(tile instanceof IInventory) {
				((IInventory) tile).setInventorySlotContents(slot, stack);
			}
		}
	}

	/**
	 * Places random bobblehead with a randomized orientation at specified location
	 */
	protected void placeRandomBobble(World world, StructureBoundingBox box, Random rand, int featureX, int featureY, int featureZ) {
		int posX = this.getXWithOffset(featureX, featureZ);
		int posY = this.getYWithOffset(featureY);
		int posZ = this.getZWithOffset(featureX, featureZ);
		BlockPos pos = new BlockPos(posX, posY, posZ);

		setBlockState(world, ModBlocks.bobblehead.getStateFromMeta(rand.nextInt(16)), featureX, featureY, featureZ, box);
		TileEntityBobble bobble = (TileEntityBobble) world.getTileEntity(pos);

		if(bobble != null) {
			bobble.type = BobbleType.VALUES[rand.nextInt(BobbleType.VALUES.length - 1) + 1];
			bobble.markDirty();
		}
	}

	/** Block Placement Utility Methods **/

	/**
	 * Places blocks underneath location until reaching a solid block; good for foundations
	 */
	protected void placeFoundationUnderneath(World world, Block placeBlock, int meta, int minX, int minZ, int maxX, int maxZ, int featureY, StructureBoundingBox box) {
		IBlockState fillState = placeBlock.getStateFromMeta(meta);

		for (int featureX = minX; featureX <= maxX; featureX++) {
			for (int featureZ = minZ; featureZ <= maxZ; featureZ++) {
				int posX = this.getXWithOffset(featureX, featureZ);
				int posY = this.getYWithOffset(featureY);
				int posZ = this.getZWithOffset(featureX, featureZ);
				BlockPos pos = new BlockPos(posX, posY, posZ);

				if (box.isVecInside(pos)) {
					int brake = 0;
					IBlockState state = world.getBlockState(pos);
					Block block = state.getBlock();

					while ((world.isAirBlock(pos)
							|| !state.getMaterial().isSolid()
							|| block.isLeaves(state, world, pos)
							|| state.getMaterial() == Material.LEAVES)
							&& pos.getY() > 1 && brake <= 15) {
						world.setBlockState(pos, fillState, 2);
						pos = pos.down();
						state = world.getBlockState(pos);
						block = state.getBlock();
						brake++;
					}
				}
			}
		}
	}

	/**
	 * Places specified blocks on top of pre-existing blocks in a given area, up to a certain height. Does NOT place blocks on top of liquids.
	 * Useful for stuff like fences and walls most likely.
	 */
	protected void placeBlocksOnTop(World world, StructureBoundingBox box, Block block, int minX, int minZ, int maxX, int maxZ, int height) {

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int posX = this.getXWithOffset(x, z);
				int posZ = this.getZWithOffset(x, z);

				BlockPos top = world.getTopSolidOrLiquidBlock(new BlockPos(posX, 0, posZ));
				IBlockState topState = world.getBlockState(top);

				if (!topState.getMaterial().isLiquid()) {
					for (int i = 0; i < height; i++) {
						BlockPos placePos = top.up(i);
						world.setBlockState(placePos, block.getDefaultState(), 2);
					}
				}
			}
		}
	}

	/** getXWithOffset & getZWithOffset Methods that are actually fixed **/
	//Turns out, this entire time every single minecraft structure is mirrored instead of rotated when facing East and North
	//Also turns out, it's a scarily easy fix that they somehow didn't see *entirely*
	@Override
	public int getXWithOffset(int x, int z) {
        return switch (this.getCoordMode()) {
            case 0 -> this.boundingBox.minX + x;
            case 1 -> this.boundingBox.maxX - z;
            case 2 -> this.boundingBox.maxX - x;
            case 3 -> this.boundingBox.minX + z;
            default -> x;
        };
	}

	@Override
	public int getZWithOffset(int x, int z) {
        return switch (this.getCoordMode()) {
            case 0 -> this.boundingBox.minZ + z;
            case 1 -> this.boundingBox.minZ + x;
            case 2 -> this.boundingBox.maxZ - z;
            case 3 -> this.boundingBox.maxZ - x;
            default -> z;
        };
	}

	/** Methods that are actually optimized, including ones that cut out replaceBlock and onlyReplace functionality when it's redundant. */
	@Override
	protected void fillWithAir(@NotNull World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

		if (getYWithOffset(minY) < box.minY || getYWithOffset(maxY) > box.maxY)
			return;

		for (int x = minX; x <= maxX; x++) { //TODO these could technically be optimized a bit more. probably won't do anything but worth
			for (int z = minZ; z <= maxZ; z++) {
				int posX = getXWithOffset(x, z);
				int posZ = getZWithOffset(x, z);

				if (posX >= box.minX && posX <= box.maxX && posZ >= box.minZ && posZ <= box.maxZ) {
					for (int y = minY; y <= maxY; y++) {
						int posY = getYWithOffset(y);
						BlockPos pos = new BlockPos(posX, posY, posZ);
						world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
					}
				}
			}
		}
	}

	@Override
	protected void fillWithBlocks(@NotNull World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, @NotNull IBlockState block, @NotNull IBlockState replaceBlock, boolean onlyReplace) {

		if (getYWithOffset(minY) < box.minY || getYWithOffset(maxY) > box.maxY)
			return;

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int posX = getXWithOffset(x, z);
				int posZ = getZWithOffset(x, z);

				if (posX >= box.minX && posX <= box.maxX && posZ >= box.minZ && posZ <= box.maxZ) {
					for (int y = minY; y <= maxY; y++) {
						int posY = getYWithOffset(y);
						BlockPos pos = new BlockPos(posX, posY, posZ);

						if (!onlyReplace || !world.isAirBlock(pos)) {
							if (x != minX && x != maxX && y != minY && y != maxY && z != minZ && z != maxZ)
								world.setBlockState(pos, replaceBlock, 2);
							else
								world.setBlockState(pos, block, 2);
						}
					}
				}
			}
		}
	}

	protected void fillWithBlocks(World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block block) {

		if (getYWithOffset(minY) < box.minY || getYWithOffset(maxY) > box.maxY)
			return;

		IBlockState state = block.getDefaultState();

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int posX = getXWithOffset(x, z);
				int posZ = getZWithOffset(x, z);

				if (posX >= box.minX && posX <= box.maxX && posZ >= box.minZ && posZ <= box.maxZ) {
					for (int y = minY; y <= maxY; y++) {
						int posY = getYWithOffset(y);
						BlockPos pos = new BlockPos(posX, posY, posZ);
						world.setBlockState(pos, state, 2);
					}
				}
			}
		}
	}

	protected void fillWithMetadataBlocks(World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block block, int meta, Block replaceBlock, int replaceMeta, boolean onlyReplace) {

		if (getYWithOffset(minY) < box.minY || getYWithOffset(maxY) > box.maxY)
			return;

		IBlockState outer = block.getStateFromMeta(meta);
		IBlockState inner = replaceBlock.getStateFromMeta(replaceMeta);

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int posX = getXWithOffset(x, z);
				int posZ = getZWithOffset(x, z);

				if (posX >= box.minX && posX <= box.maxX && posZ >= box.minZ && posZ <= box.maxZ) {
					for (int y = minY; y <= maxY; y++) {
						int posY = getYWithOffset(y);
						BlockPos pos = new BlockPos(posX, posY, posZ);

						if (!onlyReplace || !world.isAirBlock(pos)) {
							if (x != minX && x != maxX && y != minY && y != maxY && z != minZ && z != maxZ)
								world.setBlockState(pos, inner, 2);
							else
								world.setBlockState(pos, outer, 2);
						}
					}
				}
			}
		}
	}

	protected void fillWithMetadataBlocks(World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block block, int meta) {

		if (getYWithOffset(minY) < box.minY || getYWithOffset(maxY) > box.maxY)
			return;

		IBlockState state = block.getStateFromMeta(meta);

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int posX = getXWithOffset(x, z);
				int posZ = getZWithOffset(x, z);

				if (posX >= box.minX && posX <= box.maxX && posZ >= box.minZ && posZ <= box.maxZ) {
					for (int y = minY; y <= maxY; y++) {
						int posY = getYWithOffset(y);
						BlockPos pos = new BlockPos(posX, posY, posZ);
						world.setBlockState(pos, state, 2);
					}
				}
			}
		}
	}

	@Override
	protected void fillWithRandomizedBlocks(@NotNull World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean onlyReplace, @NotNull Random rand, StructureComponent.@NotNull BlockSelector selector) {

		if (getYWithOffset(minY) < box.minY || getYWithOffset(maxY) > box.maxY)
			return;

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int posX = getXWithOffset(x, z);
				int posZ = getZWithOffset(x, z);

				if (posX >= box.minX && posX <= box.maxX && posZ >= box.minZ && posZ <= box.maxZ) {
					for (int y = minY; y <= maxY; y++) {
						int posY = getYWithOffset(y);
						BlockPos pos = new BlockPos(posX, posY, posZ);

						if (!onlyReplace || !world.isAirBlock(pos)) {
							boolean boundary = (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ);
							selector.selectBlocks(rand, posX, posY, posZ, boundary);
							world.setBlockState(pos, selector.getBlockState(), 2);
						}
					}
				}
			}
		}
	}

	// so i don't have to replace shit
	protected void fillWithRandomizedBlocks(World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Random rand, StructureComponent.BlockSelector selector) {

		if (getYWithOffset(minY) < box.minY || getYWithOffset(maxY) > box.maxY)
			return;

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int posX = getXWithOffset(x, z);
				int posZ = getZWithOffset(x, z);

				if (posX >= box.minX && posX <= box.maxX && posZ >= box.minZ && posZ <= box.maxZ) {
					for (int y = minY; y <= maxY; y++) {
						int posY = getYWithOffset(y);
						BlockPos pos = new BlockPos(posX, posY, posZ);

						selector.selectBlocks(rand, posX, posY, posZ, false);
						world.setBlockState(pos, selector.getBlockState(), 2);
					}
				}
			}
		}
	}

	// stairs and shit
	protected void fillWithRandomizedBlocksMeta(World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Random rand, StructureComponent.BlockSelector selector, int meta) {

		if (getYWithOffset(minY) < box.minY || getYWithOffset(maxY) > box.maxY)
			return;

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int posX = getXWithOffset(x, z);
				int posZ = getZWithOffset(x, z);

				if (posX >= box.minX && posX <= box.maxX && posZ >= box.minZ && posZ <= box.maxZ) {
					for (int y = minY; y <= maxY; y++) {
						int posY = getYWithOffset(y);
						BlockPos pos = new BlockPos(posX, posY, posZ);

						selector.selectBlocks(rand, posX, posY, posZ, false);
						IBlockState base = selector.getBlockState();
						Block b = base.getBlock();
						int combinedMeta = meta | b.getMetaFromState(base);
						IBlockState finalState = b.getStateFromMeta(combinedMeta);

						world.setBlockState(pos, finalState, 2);
					}
				}
			}
		}
	}

	@Override
	protected void generateMaybeBox(@NotNull World world, StructureBoundingBox box, @NotNull Random rand, float randLimit, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, @NotNull IBlockState block, @NotNull IBlockState replaceBlock, boolean requireNonAir, int requiredSkylight) {

		if (getYWithOffset(minY) < box.minY || getYWithOffset(maxY) > box.maxY)
			return;

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int posX = getXWithOffset(x, z);
				int posZ = getZWithOffset(x, z);

				if (posX >= box.minX && posX <= box.maxX && posZ >= box.minZ && posZ <= box.maxZ) {
					for (int y = minY; y <= maxY; y++) {
						int posY = getYWithOffset(y);
						BlockPos pos = new BlockPos(posX, posY, posZ);

						if (rand.nextFloat() <= randLimit && (!requireNonAir || !world.isAirBlock(pos))) {
							if (x != minX && x != maxX && y != minY && y != maxY && z != minZ && z != maxZ)
								world.setBlockState(pos, replaceBlock, 2);
							else
								world.setBlockState(pos, block, 2);
						}
					}
				}
			}
		}
	}

	protected void randomlyFillWithBlocks(World world, StructureBoundingBox box, Random rand, float randLimit, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block block) {

		if (getYWithOffset(minY) < box.minY || getYWithOffset(maxY) > box.maxY)
			return;

		IBlockState state = block.getDefaultState();

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int posX = getXWithOffset(x, z);
				int posZ = getZWithOffset(x, z);

				if (posX >= box.minX && posX <= box.maxX && posZ >= box.minZ && posZ <= box.maxZ) {
					for (int y = minY; y <= maxY; y++) {
						int posY = getYWithOffset(y);
						BlockPos pos = new BlockPos(posX, posY, posZ);

						if (rand.nextFloat() <= randLimit)
							world.setBlockState(pos, state, 2);
					}
				}
			}
		}
	}

	protected void randomlyFillWithBlocks(World world, StructureBoundingBox box, Random rand, float randLimit, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block block, int meta) {

		if (getYWithOffset(minY) < box.minY || getYWithOffset(maxY) > box.maxY)
			return;

		IBlockState state = block.getStateFromMeta(meta);

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int posX = getXWithOffset(x, z);
				int posZ = getZWithOffset(x, z);

				if (posX >= box.minX && posX <= box.maxX && posZ >= box.minZ && posZ <= box.maxZ) {
					for (int y = minY; y <= maxY; y++) {
						int posY = getYWithOffset(y);
						BlockPos pos = new BlockPos(posX, posY, posZ);

						if (rand.nextFloat() <= randLimit)
							world.setBlockState(pos, state, 2);
					}
				}
			}
		}
	}

	protected EnumFacing getDirection(EnumFacing dir) {
		EnumFacing mode = this.getCoordBaseMode();
		if (mode == null) return dir;

        return switch (mode) {
			case WEST -> dir.rotateY();
			case NORTH -> dir.getOpposite();
			case EAST -> dir.rotateYCCW();
			default -> dir; // SOUTH
        };
	}

	/** Sets the core block for a BlockDummyable multiblock. WARNING: Does not take {@link com.hbm.blocks.BlockDummyable#getDirModified(ForgeDirection)} or {@link com.hbm.blocks.BlockDummyable#getMetaForCore(World, int, int, int, EntityPlayer, int)}
	 * into account yet! This will be changed as it comes up!<br>
	 * For BlockDummyables, 'dir' <b>always</b> faces the player, being the opposite of the player's direction. This is already taken into account. */
	protected void placeCore(World world, StructureBoundingBox box, Block block, EnumFacing dir, int x, int y, int z) {
		int posX = getXWithOffset(x, z);
		int posZ = getZWithOffset(x, z);
		int posY = getYWithOffset(y);

		BlockPos pos = new BlockPos(posX, posY, posZ);
		if (!box.isVecInside(pos)) return;

		if (dir == null)
			dir = EnumFacing.NORTH;

		dir = getDirection(dir.getOpposite());
		world.setBlockState(pos, block.getStateFromMeta(dir.ordinal() + BlockDummyable.offset), 2);
	}

// always set the core block first
	/** StructureComponent-friendly method for {@link com.hbm.handler.MultiblockHandlerXR#fillSpace(World, int, int, int, int[], Block, EnumFacing)}. Prevents runoff outside of the provided bounding box.<br>
	 * For BlockDummyables, 'dir' <b>always</b> faces the player, being the opposite of the player's direction. This is already taken into account. */
	protected void fillSpace(World world, StructureBoundingBox box, int x, int y, int z, int[] dim, Block block, EnumFacing dir) {

		if (getYWithOffset(y - dim[1]) < box.minY || getYWithOffset(y + dim[0]) > box.maxY)
			return;

		if (dir == null)
			dir = EnumFacing.NORTH;

		dir = getDirection(dir.getOpposite());

		int count = 0;

		int[] rot = MultiblockHandlerXR.rotate(dim, dir);

		int posX = getXWithOffset(x, z);
		int posZ = getZWithOffset(x, z);
		int posY = getYWithOffset(y);

		BlockDummyable.safeRem = true;

		for (int a = posX - rot[4]; a <= posX + rot[5]; a++) {
			for (int c = posZ - rot[2]; c <= posZ + rot[3]; c++) {

				if (a >= box.minX && a <= box.maxX && c >= box.minZ && c <= box.maxZ) {
					for (int b = posY - rot[1]; b <= posY + rot[0]; b++) {

						int meta = 0;

						if (b > posY) {
							meta = EnumFacing.UP.ordinal();
						} else if (a < posX) {
							meta = EnumFacing.WEST.ordinal();
						} else if (a > posX) {
							meta = EnumFacing.EAST.ordinal();
						} else if (c < posZ) {
							meta = EnumFacing.NORTH.ordinal();
						} else if (c > posZ) {
							meta = EnumFacing.SOUTH.ordinal();
						} else {
							continue;
						}

						BlockPos p = new BlockPos(a, b, c);
						world.setBlockState(p, block.getStateFromMeta(meta), 2);

						count++;

						if (count > 2000) {
							System.out.println("component's fillspace: ded " + a + " " + b + " " + c + " " + x + " " + y + " " + z);

							BlockDummyable.safeRem = false;
							return;
						}
					}
				}
			}
		}

		BlockDummyable.safeRem = false;
	}

	/** StructureComponent-friendly method for {@link com.hbm.blocks.BlockDummyable#makeExtra(World, int, int, int)}. Prevents runoff outside of the provided bounding box. */
	public void makeExtra(World world, StructureBoundingBox box, Block block, int x, int y, int z) {
		int posX = getXWithOffset(x, z);
		int posZ = getZWithOffset(x, z);
		int posY = getYWithOffset(y);

		BlockPos pos = new BlockPos(posX, posY, posZ);

		if (!box.isVecInside(pos))
			return;

		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != block)
			return;

		int meta = block.getMetaFromState(state);

		if (meta > 5)
			return;

		BlockDummyable.safeRem = true;
		world.setBlockState(pos, block.getStateFromMeta(meta + BlockDummyable.extra), 3);
		BlockDummyable.safeRem = false;
	}

	protected boolean generateStructureChestContents(World world, StructureBoundingBox box, Random rand, int x, int y, int z, WeightedRandomChestContentFrom1710[] chestContent, int p_74879_8_)
	{
		int i1 = this.getXWithOffset(x, z);
		int j1 = this.getYWithOffset(y);
		int k1 = this.getZWithOffset(x, z);
		BlockPos pos = new BlockPos(i1, j1, k1);
		if (box.isVecInside(pos) && world.getBlockState(pos).getBlock() != Blocks.CHEST)
		{
			world.setBlockState(pos, Blocks.CHEST.getDefaultState(), 2);
			TileEntityChest tileentitychest = (TileEntityChest)world.getTileEntity(pos);

			if (tileentitychest != null)
			{
				WeightedRandomChestContentFrom1710.generateChestContents(rand, chestContent, tileentitychest, p_74879_8_);
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	/** Block Selectors **/

	static class Sandstone extends StructureComponent.BlockSelector {

		Sandstone() { }

		/** Selects blocks */
		@Override
		public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
			float chance = rand.nextFloat();

			if(chance > 0.6F) {
				this.blockstate = Blocks.SANDSTONE.getDefaultState();
			} else if (chance < 0.5F ) {
				this.blockstate = ModBlocks.reinforced_sand.getDefaultState();
			} else {
				this.blockstate = Blocks.SAND.getDefaultState();
			}
		}
	}

	static class ConcreteBricks extends StructureComponent.BlockSelector {

		ConcreteBricks() { }

		/** Selects blocks */
		@Override
		public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
			float chance = rand.nextFloat();

			if(chance < 0.4F) {
				this.blockstate = ModBlocks.brick_concrete.getDefaultState();
			} else if (chance < 0.7F) {
				this.blockstate = ModBlocks.brick_concrete_mossy.getDefaultState();
			} else if (chance < 0.9F) {
				this.blockstate = ModBlocks.brick_concrete_cracked.getDefaultState();
			} else {
				this.blockstate = ModBlocks.brick_concrete_broken.getDefaultState();
			}
		}
	}

	static class ConcreteBricksStairs extends StructureComponent.BlockSelector {

		ConcreteBricksStairs() {
			this.blockstate = ModBlocks.brick_concrete_stairs.getDefaultState();
		}

		@Override
		public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
			float chance = rand.nextFloat();

			if (chance < 0.4F) {
				this.blockstate = ModBlocks.brick_concrete_stairs.getDefaultState();
			} else if (chance < 0.7F) {
				this.blockstate = ModBlocks.brick_concrete_mossy_stairs.getDefaultState();
			} else if (chance < 0.9F) {
				this.blockstate = ModBlocks.brick_concrete_cracked_stairs.getDefaultState();
			} else {
				this.blockstate = ModBlocks.brick_concrete_broken_stairs.getDefaultState();
			}
		}
	}

	static class ConcreteBricksSlabs extends StructureComponent.BlockSelector {

		ConcreteBricksSlabs() {
			this.blockstate = ModBlocks.brick_concrete_slab.getStateFromMeta(0);
		}

		@Override
		public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
			float chance = rand.nextFloat();
			int meta = 0;

			if (chance >= 0.4F && chance < 0.7F) {
				meta = 1;
			} else if (chance < 0.9F) {
				meta = 2;
			} else {
				meta = 3;
			}

			this.blockstate = ModBlocks.brick_concrete_slab.getStateFromMeta(meta);
		}
	}

	//ag
	static class LabTiles extends StructureComponent.BlockSelector {

		LabTiles() { }

		/** Selects blocks */
		@Override
		public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
			float chance = rand.nextFloat();

			if(chance < 0.5F) {
				this.blockstate = ModBlocks.tile_lab.getDefaultState();
			} else if (chance < 0.9F) {
				this.blockstate = ModBlocks.tile_lab_cracked.getDefaultState();
			} else {
				this.blockstate = ModBlocks.tile_lab_broken.getDefaultState();
			}
		}
	}

	static class SuperConcrete extends StructureComponent.BlockSelector {

		SuperConcrete() {
			this.blockstate = ModBlocks.concrete_super.getDefaultState();
		}

		@Override
		public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
			this.blockstate = ModBlocks.concrete_super.getStateFromMeta(rand.nextInt(6) + 10);
		}
	}

	public static class MeteorBricks extends BlockSelector {

		@Override
		public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
			float chance = rand.nextFloat();

			if(chance < 0.4F) {
				this.blockstate = ModBlocks.meteor_brick.getDefaultState();
			} else if (chance < 0.7F) {
				this.blockstate = ModBlocks.meteor_brick_mossy.getDefaultState();
			} else {
				this.blockstate = ModBlocks.meteor_brick_cracked.getDefaultState();
			}
		}

	}

	public static class SupplyCrates extends BlockSelector {

		@Override
		public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
			float chance = rand.nextFloat();

			if(chance < 0.6F) {
				this.blockstate = Blocks.AIR.getDefaultState();
			} else if(chance < 0.8F) {
				this.blockstate = ModBlocks.crate_ammo.getDefaultState();
			} else if(chance < 0.9F) {
				this.blockstate = ModBlocks.crate_can.getDefaultState();
			} else {
				this.blockstate = ModBlocks.crate.getDefaultState();
			}
		}

	}

	public static class CrabSpawners extends BlockSelector {

		@Override
		public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
			float chance = rand.nextFloat();

			if(chance < 0.8F) {
				this.blockstate = ModBlocks.meteor_brick.getDefaultState();
			} else {
				this.blockstate = ModBlocks.meteor_spawner.getDefaultState();
			}
		}

	}

	public static class GreenOoze extends BlockSelector {

		@Override
		public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
			float chance = rand.nextFloat();

			if(chance < 0.8F) {
				this.blockstate = ModBlocks.toxic_block.getDefaultState();
			} else {
				this.blockstate = ModBlocks.meteor_polished.getDefaultState();
			}
		}

	}

}
