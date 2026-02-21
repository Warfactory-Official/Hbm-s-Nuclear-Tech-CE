package com.hbm.items.special;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.items.ItemBakedBase;
import com.hbm.render.model.BakedModelTransforms;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemHot extends ItemBakedBase {

	public static int heat;
	protected final String baseTexturePath;
	private final String overlayTexturePath;
	
	public ItemHot(int heat, String s) {
		super(s);
		ItemHot.heat = heat;
		this.baseTexturePath = s;
		this.overlayTexturePath = s + "_hot";
	}
	
	public static ItemStack heatUp(ItemStack stack) {

		if(!(stack.getItem() instanceof ItemHot))
			return stack;

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("heat", heat);
		return stack;
	}

	public static ItemStack heatUp(ItemStack stack, double d) {

		if(!(stack.getItem() instanceof ItemHot))
			return stack;

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("heat", (int)(d * heat));
		return stack;
	}
	
	public static double getHeat(ItemStack stack) {

		if(!(stack.getItem() instanceof ItemHot))
			return 0;

		if(!stack.hasTagCompound())
			return 0;

		int h = stack.getTagCompound().getInteger("heat");

		return (double)h / (double)heat;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if(!world.isRemote && stack.hasTagCompound()) {

    		int h = stack.getTagCompound().getInteger("heat");

    		if(h > 0) {
    			stack.getTagCompound().setInteger("heat", h - 1);
    		} else {
    			stack.getTagCompound().removeTag("heat");
    		}
    	}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void bakeModel(ModelBakeEvent event) {
		try {
			IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));

			ResourceLocation baseSpriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath);
			ResourceLocation overlaySpriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + overlayTexturePath);

			IModel twoLayerModel = baseModel.retexture(
					ImmutableMap.of(
							"layer0", baseSpriteLoc.toString(),
							"layer1", overlaySpriteLoc.toString()
					)
			);

			IBakedModel bakedTwoLayer = twoLayerModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());

			TextureAtlasSprite baseSprite = ModelLoader.defaultTextureGetter().apply(baseSpriteLoc);
			TextureAtlasSprite overlaySprite = ModelLoader.defaultTextureGetter().apply(overlaySpriteLoc);

			List<BakedQuad> baseQuads = new ArrayList<>();
			List<BakedQuad> overlayQuadsTemplate = new ArrayList<>();

			for (BakedQuad q : bakedTwoLayer.getQuads(null, null, 0L)) {
				if (q.getSprite() == overlaySprite) {
					overlayQuadsTemplate.add(q);
				} else if (q.getSprite() == baseSprite) {
					baseQuads.add(q);
				} else {
					// Fallback: if sprite doesn't match, include in base
					baseQuads.add(q);
				}
			}

			TextureAtlasSprite particle = baseSprite;
			ModelHotBaked model = new ModelHotBaked(baseQuads, overlayQuadsTemplate, particle);
			ModelResourceLocation bakedModelLocation = new ModelResourceLocation(baseSpriteLoc, "inventory");
			event.getModelRegistry().putObject(bakedModelLocation, model);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		super.registerModel();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerSprite(TextureMap map) {
		super.registerSprite(map);
		map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + overlayTexturePath));
	}
	// Th3_Sl1ze: if you have a more elegant solution, you're free to change this shit
	// color handler won't give the same result, tried it already
	@SideOnly(Side.CLIENT)
	static class ModelHotBaked implements IBakedModel {

		private final List<BakedQuad> baseQuads;
		private final List<BakedQuad> overlayQuadsTemplate;
		private final TextureAtlasSprite particle;
		private final ItemOverrideList overrides;

		ModelHotBaked(List<BakedQuad> baseQuads, List<BakedQuad> overlayQuadsTemplate, TextureAtlasSprite particle) {
			this.baseQuads = baseQuads;
			this.overlayQuadsTemplate = overlayQuadsTemplate;
			this.particle = particle;

			this.overrides = new ItemOverrideList(ImmutableList.of()) {
				@Override
				public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
					double h = ItemHot.getHeat(stack);
					if (h <= 0.0D) {
						return new ModelHotBaked(baseQuads, ImmutableList.of(), particle);
					}
					int alpha = (int) Math.max(0, Math.min(255, Math.round(h * 255.0)));
					List<BakedQuad> overlay = recolorOverlayQuads(overlayQuadsTemplate, alpha);
					return new ModelHotBaked(baseQuads, overlay, particle);
				}
			};
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
			if (side != null) return ImmutableList.of();
			if (overlayQuadsTemplate.isEmpty()) {
				return baseQuads;
			}
			List<BakedQuad> out = new ArrayList<>(baseQuads.size() + overlayQuadsTemplate.size());
			out.addAll(baseQuads);
			out.addAll(overlayQuadsTemplate);
			return out;
		}

		@Override
		public boolean isAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean isGui3d() {
			return false;
		}

		@Override
		public boolean isBuiltInRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return particle;
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return BakedModelTransforms.defaultItemTransforms();
		}

		@Override
		public ItemOverrideList getOverrides() {
			return overrides;
		}

		private static List<BakedQuad> recolorOverlayQuads(List<BakedQuad> template, int alpha) {
			if (alpha <= 0) return ImmutableList.of();

			int color = ((alpha & 0xFF) << 24) | 0xFFFFFF;
			VertexFormat format = DefaultVertexFormats.ITEM;
			int strideInts = format.getIntegerSize();

			int colorOffsetInts = -1;
			List<VertexFormatElement> elements = format.getElements();
			for (int i = 0; i < elements.size(); i++) {
				VertexFormatElement e = elements.get(i);
				if (e.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
					colorOffsetInts = format.getOffset(i) / 4;
					break;
				}
			}

			if (colorOffsetInts < 0) {
				return template;
			}

			List<BakedQuad> result = new ArrayList<>(template.size());
			for (BakedQuad q : template) {
				int[] vd = q.getVertexData().clone();
				for (int v = 0; v < 4; v++) {
					int baseIndex = v * strideInts + colorOffsetInts;
					vd[baseIndex] = color;
				}
				BakedQuad nq = new BakedQuad(vd, q.getTintIndex(), q.getFace(), q.getSprite(), q.shouldApplyDiffuseLighting(), format);
				result.add(nq);
			}
			return result;
		}
	}
	
}
