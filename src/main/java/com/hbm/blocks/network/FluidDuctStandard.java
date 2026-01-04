package com.hbm.blocks.network;

import com.hbm.Tags;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.IBlockSpecialPlacementAABB;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.items.block.ItemBlockSpecialAABB;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.model.DuctNeoBakedModel;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FluidDuctStandard extends FluidDuctBase implements IDynamicModels, ILookOverlay, ICustomBlockItem, IBlockSpecialPlacementAABB {

	public static final PropertyInteger META = PropertyInteger.create("meta", 0, 2);
	public static final PropertyBool POS_X = PropertyBool.create("posx");
	public static final PropertyBool NEG_X = PropertyBool.create("negx");
	public static final PropertyBool POS_Y = PropertyBool.create("posy");
	public static final PropertyBool NEG_Y = PropertyBool.create("negy");
	public static final PropertyBool POS_Z = PropertyBool.create("posz");
	public static final PropertyBool NEG_Z = PropertyBool.create("negz");

	private static final AxisAlignedBB DUCT_BB = new AxisAlignedBB(1, 1, 1, -1, -1, -1);

	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite[] baseSprites;   // icon[]
	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite[] overlaySprites; // overlay[]

	private final ResourceLocation objModelLocation = new ResourceLocation(Tags.MODID, "models/blocks/pipe_neo.obj");

	public FluidDuctStandard(Material materialIn, String reg) {
		super(materialIn);
		this.setTranslationKey(reg);
		this.setRegistryName(reg);
		this.setCreativeTab(MainRegistry.controlTab);
		this.setSoundType(ModSoundTypes.pipe);
		this.useNeighborBrightness = true;

		IBlockState base = this.blockState.getBaseState()
				.withProperty(META, 0)
				.withProperty(POS_X, Boolean.FALSE)
				.withProperty(NEG_X, Boolean.FALSE)
				.withProperty(POS_Y, Boolean.FALSE)
				.withProperty(NEG_Y, Boolean.FALSE)
				.withProperty(POS_Z, Boolean.FALSE)
				.withProperty(NEG_Z, Boolean.FALSE);
		this.setDefaultState(base);

		ModBlocks.ALL_BLOCKS.add(this);
		IDynamicModels.INSTANCES.add(this);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, META, POS_X, NEG_X, POS_Y, NEG_Y, POS_Z, NEG_Z);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, meta % 3);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityPipeBaseNT();
	}

	/**
	 * Checks if it can connect to a fluid-capable neighbor.
	 */
	private boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection dir, FluidType type) {
		return Library.canConnectFluid(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir, type);
	}

	private boolean canConnectAt(IBlockAccess world, BlockPos pos, EnumFacing dir, FluidType type) {
		return switch (dir) {
			case EAST -> canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.POS_X, type);
			case WEST -> canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.NEG_X, type);
			case UP -> canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.POS_Y, type);
			case DOWN -> canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.NEG_Y, type);
			case SOUTH -> canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.POS_Z, type);
			case NORTH -> canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.NEG_Z, type);
		};
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityPipeBaseNT pipe) {
			FluidType type = pipe.getType();
			boolean pX = canConnectAt(world, pos, EnumFacing.EAST, type);
			boolean nX = canConnectAt(world, pos, EnumFacing.WEST, type);
			boolean pY = canConnectAt(world, pos, EnumFacing.UP, type);
			boolean nY = canConnectAt(world, pos, EnumFacing.DOWN, type);
			boolean pZ = canConnectAt(world, pos, EnumFacing.SOUTH, type);
			boolean nZ = canConnectAt(world, pos, EnumFacing.NORTH, type);
			return state.withProperty(POS_X, pX)
					.withProperty(NEG_X, nX)
					.withProperty(POS_Y, pY)
					.withProperty(NEG_Y, nY)
					.withProperty(POS_Z, pZ)
					.withProperty(NEG_Z, nZ);
		}
		return state;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileEntity te = source.getTileEntity(pos);
		if (te instanceof TileEntityPipeBaseNT pipe) {
			FluidType type = pipe.getType();
            return getCollisionAABB(source, pos, type);
        }
		return DUCT_BB;
	}

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new ItemBlockSpecialAABB<>(this);
        itemBlock.setRegistryName(this.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxForPlacement(World worldIn, BlockPos pos, ItemStack stack) {
        return getCollisionAABB(worldIn, pos, Fluids.NONE);
    }

    @NotNull
    private AxisAlignedBB getCollisionAABB(IBlockAccess source, BlockPos pos, FluidType type) {
        boolean nX = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), Library.NEG_X, type);
        boolean pX = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), Library.POS_X, type);
        boolean nY = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), Library.NEG_Y, type);
        boolean pY = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), Library.POS_Y, type);
        boolean nZ = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), Library.NEG_Z, type);
        boolean pZ = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), Library.POS_Z, type);
        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);

        return switch (mask) {
            case 0 -> new AxisAlignedBB(0F, 0F, 0F, 1F, 1F, 1F);
            case 0b100000, 0b010000, 0b110000 -> new AxisAlignedBB(0F, 0.3125F, 0.3125F, 1F, 0.6875F, 0.6875F);
            case 0b001000, 0b000100, 0b001100 -> new AxisAlignedBB(0.3125F, 0F, 0.3125F, 0.6875F, 1F, 0.6875F);
            case 0b000010, 0b000001, 0b000011 -> new AxisAlignedBB(0.3125F, 0.3125F, 0F, 0.6875F, 0.6875F, 1F);
            default -> new AxisAlignedBB(
                    nX ? 0F : 0.3125F,
                    nY ? 0F : 0.3125F,
                    nZ ? 0F : 0.3125F,
                    pX ? 1F : 0.6875F,
                    pY ? 1F : 0.6875F,
                    pZ ? 1F : 0.6875F);
        };
    }

    @Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
									  AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
									  @Nullable Entity entity, boolean isActualState) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityPipeBaseNT pipe) {
			FluidType type = pipe.getType();

			boolean nX = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.NEG_X, type);
			boolean pX = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.POS_X, type);
			boolean nY = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.NEG_Y, type);
			boolean pY = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.POS_Y, type);
			boolean nZ = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.NEG_Z, type);
			boolean pZ = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), Library.POS_Z, type);
			int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);

			List<AxisAlignedBB> bbs = new ArrayList<>();

			if (mask == 0) {
				bbs.add(new AxisAlignedBB(pos.getX() + 0.6875D, pos.getY() + 0.3125D, pos.getZ() + 0.3125D, pos.getX() + 1.0D, pos.getY() + 0.6875D, pos.getZ() + 0.6875D));
				bbs.add(new AxisAlignedBB(pos.getX() + 0.0D, pos.getY() + 0.3125D, pos.getZ() + 0.3125D, pos.getX() + 0.3125D, pos.getY() + 0.6875D, pos.getZ() + 0.6875D));
				bbs.add(new AxisAlignedBB(pos.getX() + 0.3125D, pos.getY() + 0.6875D, pos.getZ() + 0.3125D, pos.getX() + 0.6875D, pos.getY() + 1.0D, pos.getZ() + 0.6875D));
				bbs.add(new AxisAlignedBB(pos.getX() + 0.3125D, pos.getY() + 0.0D, pos.getZ() + 0.3125D, pos.getX() + 0.6875D, pos.getY() + 0.3125D, pos.getZ() + 0.6875D));
				bbs.add(new AxisAlignedBB(pos.getX() + 0.3125D, pos.getY() + 0.3125D, pos.getZ() + 0.6875D, pos.getX() + 0.6875D, pos.getY() + 0.6875D, pos.getZ() + 1.0D));
				bbs.add(new AxisAlignedBB(pos.getX() + 0.3125D, pos.getY() + 0.3125D, pos.getZ() + 0.0D, pos.getX() + 0.6875D, pos.getY() + 0.6875D, pos.getZ() + 0.3125D));
			} else if (mask == 0b100000 || mask == 0b010000 || mask == 0b110000) {
				bbs.add(new AxisAlignedBB(pos.getX() + 0.0D, pos.getY() + 0.3125D, pos.getZ() + 0.3125D, pos.getX() + 1.0D, pos.getY() + 0.6875D, pos.getZ() + 0.6875D));
			} else if (mask == 0b001000 || mask == 0b000100 || mask == 0b001100) {
				bbs.add(new AxisAlignedBB(pos.getX() + 0.3125D, pos.getY() + 0.0D, pos.getZ() + 0.3125D, pos.getX() + 0.6875D, pos.getY() + 1.0D, pos.getZ() + 0.6875D));
			} else if (mask == 0b000010 || mask == 0b000001 || mask == 0b000011) {
				bbs.add(new AxisAlignedBB(pos.getX() + 0.3125D, pos.getY() + 0.3125D, pos.getZ() + 0.0D, pos.getX() + 0.6875D, pos.getY() + 0.6875D, pos.getZ() + 1.0D));
			} else {
				bbs.add(new AxisAlignedBB(pos.getX() + 0.3125D, pos.getY() + 0.3125D, pos.getZ() + 0.3125D, pos.getX() + 0.6875D, pos.getY() + 0.6875D, pos.getZ() + 0.6875D));

				if (pX) bbs.add(new AxisAlignedBB(pos.getX() + 0.6875D, pos.getY() + 0.3125D, pos.getZ() + 0.3125D, pos.getX() + 1.0D, pos.getY() + 0.6875D, pos.getZ() + 0.6875D));
				if (nX) bbs.add(new AxisAlignedBB(pos.getX() + 0.0D, pos.getY() + 0.3125D, pos.getZ() + 0.3125D, pos.getX() + 0.3125D, pos.getY() + 0.6875D, pos.getZ() + 0.6875D));
				if (pY) bbs.add(new AxisAlignedBB(pos.getX() + 0.3125D, pos.getY() + 0.6875D, pos.getZ() + 0.3125D, pos.getX() + 0.6875D, pos.getY() + 1.0D, pos.getZ() + 0.6875D));
				if (nY) bbs.add(new AxisAlignedBB(pos.getX() + 0.3125D, pos.getY() + 0.0D, pos.getZ() + 0.3125D, pos.getX() + 0.6875D, pos.getY() + 0.3125D, pos.getZ() + 0.6875D));
				if (pZ) bbs.add(new AxisAlignedBB(pos.getX() + 0.3125D, pos.getY() + 0.3125D, pos.getZ() + 0.6875D, pos.getX() + 0.6875D, pos.getY() + 0.6875D, pos.getZ() + 1.0D));
				if (nZ) bbs.add(new AxisAlignedBB(pos.getX() + 0.3125D, pos.getY() + 0.3125D, pos.getZ() + 0.0D, pos.getX() + 0.6875D, pos.getY() + 0.6875D, pos.getZ() + 0.3125D));
			}

			for (AxisAlignedBB bb : bbs) {
				if (entityBox.intersects(bb)) {
					collidingBoxes.add(bb);
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return getBoundingBox(blockState, worldIn, pos);
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
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
	public boolean isFullBlock(IBlockState state) {
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
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face){
		return BlockFaceShape.CENTER;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (tab == CreativeTabs.SEARCH || tab == this.getCreativeTab()) {
			for (int i = 0; i < 3; ++i) {
				items.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityPipeBaseNT) {
			TileEntityPipeBaseNT pipe = (TileEntityPipeBaseNT) tileEntity;
			FluidType fluidType = pipe.getType();
			int retadata = fluidType.getID();
			return new ItemStack(ModItems.fluid_duct, 1, retadata);
		}
		return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		int meta = stack.getMetadata() % 3;
		worldIn.setBlockState(pos, this.getDefaultState().withProperty(META, meta), 2);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(META);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if (!(te instanceof TileEntityPipeBaseNT duct))
			return;

		List<String> text = new ArrayList<>();
		text.add("&[" + duct.getType().getColor() + "&]" + duct.getType().getLocalizedName());
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	// IDynamicModels

	@SideOnly(Side.CLIENT)
	@Override
	public void registerSprite(TextureMap map) {
		baseSprites = new TextureAtlasSprite[3];
		overlaySprites = new TextureAtlasSprite[3];

		// meta 0: use block name as base
		String base0 = "blocks/" + this.getRegistryName().getPath();
		String overlay0 = base0 + "_overlay";
		baseSprites[0] = map.registerSprite(new ResourceLocation(Tags.MODID, base0));
		overlaySprites[0] = map.registerSprite(new ResourceLocation(Tags.MODID, overlay0));

		// meta 1: silver
		baseSprites[1] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pipe_silver"));
		overlaySprites[1] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pipe_silver_overlay"));

		// meta 2: colored
		baseSprites[2] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pipe_colored"));
		overlaySprites[2] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pipe_colored_overlay"));
	}

	// do we need to separate inv meta models from block meta models? maybe not.
	// does it affect performance? no
	// did it fix the incorrect state render issue? yes
	// so let me have a good ol' rest already..
	@SideOnly(Side.CLIENT)
	@Override
	public void bakeModel(ModelBakeEvent event) {
		HFRWavefrontObject wavefront = null;
		try {
			wavefront = new HFRWavefrontObject(objModelLocation);
		} catch (Exception ignored) {}

		TextureAtlasSprite missing = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();

		for (int meta = 0; meta < 3; meta++) {
			TextureAtlasSprite base = (baseSprites != null && baseSprites[meta] != null) ? baseSprites[meta] : missing;
			TextureAtlasSprite overlay = (overlaySprites != null && overlaySprites[meta] != null) ? overlaySprites[meta] : missing;

			IBakedModel blockModel;
			IBakedModel itemModel;

			if (wavefront == null) {
				blockModel = DuctNeoBakedModel.empty(missing);
				itemModel = DuctNeoBakedModel.empty(missing);
			} else {
				blockModel = DuctNeoBakedModel.forBlock(wavefront, base, overlay);
				itemModel = DuctNeoBakedModel.forItem(wavefront, base, overlay, 1.0F, 0.0F, 0.0F, 0.0F, (float)Math.PI);
			}

			ModelResourceLocation mrlBlock = new ModelResourceLocation(getRegistryName(), "meta=" + meta);
			event.getModelRegistry().putObject(mrlBlock, blockModel);

			ModelResourceLocation mrlItem = new ModelResourceLocation(getRegistryName(), "inventory_meta=" + meta);
			event.getModelRegistry().putObject(mrlItem, itemModel);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public StateMapperBase getStateMapper(ResourceLocation loc) {
		return new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return new ModelResourceLocation(loc, "meta=" + state.getValue(META));
			}
		};
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel() {
		Item item = Item.getItemFromBlock(this);
		for (int meta = 0; meta < 3; meta++) {
			ModelResourceLocation inv = new ModelResourceLocation(this.getRegistryName(), "inventory_meta=" + meta);
			ModelLoader.setCustomModelResourceLocation(item, meta, inv);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void registerColorHandler(ColorHandlerEvent.Block evt) {
		IBlockColor colorHandler = (state, worldIn, pos, tintIndex) -> {
			// overlay quads use tintIndex 1
			if (tintIndex != 1) return 0xFFFFFF;
			if (worldIn == null || pos == null) return 0xFFFFFF;

			TileEntity te = worldIn.getTileEntity(pos);
			if (!(te instanceof TileEntityPipeBaseNT pipe)) return 0xFFFFFF;
			FluidType type = pipe.getType();
			if (type == null) return 0xFFFFFF;
			return type.getColor();
		};
		evt.getBlockColors().registerBlockColorHandler(colorHandler, ModBlocks.fluid_duct_neo); // ensure ModBlocks.fluid_duct refers to this block
	}
}
