package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Vec3NT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister(item = "gun_mk108")
public class ItemRenderMK108 extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public float getViewFOV(ItemStack stack, float fov) {
        float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
        return fov * (1 - aimingProgress * 0.33F);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0, 0, 0.875);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1F * offset, -1.5F * offset, 2.5F * offset,
                -0.75F, -0.75F, 1.5F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {
        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.mk108_tex);
        double scale = 0.375D;
        GlStateManager.scale(scale, scale, scale);

        boolean doesYeet = HbmAnimationsSedna.getRelevantAnim(0) != null && HbmAnimationsSedna.getRelevantAnim(0).animation.getBus("GRENH1") != null;
        boolean doesCycle = HbmAnimationsSedna.getRelevantAnim(0) != null && HbmAnimationsSedna.getRelevantAnim(0).animation.getBus("CYCLE") != null;
        boolean reloading = HbmAnimationsSedna.getRelevantAnim(0) != null && HbmAnimationsSedna.getRelevantAnim(0).animation.getBus("BELT") != null;
        boolean useShellCount = HbmAnimationsSedna.getRelevantAnim(0) != null && HbmAnimationsSedna.getRelevantAnim(0).animation.getBus("SHELLS") != null;
        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] cycle = HbmAnimationsSedna.getRelevantTransformation("CYCLE");
        double[] barrel = HbmAnimationsSedna.getRelevantTransformation("BARREL");
        double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
        double[] lid = HbmAnimationsSedna.getRelevantTransformation("LID");
        double[] belt = HbmAnimationsSedna.getRelevantTransformation("BELT");
        double[] drum = HbmAnimationsSedna.getRelevantTransformation("DRUM");
        double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
        double[] shellCount = HbmAnimationsSedna.getRelevantTransformation("SHELLS");

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        if(doesYeet) {
            double[][] horizontal = new double[][] {
                    HbmAnimationsSedna.getRelevantTransformation("GRENH1"),
                    HbmAnimationsSedna.getRelevantTransformation("GRENH2"),
                    HbmAnimationsSedna.getRelevantTransformation("GRENH3"),
            };
            double[][] vertical = new double[][] {
                    HbmAnimationsSedna.getRelevantTransformation("GRENV1"),
                    HbmAnimationsSedna.getRelevantTransformation("GRENV2"),
                    HbmAnimationsSedna.getRelevantTransformation("GRENV3"),
            };
            double[][] spin = new double[][] {
                    HbmAnimationsSedna.getRelevantTransformation("GRENS1"),
                    HbmAnimationsSedna.getRelevantTransformation("GRENS2"),
                    HbmAnimationsSedna.getRelevantTransformation("GRENS3"),
            };

            for(int i = 0; i < 3; i++) {
                if(horizontal[i][0] <= -4) continue;
                GlStateManager.pushMatrix();
                GlStateManager.translate(horizontal[i][0], vertical[i][1], 0);
                GlStateManager.translate(0, 0, -2.3125);
                GlStateManager.rotate(-90, 1, 0, 0);
                GlStateManager.rotate((float) -spin[i][0], 0, 1, 0);
                GlStateManager.translate(0, 0, 2.3125);
                ResourceManager.mk108.renderPart("Grenade");
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.translate(0, -1, -8);
        GlStateManager.rotate((float) equip[0], 1, 0, 0);
        GlStateManager.translate(0, 1, 8);

        GlStateManager.translate(0, 1, -4);
        GlStateManager.rotate((float) lift[0], 1, 0, 0);
        GlStateManager.translate(0, -1, 4);

        GlStateManager.translate(0, 0, recoil[2]);

        ResourceManager.mk108.renderPart("Gun");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, barrel[2] * 2);
        ResourceManager.mk108.renderPart("Barrel");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.6875, -1);
        GlStateManager.rotate((float) lid[0], 1, 0, 0);
        GlStateManager.translate(0, -0.6875, 1);
        ResourceManager.mk108.renderPart("Lid");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();

        GlStateManager.translate(drum[0], drum[1], drum[2]);
        ResourceManager.mk108.renderPart("Drum");

        double p = 0.0625D;
        double x = p * 22;
        double y = p * -46;
        double angle = 0;
        Vec3NT vec = new Vec3NT(0, 0.53125, 0);

        double[] anglesLoaded = new double[]   {0,   0,  -5,   0,   -5,  60,  45,  -10,   0};
        double[] anglesUnloaded = new double[] {0, -30, -60, -45, -45,   0,   0,   0,   0};
        double[][] shells = new double[anglesLoaded.length][3];
        double reloadProgress = !reloading ? 1D : belt[0];
        double cycleProgress = !doesCycle ? 1 : cycle[0];

        for(int i = 0; i < anglesLoaded.length; i++) {
            shells[i][0] = x;
            shells[i][1] = y;
            shells[i][2] = angle - 90;
            double delta = BobMathUtil.interp(anglesUnloaded[i], anglesLoaded[i], reloadProgress);
            angle += delta;
            vec.rotateAroundZDeg(-delta);
            x += vec.x;
            y += vec.y;
        }

        int shellAmount = useShellCount ? (int) shellCount[0] : gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, null);

        // draw belt, interp used for cycling (shells will transform towards the position/rotation of the next shell)
        for(int i = 0; i < shells.length - 1; i++) {
            double[] prevShell = shells[i];
            double[] nextShell = shells[i + 1];
            renderShell(prevShell[0], nextShell[0], prevShell[1], nextShell[1], prevShell[2], nextShell[2], shells.length - i < shellAmount + 2, cycleProgress);
        }
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 8.125);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.rotate((float) (90 * gun.shotRand), 1, 0, 0);
        this.renderMuzzleFlash(gun.lastShot[0], 50, 5);
        GlStateManager.popMatrix();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        double scale = 2.0D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(1, -2.5, 4);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 1.375D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.translate(0, 0.5, 0.25);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -9.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, 0.5, -0.25);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.mk108_tex);
        ResourceManager.mk108.renderPart("Gun");
        ResourceManager.mk108.renderPart("Barrel");
        ResourceManager.mk108.renderPart("Lid");
        ResourceManager.mk108.renderPart("Drum");

        GlStateManager.pushMatrix();

        double p = 0.0625D;
        double x = p * 22;
        double y = p * -46;
        double angle = 0;
        Vec3NT vec = new Vec3NT(0, 0.53125, 0);

        double[] anglesLoaded = new double[] { 0, 0, -5, 0, -5, 60, 45, -10, 0 };
        double[][] shells = new double[anglesLoaded.length][3];

        for(int i = 0; i < anglesLoaded.length; i++) {
            shells[i][0] = x;
            shells[i][1] = y;
            shells[i][2] = angle - 90;
            double delta = anglesLoaded[i];
            angle += delta;
            vec.rotateAroundZDeg(-delta);
            x += vec.x;
            y += vec.y;
        }

        // draw belt, interp used for cycling (shells will transform towards the position/rotation of the next shell)
        for(int i = 0; i < shells.length - 1; i++) {
            double[] prevShell = shells[i];
            double[] nextShell = shells[i + 1];
            renderShell(prevShell[0], nextShell[0], prevShell[1], nextShell[1], prevShell[2], nextShell[2], true, 0F);
        }
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    public static void renderShell(double x0, double x1, double y0, double y1, double rot0, double rot1, boolean shell, double interp) {
        renderShell(BobMathUtil.interp(x0, x1, interp), BobMathUtil.interp(y0, y1, interp), BobMathUtil.interp(rot0, rot1, interp), shell);
    }

    public static void renderShell(double x, double y, double rot, boolean shell) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.rotate((float) rot, 0, 0, 1);
        ResourceManager.mk108.renderPart("Belt");
        if(shell) ResourceManager.mk108.renderPart("Grenade");
        GlStateManager.popMatrix();
    }
}
