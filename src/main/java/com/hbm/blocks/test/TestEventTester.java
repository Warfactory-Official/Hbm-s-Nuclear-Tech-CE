package com.hbm.blocks.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerTestStorage;
import com.hbm.inventory.gui.GUITestStorage;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class TestEventTester extends BlockContainer {

	public static final int SLOT_CLICK_ID_REFRESH = -666;

	public TestEventTester(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityTestStorage();
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@AutoRegister(name = "tilentity_test_storage")
	public static class TileEntityTestStorage extends TileEntityMachineBase implements IGUIProvider {

		public TileEntityTestStorage() {
			super(8 * 50);
		}

		@Override public String getDefaultName() { return "Balls"; }

		@Override
		public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
			return new ContainerTestStorage(player.inventory, this);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
			return new GUITestStorage(player.inventory, this);
		}
	}
}
