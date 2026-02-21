package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.ICustomSelectionBox;
import com.hbm.inventory.control_panel.Control;
import com.hbm.inventory.control_panel.ControlEvent;
import com.hbm.inventory.control_panel.ControlPanel;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.items.ModItems;
import com.hbm.main.ClientProxy;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityControlPanel;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockControlPanel extends BlockContainer implements ICustomSelectionBox {

	public static final PropertyBool UP = PropertyBool.create("up");
	public static final PropertyBool DOWN = PropertyBool.create("down");
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockControlPanel(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntityControlPanel te = new TileEntityControlPanel();
		te.panel = new ControlPanel(te, 0.25F, (float) Math.toRadians(20), 0, 0, 0.25F, 0);
		return te;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!worldIn.isRemote){
			// without this attempting to press buttons will open the damned GUI
			if(playerIn.getHeldItem(hand).getItem() == ModItems.screwdriver || playerIn.getHeldItem(hand).getItem() == ModItems.screwdriver_desh)
				FMLNetworkHandler.openGui(playerIn, MainRegistry.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
		} else {
			TileEntityControlPanel control = (TileEntityControlPanel)worldIn.getTileEntity(pos);
			Control ctrl = control.panel.getSelectedControl(playerIn.getPositionEyes(1), playerIn.getLook(1));
			if(ctrl != null){
				ControlEvent evt = ControlEvent.newEvent("ctrl_press");
				evt.setVar("isSneaking", new DataValueFloat(playerIn.isSneaking()));
				NBTTagCompound dat = evt.writeToNBT(new NBTTagCompound());
				dat.setInteger("click_control", ctrl.panel.controls.indexOf(ctrl));
                PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(dat, pos));
                return true;
			}
		}
		return true;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileEntity te = source.getTileEntity(pos);
		if (te instanceof TileEntityControlPanel) {
			AxisAlignedBB ret = ((TileEntityControlPanel) te).getBoundingBox(state.getValue(UP), state.getValue(DOWN), state.getValue(FACING));
			if (ret != null) {
				return ret;
			}
		}
		return super.getBoundingBox(state, source, pos);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face){
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand){
		return this.getDefaultState()
				.withProperty(FACING, placer.getHorizontalFacing().getOpposite())
				.withProperty(UP, facing.getIndex() == 1)
				.withProperty(DOWN, facing.getIndex() == 0);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.AIR;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderBox(World world, EntityPlayer player, IBlockState state, BlockPos pos, double x, double y, double z, float partialTicks){
		TileEntityControlPanel control = (TileEntityControlPanel)world.getTileEntity(pos);
		Control ctrl = control.panel.getSelectedControl(player.getPositionEyes(partialTicks), player.getLook(partialTicks));
		//if(control.panel.controls.size() > 0)
		//	ctrl = control.panel.controls.get(0);
		if(ctrl != null){
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			control.panel.transform.store(ClientProxy.AUX_GL_BUFFER);
			ClientProxy.AUX_GL_BUFFER.rewind();
			GL11.glMultMatrix(ClientProxy.AUX_GL_BUFFER);
			if (ctrl.getBoundingBox() != null)
				// offset to bury bottom lines
				RenderGlobal.drawSelectionBoundingBox(ctrl.getBoundingBox().offset(0, -.01F, 0), 0, 0, 0, 0.4F);
			GlStateManager.popMatrix();
			return true;
		}
		return false;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, UP, DOWN, FACING);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int up = state.getValue(UP) ? 1 : 0;
		int down = state.getValue(DOWN) ? 1 : 0;
		int facing = state.getValue(FACING).getIndex();
		return (up << 3) | (down << 2) | (facing - 2);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState()
				.withProperty(UP, ((meta >> 3) & 1) > 0)
				.withProperty(DOWN, ((meta >> 2) & 1) > 0)
				.withProperty(FACING, EnumFacing.byIndex((meta & 3) + 2));
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
	{
	   return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
	}

}
