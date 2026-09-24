package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IDynamicModels;
import com.hbm.render.icon.PaddedSpriteUtil;
import com.hbm.render.icon.PaddedSpriteUtil.TextureInfo;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.RBMKMiniPanelBakedModel;
import com.hbm.render.model.RBMKMiniPanelItemBakedModel.Layer;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix4f;
import java.util.List;
import java.util.Set;

public class RBMKMiniPanelBase extends BlockContainer implements IDynamicModels, ITooltipProvider {
	@SideOnly(Side.CLIENT)
	protected TextureAtlasSprite sprite;

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public RBMKMiniPanelBase(String s) {
		super(Material.IRON);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setHardness(3F);
		this.setResistance(30F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		ModBlocks.ALL_BLOCKS.add(this);
		IDynamicModels.INSTANCES.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return null;
	}

	@Override
	protected @NotNull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isOpaqueCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public @NotNull IBlockState getStateForPlacement(@NotNull World world,@NotNull BlockPos pos,@NotNull EnumFacing facing,float hitX,float hitY,float hitZ,int meta,EntityLivingBase placer,@NotNull EnumHand hand) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public @NotNull AxisAlignedBB getBoundingBox(IBlockState state,@NotNull IBlockAccess source,@NotNull BlockPos pos) {
		EnumFacing facing = state.getValue(FACING);
		return switch (facing) {
			case WEST -> new AxisAlignedBB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
			case NORTH -> new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D);
			case EAST -> new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D);
			case SOUTH -> new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D);
			default -> FULL_BLOCK_AABB;
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos, @NotNull EnumFacing side) {
		return true;
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		Item item = Item.getItemFromBlock(this);
		ModelResourceLocation inv = new ModelResourceLocation(getRegistryName(), "inventory");
		ModelLoader.setCustomModelResourceLocation(item, 0, inv);
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
	public void registerSprite(TextureMap map) {
		ResourceLocation rl = getRegistryName();
		if (rl != null) {
			this.sprite = map.registerSprite(new ResourceLocation(rl.getNamespace(), "blocks/rbmk/rbmk_display" /*+ rl.getPath()*/));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void bakeModel(ModelBakeEvent event) {
		if (this.sprite == null) return;

		ModelResourceLocation worldLoc = new ModelResourceLocation(getRegistryName(), "normal");
		ModelResourceLocation invLoc = new ModelResourceLocation(getRegistryName(), "inventory");

		IBakedModel worldModel = new RBMKMiniPanelBakedModel(this.sprite, false);
		IBakedModel itemModel = createItemModel();

		event.getModelRegistry().putObject(worldLoc, worldModel);
		event.getModelRegistry().putObject(invLoc, itemModel);
	}

	@SideOnly(Side.CLIENT)
	protected IBakedModel createItemModel() {
		return new RBMKMiniPanelBakedModel(this.sprite, true);
	}

	@SideOnly(Side.CLIENT)
	protected static Layer texturedLayer(HFRWavefrontObject model, @Nullable Set<String> parts, ResourceLocation texture, int color, @Nullable Matrix4f localTransform, float[][] offsets) {
		TextureInfo info = PaddedSpriteUtil.inspectTexture(texture);
		TextureAtlasSprite partSprite = PaddedSpriteUtil.sprite(Minecraft.getMinecraft().getTextureMapBlocks(), info);
		return new Layer(model, parts, partSprite, info.uScale, info.vScale, color, localTransform, offsets);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		this.addStandardInfo(tooltip);
	}
}
