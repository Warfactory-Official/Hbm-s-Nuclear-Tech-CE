package com.hbm.render.item;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.util.HashMap;
import java.util.function.Consumer;

public class ItemRenderMissileGeneric extends TEISRBase {
	
	public static HashMap<ComparableStack, Consumer<TextureManager>> renderers = new HashMap<>();
	
	protected RenderMissileType category;
	
	public enum RenderMissileType {
		TYPE_TIER0,
		TYPE_TIER1,
		TYPE_TIER2,
		TYPE_TIER3,
		TYPE_STEALTH,
		TYPE_ABM,
		TYPE_NUCLEAR,
		TYPE_THERMAL,
		TYPE_ROBIN,
		TYPE_DOOMSDAY,
		TYPE_CARRIER
	}
	
	public ItemRenderMissileGeneric(RenderMissileType category) {
		this.category = category;
	}

	@Override
	public void renderByItem(ItemStack item) {
	
		Consumer<TextureManager> renderer = renderers.get(new ComparableStack(item).makeSingular());
		if(renderer == null) return;
		
		GlStateManager.pushMatrix();

		double guiScale = 1;
		double guiOffset = 0;

		switch(this.category) {
			case TYPE_TIER0: guiScale = 3.75D; guiOffset = 1D; break;
			case TYPE_TIER1: guiScale = 2.5D; guiOffset = 0.5D; break;
			case TYPE_TIER2: guiScale = 2D; guiOffset = 0.5D; break;
			case TYPE_TIER3: guiScale = 1.25D; guiOffset = 0D; break;
			case TYPE_STEALTH: guiScale = 1.75D; guiOffset = 1.5D; break;
			case TYPE_ABM: guiScale = 2.25D; guiOffset = 0.5D; break;
			case TYPE_NUCLEAR: guiScale = 1.375D; guiOffset = 0D; break;
			case TYPE_THERMAL: guiScale = 1.75D; guiOffset = 1D; break;
			case TYPE_ROBIN: guiScale = 1.25D; guiOffset = 2D; break;
			case TYPE_CARRIER: guiScale = 0.625D; break;
		}

		GlStateManager.enableLighting();
        boolean prevAlpha = RenderUtil.isAlphaEnabled();
        int prevAlphaFunc = RenderUtil.getAlphaFunc();
		float prevAlphaRef = RenderUtil.getAlphaRef();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
		GlStateManager.enableAlpha();
        switch (type) {
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                double s = 0.15;
                GlStateManager.translate(0.5, -0.25, 0.25);
                GlStateManager.scale(s, s, s);
                GlStateManager.scale(guiScale, guiScale, guiScale);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                double heldScale = 0.1;
                GlStateManager.translate(0.5, -0.25, 0.3);
                GlStateManager.scale(heldScale, heldScale, heldScale);
                GlStateManager.scale(guiScale, guiScale, guiScale);
            }
            case GROUND, FIXED, HEAD -> {
                double s2 = 0.15;
                GlStateManager.scale(s2, s2, s2);
            }
            case GUI -> {
                double s3 = 0.0625;
                GlStateManager.scale(s3, s3, s3);
                GlStateManager.translate(15 - guiOffset, 1 + guiOffset, 0);
                GlStateManager.scale(guiScale, guiScale, guiScale);
                GlStateManager.rotate(45, 0, 0, 1);
                GlStateManager.rotate(System.currentTimeMillis() / 15 % 360, 0, 1, 0);
            }
            default -> {
            }
        }
		
