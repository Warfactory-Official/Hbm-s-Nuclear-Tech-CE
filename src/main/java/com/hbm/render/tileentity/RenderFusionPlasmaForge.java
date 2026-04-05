package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.fusion.TileEntityFusionPlasmaForge;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderFusionPlasmaForge extends TileEntitySpecialRenderer<TileEntityFusionPlasmaForge> implements IItemRendererProvider {

    public static EntityItem dummy;

    @Override
    public void render(TileEntityFusionPlasmaForge forge, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.rotate(90F, 0F, 1F, 0F);

        switch(forge.getBlockMetadata() - BlockDummyable.offset) {
            case 2: GlStateManager.rotate(90F, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(180F, 0F, 1F, 0F); break;
            case 3: GlStateManager.rotate(270F, 0F, 1F, 0F); break;
            case 5: break;
            default: break;
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        if(forge.connected) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-2D, 0D, 0D);
            bindTexture(ResourceManager.fusion_torus_tex);
            ResourceManager.fusion_torus.renderPart("Bolts1");
            GlStateManager.popMatrix();
        }

        bindTexture(ResourceManager.fusion_plasma_forge_tex);
        ResourceManager.fusion_plasma_forge.renderPart("Body");

        renderPlasma(forge);
        renderRecipeItem(forge);

        bindTexture(ResourceManager.fusion_plasma_forge_tex);

        double[] striker = forge.armStriker.getPositions(partialTicks);
        double[] jet = forge.armJet.getPositions(partialTicks);
        double rotor = BobMathUtil.interp(forge.prevRing, forge.ring, partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(rotor, 0F, 1F, 0F);
        renderStrikerArm(striker);
        renderJetArm(jet);
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    private void renderStrikerArm(double[] striker) {
        GlStateManager.pushMatrix();
        ResourceManager.fusion_plasma_forge.renderPart("SliderStriker");
        GlStateManager.translate(-2.75D, 2.5D, 0D);
        GlStateManager.rotate((float) -striker[0], 0F, 0F, 1F);
        GlStateManager.translate(2.75D, -2.5D, 0D);
        ResourceManager.fusion_plasma_forge.renderPart("ArmLowerStriker");
        GlStateManager.translate(-2.75D, 3.75D, 0D);
        GlStateManager.rotate((float) -striker[1], 0F, 0F, 1F);
        GlStateManager.translate(2.75D, -3.75D, 0D);
        ResourceManager.fusion_plasma_forge.renderPart("ArmUpperStriker");
        GlStateManager.translate(-1.5D, 3.75D, 0D);
        GlStateManager.rotate((float) -striker[2], 0F, 0F, 1F);
        GlStateManager.translate(1.5D, -3.75D, 0D);
        ResourceManager.fusion_plasma_forge.renderPart("StrikerMount");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0D, 3.375D, 0.5D);
        GlStateManager.rotate((float) striker[3], 1F, 0F, 0F);
        GlStateManager.translate(0D, -3.375D, -0.5D);
        ResourceManager.fusion_plasma_forge.renderPart("StrikerRight");
        GlStateManager.translate(0D, -striker[4], 0D);
        ResourceManager.fusion_plasma_forge.renderPart("PistonRight");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0D, 3.375D, -0.5D);
        GlStateManager.rotate((float) -striker[3], 1F, 0F, 0F);
        GlStateManager.translate(0D, -3.375D, 0.5D);
        ResourceManager.fusion_plasma_forge.renderPart("StrikerLeft");
        GlStateManager.translate(0D, -striker[5], 0D);
        ResourceManager.fusion_plasma_forge.renderPart("PistonLeft");
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    private void renderJetArm(double[] jet) {
        GlStateManager.pushMatrix();
        ResourceManager.fusion_plasma_forge.renderPart("SliderJet");
        GlStateManager.translate(2.75D, 2.5D, 0D);
        GlStateManager.rotate((float) jet[0], 0F, 0F, 1F);
        GlStateManager.translate(-2.75D, -2.5D, 0D);
        ResourceManager.fusion_plasma_forge.renderPart("ArmLowerJet");
        GlStateManager.translate(2.75D, 3.75D, 0D);
        GlStateManager.rotate((float) jet[1], 0F, 0F, 1F);
        GlStateManager.translate(-2.75D, -3.75D, 0D);
        ResourceManager.fusion_plasma_forge.renderPart("ArmUpperJet");
        GlStateManager.translate(1.5D, 3.75D, 0D);
        GlStateManager.rotate((float) jet[2], 0F, 0F, 1F);
        GlStateManager.translate(-1.5D, -3.75D, 0D);
        ResourceManager.fusion_plasma_forge.renderPart("Jet");
        GlStateManager.popMatrix();
    }

    private void renderPlasma(TileEntityFusionPlasmaForge forge) {
        if(forge.plasmaEnergySync <= 0) {
            GlStateManager.color(0F, 0F, 0F, 1F);
            GlStateManager.disableTexture2D();
            ResourceManager.fusion_plasma_forge.renderPart("Plasma");
            GlStateManager.enableTexture2D();
            GlStateManager.color(1F, 1F, 1F, 1F);
            return;
        }

        long time = Clock.get_ms() + forge.timeOffset;
        float alpha = 0.5F + (float) (Math.sin(time / 500D) * 0.25D);
        double mainOsc = BobMathUtil.sps(time / 750D) % 1D;
        double glowOsc = Math.sin(time / 1000D) % 1D;
        double glowExtra = time / 10000D % 1D;

        RenderArcFurnace.fullbright(true);
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();

        GlStateManager.color(forge.plasmaRed * alpha, forge.plasmaGreen * alpha, forge.plasmaBlue * alpha, 1F);
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        bindTexture(ResourceManager.fusion_plasma_tex);
        GlStateManager.translate(0D, mainOsc, 0D);
        ResourceManager.fusion_plasma_forge.renderPart("Plasma");
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);

        GlStateManager.color(forge.plasmaRed * 2F, forge.plasmaGreen * 2F, forge.plasmaBlue * 2F, 1F);
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        bindTexture(ResourceManager.fusion_plasma_glow_tex);
        GlStateManager.translate(0D, glowOsc + glowExtra, 0D);
        ResourceManager.fusion_plasma_forge.renderPart("Plasma");
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        RenderArcFurnace.fullbright(false);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private void renderRecipeItem(TileEntityFusionPlasmaForge forge) {
        if(forge.plasmaModule.getRecipe() == null) return;
        if(MainRegistry.proxy.me().getDistanceSq(forge.getPos().getX() + 0.5D, forge.getPos().getY() + 1D, forge.getPos().getZ() + 0.5D) > 35D * 35D) return;

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90F, 0F, 1F, 0F);
        GlStateManager.translate(0D, 1.75D, 0D);

        ItemStack stack = forge.plasmaModule.getRecipe().getIcon().copy();
        stack.setCount(1);

        if(stack.getItem() instanceof ItemBlock) {
            boolean is3D = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, forge.getWorld(), null).isGui3d();
            if(is3D) {
                GlStateManager.translate(0D, -0.0625D, 0D);
            } else {
                GlStateManager.scale(0.5D, 0.5D, 0.5D);
            }
        } else {
            GlStateManager.rotate(90F, 0F, 1F, 0F);
        }

        GlStateManager.translate(0D, Math.sin((MainRegistry.proxy.me().ticksExisted + partialTicks()) * 0.1D) * 0.0625D, 0D);
        GlStateManager.scale(1.5D, 1.5D, 1.5D);

        if(dummy == null || dummy.world != forge.getWorld()) dummy = new EntityItem(forge.getWorld(), 0D, 0D, 0D, stack);
        dummy.setItem(stack);
        dummy.hoverStart = 0.0F;
        Minecraft.getMinecraft().getRenderManager().renderEntity(dummy, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, false);
        GlStateManager.popMatrix();
    }

    private float partialTicks() {
        return Minecraft.getMinecraft().getRenderPartialTicks();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.fusion_plasma_forge);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.translate(0D, -1D, 0D);
                GlStateManager.scale(2.75D, 2.75D, 2.75D);
                GlStateManager.rotate(90F, 0F, 1F, 0F);
            }

            @Override
            public void renderCommon() {
                GlStateManager.scale(0.5D, 0.5D, 0.5D);
                GlStateManager.rotate(90F, 0F, 1F, 0F);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.fusion_plasma_forge_tex);
                ResourceManager.fusion_plasma_forge.renderAllExcept("Plasma");

                GlStateManager.disableTexture2D();
                GlStateManager.color(0F, 0F, 0F, 1F);
                ResourceManager.fusion_plasma_forge.renderPart("Plasma");
                GlStateManager.color(1F, 1F, 1F, 1F);
                GlStateManager.enableTexture2D();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
