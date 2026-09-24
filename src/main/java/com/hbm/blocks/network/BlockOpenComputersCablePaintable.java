package com.hbm.blocks.network;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.CompatHandler.OCColors;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.ICopiable;
import com.hbm.items.IDynamicModels;
import com.hbm.render.model.BakedModelTransforms;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.Network;
import li.cil.oc.api.internal.Colored;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;
import li.cil.oc.api.network.Visibility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BlockOpenComputersCablePaintable extends BlockContainer implements IDynamicModels, IToolable, ITooltipProvider {

	public static final IUnlistedProperty<IBlockState> DISGUISED_STATE = new SimpleUnlistedProperty<>("disguised_state", IBlockState.class);
	public static final PropertyBool DEFUSED = PropertyBool.create("defused");

	@SideOnly(Side.CLIENT) private static TextureAtlasSprite baseSprite;
	@SideOnly(Side.CLIENT) private static TextureAtlasSprite overlaySprite;
	@SideOnly(Side.CLIENT) private static TextureAtlasSprite colorSprite;

	public BlockOpenComputersCablePaintable(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setDefaultState(this.blockState.getBaseState().withProperty(DEFUSED, false));
		this.useNeighborBrightness = true;

		IDynamicModels.INSTANCES.add(this);
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityOpenComputersCablePaintable();
	}

	@Override
	protected @NotNull BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[]{DEFUSED}, new IUnlistedProperty[]{DISGUISED_STATE});
	}

	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(DEFUSED, meta != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(DEFUSED) ? 1 : 0;
	}

	@Override
	public @NotNull IBlockState getExtendedState(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
		if(!(state instanceof IExtendedBlockState ext)) return state;

		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityOpenComputersCablePaintable cable && cable.block != null) {
			IBlockState disguiseState = cable.block.getStateFromMeta(cable.meta);
			if(cable.block != this) disguiseState = cable.block.getExtendedState(disguiseState, world, pos);
			return ext.withProperty(DISGUISED_STATE, disguiseState);
		}

		return ext.withProperty(DISGUISED_STATE, null);
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override public boolean isOpaqueCube(@NotNull IBlockState state) { return false; }
	@Override public boolean isFullCube(@NotNull IBlockState state) { return false; }

	@Override
	@SideOnly(Side.CLIENT)
	public @NotNull BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if(stack.isEmpty()) return false;

		TileEntity tile = world.getTileEntity(pos);
		if(!(tile instanceof TileEntityOpenComputersCablePaintable cable)) return false;

		if(stack.getItem() instanceof ItemBlock ib) {
			Block disguise = ib.getBlock();
			if(disguise == this || cable.block != null) return false;

			IBlockState disguiseState = disguise.getStateFromMeta(stack.getMetadata());
			if(!disguiseState.isFullCube() || !disguiseState.isOpaqueCube()) return false;

			if(!world.isRemote) {
				cable.block = disguise;
				cable.meta = stack.getMetadata() & 15;
				cable.markDirty();
				world.notifyBlockUpdate(pos, state, state, 3);
			}
			return true;
		}

		OCColors dye = OCColors.fromDye(stack);
		if(dye == OCColors.NONE) return false;

		if(!world.isRemote) {
			cable.setColor(dye.getColor());
			cable.markDirty();
			world.notifyBlockUpdate(pos, state, state, 3);
		}
		return true;
	}

	@Override
	public boolean onScrew(World world, @NotNull EntityPlayer player, int x, int y, int z, @NotNull EnumFacing side, float fX, float fY, float fZ, @NotNull EnumHand hand, ToolType tool) {
		BlockPos pos = new BlockPos(x, y, z);

		if(tool == ToolType.SCREWDRIVER) {
			TileEntity tile = world.getTileEntity(pos);
			if(!(tile instanceof TileEntityOpenComputersCablePaintable cable) || cable.block == null) return false;

			if(!world.isRemote) {
				cable.block = null;
				cable.meta = 0;
				cable.markDirty();
				world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			}
			return true;
		}

		if(tool == ToolType.DEFUSER) {
			if(!world.isRemote) world.setBlockState(pos, world.getBlockState(pos).cycleProperty(DEFUSED), 3);
			return true;
		}

		return false;
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
		this.addStandardInfo(tooltip);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerSprite(TextureMap map) {
		baseSprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/oc_cable_base"));
		overlaySprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/oc_cable_overlay"));
		colorSprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/oc_cable_color"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void bakeModel(ModelBakeEvent event) {
		OpenComputersCablePaintableModel model = new OpenComputersCablePaintableModel(baseSprite, overlaySprite, colorSprite);

		ResourceLocation name = Objects.requireNonNull(getRegistryName());
		event.getModelRegistry().putObject(new ModelResourceLocation(name, "normal"), model);
		event.getModelRegistry().putObject(new ModelResourceLocation(name, "inventory"), model);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0,
				new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public StateMapperBase getStateMapper(ResourceLocation loc) {
		return new StateMapperBase() {
			@Override
			protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
				return new ModelResourceLocation(loc, "normal");
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColorHandler() {
		return (state, world, pos, tintIndex) -> {
			if(tintIndex != 0 || world == null || pos == null) return 0xFFFFFF;
			TileEntity tile = world.getTileEntity(pos);
			return tile instanceof TileEntityOpenComputersCablePaintable cable ? cable.getColor() : OCColors.LIGHTGRAY.getColor();
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColorHandler() {
		return (stack, tintIndex) -> tintIndex == 0 ? OCColors.LIGHTGRAY.getColor() : 0xFFFFFF;
	}

	@Override
	public Object getSelf() {
		return this;
	}

	@SideOnly(Side.CLIENT)
	public static class OpenComputersCablePaintableModel implements IBakedModel {

		private static final FaceBakery FACE_BAKERY = new FaceBakery();

		private final TextureAtlasSprite particle;
		private final ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> baseFaces;
		private final ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> overlayFaces;
		private final ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> colorFaces;
		private final ImmutableList<BakedQuad> baseGeneral;
		private final ImmutableList<BakedQuad> overlayGeneral;
		private final ImmutableList<BakedQuad> colorGeneral;

		public OpenComputersCablePaintableModel(TextureAtlasSprite base, TextureAtlasSprite overlay, TextureAtlasSprite color) {
			this.particle = base;
			this.baseFaces = buildFaceMap(base, -1, 0F);
			this.overlayFaces = buildFaceMap(overlay, -1, 0.001F);
			this.colorFaces = buildFaceMap(color, 0, 0.002F);
			this.baseGeneral = flatten(this.baseFaces);
			this.overlayGeneral = flatten(this.overlayFaces);
			this.colorGeneral = flatten(this.colorFaces);
		}

		@Override
		public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			List<BakedQuad> quads = new ArrayList<>();
			BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
			boolean renderCable = layer == null || layer == BlockRenderLayer.CUTOUT_MIPPED;

			if(state == null) {
				if(renderCable && side == null) {
					quads.addAll(baseGeneral);
					quads.addAll(overlayGeneral);
					quads.addAll(colorGeneral);
				}
				return quads;
			}

			IBlockState disguiseState = state instanceof IExtendedBlockState ext ? ext.getValue(DISGUISED_STATE) : null;

			if(disguiseState != null) {
				IBlockState lookup = disguiseState instanceof IExtendedBlockState ext ? ext.getClean() : disguiseState;
				IBakedModel disguiseModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(lookup);
				quads.addAll(disguiseModel.getQuads(disguiseState, side, rand));
			} else if(renderCable) {
				quads.addAll(side == null ? baseGeneral : baseFaces.get(side));
			}

			if(renderCable && !state.getValue(DEFUSED)) {
				quads.addAll(side == null ? overlayGeneral : overlayFaces.get(side));
				quads.addAll(side == null ? colorGeneral : colorFaces.get(side));
			}

			return quads;
		}

		@Override public boolean isAmbientOcclusion() { return true; }
		@Override public boolean isGui3d() { return true; }
		@Override public boolean isBuiltInRenderer() { return false; }
		@Override public @NotNull TextureAtlasSprite getParticleTexture() { return particle; }
		@Override public @NotNull ItemCameraTransforms getItemCameraTransforms() { return BakedModelTransforms.isbrh(); }
		@Override public @NotNull ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }

		private static ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> buildFaceMap(TextureAtlasSprite sprite, int tintIndex, float offset) {
			ImmutableMap.Builder<EnumFacing, ImmutableList<BakedQuad>> builder = ImmutableMap.builder();
			for(EnumFacing face : EnumFacing.VALUES) {
				builder.put(face, ImmutableList.of(createQuad(face, sprite, tintIndex, offset)));
			}
			return builder.build();
		}

		private static ImmutableList<BakedQuad> flatten(Map<EnumFacing, ImmutableList<BakedQuad>> map) {
			ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
			for(EnumFacing face : EnumFacing.VALUES) builder.addAll(map.get(face));
			return builder.build();
		}

		private static BakedQuad createQuad(EnumFacing face, TextureAtlasSprite sprite, int tintIndex, float eps) {
			Vector3f from = new Vector3f(0F, 0F, 0F);
			Vector3f to = new Vector3f(16F, 16F, 16F);

			if(eps > 0F) {
				switch(face) {
				case DOWN -> from.setY(-eps);
				case UP -> to.setY(16F + eps);
				case NORTH -> from.setZ(-eps);
				case SOUTH -> to.setZ(16F + eps);
				case WEST -> from.setX(-eps);
				case EAST -> to.setX(16F + eps);
				}
			}

			BlockFaceUV uv = new BlockFaceUV(new float[]{0F, 0F, 16F, 16F}, 0);
			BlockPartFace partFace = new BlockPartFace(null, tintIndex, "", uv);
			return FACE_BAKERY.makeBakedQuad(from, to, partFace, sprite, face, ModelRotation.X0_Y0, null, false, true);
		}
	}

	@AutoRegister(name = "tileentity_oc_cable_paintable")
	@Optional.InterfaceList({
		@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "opencomputers"),
		@Optional.Interface(iface = "li.cil.oc.api.network.SidedEnvironment", modid = "opencomputers"),
		@Optional.Interface(iface = "li.cil.oc.api.internal.Colored", modid = "opencomputers")
	})
	public static class TileEntityOpenComputersCablePaintable extends TileEntityLoadedBase implements ITickable, Environment, SidedEnvironment, Colored, ICopiable {

		protected Node node;
		protected boolean addedToNetwork = false;

		public Block block;
		public int meta;
		private Block lastBlock;
		private int lastMeta;
		private OCColors lastColor;
		private OCColors color = OCColors.LIGHTGRAY;

		public TileEntityOpenComputersCablePaintable() {
			node = Network.newNode(this, Visibility.None).create();
		}

		@Override
		public void update() {

			if(world.isRemote && (lastBlock != block || lastMeta != meta || lastColor != color)) {
				world.markBlockRangeForRenderUpdate(pos, pos);
				lastBlock = block;
				lastMeta = meta;
				lastColor = color;
			}

			if(!world.isRemote && !addedToNetwork) {
				addedToNetwork = true;
				Network.joinOrCreateNetwork(this);
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);

			if(nbt.hasKey("block")) {
				this.block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("block")));
			} else {
				this.block = null;
			}
			this.meta = nbt.getInteger("meta");

			this.color = OCColors.fromInt(nbt.getInteger("dyeColor"));

			if(node != null && node.host() == this) {
				node.load(nbt.getCompoundTag("oc:node"));
			}
		}

		@Override
		public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {

			if(block != null && block.getRegistryName() != null) nbt.setString("block", block.getRegistryName().toString());
			nbt.setInteger("meta", meta);

			nbt.setInteger("dyeColor", color.getColor());

			if(node != null && node.host() == this) {
				final NBTTagCompound nodeNbt = new NBTTagCompound();
				node.save(nodeNbt);
				nbt.setTag("oc:node", nodeNbt);
			}

			return super.writeToNBT(nbt);
		}

		@Override
		public NBTTagCompound getSettings(World world, int x, int y, int z) {
			NBTTagCompound nbt = new NBTTagCompound();
			if(block != null && block.getRegistryName() != null) {
				nbt.setString("paintblock", block.getRegistryName().toString());
				nbt.setInteger("paintmeta", meta);
			}
			return nbt;
		}

		@Override
		public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
			if(nbt.hasKey("paintblock")) {
				this.block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("paintblock")));
				this.meta = nbt.getInteger("paintmeta");
				this.color = OCColors.fromInt(nbt.getInteger("dyeColor"));
			}
		}

		// OC cable things

		@Override
		public Node node() {
			return node;
		}

		@Override
		public Node sidedNode(EnumFacing side) {
			if(side == null) return null;

			TileEntity neighbor = world.getTileEntity(pos.offset(side));

			// If a cable does not support colors but is a valid cable block, allow it to connect regardless of color.
			if(!(neighbor instanceof Colored)) {
				return neighbor instanceof Environment ? node : null;
			}

			Colored cable = (Colored) neighbor;
			return cable.getColor() == color.getColor() ? node : null;
		}

		@Override public void onConnect(Node node) {}
		@Override public void onDisconnect(Node node) {}
		@Override public void onMessage(Message message) {}

		@Override
		public void onChunkUnload() {
			super.onChunkUnload();
			if(node != null) node.remove();
		}

		@Override
		public void invalidate() {
			super.invalidate();
			if(node != null) node.remove();
		}

		@Override
		public boolean canConnect(EnumFacing side) {
			if(side == null) return false;

			TileEntity neighbor = world.getTileEntity(pos.offset(side));

			if(!(neighbor instanceof Colored)) {
				return neighbor instanceof Environment;
			}

			return ((Colored) neighbor).getColor() == color.getColor();
		}

		@Override
		public void setColor(int newColor) {
			color = OCColors.fromInt(newColor);
		}

		@Override
		public int getColor() {
			return color.getColor();
		}

		@Override
		public boolean controlsConnectivity() {
			return true;
		}
	}
}