		GlStateManager.disableCull();
		renderer.accept(Minecraft.getMinecraft().renderEngine);
		GlStateManager.enableCull();
        if (!prevAlpha) GlStateManager.disableAlpha();
        GlStateManager.alphaFunc(prevAlphaFunc, prevAlphaRef);
		GlStateManager.popMatrix();
	}
	
	public static Consumer<TextureManager> generateStandard(ResourceLocation texture, IModelCustom model) { return generateWithScale(texture, model, 1F); }
	public static Consumer<TextureManager> generateLarge(ResourceLocation texture, IModelCustom model) { return generateWithScale(texture, model, 1.5F); }
	public static Consumer<TextureManager> generateDouble(ResourceLocation texture, IModelCustom model) { return generateWithScale(texture, model, 2F); }
	
	public static Consumer<TextureManager> generateWithScale(ResourceLocation texture, IModelCustom model, float scale) {
		return x -> {
            int prevShade = RenderUtil.getShadeModel();
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			x.bindTexture(texture); model.renderAll();
			GlStateManager.shadeModel(prevShade);
		};
	}
	
	public static void init() {

		renderers.put(new ComparableStack(ModItems.missile_taint), generateStandard(ResourceManager.missileTaint_tex, ResourceManager.missileMicro));
		renderers.put(new ComparableStack(ModItems.missile_micro), generateStandard(ResourceManager.missileMicro_tex, ResourceManager.missileMicro));
		renderers.put(new ComparableStack(ModItems.missile_bhole), generateStandard(ResourceManager.missileMicroBHole_tex, ResourceManager.missileMicro));
		renderers.put(new ComparableStack(ModItems.missile_schrabidium), generateStandard(ResourceManager.missileMicroSchrab_tex, ResourceManager.missileMicro));
		renderers.put(new ComparableStack(ModItems.missile_emp), generateStandard(ResourceManager.missileMicroEMP_tex, ResourceManager.missileMicro));

		renderers.put(new ComparableStack(ModItems.missile_stealth), x -> {
            int prevShade = RenderUtil.getShadeModel();
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			x.bindTexture(ResourceManager.missileStealth_tex); ResourceManager.missileStealth.renderAll();
            GlStateManager.shadeModel(prevShade);
		});

		renderers.put(new ComparableStack(ModItems.missile_generic), generateStandard(ResourceManager.missileV2_HE_tex, ResourceManager.missileV2));
		renderers.put(new ComparableStack(ModItems.missile_incendiary), generateStandard(ResourceManager.missileV2_IN_tex, ResourceManager.missileV2));
		renderers.put(new ComparableStack(ModItems.missile_cluster), generateStandard(ResourceManager.missileV2_CL_tex, ResourceManager.missileV2));
		renderers.put(new ComparableStack(ModItems.missile_buster), generateStandard(ResourceManager.missileV2_BU_tex, ResourceManager.missileV2));
		renderers.put(new ComparableStack(ModItems.missile_decoy), generateStandard(ResourceManager.missileV2_decoy_tex, ResourceManager.missileV2));
		renderers.put(new ComparableStack(ModItems.missile_anti_ballistic), generateStandard(ResourceManager.missileAA_tex, ResourceManager.missileABM));

		renderers.put(new ComparableStack(ModItems.missile_strong), generateLarge(ResourceManager.missileStrong_HE_tex, ResourceManager.missileStrong));
		renderers.put(new ComparableStack(ModItems.missile_incendiary_strong), generateLarge(ResourceManager.missileStrong_IN_tex, ResourceManager.missileStrong));
		renderers.put(new ComparableStack(ModItems.missile_cluster_strong), generateLarge(ResourceManager.missileStrong_CL_tex, ResourceManager.missileStrong));
		renderers.put(new ComparableStack(ModItems.missile_buster_strong), generateLarge(ResourceManager.missileStrong_BU_tex, ResourceManager.missileStrong));
		renderers.put(new ComparableStack(ModItems.missile_emp_strong), generateLarge(ResourceManager.missileStrong_EMP_tex, ResourceManager.missileStrong));
		
		renderers.put(new ComparableStack(ModItems.missile_burst), generateStandard(ResourceManager.missileHuge_HE_tex, ResourceManager.missileHuge));
		renderers.put(new ComparableStack(ModItems.missile_inferno), generateStandard(ResourceManager.missileHuge_IN_tex, ResourceManager.missileHuge));
		renderers.put(new ComparableStack(ModItems.missile_rain), generateStandard(ResourceManager.missileHuge_CL_tex, ResourceManager.missileHuge));
		renderers.put(new ComparableStack(ModItems.missile_drill), generateStandard(ResourceManager.missileHuge_BU_tex, ResourceManager.missileHuge));

		renderers.put(new ComparableStack(ModItems.missile_nuclear), generateStandard(ResourceManager.missileNuclear_tex, ResourceManager.missileNuclear));
		renderers.put(new ComparableStack(ModItems.missile_nuclear_cluster), generateStandard(ResourceManager.missileMIRV_tex, ResourceManager.missileNuclear));
		renderers.put(new ComparableStack(ModItems.missile_volcano), generateStandard(ResourceManager.missileVolcano_tex, ResourceManager.missileNuclear));
		renderers.put(new ComparableStack(ModItems.missile_n2), generateLarge(ResourceManager.missileN2_tex, ResourceManager.missileN2));
		
		renderers.put(new ComparableStack(ModItems.missile_endo), generateLarge(ResourceManager.missileEndo_tex, ResourceManager.missileThermo));
		renderers.put(new ComparableStack(ModItems.missile_exo), generateLarge(ResourceManager.missileExo_tex, ResourceManager.missileThermo));
		renderers.put(new ComparableStack(ModItems.missile_shuttle), generateStandard(ResourceManager.missileShuttle_tex, ResourceManager.missileShuttle));

		renderers.put(new ComparableStack(ModItems.missile_doomsday), generateStandard(ResourceManager.missileDoomsday_tex, ResourceManager.missileNuclear));
		renderers.put(new ComparableStack(ModItems.missile_doomsday_rusted), generateStandard(ResourceManager.missileDoomsdayRusted_tex, ResourceManager.missileNuclear));
		renderers.put(new ComparableStack(ModItems.missile_carrier), x -> {
			GlStateManager.scale(2F, 2F, 2F);
			x.bindTexture(ResourceManager.missileCarrier_tex);
			ResourceManager.missileCarrier.renderAll();
			GlStateManager.translate(0.0D, 0.5D, 0.0D);
			GlStateManager.translate(1.25D, 0.0D, 0.0D);
			x.bindTexture(ResourceManager.missileBooster_tex);
			ResourceManager.missileBooster.renderAll();
			GlStateManager.translate(-2.5D, 0.0D, 0.0D);
			ResourceManager.missileBooster.renderAll();
			GlStateManager.translate(1.25D, 0.0D, 0.0D);
			GlStateManager.translate(0.0D, 0.0D, 1.25D);
			ResourceManager.missileBooster.renderAll();
			GlStateManager.translate(0.0D, 0.0D, -2.5D);
			ResourceManager.missileBooster.renderAll();
			GlStateManager.translate(0.0D, 0.0D, 1.25D);
		});
	}
}
