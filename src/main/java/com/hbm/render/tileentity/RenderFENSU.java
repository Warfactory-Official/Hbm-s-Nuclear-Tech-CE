package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineFENSU;
import com.hbm.util.RenderUtil;
import com.hbm.wiaj.WorldInAJar;
import com.hbm.wiaj.actors.ITileActorRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;
@Deprecated
@AutoRegister
public class RenderFENSU extends TileEntitySpecialRenderer<TileEntityMachineFENSU> implements IItemRendererProvider, ITileActorRenderer {

    @Override
    public void render(TileEntityMachineFENSU fensu, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        GlStateManager.translate((float) x + 0.5F, (float) y, (float) z + 0.5F);

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch (fensu.getBlockMetadata() - BlockDummyable.offset) {
            case 2 -> GlStateManager.rotate(90, 0F, 1F, 0F);
            case 4 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(270, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(0, 0F, 1F, 0F);
        }


        bindTexture(ResourceManager.fensu_tex[fensu.color.getMetadata()]);
        ResourceManager.fensu.renderPart("Base");
        float rot = fensu.prevRotation + (fensu.rotation - fensu.prevRotation) * partialTicks;

        GlStateManager.translate(0, 2.5, 0);
        GL11.glRotated(rot, 1, 0, 0);
        GlStateManager.translate(0, -2.5, 0);
        ResourceManager.fensu.renderPart("Disc");

        GlStateManager.pushMatrix();

        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        ResourceManager.fensu.renderPart("Lights");
        GlStateManager.enableCull();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public void renderActor(WorldInAJar world, int ticks, float interp, NBTTagCompound data) {
        double x = data.getDouble("x");
        double y = data.getDouble("y");
        double z = data.getDouble("z");
        int rotation = data.getInteger("rotation");
        float lastSpin = data.getFloat("lastSpin");
        float spin = data.getFloat("spin");

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch (rotation) {
            case 3:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
            case 2:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
        }

        ITileActorRenderer.bindTexture(ResourceManager.fensu_tex[0]);
        ResourceManager.fensu.renderPart("Base");

        float rot = lastSpin + (spin - lastSpin) * interp;

        GlStateManager.translate(0, 2.5, 0);
        GL11.glRotated(rot, 1, 0, 0);
        GlStateManager.translate(0, -2.5, 0);
        ResourceManager.fensu.renderPart("Disc");
        ResourceManager.fensu.renderPart("Lights");
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public void updateActor(int ticks, NBTTagCompound data) {

        float lastSpin = 0;
        float spin = data.getFloat("spin");
        float speed = data.getFloat("speed");

        lastSpin = spin;
        spin += speed;

        if (spin >= 360) {
            lastSpin -= 360;
            spin -= 360;
        }

        data.setFloat("lastSpin", lastSpin);
        data.setFloat("spin", spin);
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_fensu);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.translate(0, -2, 0);
                GlStateManager.scale(2.5, 2.5, 2.5);
            }

            public void renderCommon() {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.fensu_tex[3]);
                ResourceManager.fensu.renderPart("Base");
                ResourceManager.fensu.renderPart("Disc");
                boolean prevLighting = RenderUtil.isLightingEnabled();
                if (prevLighting) GlStateManager.disableLighting();
                GlStateManager.disableCull();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
                ResourceManager.fensu.renderPart("Lights");
                if (prevLighting) GlStateManager.enableLighting();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
