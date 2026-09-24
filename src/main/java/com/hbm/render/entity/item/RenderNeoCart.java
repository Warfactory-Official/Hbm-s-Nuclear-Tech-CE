package com.hbm.render.entity.item;

import com.hbm.entity.cart.EntityMinecartNTM;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class RenderNeoCart extends Render<EntityMinecartNTM> {

    public RenderNeoCart(RenderManager manager) {
        super(manager);
    }

    @Override
    public void doRender(@NotNull EntityMinecartNTM cart, double x, double y, double z, float rot, float interp) {

        GlStateManager.pushMatrix();
        this.bindEntityTexture(cart);
        long rand = (long) cart.getEntityId() * 493286711L;
        rand = rand * rand * 4392167121L + rand * 98761L;
        float randX = (((float) (rand >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float randY = (((float) (rand >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float randZ = (((float) (rand >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        GlStateManager.translate(randX, randY, randZ);
        double interpX = cart.lastTickPosX + (cart.posX - cart.lastTickPosX) * (double) interp;
        double interpY = cart.lastTickPosY + (cart.posY - cart.lastTickPosY) * (double) interp;
        double interpZ = cart.lastTickPosZ + (cart.posZ - cart.lastTickPosZ) * (double) interp;
        double mult = 0.3;
        Vec3d vec3 = cart.getPos(interpX, interpY, interpZ);
        float interpPitch = cart.prevRotationPitch + (cart.rotationPitch - cart.prevRotationPitch) * interp;

        if(vec3 != null) {
            Vec3d vec31 = cart.getPosOffset(interpX, interpY, interpZ, mult);
            Vec3d vec32 = cart.getPosOffset(interpX, interpY, interpZ, -mult);

            if(vec31 == null) {
                vec31 = vec3;
            }

            if(vec32 == null) {
                vec32 = vec3;
            }

            x += vec3.x - interpX;
            y += (vec31.y + vec32.y) / 2.0D - interpY;
            z += vec3.z - interpZ;
            Vec3d vec33 = vec32.add(-vec31.x, -vec31.y, -vec31.z);

            if(vec33.length() != 0.0D) {
                vec33 = vec33.normalize();
                rot = (float) (Math.atan2(vec33.z, vec33.x) * 180.0D / Math.PI);
                interpPitch = (float) (Math.atan(vec33.y) * 73.0D);
            }
        }

        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(180.0F - rot, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-interpPitch, 0.0F, 0.0F, 1.0F);
        float interpRoll = (float) cart.getRollingAmplitude() - interp;
        float interpDamage = cart.getDamage() - interp;

        if(interpDamage < 0.0F) {
            interpDamage = 0.0F;
        }

        GlStateManager.translate(0, -0.0625F, 0);
        GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F);
        ResourceManager.cart.renderPart("Carriage");

        if(interpRoll > 0.0F) {
            GlStateManager.translate(0, 0.75F, 0);
            GlStateManager.rotate(MathHelper.sin(interpRoll) * interpRoll * interpDamage / 10.0F * (float) cart.getRollingDirection(), 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0, -0.75F, 0);
        }

        ResourceManager.cart.renderPart("Bucket");
        cart.renderSpecialContent(this);

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(@NotNull EntityMinecartNTM entity) {
        switch(entity.getBase()) {
            case PAINTED: return ResourceManager.cart_metal;
            case WOOD: return ResourceManager.cart_wood;
            default: return ResourceManager.cart_blank;
        }
    }

    @Override
    public void bindTexture(@NotNull ResourceLocation loc) {
        this.renderManager.renderEngine.bindTexture(loc);
    }
}
