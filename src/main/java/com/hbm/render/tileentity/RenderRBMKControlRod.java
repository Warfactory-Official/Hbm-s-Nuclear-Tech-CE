package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKControl;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKControlAuto;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKControlManual;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

@AutoRegister(tileentity = TileEntityRBMKControlManual.class)
@AutoRegister(tileentity = TileEntityRBMKControlAuto.class)
public class RenderRBMKControlRod extends TileEntitySpecialRenderer<TileEntityRBMKControl> {

	private final ResourceLocation[] textures = new ResourceLocation[] {
			new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_control_red.png"),
			new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_control_yellow.png"),
			new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_control_green.png"),
			new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_control_blue.png"),
			new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_control_purple.png")
	};
	private final ResourceLocation textureStandard = new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_control.png");
	private final ResourceLocation textureAuto = new ResourceLocation(Tags.MODID + ":textures/blocks/rbmk/rbmk_control_auto.png");

	@Override
	public void render(TileEntityRBMKControl te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		GlStateManager.pushMatrix();

		int offset = 1;
		BlockPos pos = te.getPos();
		Block blockType = te.getBlockType();

		for(int o = 1; o < 16; o++) {
			BlockPos checkPos = pos.up(o);
			if(te.getWorld().getBlockState(checkPos).getBlock() == blockType) {
				offset = o;
			} else {
				break;
			}
		}

		GlStateManager.translate(x + 0.5, y + offset, z + 0.5);

		GlStateManager.enableLighting();
		GlStateManager.enableCull();

		BlockPos lightPos = pos.up(offset + 1);
		int brightness = te.getWorld().getCombinedLight(lightPos, 0);
		int lX = brightness % 65536;
		int lY = brightness / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lX, (float)lY);

		ResourceLocation texture = textureAuto;

		if(te instanceof TileEntityRBMKControlManual crm) {
            if(crm.color == null) texture = textureStandard;
			else texture = textures[crm.color.ordinal()];
		}

		this.bindTexture(texture);

		double level = te.lastLevel + (te.level - te.lastLevel) * partialTicks;

		GlStateManager.translate(0.0, level, 0.0);
		ResourceManager.rbmk_rods_vbo.renderPart("Lid");

		GlStateManager.popMatrix();
	}
}

