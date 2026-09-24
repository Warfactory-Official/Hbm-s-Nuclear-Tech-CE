package com.hbm.render.entity.item;

import com.hbm.entity.item.EntityMovingItem;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

@AutoRegister(factory = "FACTORY")
public class RenderMovingItem extends Render<EntityMovingItem> {

	public static final IRenderFactory<EntityMovingItem> FACTORY = RenderMovingItem::new;

	private static final int CACHE_SIZE = 2048;
	private static final Map<ListKey, Integer> DISPLAY_LISTS = new LinkedHashMap<>(256, 0.75F, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<ListKey, Integer> eldest) {
			if(size() > CACHE_SIZE) {
				GLAllocation.deleteDisplayLists(eldest.getValue());
				return true;
			}
			return false;
		}
	};

	private final Random rand = new Random();

	protected RenderMovingItem(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityMovingItem item, double x, double y, double z, float entityYaw, float partialTicks) {

		ItemStack stack = item.getItemStack();
		if(stack.isEmpty()) return;

		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		IBakedModel model = renderItem.getItemModelWithOverrides(stack, item.world, null);
		int copies = getModelCount(stack.getCount());

		rand.setSeed(item.getEntityId());
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + rand.nextDouble() * 0.0625, z);
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		this.bindEntityTexture(item);

		if(model.isBuiltInRenderer() || stack.hasEffect()) {
			renderStack(renderItem, stack, model, copies);
		} else {
			ListKey key = new ListKey(model, stack.getItem(), stack.getMetadata(), stack.hasTagCompound() ? stack : null, copies);
			Integer list = DISPLAY_LISTS.get(key);

			if(list == null) {
				list = GLAllocation.generateDisplayLists(1);
				GlStateManager.glNewList(list, GL11.GL_COMPILE);
				renderStack(renderItem, stack, model, copies);
				GlStateManager.glEndList();
				DISPLAY_LISTS.put(key, list);
			}

			GlStateManager.callList(list);
			GlStateManager.resetColor();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private void renderStack(RenderItem renderItem, ItemStack stack, IBakedModel model, int copies) {

		rand.setSeed(187L);

		if(model.isGui3d()) {
			GlStateManager.scale(1.25F, 1.25F, 1.25F);
			GlStateManager.translate(0.0F, -0.06F, 0.0F);

			for(int i = 0; i < copies; i++) {
				GlStateManager.pushMatrix();
				if(i > 0) {
					GlStateManager.translate((rand.nextFloat() * 2F - 1F) * 0.2F, (rand.nextFloat() * 2F - 1F) * 0.2F, (rand.nextFloat() * 2F - 1F) * 0.2F);
				}
				renderItem.renderItem(stack, ForgeHooksClient.handleCameraTransforms(model, TransformType.GROUND, false));
				GlStateManager.popMatrix();
			}
		} else {
			GlStateManager.rotate(90F, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 0.0F, -0.03F);

			for(int i = 0; i < copies; i++) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0.0F, 0.0F, -0.084F * i);
				renderItem.renderItem(stack, ForgeHooksClient.handleCameraTransforms(model, TransformType.FIXED, false));
				GlStateManager.popMatrix();
			}
		}
	}

	private static int getModelCount(int count) {
		if(count > 40) return 5;
		if(count > 20) return 4;
		if(count > 5) return 3;
		if(count > 1) return 2;
		return 1;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMovingItem entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

	private record ListKey(IBakedModel model, Item item, int meta, ItemStack taggedStack, int copies) { }
}
