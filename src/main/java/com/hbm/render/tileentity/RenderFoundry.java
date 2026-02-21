package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.machine.IRenderFoundry;
import com.hbm.tileentity.machine.TileEntityFoundryCastingBase;
import com.hbm.wiaj.WorldInAJar;
import com.hbm.wiaj.actors.ITileActorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;
@AutoRegister
public class RenderFoundry extends TileEntitySpecialRenderer<TileEntityFoundryCastingBase> implements ITileActorRenderer {
	
	public static final ResourceLocation lava = new ResourceLocation(Tags.MODID, "textures/models/machines/lava_gray.png");

	private static void drawItem(ItemStack stack, double height, World world) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5D, height, 0.5D);

		if(!(stack.getItem() instanceof ItemBlock)) {
			GlStateManager.rotate(-180, 0F, 1F, 0F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
		} else {
			GlStateManager.translate(0, -0.352, 0);
		}
		
		double scale = 24D / 16D;
	GlStateManager.scale(scale, scale, scale);
		
		GlStateManager.rotate(90, 1, 0, 0);
		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, world, null);
		model = ForgeHooksClient.handleCameraTransforms(model, TransformType.FIXED, false);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
		GlStateManager.enableAlpha();

		GlStateManager.popMatrix();
	}
	@Override
	public void render(TileEntityFoundryCastingBase te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		IRenderFoundry foundry = (IRenderFoundry) te;

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

        ItemStackHandler inv = te.inventory;

        ItemStack mold = inv.getStackInSlot(0);
        if (!mold.isEmpty()) {
            drawItem(mold, foundry.moldHeight(), te.getWorld());
        }

        ItemStack out = inv.getStackInSlot(1);
        if (!out.isEmpty()) {
            drawItem(out, foundry.outHeight(), te.getWorld());
        }

        if (foundry.shouldRender()) {
			int hex = foundry.getMat().moltenColor;
			Color color = new Color(hex);

			GlStateManager.pushMatrix();
			GlStateManager.disableCull();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			GlStateManager.disableLighting();
			ITileActorRenderer.bindTexture(lava);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1.0F);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			float yLevel = (float) foundry.getLevel();
			float minX = (float) foundry.minX();
			float maxX = (float) foundry.maxX();
			float minZ = (float) foundry.minZ();
			float maxZ = (float) foundry.maxZ();

			// Bottom left
			buffer.pos(minX, yLevel, minZ).tex(minZ, maxX).endVertex();
			// Top left
			buffer.pos(minX, yLevel, maxZ).tex(maxZ, maxX).endVertex();
			// Top right
			buffer.pos(maxX, yLevel, maxZ).tex(maxZ, minX).endVertex();
			// Bottom right
			buffer.pos(maxX, yLevel, minZ).tex(minZ, minX).endVertex();

			tessellator.draw();

			GlStateManager.enableCull();
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}

	@Override
	public void renderActor(WorldInAJar world, int ticks, float interp, NBTTagCompound data) {
		int x = data.getInteger("x");
		int y = data.getInteger("y");
		int z = data.getInteger("z");
		render((TileEntityFoundryCastingBase) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z))), x, y, z, interp, 0, 0);
	}

	@Override
	public void updateActor(int ticks, NBTTagCompound data) { }
}
