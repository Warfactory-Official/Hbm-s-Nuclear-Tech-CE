package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityLaunchpadLambda;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

import java.nio.DoubleBuffer;

@AutoRegister
public class RenderLaunchpadLambda extends TileEntitySpecialRenderer<TileEntityLaunchpadLambda> {

	private static DoubleBuffer buf = null;

	@Override
	public void render(TileEntityLaunchpadLambda launchpad, double x, double y, double z, float interp, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		if(buf == null) buf = GLAllocation.createDirectByteBuffer(8 * 4).asDoubleBuffer();

		float rotation = 0F;

		switch(launchpad.getBlockMetadata() - 10) {
		case 2: rotation = 90F; break;
		case 4: rotation = 180F; break;
		case 3: rotation = 270F; break;
		case 5: rotation = 0F; break;
		}

		GlStateManager.rotate(rotation, 0F, 1F, 0F);

		double doors = launchpad.getInterpPos(TileEntityLaunchpadLambda.INDEX_DOORS, interp);
		double erector = launchpad.getInterpPos(TileEntityLaunchpadLambda.INDEX_ERECTOR, interp) - 25D;
		double rotor = launchpad.getInterpPos(TileEntityLaunchpadLambda.INDEX_ROTOR, interp);
		double clamps = launchpad.getInterpPos(TileEntityLaunchpadLambda.INDEX_CLAMPS, interp);
		double pistons = launchpad.getInterpPos(TileEntityLaunchpadLambda.INDEX_PISTONS, interp);

		boolean renderRocket = launchpad.erecting || launchpad.erected;
		boolean rocketFixed = launchpad.erected;

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 2, 0);
		if(rocketFixed) {
			GlStateManager.rotate(-rotation, 0F, 1F, 0F);
			bindTexture(ResourceManager.lambda_rocket_tex);
			ResourceManager.lambda_rocket.renderAll();
		}
		GlStateManager.popMatrix();

		bindTexture(ResourceManager.launchpad_lambda_tex);
		ResourceManager.launchpad_lambda.renderPart("Silo");

		GlStateManager.pushMatrix(); {
			GL11.glEnable(GL11.GL_CLIP_PLANE0);
			buf.put(new double[] { 0, 0, -1, 6 }).rewind();
			GL11.glClipPlane(GL11.GL_CLIP_PLANE0, buf);

			GlStateManager.translate(0, 0, doors);
			ResourceManager.launchpad_lambda.renderPart("DoorLeft");
			GL11.glDisable(GL11.GL_CLIP_PLANE0);
		} GlStateManager.popMatrix();

		GlStateManager.pushMatrix(); {
			GL11.glEnable(GL11.GL_CLIP_PLANE0);
			buf.put(new double[] { 0, 0, 1, 6 }).rewind();
			GL11.glClipPlane(GL11.GL_CLIP_PLANE0, buf);

			GlStateManager.translate(0, 0, -doors);
			ResourceManager.launchpad_lambda.renderPart("DoorRight");
			GL11.glDisable(GL11.GL_CLIP_PLANE0);
		} GlStateManager.popMatrix();

		GL11.glEnable(GL11.GL_CLIP_PLANE0);
		buf.put(new double[] { 0, 1, 0, -0.25 }).rewind();
		GL11.glClipPlane(GL11.GL_CLIP_PLANE0, buf);

		GlStateManager.translate(0, erector, 0);

		ResourceManager.launchpad_lambda.renderPart("Erector");

		GlStateManager.translate(0, 13.25, 0);
		GlStateManager.rotate((float) rotor, 1, 0, 0);
		GlStateManager.translate(0, -13.25, 0);

		ResourceManager.launchpad_lambda.renderPart("Rotor");

		GlStateManager.pushMatrix(); {
			GlStateManager.translate(3.5, 19.75, 0);
			GlStateManager.rotate((float) -clamps, 0, 0, 1);
			GlStateManager.translate(-3.5, -19.75, 0);
			ResourceManager.launchpad_lambda.renderPart("PivotUpper1");
			GlStateManager.translate(pistons, 0, 0);
			ResourceManager.launchpad_lambda.renderPart("ClampUpper1");
		} GlStateManager.popMatrix();

		GlStateManager.pushMatrix(); {
			GlStateManager.translate(3.5, 6.75, 0);
			GlStateManager.rotate((float) clamps, 0, 0, 1);
			GlStateManager.translate(-3.5, -6.75, 0);
			ResourceManager.launchpad_lambda.renderPart("PivotLower1");
			GlStateManager.translate(pistons, 0, 0);
			ResourceManager.launchpad_lambda.renderPart("ClampLower1");
		} GlStateManager.popMatrix();

		GlStateManager.pushMatrix(); {
			GlStateManager.translate(-3.5, 19.75, 0);
			GlStateManager.rotate((float) clamps, 0, 0, 1);
			GlStateManager.translate(3.5, -19.75, 0);
			ResourceManager.launchpad_lambda.renderPart("PivotUpper2");
			GlStateManager.translate(-pistons, 0, 0);
			ResourceManager.launchpad_lambda.renderPart("ClampUpper2");
		} GlStateManager.popMatrix();

		GlStateManager.pushMatrix(); {
			GlStateManager.translate(-3.5, 6.75, 0);
			GlStateManager.rotate((float) -clamps, 0, 0, 1);
			GlStateManager.translate(3.5, -6.75, 0);
			ResourceManager.launchpad_lambda.renderPart("PivotLower2");
			GlStateManager.translate(-pistons, 0, 0);
			ResourceManager.launchpad_lambda.renderPart("ClampLower2");
		} GlStateManager.popMatrix();

		GlStateManager.translate(0, 2, 0);

		if(renderRocket && !rocketFixed) {
			GlStateManager.rotate(-rotation, 0F, 1F, 0F);
			bindTexture(ResourceManager.lambda_rocket_tex);
			ResourceManager.lambda_rocket.renderAll();
		}

		GL11.glDisable(GL11.GL_CLIP_PLANE0);

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}
}
