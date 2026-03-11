package com.hbm.blocks.machine.rbmk;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemRBMKLid;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKBase;
import com.hbm.render.model.RBMKColumnBakedModel;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public abstract class RBMKBase extends BlockDummyable implements IToolable, ILookOverlay, IDynamicModels {

	public static boolean dropLids = true;
	public static boolean digamma = false;

	public static final int LID_NONE = 0;
	public static final int LID_STANDARD = 1;
	public static final int LID_GLASS = 2;
	public static int renderLid = LID_NONE;
	public static boolean overrideOnlyRenderSides = false;

	@SideOnly(Side.CLIENT) protected TextureAtlasSprite topSprite;
	@SideOnly(Side.CLIENT) protected TextureAtlasSprite sideSprite;
	@SideOnly(Side.CLIENT) protected TextureAtlasSprite coverTopSprite;
	@SideOnly(Side.CLIENT) protected TextureAtlasSprite coverSideSprite;
	@SideOnly(Side.CLIENT) protected TextureAtlasSprite glassTopSprite;
	@SideOnly(Side.CLIENT) protected TextureAtlasSprite glassSideSprite;
	public String columnTexture;

	protected RBMKBase(String s, String c) {
		super(Material.IRON, s);
		columnTexture = c;
		this.setHardness(3F);
		this.setResistance(30F);
	}

	@Override public boolean isOpaqueCube(@NotNull IBlockState state) { return true; }
	@Override public boolean isFullCube(@NotNull IBlockState state) { return true; }

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public @NotNull BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos, @NotNull EnumFacing side) {
		if(overrideOnlyRenderSides && side.getAxis().isVertical()) return false;
		if(renderLid != LID_NONE && side != EnumFacing.DOWN && side != EnumFacing.UP) return true;
		return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
	}

	public boolean hasOwnLid() {
		return this == ModBlocks.rbmk_control || this == ModBlocks.rbmk_control_auto || this == ModBlocks.rbmk_control_mod ||
				this == ModBlocks.rbmk_control_reasim || this == ModBlocks.rbmk_control_reasim_auto;
	}

	@Override
	public void onBlockPlacedBy(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

		if (!worldIn.isRemote) {
			BlockPos core = this.findCore(worldIn, pos);
			if (core != null) {
				worldIn.setBlockState(core, this.getStateFromMeta(DIR_NO_LID.getIndex() + offset), 3); // idk how but otherwise it spawns a fucking lid out of nowhere
			}
		}
	}


	@Override
	public int[] getDimensions() {
		return new int[] {3, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	public boolean openInv(World world, int x, int y, int z, EntityPlayer player, EnumHand hand) {
		if(world.isRemote) return true;

		BlockPos core = this.findCore(world, new BlockPos(x,y,z));
		if(core == null) return false;

		TileEntity te = world.getTileEntity(core);
		if(!(te instanceof TileEntityRBMKBase rbmk)) return false;

		if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof ItemRBMKLid) {
			if(!rbmk.hasLid()) return false;
		}

		if(!player.isSneaking())
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, core.getX(), core.getY(), core.getZ());

		return true;
	}

	@Override
	public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
		float height = 0.0F;
		BlockPos core = this.findCore(source, pos);

		if(core != null) {
			TileEntity te = source.getTileEntity(core);
			if(te instanceof TileEntityRBMKBase rbmk) {
                if(rbmk.hasLid()) {
					height += 0.25F;
				}
			}
		}
		return new AxisAlignedBB(0, 0, 0, 1, 1 + height, 1);
	}

	public static final EnumFacing DIR_NO_LID = EnumFacing.NORTH;
	public static final EnumFacing DIR_NORMAL_LID = EnumFacing.EAST;
	public static final EnumFacing DIR_GLASS_LID = EnumFacing.SOUTH;

	public static int metaToLid(int meta) {
		if(meta - offset == DIR_NORMAL_LID.getIndex()) return LID_STANDARD;
		if(meta - offset == DIR_GLASS_LID.getIndex()) return LID_GLASS;
		return LID_NONE;
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o){
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(world), this, dir);
		this.makeExtra(world, x, y + RBMKDials.getColumnHeight(world), z);
	}

	@Override
	protected EnumFacing getDirModified(EnumFacing dir) {
		return DIR_NO_LID;
	}

	public int[] getDimensions(World world) {
		return new int[] {RBMKDials.getColumnHeight(world), 0, 0, 0, 0, 0};
	}

	@Override
	public void breakBlock(World world, @NotNull BlockPos pos, IBlockState state) {
		if(!world.isRemote && dropLids) {
			int meta = state.getBlock().getMetaFromState(state);
			BlockPos spawnPos = pos.up(RBMKDials.getColumnHeight(world));

			if(meta == DIR_NORMAL_LID.getIndex() + offset) {
				world.spawnEntity(new EntityItem(world, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, new ItemStack(ModItems.rbmk_lid)));
			}
			if(meta == DIR_GLASS_LID.getIndex() + offset) {
				world.spawnEntity(new EntityItem(world, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, new ItemStack(ModItems.rbmk_lid_glass)));
			}
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
		TileEntityRBMKBase.diagnosticPrintHook(event);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool){
		if(tool != ToolType.SCREWDRIVER) return false;
		int[] pos = this.findCore(world, x, y, z);
		
		if(pos != null) {
			TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));

			if(te instanceof TileEntityRBMKBase rbmk) {

				int i = rbmk.getBlockMetadata();
				int lidType = metaToLid(i);

				if(rbmk.hasLid() && rbmk.isLidRemovable()) {

					if(!world.isRemote) {
						if(lidType == LID_STANDARD) {
							world.spawnEntity(new EntityItem(world, pos[0] + 0.5, pos[1] + 0.5 + RBMKDials.getColumnHeight(world), pos[2] + 0.5, new ItemStack(ModItems.rbmk_lid)));
						}
						if(lidType == LID_GLASS) {
							world.spawnEntity(new EntityItem(world, pos[0] + 0.5, pos[1] + 0.5 + RBMKDials.getColumnHeight(world), pos[2] + 0.5, new ItemStack(ModItems.rbmk_lid_glass)));
						}
						
						world.setBlockState(new BlockPos(pos[0], pos[1], pos[2]), this.getDefaultState().withProperty(META, DIR_NO_LID.ordinal() + BlockDummyable.offset), 3);
						NBTTagCompound nbt = rbmk.writeToNBT(new NBTTagCompound());
						world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2])).readFromNBT(nbt);
					}
					
					return true;

				}
			}
		}
		return false;
	}


	@SideOnly(Side.CLIENT)
	@Override
	public void registerSprite(TextureMap map) {
		this.sideSprite = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/" + columnTexture + "_side"));
		this.topSprite = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/" + columnTexture + "_top"));

		if (!hasOwnLid()) {
			this.coverTopSprite = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/" + columnTexture + "_cover_top"));
			this.coverSideSprite = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/" + columnTexture + "_cover_side"));
			this.glassTopSprite = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/" + columnTexture + "_glass_top"));
			this.glassSideSprite = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/" + columnTexture + "_glass_side"));
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void bakeModel(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "inventory"),
				new RBMKColumnBakedModel(topSprite, sideSprite, coverTopSprite, coverSideSprite, glassTopSprite, glassSideSprite, true));

		event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "normal"),
				new RBMKColumnBakedModel(topSprite, sideSprite, coverTopSprite, coverSideSprite, glassTopSprite, glassSideSprite, false));
	}
}
