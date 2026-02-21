package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBobble;
import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.blocks.generic.BlockBobble.TileEntityBobble;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import com.hbm.render.item.ItemRenderBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderBobble extends TileEntitySpecialRenderer<TileEntityBobble> implements IItemRendererProvider {

    public static RenderBobble instance = new RenderBobble();

    public static final IModelCustom bobble = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/trinkets/bobble.obj")).asVBO();
    public static final ResourceLocation socket = new ResourceLocation(Tags.MODID, "textures/models/trinkets/socket.png");
    public static final ResourceLocation glow = new ResourceLocation(Tags.MODID, "textures/models/trinkets/glow.png");
    public static final ResourceLocation lamp = new ResourceLocation(Tags.MODID, "textures/blocks/fluorescent_lamp.png");

    public static final ResourceLocation bobble_vaultboy = new ResourceLocation(Tags.MODID, "textures/models/trinkets/vaultboy.png");
    public static final ResourceLocation bobble_hbm = new ResourceLocation(Tags.MODID, "textures/models/trinkets/hbm.png");
    public static final ResourceLocation bobble_pu238 = new ResourceLocation(Tags.MODID, "textures/models/trinkets/pellet.png");
    public static final ResourceLocation bobble_frizzle = new ResourceLocation(Tags.MODID, "textures/models/trinkets/frizzle.png");
    public static final ResourceLocation bobble_vt = new ResourceLocation(Tags.MODID, "textures/models/trinkets/vt.png");
    public static final ResourceLocation bobble_doc = new ResourceLocation(Tags.MODID, "textures/models/trinkets/doctor17ph.png");
    public static final ResourceLocation bobble_blue = new ResourceLocation(Tags.MODID, "textures/models/trinkets/thebluehat.png");
    public static final ResourceLocation bobble_pheo = new ResourceLocation(Tags.MODID, "textures/models/trinkets/pheo.png");
    public static final ResourceLocation bobble_adam = new ResourceLocation(Tags.MODID, "textures/models/trinkets/adam29.png");
    public static final ResourceLocation bobble_uffr = new ResourceLocation(Tags.MODID, "textures/models/trinkets/uffr.png");
    public static final ResourceLocation bobble_vaer = new ResourceLocation(Tags.MODID, "textures/models/trinkets/vaer.png");
    public static final ResourceLocation bobble_nos = new ResourceLocation(Tags.MODID, "textures/models/trinkets/nos.png");
    public static final ResourceLocation bobble_drillgon = new ResourceLocation(Tags.MODID, "textures/models/trinkets/drillgon200.png");
    public static final ResourceLocation bobble_cirno = new ResourceLocation(Tags.MODID, "textures/models/trinkets/cirno.png");
    public static final ResourceLocation bobble_microwave = new ResourceLocation(Tags.MODID, "textures/models/trinkets/microwave.png");
    public static final ResourceLocation bobble_peep = new ResourceLocation(Tags.MODID, "textures/models/trinkets/peep.png");
    public static final ResourceLocation bobble_mellow = new ResourceLocation(Tags.MODID, "textures/models/trinkets/mellowrpg8.png");
    public static final ResourceLocation bobble_mellow_glow = new ResourceLocation(Tags.MODID, "textures/models/trinkets/mellowrpg8_glow.png");
    public static final ResourceLocation bobble_abel = new ResourceLocation(Tags.MODID, "textures/models/trinkets/abel.png");
    public static final ResourceLocation bobble_abel_glow = new ResourceLocation(Tags.MODID, "textures/models/trinkets/abel_glow.png");

    private long time;

    @Override
    public void render(TileEntityBobble te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        time = System.currentTimeMillis();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);

        double scale = 0.25D;
        GlStateManager.scale(scale, scale, scale);

        BobbleType type = te.type;

        int rot = te.getBlockMetadata();
        GlStateManager.rotate(22.5F * rot + 90F, 0F, -1F, 0F);

        renderBobble(type);

        GlStateManager.popMatrix();
    }

    public void renderBobble(BobbleType type) {
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();

        bindTexture(socket);
        bobble.renderPart("Socket");

        switch (type) {
            case STRENGTH:
            case PERCEPTION:
            case ENDURANCE:
            case CHARISMA:
            case INTELLIGENCE:
            case AGILITY:
            case LUCK:
                bindTexture(bobble_vaultboy); break;
            case BOB: bindTexture(bobble_hbm); break;
            case PU238: bindTexture(bobble_pu238); break;
            case FRIZZLE: bindTexture(bobble_frizzle); break;
            case VT: bindTexture(bobble_vt); break;
            case DOC: bindTexture(bobble_doc); break;
            case BLUEHAT: bindTexture(bobble_blue); break;
            case PHEO: bindTexture(bobble_pheo); break;
            case CIRNO: bindTexture(bobble_cirno); break;
            case ADAM29: bindTexture(bobble_adam); break;
            case UFFR: bindTexture(bobble_uffr); break;
            case VAER: bindTexture(bobble_vaer); break;
            case NOS: bindTexture(bobble_nos); break;
            case DRILLGON: bindTexture(bobble_drillgon); break;
            case MICROWAVE: bindTexture(bobble_microwave); break;
            case PEEP: bindTexture(bobble_peep); break;
            case MELLOW: bindTexture(bobble_mellow); break;
            case ABEL: bindTexture(bobble_abel); break;
            default: bindTexture(ResourceManager.universal);
        }

        switch (type) {
            case PU238: renderPellet(type); break;
            case UFFR: renderFumo(type); break;
            case DRILLGON: renderDrillgon(type); break;
            default: renderGuy(type);
        }

        GlStateManager.pushMatrix();
        renderPost(type);
        GlStateManager.popMatrix();

        renderSocket(type);
    }

    /* RENDER STANDARD PLAYER MODEL */
    public static double[] rotLeftArm = {0, 0, 0};
    public static double[] rotRightArm = {0, 0, 0};
    public static double[] rotLeftLeg = {0, 0, 0};
    public static double[] rotRightLeg = {0, 0, 0};
    public static double rotBody = 0;
    public static double[] rotHead = {0, 0, 0};

    public void resetFigurineRotation() {
        rotLeftArm = new double[]{0, 0, 0};
        rotRightArm = new double[]{0, 0, 0};
        rotLeftLeg = new double[]{0, 0, 0};
        rotRightLeg = new double[]{0, 0, 0};
        rotBody = 0;
        rotHead = new double[]{0, 0, 0};
    }

    @SuppressWarnings("incomplete-switch")
    public void setupFigurineRotation(BobbleType type) {
        switch (type) {
            case STRENGTH:
                rotLeftArm = new double[]{0, 25, 135};
                rotRightArm = new double[]{0, -45, 135};
                rotLeftLeg = new double[]{0, 0, -5};
                rotRightLeg = new double[]{0, 0, 5};
                rotHead = new double[]{15, 0, 0};
                break;
            case PERCEPTION:
                rotLeftArm = new double[]{0, -15, 135};
                rotRightArm = new double[]{-5, 0, 0};
                break;
            case ENDURANCE:
                rotBody = 45;
                rotLeftArm = new double[]{0, -25, 30};
                rotRightArm = new double[]{0, 45, 30};
                rotHead = new double[]{0, -45, 0};
                break;
            case CHARISMA:
                rotBody = 45;
                rotRightArm = new double[]{0, -45, 90};
                rotLeftLeg = new double[]{0, 0, -5};
                rotRightLeg = new double[]{0, 0, 5};
                rotHead = new double[]{-5, -45, 0};
                break;
            case INTELLIGENCE:
                rotHead = new double[]{0, 30, 0};
                rotLeftArm = new double[]{5, 0, 0};
                rotRightArm = new double[]{15, 0, 170};
                break;
            case AGILITY:
                rotLeftArm = new double[]{0, 0, 60};
                rotRightArm = new double[]{0, 0, -45};
                rotLeftLeg = new double[]{0, 0, -15};
                rotRightLeg = new double[]{0, 0, 45};
                break;
            case LUCK:
                rotLeftArm = new double[]{135, 45, 0};
                rotRightArm = new double[]{-135, -45, 0};
                rotRightLeg = new double[]{-5, 0, 0};
                break;
            case VT:
                rotLeftArm = new double[]{0, -45, 60};
                rotRightArm = new double[]{0, 0, 45};
                rotLeftLeg = new double[]{2, 0, 0};
                rotRightLeg = new double[]{-2, 0, 0};
                break;
            case BLUEHAT:
                rotLeftArm = new double[]{0, 90, 60};
                break;
            case FRIZZLE:
                rotLeftArm = new double[]{0, 15, 45};
                rotRightArm = new double[]{0, 0, 80};
                rotLeftLeg = new double[]{0, 0, 2};
                rotRightLeg = new double[]{0, 0, -2};
                break;
            case ADAM29:
                rotRightArm = new double[]{0, 0, 60};
                break;
            case PHEO:
                rotLeftArm = new double[]{0, 0, 80};
                rotRightArm = new double[]{0, 0, 45};
                break;
            case VAER:
                rotLeftArm = new double[]{0, -5, 45};
                rotRightArm = new double[]{0, 15, 45};
                break;
            case PEEP:
                rotLeftArm = new double[]{0, 0, 1};
                rotRightArm = new double[]{0, 0, 1};
                break;
            case MELLOW:
                rotLeftArm = new double[]{0, 10, 0};
                rotRightArm = new double[]{0, -10, 0};
                rotLeftLeg = new double[]{3, 5, 2};
                rotRightLeg = new double[]{-3, -5, 0};
                break;
            case ABEL:
                rotLeftArm = new double[]{0, 80, 90};
                rotRightArm = new double[]{0, -80, 90};
                break;
        }
    }

    public void renderGuy(BobbleType type) {
        resetFigurineRotation();
        setupFigurineRotation(type);

        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) rotBody, 0, 1, 0);

        if (type == BobbleType.PEEP) bobble.renderPart("PeepTail");

        GlStateManager.disableCull();

        String suffix = type.skinLayers ? "" : "17";

        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(1F, 1F, 1F, 1F);

        // LEFT LEG
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1, -0.125);
        GlStateManager.rotate((float) rotLeftLeg[0], 1, 0, 0);
        GlStateManager.rotate((float) rotLeftLeg[1], 0, 1, 0);
        GlStateManager.rotate((float) rotLeftLeg[2], 0, 0, 1);
        GlStateManager.translate(0, -1, 0.125);
        bobble.renderPart("LL" + suffix);
        GlStateManager.popMatrix();

        // RIGHT LEG
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1, 0.125);
        GlStateManager.rotate((float) rotRightLeg[0], 1, 0, 0);
        GlStateManager.rotate((float) rotRightLeg[1], 0, 1, 0);
        GlStateManager.rotate((float) rotRightLeg[2], 0, 0, 1);
        GlStateManager.translate(0, -1, -0.125);
        bobble.renderPart("RL" + suffix);
        GlStateManager.popMatrix();

        // LEFT ARM
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.625, -0.25);
        GlStateManager.rotate((float) rotLeftArm[0], 1, 0, 0);
        GlStateManager.rotate((float) rotLeftArm[1], 0, 1, 0);
        GlStateManager.rotate((float) rotLeftArm[2], 0, 0, 1);
        GlStateManager.translate(0, -1.625, 0.25);
        bobble.renderPart("LA" + suffix);
        GlStateManager.popMatrix();

        // RIGHT ARM
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.625, 0.25);
        GlStateManager.rotate((float) rotRightArm[0], 1, 0, 0);
        GlStateManager.rotate((float) rotRightArm[1], 0, 1, 0);
        GlStateManager.rotate((float) rotRightArm[2], 0, 0, 1);
        GlStateManager.translate(0, -1.625, -0.25);
        bobble.renderPart("RA" + suffix);
        GlStateManager.popMatrix();

        // BODY
        bobble.renderPart("Body" + suffix);

        // HEAD (light bobble)
        double speed = 0.005;
        double amplitude = 1;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.75, 0);
        GlStateManager.rotate((float) (Math.sin(time * speed) * amplitude), 1, 0, 0);
        GlStateManager.rotate((float) (Math.sin(time * speed + (Math.PI * 0.5)) * amplitude), 0, 0, 1);

        GlStateManager.rotate((float) rotHead[0], 1, 0, 0);
        GlStateManager.rotate((float) rotHead[1], 0, 1, 0);
        GlStateManager.rotate((float) rotHead[2], 0, 0, 1);

        GlStateManager.translate(0, -1.75, 0);
        bobble.renderPart("Head" + suffix);

        if (type == BobbleType.VT) bobble.renderPart("Horn");
        if (type == BobbleType.PEEP) bobble.renderPart("PeepHat");

        if (type == BobbleType.VAER) {
            GlStateManager.translate(0.25, 1.9, 0.075);
            GlStateManager.rotate(-60, 0, 0, 1);
            GlStateManager.scale(0.5, 0.5, 0.5);
            this.renderItem(new ItemStack(ModItems.cigarette));
        }

        if (type == BobbleType.NOS) {
            GlStateManager.translate(0, 1.75, 0);
            GlStateManager.rotate(180, 1, 0, 0);
            double s = 0.095D;
            GlStateManager.scale(s, s, s);
            this.bindTexture(ResourceManager.hat);
            ResourceManager.armor_hat.renderAll();
        }

        GlStateManager.popMatrix();

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableBlend();

        GlStateManager.enableCull();

        GlStateManager.popMatrix();
    }

    public void renderPellet(BobbleType type) {
        GlStateManager.enableCull();

        GlStateManager.pushMatrix();
        // Bright pass for glow layer
        float lastX = net.minecraft.client.renderer.OpenGlHelper.lastBrightnessX;
        float lastY = net.minecraft.client.renderer.OpenGlHelper.lastBrightnessY;
        net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, 240F, 240F);
        GlStateManager.disableLighting();
        bobble.renderPart("Pellet");

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(1F, 1F, 0F, 0.1F + (float) Math.sin(time * 0.001D) * 0.05F);
        bobble.renderPart("PelletShine");
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();

        GlStateManager.enableLighting();
        net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, lastX, lastY);
        GlStateManager.popMatrix();
    }

    public void renderFumo(BobbleType type) {
        GlStateManager.enableCull();
        bobble.renderPart("Fumo");

        double speed = 0.005;
        double amplitude = 1;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.75, 0);
        GlStateManager.rotate((float) (Math.sin(time * speed) * amplitude), 1, 0, 0);
        GlStateManager.rotate((float) (Math.sin(time * speed + (Math.PI * 0.5)) * amplitude), 0, 0, 1);
        GlStateManager.translate(0, -0.75, 0);

        GlStateManager.disableCull();
        bobble.renderPart("FumoHead");

        GlStateManager.popMatrix();
    }

    public void renderDrillgon(BobbleType type) {
        bobble.renderPart("Drillgon");
    }

    private final ResourceLocation shot_tex = new ResourceLocation(Tags.MODID + ":textures/models/ModelUboinik.png");

    /* RENDER ADDITIONAL ITEMS */
    @SuppressWarnings("incomplete-switch")
    public void renderPost(BobbleType type) {
        switch (type) {
            case BLUEHAT: {
                double s = 0.0625D;
                GlStateManager.translate(0D, 0.875D, -0.5D);
                GlStateManager.rotate(-90, 0, 1, 0);
                GlStateManager.rotate(-160, 0, 0, 1);
                GlStateManager.scale(s, s, s);
                bindTexture(ResourceManager.hev_helmet);
                ResourceManager.armor_hev.renderPart("Head");
                break;
            }
            case FRIZZLE: {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.8, 1.6, 0.4);
                GlStateManager.scale(0.125, 0.125, 0.125);
                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.rotate(10, 1, 0, 0);
                this.bindTexture(ResourceManager.n_i_4_n_i_tex);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                ResourceManager.n_i_4_n_i.renderPart("FrameDark");
                ResourceManager.n_i_4_n_i.renderPart("Grip");
                ResourceManager.n_i_4_n_i.renderPart("FrameLight");
                ResourceManager.n_i_4_n_i.renderPart("Cylinder");
                ResourceManager.n_i_4_n_i.renderPart("Barrel");
                GlStateManager.shadeModel(GL11.GL_FLAT);
                GlStateManager.popMatrix();

                GlStateManager.translate(0.3, 1.4, -0.2);
                GlStateManager.rotate(-100, 1, 0, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);
//                renderItem(new ItemStack(ModItems.weapon_mod_special, 1, EnumModSpecial.DOUBLOONS.ordinal())); // TODO
                break;
            }
            case ADAM29: {
                GlStateManager.translate(0.4, 1.15, 0.4);
                GlStateManager.scale(0.5, 0.5, 0.5);
                renderItem(new ItemStack(ModItems.can_redbomb));
                break;
            }
            case PHEO: {
                GlStateManager.translate(0.5, 1.15, 0.45);
                GlStateManager.rotate(-60, 1, 0, 0);
                GlStateManager.scale(2, 2, 2);
                this.bindTexture(ResourceManager.shimmer_axe_tex);
                ResourceManager.shimmer_axe.renderAll();
                break;
            }
            case BOB: {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                this.bindTexture(ResourceManager.mini_nuke_tex);
                GlStateManager.scale(0.5, 0.5, 0.5);
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.75, 1, 0.9);
                for (int i = 0; i < 3; i++) {
                    ResourceManager.projectiles.renderPart("MiniNuke");
                    GlStateManager.translate(-0.75, 0, 0);
                }
                GlStateManager.popMatrix();
                this.bindTexture(ResourceManager.mini_mirv_tex);
                GlStateManager.translate(0, 0.75, -0.9);
                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.rotate(90, 1, 0, 0);
                ResourceManager.projectiles.renderPart("MiniMIRV");
                GlStateManager.shadeModel(GL11.GL_FLAT);
                break;
            }
            case VAER: {
                this.bindTexture(shot_tex);
                GlStateManager.translate(0.6, 1.5, 0);
                GlStateManager.rotate(140, 0, 0, 1);
                GlStateManager.rotate(-60, 0, 1, 0);
                GlStateManager.translate(-0.2, 0, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);
                // shotgun.renderDud(0.0625F);
                break;
            }
            case MELLOW: {
                float lastX = net.minecraft.client.renderer.OpenGlHelper.lastBrightnessX;
                float lastY = net.minecraft.client.renderer.OpenGlHelper.lastBrightnessY;
                net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, 240F, 240F);
                bindTexture(bobble_mellow_glow);
                renderGuy(type);
                GlStateManager.enableBlend();
                GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                this.bindTexture(lamp);
                bobble.renderPart("Fluoro");
                this.bindTexture(glow);
                bobble.renderPart("Glow");
                GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
                GlStateManager.disableBlend();
                net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, lastX, lastY);
                break;
            }
            case ABEL: {
                float lastX = net.minecraft.client.renderer.OpenGlHelper.lastBrightnessX;
                float lastY = net.minecraft.client.renderer.OpenGlHelper.lastBrightnessY;
                net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, 240F, 240F);
                bindTexture(bobble_abel_glow);
                renderGuy(type);
                net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, lastX, lastY);
                break;
            }
        }
    }

    private void renderItem(ItemStack stack) {
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
    }

    public void renderOrigin() {
        GlStateManager.disableCull();
        GlStateManager.disableTexture2D();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        double d = 0.125D;
        // red diamond
        float r = 1F, g = 0F, b = 0F, a = 1F;
        buf.pos(0, d, 0).color(r, g, b, a).endVertex();
        buf.pos(d, 0, 0).color(r, g, b, a).endVertex();
        buf.pos(0, 0, d).color(r, g, b, a).endVertex();

        buf.pos(0, d, 0).color(r, g, b, a).endVertex();
        buf.pos(-d, 0, 0).color(r, g, b, a).endVertex();
        buf.pos(0, 0, d).color(r, g, b, a).endVertex();

        buf.pos(0, d, 0).color(r, g, b, a).endVertex();
        buf.pos(d, 0, 0).color(r, g, b, a).endVertex();
        buf.pos(0, 0, -d).color(r, g, b, a).endVertex();

        buf.pos(0, d, 0).color(r, g, b, a).endVertex();
        buf.pos(-d, 0, 0).color(r, g, b, a).endVertex();
        buf.pos(0, 0, -d).color(r, g, b, a).endVertex();

        buf.pos(0, -d, 0).color(r, g, b, a).endVertex();
        buf.pos(d, 0, 0).color(r, g, b, a).endVertex();
        buf.pos(0, 0, d).color(r, g, b, a).endVertex();

        buf.pos(0, -d, 0).color(r, g, b, a).endVertex();
        buf.pos(d, 0, 0).color(r, g, b, a).endVertex();
        buf.pos(0, 0, -d).color(r, g, b, a).endVertex();

        buf.pos(0, -d, 0).color(r, g, b, a).endVertex();
        buf.pos(-d, 0, 0).color(r, g, b, a).endVertex();
        buf.pos(0, 0, d).color(r, g, b, a).endVertex();

        buf.pos(0, -d, 0).color(r, g, b, a).endVertex();
        buf.pos(-d, 0, 0).color(r, g, b, a).endVertex();
        buf.pos(0, 0, -d).color(r, g, b, a).endVertex();

        tess.draw();
        GlStateManager.enableTexture2D();
    }

    /* RENDER BASE PEDESTAL */
    public void renderSocket(BobbleType type) {
        GlStateManager.disableLighting();
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        float f3 = 0.01F;
        GlStateManager.translate(0.63, 0.175F, 0.0);
        GlStateManager.scale(f3, -f3, f3);
        GlStateManager.translate(0, 0, font.getStringWidth(type.label) * 0.5D);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.depthMask(false);
        GlStateManager.translate(0, 1, 0);
        font.drawString(type.label, 0, 0, type == BobbleType.VT ? 0xff0000 : 0xffffff);
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
    }

    @Override
    protected void bindTexture(ResourceLocation loc) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.bobblehead);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -3.5, 0);
                GlStateManager.scale(10, 10, 10);
            }

            public void renderCommon(ItemStack stack) {
                GlStateManager.scale(0.5, 0.5, 0.5);
                RenderBobble.instance.renderBobble(BobbleType.VALUES[stack.getItemDamage()]);
            }

            public void renderGround() {
                GlStateManager.scale(5.0, 5.0, 5.0);
            }

            public void renderFirstPersonRightHand() {
                GlStateManager.scale(2.0, 2.0, 2.0);
                GlStateManager.rotate(45, 0, 1, 0);
            }
        };
    }
}
