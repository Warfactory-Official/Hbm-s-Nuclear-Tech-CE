package com.hbm.render.item;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.grenade.ItemGrenadeFilling.EnumGrenadeFilling;
import com.hbm.items.weapon.grenade.ItemGrenadeFuze.EnumGrenadeFuze;
import com.hbm.items.weapon.grenade.ItemGrenadeShell.EnumGrenadeShell;
import com.hbm.items.weapon.grenade.ItemGrenadeUniversal;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.tileentity.RenderArcFurnace;
import com.hbm.util.ColorUtil;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@AutoRegister(item = "grenade_universal")
public class ItemRenderGrenade extends TEISRBase {

    // 1.7-exact frames need identity binding (see ItemRenderFrames17 / boltgun); the default TEISRBase
    // binding pre-multiplies defaultItemTransforms(), which the old per-context ops used to cancel.
    @Override
    public ModelBinding createModelBinding(Item item) {
        return ModelBinding.inventory(item, ItemCameraTransforms.DEFAULT);
    }

    @Override
    public boolean useIdentityTransform(Item item) {
        return true;
    }

    @Override
    public void renderByItem(ItemStack stack) {
        final boolean prevCull  = RenderUtil.isCullEnabled();
        final int     prevShade = RenderUtil.getShadeModel();

        GlStateManager.pushMatrix();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        // Each context plays the verbatim 1.7 ItemRenderGrenade body on top of the ItemRenderFrames17 base
        // frame for that context (which reproduces the raw 1.7 render space); the old preambles here were
        // compensating for the pre-e6fceb4a9 non-identity TEISR binding.
        switch (type) {
            case FIRST_PERSON_RIGHT_HAND:
            case FIRST_PERSON_LEFT_HAND: {
                EnumHand hand = type == TransformType.FIRST_PERSON_RIGHT_HAND ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                GlStateManager.multMatrix(type == TransformType.FIRST_PERSON_LEFT_HAND ? ItemRenderFrames17.FIRST_PERSON_LEFT : ItemRenderFrames17.FIRST_PERSON);
                renderFirstPerson(stack, hand);
                break;
            }
            case THIRD_PERSON_RIGHT_HAND:
            case THIRD_PERSON_LEFT_HAND:
            case HEAD:
                // 1.7 EQUIPPED: scale(0.125), translate(3,1,-0.5)
                GlStateManager.multMatrix(type == TransformType.HEAD ? ItemRenderFrames17.HEAD
                        : type == TransformType.THIRD_PERSON_LEFT_HAND ? ItemRenderFrames17.THIRD_PERSON_LEFT
                        : ItemRenderFrames17.THIRD_PERSON);
                GlStateManager.scale(0.125, 0.125, 0.125);
                GlStateManager.translate(3, 1, -0.5);
                renderGrenade(stack, type);
                break;
            case GROUND:
                // 1.7 ENTITY: scale(0.125)
                GlStateManager.multMatrix(ItemRenderFrames17.GROUND);
                GlStateManager.scale(0.125, 0.125, 0.125);
                renderGrenade(stack, type);
                break;
            case FIXED:
                // 1.7 ENTITY body rendered in the item frame: scale(0.125)
                GlStateManager.multMatrix(ItemRenderFrames17.FIXED);
                GlStateManager.scale(0.125, 0.125, 0.125);
                renderGrenade(stack, type);
                break;
            case GUI:
                // 1.7 INVENTORY
                GlStateManager.enableLighting();
                GlStateManager.multMatrix(ItemRenderFrames17.GUI);
                GlStateManager.translate(8, 8, 0);
                GlStateManager.scale(-1, -1, -1);
                GlStateManager.rotate(45F, 0F, 0F, 1F);
                GlStateManager.rotate(150F, 0F, 1F, 0F);
                GlStateManager.rotate(15F, 1F, 0F, 0F);
                renderGrenade(stack, type);
                break;
            default:
                break;
        }

        GlStateManager.shadeModel(prevShade);
        if (!prevCull) GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }

