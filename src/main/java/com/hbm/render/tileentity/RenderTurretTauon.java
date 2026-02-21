package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import com.hbm.tileentity.turret.TileEntityTurretTauon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

@AutoRegister(tileentity = TileEntityTurretTauon.class)
public class RenderTurretTauon extends RenderTurretBase<TileEntityTurretTauon> implements IItemRendererProvider {

    @Override
    public void render(TileEntityTurretTauon turret, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Vec3d off = turret.byHorizontalIndexOffset();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + off.x, y, z + off.z);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        this.renderConnectors(turret, true, false, null);

        bindTexture(ResourceManager.turret_base_tex);
        ResourceManager.turret_chekhov.renderPart("Base");

        double yaw = -Math.toDegrees(turret.lastRotationYaw + (turret.rotationYaw - turret.lastRotationYaw) * partialTicks) - 90D;
        double pitch = Math.toDegrees(turret.lastRotationPitch + (turret.rotationPitch - turret.lastRotationPitch) * partialTicks);

        GL11.glRotated(yaw, 0, 1, 0);
        bindTexture(ResourceManager.turret_carriage_tex);
        ResourceManager.turret_chekhov.renderPart("Carriage");

        GlStateManager.translate(0, 1.5, 0);
        GL11.glRotated(pitch, 0, 0, 1);
        GlStateManager.translate(0, -1.5, 0);
        bindTexture(ResourceManager.turret_tauon_tex);
        ResourceManager.turret_tauon.renderPart("Cannon");

        if (turret.beam > 0) {
            GlStateManager.pushMatrix();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            GlStateManager.translate(0, 1.5D, 0);
            BeamPronter.prontBeam(new Vec3(new Vec3d(turret.lastDist, 0, 0)).toVec3d(), EnumWaveType.RANDOM, EnumBeamType.LINE, 0xffa200, 0xffd000, (int) turret.getWorld().getTotalWorldTime() / 5 % 360, (int) turret.lastDist + 1, 0.1F, 0, 0);
            GlStateManager.popMatrix();
        }

        float rot = turret.lastSpin + (turret.spin - turret.lastSpin) * partialTicks;
        GlStateManager.translate(0, 1.375, 0);
        GL11.glRotated(rot, -1, 0, 0);
        GlStateManager.translate(0, -1.375, 0);
        ResourceManager.turret_tauon.renderPart("Rotor");

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.turret_tauon);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2, 0);
                GlStateManager.scale(5, 5, 5);
            }

            public void renderCommon() {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.turret_base_tex);
                ResourceManager.turret_chekhov.renderPart("Base");
                bindTexture(ResourceManager.turret_carriage_tex);
                ResourceManager.turret_chekhov.renderPart("Carriage");
                bindTexture(ResourceManager.turret_tauon_tex);
                ResourceManager.turret_tauon.renderPart("Cannon");
                ResourceManager.turret_tauon.renderPart("Rotor");
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
