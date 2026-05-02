package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.container.ContainerAnvil;
import com.hbm.inventory.gui.GUIAnvil;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NTMAnvil extends BlockFalling implements IGUIProvider {
	
	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public static final int TIER_IRON = 1;
	public static final int TIER_STEEL = 2;
	public static final int TIER_OIL = 3;
	public static final int TIER_NUCLEAR = 4;
	public static final int TIER_RBMK = 5;
	public static final int TIER_FUSION = 6;
	public static final int TIER_PARTICLE = 7;
	public static final int TIER_GERALD = 8;
	public final int tier;

	public static final HashMap<Integer, List<NTMAnvil>> tierMap = new HashMap<>();

	public NTMAnvil(Material mat, int tier, String s) {
		super(mat);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setSoundType(SoundType.ANVIL);
		this.setHardness(5.0F);
		this.setResistance(100.0F);
		this.tier = tier;

		List<NTMAnvil> anvils = tierMap.get(tier);
		if(anvils == null)
			anvils = new ArrayList<>();
		anvils.add(this);
		tierMap.put(tier, anvils);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	public static List<ItemStack> getAnvilsFromTier(int tier) {
		List<NTMAnvil> anvils = tierMap.get(tier);

		if(anvils != null) {
			List<ItemStack> stacks = new ArrayList<>();

			for(NTMAnvil anvil : anvils)
				stacks.add(new ItemStack(anvil));

			return stacks;
		}

		return new ArrayList<>();
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}
	
	@Override
	public boolean isBlockNormalCube(IBlockState state){
		return false;
	}
	
	@Override
	public boolean isNormalCube(IBlockState state){
		return false;
	}
	
	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos){
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state){
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		
		return false;
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		AxisAlignedBB bb = NULL_AABB;
		EnumFacing.Axis axis = state.getValue(FACING).getAxis();
		if(axis == EnumFacing.Axis.X){
			bb = new AxisAlignedBB(0.25F, 0.0F, 0.0F, 0.75F, 0.75F, 1.0F);
		} else if(axis == EnumFacing.Axis.Z){
			bb = new AxisAlignedBB(0.0F, 0.0F, 0.25F, 1.0F, 0.75F, 0.75F);
		}
		return bb;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing) state.getValue(FACING)).getIndex();
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
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add("§6Tier: "+this.tier);
		super.addInformation(stack, player, tooltip, advanced);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerAnvil(player.inventory, tier);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIAnvil(player, tier);
	}
}