    public static void renderFirstPerson(ItemStack stack, EnumHand hand) {
        EnumGrenadeShell shell = ItemGrenadeUniversal.getShell(stack);

        // 1.7 renderFirstPerson: scale(0.125), translate(3,1,-3), rotate(180,0,-1,0)
        GlStateManager.scale(0.125, 0.125, 0.125);
        GlStateManager.translate(3, 1, -3);
        GlStateManager.rotate(180F, 0F, -1F, 0F);

        double[] bodyMove = HbmAnimations.getRelevantTransformation("BODYMOVE", hand);
        double[] bodyTurn = HbmAnimations.getRelevantTransformation("BODYTURN", hand);
        double[] ringMove = HbmAnimations.getRelevantTransformation("RINGMOVE", hand);
        double[] ringTurn = HbmAnimations.getRelevantTransformation("RINGTURN", hand);
        double[] renderRing = HbmAnimations.getRelevantTransformation("RENDERRING", hand);
        GlStateManager.translate(bodyMove[0], bodyMove[1], bodyMove[2]);

        if (shell == EnumGrenadeShell.FRAG) {
            GlStateManager.rotate((float) bodyTurn[2], 1F, 0F, 0F);
            renderFragBody(stack);
            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.grenade_frag_tex);
            ResourceManager.grenades.renderPart("FragSpoon");
            if (renderRing[0] != 0) {
                GlStateManager.translate(ringMove[0], ringMove[1], ringMove[2]);
                GlStateManager.rotate((float) ringTurn[2], 1F, 0F, 0F);
                ResourceManager.grenades.renderPart("FragRing");
            }
        } else if (shell == EnumGrenadeShell.STICK) {
            GlStateManager.rotate((float) bodyTurn[2], 0F, 0F, 1F);
            renderStickBody(stack);
            if (renderRing[0] != 0) {
                GlStateManager.translate(ringMove[0], ringMove[1], ringMove[2]);
                GlStateManager.rotate((float) ringTurn[1], 0F, 1F, 0F);
                EnumGrenadeFilling filling = ItemGrenadeUniversal.getFilling(stack);

                final boolean prevBlend    = RenderUtil.isBlendEnabled();
                final int     prevBlendSrc = RenderUtil.getBlendSrcFactor();
                final int     prevBlendDst = RenderUtil.getBlendDstFactor();
                final int     prevBlendSrcA = RenderUtil.getBlendSrcAlphaFactor();
                final int     prevBlendDstA = RenderUtil.getBlendDstAlphaFactor();

                if (!prevBlend) GlStateManager.enableBlend();
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                bind(ResourceManager.grenade_stick_tex);
                ResourceManager.grenades.renderPart("StickCap");
                setColor(filling.bodyColor);
                bind(ResourceManager.grenade_stick_body_tex);
                ResourceManager.grenades.renderPart("StickCap");

                GlStateManager.color(1F, 1F, 1F, 1F);
                GlStateManager.tryBlendFuncSeparate(prevBlendSrc, prevBlendDst, prevBlendSrcA, prevBlendDstA);
                if (!prevBlend) GlStateManager.disableBlend();
            }
        } else if (shell == EnumGrenadeShell.TECH) {
            GlStateManager.rotate((float) bodyTurn[2], 1F, 0F, 0F);
            renderTechBody(stack);
            if (renderRing[0] != 0) {
                GlStateManager.translate(ringMove[0], ringMove[1], ringMove[2]);
                GlStateManager.rotate((float) ringTurn[2], 1F, 0F, 0F);
                Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.grenade_tech_tex);
                ResourceManager.grenades.renderPart("TechRing");
            }
        } else if (shell == EnumGrenadeShell.NUKE) {
            GlStateManager.rotate((float) bodyTurn[2], 0F, 0F, 1F);
            renderNukeBody(stack);
            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.grenade_nuka_tex);
            ResourceManager.grenades.renderPart("NukaSpoon");
            if (renderRing[0] != 0) {
                GlStateManager.translate(ringMove[0], ringMove[1], ringMove[2]);
                GlStateManager.translate(-1, 5, 0);
                GlStateManager.rotate((float) ringTurn[2], 0F, 0F, -1F);
                GlStateManager.translate(1, -5, 0);
                ResourceManager.grenades.renderPart("NukaRing");
            }
        }
    }

    // Verbatim 1.7 ItemRenderGrenade.renderGrenade branch structure. 1.7 ItemRenderType mapping:
    // GUI=INVENTORY, THIRD_PERSON_*/HEAD=EQUIPPED, GROUND/FIXED=ENTITY (no extra transform), null=the
    // world thrown-grenade entity (RenderGrenadeUniversal), which alone omits the spoon/ring.
    public static void renderGrenade(ItemStack stack, TransformType renderType) {
        EnumGrenadeShell shell = ItemGrenadeUniversal.getShell(stack);
        boolean inv      = renderType == TransformType.GUI;
        boolean equipped = renderType == TransformType.THIRD_PERSON_RIGHT_HAND
                || renderType == TransformType.THIRD_PERSON_LEFT_HAND
                || renderType == TransformType.HEAD;
        boolean world    = renderType == null;

        if (shell == EnumGrenadeShell.FRAG) {
            if (inv)   { GlStateManager.scale(3, 3, 3); GlStateManager.translate(0, -2, 0); }
            if (world) { GlStateManager.translate(0, -2, 0); }
            renderFragBody(stack);
            if (!world) {
                bind(ResourceManager.grenade_frag_tex);
                ResourceManager.grenades.renderPart("FragSpoon");
                ResourceManager.grenades.renderPart("FragRing");
            }
        } else if (shell == EnumGrenadeShell.STICK) {
            if (inv)      { GlStateManager.scale(2, 2, 2); GlStateManager.translate(0, -4.5, 0); }
            if (equipped) { GlStateManager.translate(0, -2, 0); }
            if (world)    { GlStateManager.translate(0, -2, 0); }
            renderStickBody(stack);
            if (!world) {
                EnumGrenadeFilling filling = ItemGrenadeUniversal.getFilling(stack);
                GlStateManager.color(ColorUtil.fr(filling.bodyColor), ColorUtil.fg(filling.bodyColor), ColorUtil.fb(filling.bodyColor));
                bind(ResourceManager.grenade_stick_body_tex);
                ResourceManager.grenades.renderPart("StickCap");
                GlStateManager.color(1F, 1F, 1F, 1F);
            }
        } else if (shell == EnumGrenadeShell.TECH) {
            if (inv)      { GlStateManager.scale(3.5, 3.5, 3.5); GlStateManager.translate(0, -1.75, 0); }
            if (equipped) { GlStateManager.scale(1.5, 1.5, 1.5); GlStateManager.translate(0.5, -1, 0.5); }
            if (world)    { GlStateManager.scale(1.5, 1.5, 1.5); GlStateManager.translate(0, -1, 0); }
            renderTechBody(stack);
            if (!world) {
                bind(ResourceManager.grenade_tech_tex);
                ResourceManager.grenades.renderPart("TechRing");
            }
        } else if (shell == EnumGrenadeShell.NUKE) {
            if (inv)      { GlStateManager.scale(2.5, 2.5, 2.5); GlStateManager.translate(0, -2.75, 0); }
            if (equipped) { GlStateManager.scale(1.5, 1.5, 1.5); GlStateManager.translate(0.5, -3, 0.5); }
            if (world)    { GlStateManager.scale(1.5, 1.5, 1.5); GlStateManager.translate(0, -3, 0); }
            renderNukeBody(stack);
            if (!world) {
                bind(ResourceManager.grenade_nuka_tex);
                ResourceManager.grenades.renderPart("NukaSpoon");
                ResourceManager.grenades.renderPart("NukaRing");
            }
        }
    }

    public static void renderFragBody(ItemStack stack) {
        renderBodyStandard(stack, "Frag", ResourceManager.grenade_frag_tex, ResourceManager.grenade_frag_body_tex, ResourceManager.grenade_frag_label_tex, ResourceManager.grenade_frag_fuze_tex);
    }

    public static void renderStickBody(ItemStack stack) {
        renderBodyStandard(stack, "Stick", ResourceManager.grenade_stick_tex, ResourceManager.grenade_stick_body_tex, ResourceManager.grenade_stick_label_tex, ResourceManager.grenade_stick_fuze_tex);
    }

    public static void renderTechBody(ItemStack stack) {
        EnumGrenadeFilling filling = ItemGrenadeUniversal.getFilling(stack);
        EnumGrenadeFuze fuze = ItemGrenadeUniversal.getFuze(stack);

        renderWithTexture(ResourceManager.grenade_tech_tex, "Tech");

        final boolean prevBlend    = RenderUtil.isBlendEnabled();
        final int     prevBlendSrc = RenderUtil.getBlendSrcFactor();
        final int     prevBlendDst = RenderUtil.getBlendDstFactor();
        final int     prevBlendSrcA = RenderUtil.getBlendSrcAlphaFactor();
        final int     prevBlendDstA = RenderUtil.getBlendDstAlphaFactor();

        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        setColor(filling.bodyColor);
        renderWithTexture(ResourceManager.grenade_tech_body_tex, "Tech");
        setColor(fuze.bandColor);
        renderWithTexture(ResourceManager.grenade_tech_fuze_tex, "Tech");
        RenderArcFurnace.fullbright(true);
        setColor(filling.labelColor);
        renderWithTexture(ResourceManager.grenade_tech_lights_tex, "Tech");
        RenderArcFurnace.fullbright(false);

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.tryBlendFuncSeparate(prevBlendSrc, prevBlendDst, prevBlendSrcA, prevBlendDstA);
        if (!prevBlend) GlStateManager.disableBlend();
    }

    public static void renderNukeBody(ItemStack stack) {
        renderBodyStandard(stack, "Nuka", ResourceManager.grenade_nuka_tex, ResourceManager.grenade_nuka_body_tex, ResourceManager.grenade_nuka_label_tex, ResourceManager.grenade_nuka_fuze_tex);
    }

    public static void renderBodyStandard(ItemStack stack, String part, ResourceLocation baseTex, ResourceLocation bodyTex, ResourceLocation labelTex, ResourceLocation fuzeTex) {
        EnumGrenadeFilling filling = ItemGrenadeUniversal.getFilling(stack);
        EnumGrenadeFuze fuze = ItemGrenadeUniversal.getFuze(stack);

        renderWithTexture(baseTex, part);

        final boolean prevBlend    = RenderUtil.isBlendEnabled();
        final int     prevBlendSrc = RenderUtil.getBlendSrcFactor();
        final int     prevBlendDst = RenderUtil.getBlendDstFactor();
        final int     prevBlendSrcA = RenderUtil.getBlendSrcAlphaFactor();
        final int     prevBlendDstA = RenderUtil.getBlendDstAlphaFactor();

        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        setColor(filling.bodyColor);
        renderWithTexture(bodyTex, part);
        setColor(filling.labelColor);
        renderWithTexture(labelTex, part);
        setColor(fuze.bandColor);
        renderWithTexture(fuzeTex, part);

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.tryBlendFuncSeparate(prevBlendSrc, prevBlendDst, prevBlendSrcA, prevBlendDstA);
        if (!prevBlend) GlStateManager.disableBlend();
    }

    public static void setColor(int hex) {
        GlStateManager.color(ColorUtil.fr(hex), ColorUtil.fg(hex), ColorUtil.fb(hex));
    }

    public static void bind(ResourceLocation res) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
    }

    public static void renderWithTexture(ResourceLocation res, String part) {
        bind(res);
        ResourceManager.grenades.renderPart(part);
    }
}
