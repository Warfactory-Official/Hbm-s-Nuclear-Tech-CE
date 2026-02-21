package com.hbm.render.tileentity;

import com.hbm.blocks.machine.MachineICFController;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityICFController;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

@AutoRegister
public class RenderICFController extends TileEntitySpecialRenderer<TileEntityICFController> {

    @Override
    public void render(TileEntityICFController controller, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        if (controller.laserLength > 0) {
            float lastLightmapX = OpenGlHelper.lastBrightnessX;
            float lastLightmapY = OpenGlHelper.lastBrightnessY;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            GlStateManager.rotate(90, 0F, 1F, 0F);
            IBlockState state = controller.getWorld().getBlockState(controller.getPos());
            EnumFacing facing = state.getValue(MachineICFController.FACING);
            switch (facing) {
                case SOUTH -> GlStateManager.rotate(180, 0F, 1F, 0F);
                case EAST -> GlStateManager.rotate(270, 0F, 1F, 0F);
                case WEST -> GlStateManager.rotate(90, 0F, 1F, 0F);
            }
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            GlStateManager.disableTexture2D();
            BeamPronter.prontBeam(new Vec3d(controller.laserLength, 0, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x202020,
                    0x100000, 0, 1, 0F, 10, 0.125F);
            GlStateManager.enableTexture2D();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastLightmapX, lastLightmapY);
            GlStateManager.popMatrix();
        }
    }
}
