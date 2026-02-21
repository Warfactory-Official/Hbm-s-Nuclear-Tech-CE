package com.hbm.blocks.network.energy;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.items.IDynamicModels;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.render.model.BlockCableBakedModel;
import com.hbm.tileentity.network.energy.TileEntityCableBaseNT;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class BlockCable extends BlockContainer implements IDynamicModels {
	// Th3_Sl1ze: believe me, this shit will inevitably cause stackoverflowexception if you're going to load a shitton of cables in a single place
	// though, it still works and it doesn't crash
	public static final PropertyBool POS_X = PropertyBool.create("posx");
	public static final PropertyBool NEG_X = PropertyBool.create("negx");
	public static final PropertyBool POS_Y = PropertyBool.create("posy");
	public static final PropertyBool NEG_Y = PropertyBool.create("negy");
	public static final PropertyBool POS_Z = PropertyBool.create("posz");
	public static final PropertyBool NEG_Z = PropertyBool.create("negz");

	private final ResourceLocation objModelLocation = new ResourceLocation(Tags.MODID, "models/blocks/cable_neo.obj");
	private final ResourceLocation textureLocation = new ResourceLocation(Tags.MODID, "blocks/cable_neo");

	public BlockCable(Material material, String registryName) {
		super(material);
		this.setRegistryName(registryName);
		this.setTranslationKey(registryName);
		IBlockState base = this.blockState.getBaseState()
				.withProperty(POS_X, Boolean.FALSE)
				.withProperty(NEG_X, Boolean.FALSE)
				.withProperty(POS_Y, Boolean.FALSE)
				.withProperty(NEG_Y, Boolean.FALSE)
				.withProperty(POS_Z, Boolean.FALSE)
				.withProperty(NEG_Z, Boolean.FALSE);
		this.setDefaultState(base);
		this.fullBlock = false;
		this.lightOpacity = 0;
		this.translucent = true;
		IDynamicModels.INSTANCES.add(this);
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityCableBaseNT();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, POS_X, NEG_X, POS_Y, NEG_Y, POS_Z, NEG_Z);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	/**
	 * Checks if it can connect to a HE or FE neighbor.
	 */
	private boolean canConnectToNeighbor(IBlockAccess world, BlockPos pos, ForgeDirection dir) {
		if (Library.canConnect(world, pos, dir)) {
			return true;
		}

		TileEntity neighbor = world.getTileEntity(pos);
		if (neighbor != null && !neighbor.isInvalid()) {
			EnumFacing facing = dir.getOpposite().toEnumFacing();
			if (neighbor.hasCapability(CapabilityEnergy.ENERGY, facing)) {
				IEnergyStorage storage = neighbor.getCapability(CapabilityEnergy.ENERGY, facing);
				return storage != null && (storage.canReceive() || storage.canExtract());
			}
		}

		return false;
	}

	private boolean canConnect(IBlockAccess world, BlockPos pos, EnumFacing dir) {
		return switch (dir) {
			case EAST -> canConnectToNeighbor(world, pos, Library.POS_X);
			case WEST -> canConnectToNeighbor(world, pos, Library.NEG_X);
			case UP -> canConnectToNeighbor(world, pos, Library.POS_Y);
			case DOWN -> canConnectToNeighbor(world, pos, Library.NEG_Y);
			case SOUTH -> canConnectToNeighbor(world, pos, Library.POS_Z);
			case NORTH -> canConnectToNeighbor(world, pos, Library.NEG_Z);
		};
	}

	private boolean getConnectAt(IBlockAccess world, BlockPos pos, EnumFacing dir) {
		BlockPos adj = pos.offset(dir);
		return canConnect(world, adj, dir);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		boolean pX = getConnectAt(world, pos, EnumFacing.EAST);
		boolean nX = getConnectAt(world, pos, EnumFacing.WEST);
		boolean pY = getConnectAt(world, pos, EnumFacing.UP);
		boolean nY = getConnectAt(world, pos, EnumFacing.DOWN);
		boolean pZ = getConnectAt(world, pos, EnumFacing.SOUTH);
		boolean nZ = getConnectAt(world, pos, EnumFacing.NORTH);
		return state.withProperty(POS_X, pX)
				.withProperty(NEG_X, nX)
				.withProperty(POS_Y, pY)
				.withProperty(NEG_Y, nY)
				.withProperty(POS_Z, pZ)
				.withProperty(NEG_Z, nZ);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		state = getActualState(state, source, pos);

		boolean posX = state.getValue(POS_X);
		boolean negX = state.getValue(NEG_X);
		boolean posY = state.getValue(POS_Y);
		boolean negY = state.getValue(NEG_Y);
		boolean posZ = state.getValue(POS_Z);
		boolean negZ = state.getValue(NEG_Z);

		float pixel = 0.0625F;
		float min = pixel * 5.5F;
		float max = pixel * 10.5F;

		float minX = negX ? 0F : min;
		float maxX = posX ? 1F : max;
		float minY = negY ? 0F : min;
		float maxY = posY ? 1F : max;
		float minZ = negZ ? 0F : min;
		float maxZ = posZ ? 1F : max;

		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos,
									  AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
									  @Nullable Entity entityIn, boolean isActualState) {
		AxisAlignedBB bb = getBoundingBox(state, worldIn, pos);
		super.addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void registerSprite(TextureMap map) {
		if (textureLocation != null) {
			map.registerSprite(textureLocation);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void bakeModel(ModelBakeEvent event) {
		HFRWavefrontObject wavefront = null;
		try {
			wavefront = new HFRWavefrontObject(objModelLocation);
		} catch (Exception ignored) {}

		TextureAtlasSprite sprite;
		if (textureLocation != null) {
			sprite = Minecraft.getMinecraft()
					.getTextureMapBlocks()
					.getAtlasSprite(textureLocation.toString());
		} else {
			sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
		}

		IBakedModel blockModel;
		IBakedModel itemModel;
		if (wavefront == null) {
			TextureAtlasSprite missing = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
			blockModel = BlockCableBakedModel.empty(missing);
			itemModel = BlockCableBakedModel.empty(missing);
		} else {
			blockModel = BlockCableBakedModel.forBlock(wavefront, sprite);
			itemModel = BlockCableBakedModel.forItem(wavefront, sprite, 1F, 0.5F, 0.0F, 0.5F, (float)Math.PI);
		}

		ModelResourceLocation mrlBlock = new ModelResourceLocation(getRegistryName(), "normal");
		ModelResourceLocation mrlItem = new ModelResourceLocation(getRegistryName(), "inventory");

		event.getModelRegistry().putObject(mrlBlock, blockModel);
		event.getModelRegistry().putObject(mrlItem, itemModel);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public StateMapperBase getStateMapper(ResourceLocation loc) {
		return new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return new ModelResourceLocation(loc, "normal");
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		Item item = Item.getItemFromBlock(this);
		ModelResourceLocation inv = new ModelResourceLocation(this.getRegistryName(), "inventory");
		ModelLoader.setCustomModelResourceLocation(item, 0, inv);
	}
}
