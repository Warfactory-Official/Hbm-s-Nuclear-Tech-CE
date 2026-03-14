package com.hbm.render.item;

import com.google.common.collect.ImmutableList;
import com.hbm.Tags;
import com.hbm.render.model.BakedModelTransforms;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;

import java.util.Objects;

public class TEISRBase extends TileEntityItemStackRenderer {

	public IBakedModel itemModel;
	public TransformType type;
	/** Can be null. */
	public EntityLivingBase entity;
	public World world;

	public ModelBinding createModelBinding(Item item) {
		if (item instanceof ItemBlock) {
			return ModelBinding.inventory(item, ItemCameraTransforms.DEFAULT);
		}
		return ModelBinding.inventory(item, BakedModelTransforms.defaultItemTransforms());
	}

	public IModel loadModel(Item item, ModelResourceLocation location) {
		return createModelBinding(item).loadModel();
	}

	public boolean useIdentityTransform(Item item) {
		return false;
	}

	public boolean doNullTransform(){
		return false;
	}

	public static final class ModelBinding {
		private static final ResourceLocation MISSINGNO = new ResourceLocation("missingno");
		private final ModelResourceLocation modelLocation;
		private final ItemCameraTransforms transforms;
		private final boolean useBaseModelInGui;
		private final ImmutableList<ResourceLocation> textureLayers;

		private ModelBinding(ModelResourceLocation modelLocation, ItemCameraTransforms transforms, boolean useBaseModelInGui, ImmutableList<ResourceLocation> textureLayers) {
			this.modelLocation = modelLocation;
			this.transforms = transforms;
			this.useBaseModelInGui = useBaseModelInGui;
			this.textureLayers = textureLayers;
		}

		public static ModelBinding inventory(Item item, ItemCameraTransforms transforms) {
			return synthetic(item, transforms, false, MISSINGNO);
		}

		public static ModelBinding inventoryWithGuiModel(Item item, ItemCameraTransforms transforms) {
			ResourceLocation itemId = Objects.requireNonNull(item.getRegistryName());
			return inventoryWithGuiModel(item, transforms, new ResourceLocation(itemId.getNamespace(), "items/" + itemId.getPath()));
		}

		public static ModelBinding of(ModelResourceLocation modelLocation, ItemCameraTransforms transforms, boolean useBaseModelInGui) {
			return new ModelBinding(modelLocation, transforms, useBaseModelInGui, ImmutableList.of(MISSINGNO));
		}

		public static ModelBinding inventoryModel(Item item, ItemCameraTransforms transforms, ResourceLocation... textureLayers) {
			return synthetic(item, transforms, false, textureLayers);
		}

		public static ModelBinding inventoryWithGuiModel(Item item, ItemCameraTransforms transforms, ResourceLocation... textureLayers) {
			return synthetic(item, transforms, true, textureLayers);
		}

		private static ModelBinding synthetic(Item item, ItemCameraTransforms transforms, boolean useBaseModelInGui, ResourceLocation... textureLayers) {
			ResourceLocation itemId = Objects.requireNonNull(item.getRegistryName());
			ModelResourceLocation syntheticLocation = new ModelResourceLocation(new ResourceLocation(Tags.MODID, "teisr/" + itemId.getNamespace() + "/" + itemId.getPath()), "inventory");
			return new ModelBinding(syntheticLocation, transforms, useBaseModelInGui, ImmutableList.copyOf(textureLayers));
		}

		public ModelResourceLocation getModelLocation() {
			return modelLocation;
		}

		public ItemCameraTransforms getTransforms() {
			return transforms;
		}

		public boolean useBaseModelInGui() {
			return useBaseModelInGui;
		}

		public IModel loadModel() {
			return new ItemLayerModel(textureLayers);
		}
	}
}
