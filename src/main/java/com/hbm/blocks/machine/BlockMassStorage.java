package com.hbm.blocks.machine;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemLock;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

//import static com.hbm.handler.BulletConfigSyncingUtil.i;

// окей, теперь заметка для металлолома, вроде как №3. по-хорошему я бы это въебал через BlockEnumMulti потому что метаданные тут сами по себе
// ну нихуя не работают, но мне лень его писать, так что пока так поживём
public class BlockMassStorage extends BlockContainer implements ILookOverlay, ITooltipProvider {


	public BlockMassStorage(Material material, String s) {
		super(material);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setSoundType(SoundType.METAL);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMassStorage(getCapacity());
	}


	public int getCapacity() {
		return this == ModBlocks.mass_storage_wood ? 1000 : this == ModBlocks.mass_storage_iron ? 10_000 : this == ModBlocks.mass_storage_desh ? 100_000 : this == ModBlocks.mass_storage ? 1_000_000 : 0;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.getHeldItemMainhand().isEmpty() && (player.getHeldItemMainhand().getItem() instanceof ItemLock || player.getHeldItemMainhand().getItem() == ModItems.key_kit)) {
			return false;
		} else if(!player.isSneaking()) {
			TileEntity entity = world.getTileEntity(pos);
			if(entity instanceof TileEntityMassStorage && ((TileEntityMassStorage) entity).canAccess(player)) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		} else {
			return false;
		}
	}

	private static boolean dropInv = true;

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {

		if (!player.capabilities.isCreativeMode && !world.isRemote && willHarvest) {

			ItemStack drop = new ItemStack(this);
			TileEntity te = world.getTileEntity(pos);

			NBTTagCompound nbt = new NBTTagCompound();

			if(te != null) {
				IItemHandler inventory = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

				for(int i = 0; i < inventory.getSlots(); i++) {

					ItemStack stack = inventory.getStackInSlot(i);
					if(stack.isEmpty())
						continue;

					NBTTagCompound slot = new NBTTagCompound();
					stack.writeToNBT(slot);
					nbt.setTag("slot" + i, slot);
				}
			}

			if(te instanceof TileEntityLockableBase) {
				TileEntityLockableBase lockable = (TileEntityLockableBase) te;

				if(lockable.isLocked()) {
					nbt.setInteger("lock", lockable.getPins());
					nbt.setDouble("lockMod", lockable.getMod());
				}
			}

			if(te instanceof TileEntityMassStorage && nbt.getKeySet().size() > 0) {
				TileEntityMassStorage storage = (TileEntityMassStorage) te;
				nbt.setInteger("stack", storage.getStockpile());
			}

			if(!nbt.isEmpty()) {
				drop.setTagCompound(nbt);
			}

			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), drop);
		}

		dropInv = false;
		boolean flag = world.setBlockToAir(pos);
		dropInv = true;

		return flag;
	}


	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

		TileEntity te = world.getTileEntity(pos);

		if (te != null && stack.hasTagCompound()) {
			IItemHandler inventory = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

			NBTTagCompound nbt = stack.getTagCompound();
			for (int i = 0; i < inventory.getSlots(); i++) {
				inventory.insertItem(i, new ItemStack(nbt.getCompoundTag("slot" + i)), false);
			}

			if (te instanceof TileEntityMassStorage) {
				TileEntityMassStorage lockable = (TileEntityMassStorage) te;

				if (nbt.hasKey("lock")) {
					lockable.setPins(nbt.getInteger("lock"));
					lockable.setMod(nbt.getDouble("lockMod"));
					lockable.lock();
				}

				lockable.setStockpile(nbt.getInteger("stack"));
			}
		}

		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}


	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		if (this.dropInv) {
			InventoryHelper.dropInventoryItems(worldIn, pos, worldIn.getTileEntity(pos));
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ItemStack.EMPTY.getItem();
	}

	@Override
	public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

		if (!(te instanceof TileEntityMassStorage))
			return;

		TileEntityMassStorage storage = (TileEntityMassStorage) te;

		List<String> text = new ArrayList();
		String title = "Empty";
		boolean full = !storage.type.isEmpty() && storage.type != null && storage.type.getItem() != Items.AIR;

		if (full) {

			title = storage.type.getDisplayName();
			text.add(String.format(Locale.US, "%,d", storage.getStockpile()) + " / " + String.format(Locale.US, "%,d", storage.getCapacity()));

			double percent = (double) storage.getStockpile() / (double) storage.getCapacity();
			int charge = (int) Math.floor(percent * 10_000D);
			int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int) (0xFF * percent) << 8);

			text.add("&[" + color + "&]" + (charge / 100D) + "%");
		}

		ILookOverlay.printGeneric(event, title, full ? 0xffff00 : 0x00ffff, full ? 0x404000 : 0x004040, text);
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {

		if (!stack.hasTagCompound()) return;

		ItemStack type = new ItemStack(stack.getTagCompound().getCompoundTag("slot1"));

		if (!type.isEmpty()) {
			tooltip.add("§6" + type.getDisplayName());
			tooltip.add(String.format(Locale.US, "%,d", stack.getTagCompound().getInteger("stack")) + " / " + String.format(Locale.US, "%,d", getCapacity()));
		}
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
}
/*
	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
		return ((TileEntityMassStorage) world.getTileEntity(x, y, z)).redstone;
	}
}

 */