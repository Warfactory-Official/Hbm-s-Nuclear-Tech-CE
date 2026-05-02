package com.hbm.blocks.network.energy;

import com.hbm.Tags;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IBlockSpecialPlacementAABB;
import com.hbm.items.IDynamicModels;
import com.hbm.items.block.ItemBlockSpecialAABB;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.BlockCableBakedModel;
import com.hbm.tileentity.network.energy.TileEntityCableBaseNT;
import net.minecraft.block.Block;
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
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class BlockCable extends BlockContainer implements IDynamicModels, ICustomBlockItem, IBlockSpecialPlacementAABB {
	// Th3_Sl1ze: believe me, this shit will inevitably cause stackoverflowexception if you're going to load a shitton of cables in a single place
	// though, it still works and it doesn't crash
	public static final PropertyBool POS_X = PropertyBool.create("posx");
	public static final PropertyBool NEG_X = PropertyBool.create("negx");
	public static final PropertyBool POS_Y = PropertyBool.create("posy");
	public static final PropertyBool NEG_Y = PropertyBool.create("negy");
	public static final PropertyBool POS_Z = PropertyBool.create("posz");
	public static final PropertyBool NEG_Z = PropertyBool.create("negz");

	private static final AxisAlignedBB[] AABB_BY_MASK = new AxisAlignedBB[64];
	static {
		float pixel = 0.0625F;
		float min = pixel * 5.5F;
		float max = pixel * 10.5F;
		for (int m = 0; m < 64; m++) {
			float minX = (m & (1 << EnumFacing.WEST.getIndex()))  != 0 ? 0F : min;
			float maxX = (m & (1 << EnumFacing.EAST.getIndex()))  != 0 ? 1F : max;
			float minY = (m & (1 << EnumFacing.DOWN.getIndex()))  != 0 ? 0F : min;
			float maxY = (m & (1 << EnumFacing.UP.getIndex()))    != 0 ? 1F : max;
			float minZ = (m & (1 << EnumFacing.NORTH.getIndex())) != 0 ? 0F : min;
			float maxZ = (m & (1 << EnumFacing.SOUTH.getIndex())) != 0 ? 1F : max;
			AABB_BY_MASK[m] = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
		}
	}

	private final IBlockState[] statesByMask = new IBlockState[64];

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
		for (int m = 0; m < 64; m++) {
			this.statesByMask[m] = base
					.withProperty(POS_X, (m & (1 << EnumFacing.EAST.getIndex()))  != 0)
					.withProperty(NEG_X, (m & (1 << EnumFacing.WEST.getIndex()))  != 0)
					.withProperty(POS_Y, (m & (1 << EnumFacing.UP.getIndex()))    != 0)
					.withProperty(NEG_Y, (m & (1 << EnumFacing.DOWN.getIndex()))  != 0)
					.withProperty(POS_Z, (m & (1 << EnumFacing.SOUTH.getIndex())) != 0)
					.withProperty(NEG_Z, (m & (1 << EnumFacing.NORTH.getIndex())) != 0);
		}
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

	public static boolean computeConnectToNeighbor(IBlockAccess world, BlockPos pos, ForgeDirection dir) {
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

	public static byte computeConnectionMask(IBlockAccess world, BlockPos pos) {
		byte mask = 0;
		for (EnumFacing facing : EnumFacing.VALUES) {
			BlockPos adj = pos.offset(facing);
			if (world instanceof World w && !w.isBlockLoaded(adj)) {
				continue;
			}
			ForgeDirection dir = ForgeDirection.getOrientation(facing);
			if (computeConnectToNeighbor(world, adj, dir)) {
				mask |= (byte) (1 << facing.getIndex());
			}
		}
		return mask;
	}

	private int resolveMask(IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityCableBaseNT) {
			return ((TileEntityCableBaseNT) te).getCachedConnectionMask(world) & 0x3F;
		}
		return computeConnectionMask(world, pos) & 0x3F;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		invalidateConnectionCache(world, pos);
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);
		invalidateConnectionCache(world, pos);
	}

	private static void invalidateConnectionCache(IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityCableBaseNT) {
			((TileEntityCableBaseNT) te).invalidateConnectionCache();
		}
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return this.statesByMask[resolveMask(world, pos)];
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AABB_BY_MASK[resolveMask(source, pos)];
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos,
									  AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
									  @Nullable Entity entityIn, boolean isActualState) {
		super.addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_MASK[resolveMask(worldIn, pos)]);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxForPlacement(World worldIn, BlockPos pos, IBlockState stateForPlacement, ItemStack stack) {
		return AABB_BY_MASK[resolveMask(worldIn, pos)];
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

	@Override
	public void registerItem() {
		ItemBlock itemBlock = new ItemBlockSpecialAABB<>(this);
		itemBlock.setRegistryName(this.getRegistryName());
		ForgeRegistries.ITEMS.register(itemBlock);
	}
}
