package com.hbm.blocks.network.energy;

import com.hbm.api.energymk2.PowerNetMK2;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.energy.TileEntityCableBaseNT;
import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class BlockCableGauge extends BlockContainer implements ILookOverlay, ITooltipProvider {
	
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
	public BlockCableGauge(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.blockTab);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(FACING).getIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot){
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn){
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCableGauge();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        this.addStandardInfo(tooltip);
        super.addInformation(stack, player, tooltip, advanced);
    }

    @SideOnly(Side.CLIENT)
	public void printHook(Pre event, World world, BlockPos pos){
		TileEntity te = world.getTileEntity(pos);
		
		if(!(te instanceof TileEntityCableGauge diode))
			return;

        List<String> text = new ArrayList<>();
		text.add(Library.getShortNumber(diode.deltaLastSecond) + "HE/s");
		
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
	@AutoRegister
	public static class TileEntityCableGauge extends TileEntityCableBaseNT implements SimpleComponent {

		private long deltaTick = 10;
		private long deltaSecond = 0;
		public long deltaLastSecond = 0;
		
		@Override
		public void update() {
			super.update();

			if(!world.isRemote) {

				if(this.node != null && this.node.net != null) {

					PowerNetMK2 net = this.node.net;

					this.deltaTick = net.energyTracker;
					if(world.getTotalWorldTime() % 20 == 0) {
						this.deltaLastSecond = this.deltaSecond;
						this.deltaSecond = 0;
					}
					this.deltaSecond += deltaTick;
				}

				networkPackNT(25);
			}
		}

		@Override
		public void serialize(ByteBuf buf) {
			buf.writeLong(deltaTick);
			buf.writeLong(deltaLastSecond);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			this.deltaTick = Math.max(buf.readLong(), 0);
			this.deltaLastSecond = Math.max(buf.readLong(), 0);
		}
	
		@Override
		@Optional.Method(modid = "opencomputers")
		public String getComponentName() {
			return "power_gauge";
		}

		@Callback(doc = "getPowerPerS(); returns the power(long) per s traveling through the gauge.")
		@Optional.Method(modid = "opencomputers")
		public Object[] getPowerPerS(Context context, Arguments args) {
			return new Object[] {deltaLastSecond};
		}
	}
}
