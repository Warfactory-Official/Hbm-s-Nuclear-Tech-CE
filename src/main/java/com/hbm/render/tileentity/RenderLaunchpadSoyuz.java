package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.SoyuzPronter;
import com.hbm.tileentity.machine.TileEntityLaunchpadSoyuz;
import com.hbm.tileentity.machine.TileEntityLaunchpadSoyuz.SoyuzStatus;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderLaunchpadSoyuz extends TileEntitySpecialRenderer<TileEntityLaunchpadSoyuz> implements IItemRendererProvider {

	@Override
	public void render(TileEntityLaunchpadSoyuz launchpad, double x, double y, double z, float interp, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		float rotation = 0F;

		switch(launchpad.getBlockMetadata() - 10) {
		case 2: rotation = 90F; break;
		case 4: rotation = 180F; break;
		case 3: rotation = 270F; break;
		case 5: rotation = 0F; break;
		}

		GlStateManager.rotate(rotation, 0F, 1F, 0F);

		GlStateManager.translate(-4, 0, -4);

		float rotor = MathHelper.clamp(launchpad.getInterpPos(TileEntityLaunchpadSoyuz.INDEX_ROTOR, interp) * -180F + 180F, 0F, 180F);
		float carriage = MathHelper.clamp(launchpad.getInterpPos(TileEntityLaunchpadSoyuz.INDEX_CARRIAGE, interp) * -19.5F + 19.5F, 0F, 19.5F);
		float wheels = (float) (carriage * 360D / Math.PI);
		float tilt = launchpad.getInterpPos(TileEntityLaunchpadSoyuz.INDEX_TILT, interp);

		boolean renderSoyuz = launchpad.loadedType >= 0 && launchpad.soyuzStatus != SoyuzStatus.ABSENT;
		boolean lockSoyuz = launchpad.soyuzStatus == SoyuzStatus.LAUNCHING;

		if(renderSoyuz && lockSoyuz) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 4, 0);
			GlStateManager.rotate(rotation, 0F, -1F, 0F);
			SoyuzPronter.prontSoyuz(launchpad.loadedType);
			GlStateManager.popMatrix();
		}

		bindTexture(ResourceManager.launchpad_soyuz_tex);

		ResourceManager.launchpad_soyuz.renderPart("Launchpad");

		for(int i = 1; i <= 5; i++) {
			GlStateManager.pushMatrix();
			float ext = i == 5 ? 3F : 4.5F;
			float strut = MathHelper.clamp(launchpad.getInterpPos(i - 1, interp) * -ext + ext, 0F, ext);
			GlStateManager.translate(0, 0, strut);
			ResourceManager.launchpad_soyuz.renderPart("Strut" + i);
			GlStateManager.popMatrix();
		}

		GlStateManager.translate(0, 0, -carriage);

		GlStateManager.translate(0, 1.5, -32);
		GlStateManager.rotate(-tilt, 1, 0, 0);
		GlStateManager.translate(0, -1.5, 32);

		ResourceManager.launchpad_soyuz.renderPart("Carriage");

		double[] wheelsForward = new double[] {17D, 19D, 29D, 31D};
		double[] wheelsSide = new double[] {6.75D, 5.25D, -5.25D, -6.75D};

		for(int i = 1; i <= 4; i++) for(int j = 1; j <= 4; j++) {
			GlStateManager.pushMatrix();
			double v0 = wheelsForward[i - 1];
			double v1 = wheelsSide[j - 1];

			GlStateManager.translate(v1, 0, -v0);
			GlStateManager.rotate(wheels * (j % 2 == 0 ? -1 : 1), 0, 1, 0);
			GlStateManager.translate(-v1, 0, v0);

			ResourceManager.launchpad_soyuz.renderPart("Wheel_" + i + "_" + j);
			GlStateManager.popMatrix();
		}

		GlStateManager.translate(0, 24.5, -18);
		GlStateManager.rotate(-rotor, 1, 0, 0);
		GlStateManager.translate(0, -24.5, 18);

		ResourceManager.launchpad_soyuz.renderPart("Rotor");

		GlStateManager.translate(0, 24.5, -6);
		GlStateManager.rotate(rotor, 1, 0, 0);
		GlStateManager.translate(0, -24.5, 6);

		ResourceManager.launchpad_soyuz.renderPart("Mount");

		if(renderSoyuz && !lockSoyuz) {
			GlStateManager.translate(0, 4, 0);
			GlStateManager.rotate(rotation, 0F, -1F, 0F);
			SoyuzPronter.prontSoyuz(launchpad.loadedType);
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.launchpad_soyuz);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GlStateManager.translate(0, -4, 0);
				GlStateManager.scale(3, 3, 3);
			}

			public void renderCommon() {
				GlStateManager.rotate(90, 0F, 1F, 0F);
				GlStateManager.scale(1D / 48D, 1D / 48D, 1D / 48D);
				GlStateManager.translate(-0.5D, 0D, 21.375D);
				GlStateManager.shadeModel(GL11.GL_SMOOTH);

				bindTexture(ResourceManager.launchpad_soyuz_tex);
				ResourceManager.launchpad_soyuz.renderPart("Launchpad");

				for(int i = 1; i <= 5; i++) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(0, 0, i == 5 ? 3F : 4.5F);
					ResourceManager.launchpad_soyuz.renderPart("Strut" + i);
					GlStateManager.popMatrix();
				}

				GlStateManager.shadeModel(GL11.GL_FLAT);
			}
		};
	}
}
