package com.hbm.blocks.machine;

import com.hbm.blocks.ICustomBlockItem;
import com.hbm.config.CustomMachineConfigJSON;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration;
import com.hbm.items.ModItems;
import com.hbm.items.block.ItemCustomMachine;
import com.hbm.main.MainRegistry;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.machine.TileEntityCustomMachine;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlockCustomMachine extends BlockContainerBakeable implements ICustomBlockItem {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockCustomMachine(String registryName) {
		super(Material.IRON, registryName, BlockBakeFrame.southFacingCube("cm_terminal_side", "cm_terminal_front"));
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityCustomMachine();
	}

	@Override
	protected @NotNull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public @NotNull IBlockState getStateForPlacement(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {

			TileEntity te = world.getTileEntity(pos);

			if(te instanceof TileEntityCustomMachine tile) {

				if(tile.checkStructure()) {
					FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				} else if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == ModItems.wand_s) {
					tile.buildStructure();
				}
			}
			return true;
		}

		return false;
	}

	@Override
	public void onBlockPlacedBy(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase placer, ItemStack stack) {

		TileEntity te = world.getTileEntity(pos);

		if(te instanceof TileEntityCustomMachine tile) {
			int id = stack.getItemDamage() - 100;

			if(id >= 0 && id < CustomMachineConfigJSON.niceList.size()) {

				MachineConfiguration config = CustomMachineConfigJSON.niceList.get(id);

				if(config != null) {
					tile.machineType = config.unlocalizedName;
					tile.init();
					tile.markChanged();
				}
			}
		}
	}

	@Override
	public void onBlockHarvested(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player) {
		if(player.capabilities.isCreativeMode && world.getTileEntity(pos) instanceof TileEntityCustomMachine tile) {
			tile.setDestroyedByCreativePlayer();
		}
	}

	@Override
	public void breakBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {

		TileEntity te = world.getTileEntity(pos);

		if(te instanceof TileEntityCustomMachine tile) {

			for(int i = 0; i < tile.inventory.getSlots(); i++) {

				if(i >= 10 && i <= 15)
					continue; // do NOT drop the filters

				ItemStack stack = tile.inventory.getStackInSlot(i);
				if(!stack.isEmpty()) {
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}

			if(!tile.isDestroyedByCreativePlayer() && tile.config != null) {
				ItemStack drop = new ItemStack(this, 1, CustomMachineConfigJSON.niceList.indexOf(tile.config) + 100);
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), drop);
			}

			world.updateComparatorOutputLevel(pos, this);
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	public void dropBlockAsItemWithChance(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, float chance, int fortune) {
	}

	@Override
	public @NotNull ItemStack getPickBlock(@NotNull IBlockState state, RayTraceResult target, @NotNull World world, @NotNull BlockPos pos, EntityPlayer player) {

		TileEntity te = world.getTileEntity(pos);

		if(te instanceof TileEntityCustomMachine tile && tile.machineType != null && !tile.machineType.isEmpty()) {
			return new ItemStack(this, 1, CustomMachineConfigJSON.niceList.indexOf(tile.config) + 100);
		}

		return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public void registerItem() {
		ItemBlock itemBlock = new ItemCustomMachine(this);
		itemBlock.setRegistryName(Objects.requireNonNull(this.getRegistryName()));
		itemBlock.setCreativeTab(this.getCreativeTab());
		ForgeRegistries.ITEMS.register(itemBlock);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		Item item = Item.getItemFromBlock(this);
		ModelResourceLocation inventory = new ModelResourceLocation(Objects.requireNonNull(this.getRegistryName()), "inventory");
		ModelLoader.setCustomMeshDefinition(item, stack -> inventory);
	}
}
