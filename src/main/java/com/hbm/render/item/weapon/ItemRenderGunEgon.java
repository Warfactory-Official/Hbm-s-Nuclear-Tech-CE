package com.hbm.render.item.weapon;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.ItemGunEgon;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.main.ModEventHandlerClient;
import com.hbm.main.ResourceManager;
import com.hbm.particle.gluon.ParticleGluonMuzzleSmoke;
import com.hbm.render.item.TEISRBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_egon")
public class ItemRenderGunEgon extends TEISRBase {

	@Override
	public void renderByItem(ItemStack stack) {
		boolean prevBlend = RenderUtil.isBlendEnabled();
		int prevSrc = RenderUtil.getBlendSrcFactor();
		int prevDst = RenderUtil.getBlendDstFactor();
		int prevSrcAlpha = RenderUtil.getBlendSrcAlphaFactor();
		int prevDstAlpha = RenderUtil.getBlendDstAlphaFactor();
        GlStateManager.enableCull();
        switch(type){
        case FIRST_PERSON_LEFT_HAND:
            return;
        case FIRST_PERSON_RIGHT_HAND:
            GL11.glScaled(0.5, 0.4, 0.5);
            if(type == TransformType.FIRST_PERSON_RIGHT_HAND){
                GlStateManager.translate(-2, -0.5, 3);
                //GL11.glRotated(7, 0, 0, 1);
                GL11.glRotated(90, 1, 0, 0);
                GL11.glRotated(-90, 0, 0, 1);
            } else {
                GL11.glRotated(180, 0, 1, 0);
                GL11.glRotated(140, 0, 0, 1);
                GlStateManager.translate(4, 1, 1);
                GL11.glRotated(170, 0, 1, 0);
                GL11.glRotated(180, 1, 0, 0);
            }
            float time = Minecraft.getMinecraft().world.getTotalWorldTime() + MainRegistry.proxy.partialTicks();
            float fade = entity instanceof EntityPlayer ? ItemGunEgon.getFirstPersonAnimFade((EntityPlayer) entity) : 0;
            float[] offset = getOffset(time);
            float[] jitter = getJitter(time);
            GlStateManager.translate(offset[0]*fade-jitter[1]*fade*0.1F, offset[1]*fade*fade-jitter[0]*fade*0.05F, 0);
            GL11.glRotated(jitter[0]*fade, 1, 0, 0);
            GL11.glRotated(jitter[1]*fade, 0, 1, 0);
            float rec = -MathHelper.sin(Math.min(fade*1.5F, 1));
            GlStateManager.translate(0, 0, rec*1.5F);
            GL11.glRotated(7*rec, 1, 0, 0);
            break;
        case THIRD_PERSON_LEFT_HAND:
        case THIRD_PERSON_RIGHT_HAND:
        case HEAD:
        case FIXED:
        case GROUND:
            GlStateManager.translate(0.5, 0.55, 0.7);
            GL11.glRotated(180, 0, 1, 0);
            GL11.glScaled(0.125, 0.125, 0.125);
            break;
        case GUI:
            GlStateManager.translate(0.4, 0, 0.5);
            GL11.glScaled(0.15, 0.15, 0.15);
            GL11.glRotated(45, 0, 1, 0);
            GL11.glRotated(45, 1, 0, 0);
            break;
        case NONE:
            break;
        }
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.egon_hose_tex);
        ResourceManager.egon_hose.renderAllExcept("Screen");
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.egon_display_tex);
        ResourceManager.egon_hose.renderPart("Screen");
        GlStateManager.shadeModel(GL11.GL_FLAT);
        if(type == TransformType.FIRST_PERSON_RIGHT_HAND){
            //Ah yes, optimization but I can't be bothered to write good code at the same time
            GlStateManager.disableAlpha();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
            Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.gluon_muzzle_smoke);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            for(Particle p : ModEventHandlerClient.firstPersonAuxParticles){
                if(p instanceof ParticleGluonMuzzleSmoke && ((ParticleGluonMuzzleSmoke) p).tex == ResourceManager.gluon_muzzle_smoke)
                    p.renderParticle(Tessellator.getInstance().getBuffer(), entity, MainRegistry.proxy.partialTicks(), 0, 0, 0, 0, 0);
            }
            Tessellator.getInstance().draw();
            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.gluon_muzzle_glow);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
            for(Particle p : ModEventHandlerClient.firstPersonAuxParticles){
                if(p instanceof ParticleGluonMuzzleSmoke && ((ParticleGluonMuzzleSmoke) p).tex == ResourceManager.gluon_muzzle_glow)
                    p.renderParticle(Tessellator.getInstance().getBuffer(), entity, MainRegistry.proxy.partialTicks(), 0, 0, 0, 0, 0);
            }
            Tessellator.getInstance().draw();
            GlStateManager.enableAlpha();
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
        }
        GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcAlpha, prevDstAlpha);
        if (prevBlend) {
            GlStateManager.enableBlend();
        } else {
            GlStateManager.disableBlend();
        }
	}
	
	public static float[] getOffset(float time){
		float sinval = MathHelper.sin(time*0.15F)+MathHelper.sin(time*0.25F-10)+MathHelper.sin(time*0.1F+10);
		sinval/=3;
		float sinval2 = MathHelper.sin(time*0.1F)+MathHelper.sin(time*0.05F+20)+MathHelper.sin(time*0.13F+20);
		sinval/=3;
		return new float[]{BobMathUtil.remap((float) Library.smoothstep(sinval, -1, 1), 0, 1, -2, 1.5F), BobMathUtil.remap(sinval2, -1, 1, -0.03F, 0.05F)};
	}
	
	public static float[] getJitter(float time){
		float sinval = MathHelper.sin(time*0.8F)+MathHelper.sin(time*0.6F-10)+MathHelper.sin(time*0.9F+10);
		sinval/=3;
		float sinval2 = MathHelper.sin(time*0.3F)+MathHelper.sin(time*0.2F+20)+MathHelper.sin(time*0.1F+20);
		sinval/=3;
		return new float[]{BobMathUtil.remap(sinval, -1, 1, -3, 3), BobMathUtil.remap(sinval2, -1, 1, -1F, 1F)};
	}
}
