package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.network.TileEntityPipeAnchor;
import com.hbm.util.ColorUtil;
import com.hbm.util.Compat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderPipeAnchor extends TileEntitySpecialRenderer<TileEntityPipeAnchor> implements IItemRendererProvider {

    @Override
    public void render(TileEntityPipeAnchor anchor, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.pushMatrix();
        switch(anchor.getBlockMetadata()) {
            case 0: GlStateManager.rotate(180, 1, 0, 0); break;
            case 1: break;
            case 2: GlStateManager.rotate(90, 1, 0, 0); GlStateManager.rotate(180, 0, 0, 1); break;
            case 3: GlStateManager.rotate(90, 1, 0, 0); break;
            case 4: GlStateManager.rotate(90, 1, 0, 0); GlStateManager.rotate(90, 0, 0, 1); break;
            case 5: GlStateManager.rotate(90, 1, 0, 0); GlStateManager.rotate(270, 0, 0, 1); break;
        }

        GlStateManager.translate(0, -0.5F, 0);
        bindTexture(ResourceManager.pipe_anchor_tex);
        ResourceManager.pipe_anchor.renderPart("Anchor");
        GlStateManager.popMatrix();

        for(int[] pos : anchor.getConnected()) {
            TileEntity tile = Compat.getTileStandard(anchor.getWorld(), pos[0], pos[1], pos[2]);
            if(tile instanceof TileEntityPipeAnchor other) {
                if(anchor.getType() != other.getType()) continue;
                Vec3d anchorPoint = anchor.getConnectionPoint();
                Vec3d connectionPoint = other.getConnectionPoint();

                if(isDominant(anchorPoint, connectionPoint)) {
                    double dX = connectionPoint.x - anchorPoint.x;
                    double dY = connectionPoint.y - anchorPoint.y;
                    double dZ = connectionPoint.z - anchorPoint.z;

                    double hyp = Math.sqrt(dX * dX + dZ * dZ);
                    double yaw = Math.toDegrees(Math.atan2(dX, dZ));
                    double pitch = Math.toDegrees(Math.atan2(dY, hyp));
                    double length = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(yaw, 0, 1, 0);
                    GlStateManager.rotate(90 - pitch, 1, 0, 0);

                    GlStateManager.pushMatrix();
                    GlStateManager.scale(1, length, 1);
                    GlStateManager.translate(0, -0.5, 0);
                    int color = ColorUtil.lightenColor(anchor.getType().getColor(), 0.25D);
                    GlStateManager.color(ColorUtil.fr(color), ColorUtil.fg(color), ColorUtil.fb(color));
                    ResourceManager.pipe_anchor.renderPart("Pipe");
                    GlStateManager.color(1F, 1F, 1F);
                    GlStateManager.popMatrix();

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(0, length / 2D - 1.5, 0);
                    ResourceManager.pipe_anchor.renderPart("Ring");
                    GlStateManager.popMatrix();

                    GlStateManager.popMatrix();
                }
            }
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    /** Determines the "dominant" anchor, i.e. the one that should render the pipe (instead of rendering two half segments, gives marginally better performance) */
    public static boolean isDominant(Vec3d first, Vec3d second) {
        if(first.x < second.x) return true;
        if(first.x > second.x) return false;
        if(first.y < second.y) return true;
        if(first.y > second.y) return false;
        return first.z < second.z;// exact same pos? no need to render anything
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.pipe_anchor);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -3.5, 0);
                double scale = 10;
                GlStateManager.scale(scale, scale, scale);
            }
            public void renderCommon() {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.pipe_anchor_tex);
                ResourceManager.pipe_anchor.renderPart("Anchor");
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }};
    }
}
