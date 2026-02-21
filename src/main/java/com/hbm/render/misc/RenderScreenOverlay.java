package com.hbm.render.misc;

import com.hbm.Tags;
import com.hbm.capability.HbmCapability;
import com.hbm.config.RadiationConfig;
import com.hbm.interfaces.Spaghetti;
import com.hbm.items.weapon.sedna.impl.ItemGunStinger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderScreenOverlay {

	private static final ResourceLocation misc = new ResourceLocation(Tags.MODID + ":textures/misc/overlay_misc.png");
	private static final RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
	
	private static long lastRadSurvey;
	private static float prevRadResult;
	private static float lastRadResult;

	private static long lastDigSurvey;
	private static float prevDigResult;
	private static float lastDigResult;

	private static float fadeOut = 0F;
	
	public static void renderRadCounter(ScaledResolution resolution, float in, Gui gui) {
		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
        
        float radiation = 0;
        
        radiation = lastRadResult - prevRadResult;
        
        if(System.currentTimeMillis() >= lastRadSurvey + 1000) {
        	lastRadSurvey = System.currentTimeMillis();
        	prevRadResult = lastRadResult;
        	lastRadResult = in;
        }
		
		int length = 74;
		int maxRad = 1000;
		
		int bar = getScaled(in, maxRad, 74);
		
		//if(radiation >= 1 && radiation <= 999)
		//	bar -= (1 + Minecraft.getMinecraft().theWorld.rand.nextInt(3));
		
		int posX = RadiationConfig.geigerX;
		int posY = resolution.getScaledHeight() - 18 - RadiationConfig.geigerY;

		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
        gui.drawTexturedModalRect(posX, posY, 0, 0, 94, 18);
        gui.drawTexturedModalRect(posX + 1, posY + 1, 1, 19, bar, 16);
        
        if(radiation >= 25) {
            gui.drawTexturedModalRect(posX + length + 2 + 18, posY, 36, 36, 18, 18);
        	
        } else if(radiation >= 10) {
            gui.drawTexturedModalRect(posX + length + 2 + 18, posY, 18, 36, 18, 18);
        	
        } else if(radiation >= 2.5) {
            gui.drawTexturedModalRect(posX + length + 2 + 18, posY, 0, 36, 18, 18);
        	
        }
		
		if(radiation > 1000) {
			Minecraft.getMinecraft().fontRenderer.drawString(">1000 RAD/s", posX, posY - 8, 0xFF0000);
		} else if(radiation >= 1) {
			Minecraft.getMinecraft().fontRenderer.drawString(((int)Math.round(radiation)) + " RAD/s", posX, posY - 8, 0xFFFF00);
		} else if(radiation > 0) {
			Minecraft.getMinecraft().fontRenderer.drawString("<1 RAD/s", posX, posY - 8, 0x00FF00);
		}

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}


	public static void renderDigCounter(ScaledResolution resolution, float in, Gui gui) {
		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
        
        float digamma = 0;
        
        digamma = lastDigResult - prevDigResult;
        
        if(System.currentTimeMillis() >= lastDigSurvey + 1000) {
        	lastDigSurvey = System.currentTimeMillis();
        	prevDigResult = lastDigResult;
        	lastDigResult = in;
        }
		
		int length = 74;
		int maxDig = 10;
		
		int bar = getScaled(in, maxDig, 74);
		
		//if(radiation >= 1 && radiation <= 999)
		//	bar -= (1 + Minecraft.getMinecraft().theWorld.rand.nextInt(3));
		
		int posX = RadiationConfig.digammaX;
		int posY = resolution.getScaledHeight() - 18 - RadiationConfig.digammaY;

		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
        gui.drawTexturedModalRect(posX, posY, 0, 218, 94, 18);
        gui.drawTexturedModalRect(posX + 1, posY + 1, 1, 237, bar, 16);
        
        if(digamma >= 0.25) {
            gui.drawTexturedModalRect(posX + length + 2 + 18, posY, 108, 72, 18, 18);
        	
        } else if(digamma >= 0.1) {
            gui.drawTexturedModalRect(posX + length + 2 + 18, posY, 90, 72, 18, 18);
        	
        } else if(digamma >= 0.025) {
            gui.drawTexturedModalRect(posX + length + 2 + 18, posY, 72, 72, 18, 18);
        	
        }
		
		if(digamma > 0.1) {
			Minecraft.getMinecraft().fontRenderer.drawString(">100 mDRX/s", posX, posY - 8, 0xCC0000);
		} else if(digamma >= 0.01) {
			Minecraft.getMinecraft().fontRenderer.drawString(((int)Math.round(digamma*1000D)) + " mDRX/s", posX, posY - 8, 0xFF0000);
		} else if(digamma > 0) {
			Minecraft.getMinecraft().fontRenderer.drawString("<10 mDRX/s", posX, posY - 8, 0xFF3232);
		}

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}
	
	private static int getScaled(double cur, double max, double scale) {
		
		return (int) Math.min(cur / max * scale, scale);
	}

	
	public static void renderCustomCrosshairs(ScaledResolution resolution, Gui gui, Crosshair cross) {
		if(cross == Crosshair.NONE) {
			Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
			return;
		}
		
		int size = cross.size;

		GlStateManager.pushMatrix();
			Minecraft.getMinecraft().renderEngine.bindTexture(misc);
	        GlStateManager.enableBlend();
	        GlStateManager.tryBlendFuncSeparate(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ONE_MINUS_SRC_COLOR, SourceFactor.ONE, DestFactor.ZERO);
	        gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - (size / 2), resolution.getScaledHeight() / 2 - (size / 2), cross.x, cross.y, size, size);
	        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
	        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}

	public static void renderStingerLockon(ScaledResolution resolution, Gui gui) {

		int progress = (int) (ItemGunStinger.lockon * 28);

		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
		GlStateManager.disableBlend();
		gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 15, resolution.getScaledHeight() / 2  + 18, 146, 18, 30, 10);
		gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 14, resolution.getScaledHeight() / 2  + 19, 147, 29, progress, 8);
		GlStateManager.popMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}
	
	public static void renderAmmo(ScaledResolution resolution, Gui gui, Item ammo, int count, int max, int dura, EnumHand hand, boolean renderCount) {
		
		GlStateManager.pushMatrix();
        
		int pX = resolution.getScaledWidth() / 2 + 62 + 36;
		int pZ = resolution.getScaledHeight() - 21;
		if(hand == EnumHand.OFF_HAND){
			pX -= 277;
		}
		
		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
        gui.drawTexturedModalRect(pX, pZ + 16, 94, 0, 52, 3);
        gui.drawTexturedModalRect(pX + 1, pZ + 16, 95, 3, 50 - dura, 3);
		
		String cap = max == -1 ? ("∞") : ("" + max);
		
		//if(renderCount)
		Minecraft.getMinecraft().fontRenderer.drawString(count + " / " + cap, pX + 16, pZ + 6, 0xFFFFFF);

        GlStateManager.disableBlend();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        itemRenderer.renderItemAndEffectIntoGUI(null, new ItemStack(ammo), pX, pZ);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableBlend();
        
        GlStateManager.popMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}
	
	public static void renderAmmoAlt(ScaledResolution resolution, Gui gui, Item ammo, int count, EnumHand hand) {

		GlStateManager.pushMatrix();

		int pX = resolution.getScaledWidth() / 2 + 62 + 36 + 18;
		int pZ = resolution.getScaledHeight() - 21 - 16;
		if(hand == EnumHand.OFF_HAND){
			pX -= 296;
		}
		
		Minecraft.getMinecraft().renderEngine.bindTexture(misc);

		Minecraft.getMinecraft().fontRenderer.drawString(count + "x", pX + 16, pZ + 6, 0xFFFFFF);

        GlStateManager.disableBlend();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        	itemRenderer.renderItemAndEffectIntoGUI(null, new ItemStack(ammo), pX, pZ);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();

        GlStateManager.popMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}

	@Spaghetti("like a fella once said, aint that a kick in the head")
	public static void renderDashBar(ScaledResolution resolution, Gui gui, HbmCapability.IHBMData props) {

		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableAlpha();

		Minecraft mc = Minecraft.getMinecraft();

		int width = 30;

		int posX = 16;//(int)(resolution.getScaledWidth()/2 - ((props.getDashCount()*(width+2))/2));
		int posY = resolution.getScaledHeight() - 40 - 2;

		mc.renderEngine.bindTexture(misc);

		gui.drawTexturedModalRect(posX-10, posY, 107, 18, 7, 10);

		int stamina = props.getStamina();

		int dashes = props.getDashCount();

		//int count = props.getDashCount();
		//int x3count = count / 3;

		int rows = dashes / 3;
		int finalColumns = dashes % 3;

		for(int y = 0; y < rows; y++) {
			for(int x = 0; x < 3; x++) {
				if(y == rows && x > finalColumns)
					break;
				gui.drawTexturedModalRect(posX + (width+2)*x, posY - 12*y, 76, 48, width, 10);
				int staminaDiv = stamina / 30;
				int staminaMod = stamina % 30;
				int barID = (3*y)+x;
				int barStatus = 1; //0 = red, 1 = normal, 2 = greyed, 3 = dashed, 4 = ascended
				int barSize = width;
				if(staminaDiv < barID) {
					barStatus = 3;
				} else if(staminaDiv == barID) {
					barStatus = 2;
					barSize = (int)((float)(stamina % 30) * (width/30F) );
					if(barID == 0)
						barStatus = 0;
				}
				gui.drawTexturedModalRect(posX + (width+2)*x, posY - 12*y, 76, 18+(10*barStatus), barSize, 10);

				if(staminaDiv == barID && staminaMod >= 27) {
					fadeOut = 1F;
				}
				if(fadeOut > 0 && staminaDiv-1 == barID) {
					GlStateManager.color(1F, 1F, 1F, fadeOut);
					int bar = barID;
					if(stamina % 30 >= 25)
						bar++;
					if(bar / 3 != y)
						y++;
					bar = bar % 3;
					gui.drawTexturedModalRect(posX + (width + 2) * bar, posY - 12 * y, 76, 58, width, 10);
					fadeOut -= 0.04F;
					GlStateManager.color(1F, 1F, 1F, 1F);
				}
			}
		}

		/*for(int x = 0; x < props.getDashCount(); x++) {
			int status = 3;
			gui.drawTexturedModalRect(posX + (24)*x, posY, 76, 48, 24, 10);
			int staminaDiv = stamina / 60;
			if(staminaDiv > x) {
				status = 1;
			} else if(staminaDiv == x) {
				width = (int)( (float)(stamina % 60) * (width/60F) );
				status = 2;
				if(staminaDiv == 0)
					status = 0;
			}
			/*if(staminaDiv-1 == x && (stamina % 60 < 20 && stamina % 60 != 0)) {
				status = 4;
			}
			/*if(((staminaDiv == x && stamina % 60 >= 55) || (staminaDiv-1 == x && stamina % 60 <= 5)) && !(stamina == props.totalDashCount * 60)) {
				status = 4;
			}
			gui.drawTexturedModalRect(posX + (24)*x, posY, 76, 18+(10*status), width, 10);

			if(staminaDiv == x && stamina % 60 >= 57) {
				fadeOut = 1F;
			}
			if(fadeOut > 0 && staminaDiv-1 == x) {
				GlStateManager.color(1F, 1F, 1F, fadeOut);
				int bar = x;
				if(stamina % 60 >= 50)
					bar++;
				System.out.println(bar);
				gui.drawTexturedModalRect(posX + 24*bar, posY, 76, 58, width, 10);
				fadeOut -= 0.04F;
				GlStateManager.color(1F, 1F, 1F, 1F);
			}
		}*/


		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();
		mc.renderEngine.bindTexture(Gui.ICONS);
	}

	//call in post health bar rendering event
	public static void renderShieldBar(ScaledResolution resolution, Gui gui) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		HbmCapability.IHBMData props = HbmCapability.getData(player);
		if (props == null || props.getShield() <= 0) return;

		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		int height = resolution.getScaledHeight();
		int width = resolution.getScaledWidth();
		int left = width / 2 - 91;
		int top = height - GuiIngameForge.left_height;

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(misc);
		gui.drawTexturedModalRect(left, top, 146, 0, 81, 9);
		if (props.getEffectiveMaxShield(player) > 0) {
			int barWidth = (int) Math.ceil(props.getShield() * 79.0F / props.getEffectiveMaxShield(player));
			barWidth = Math.min(barWidth, 79);

			if (barWidth > 0) {
				gui.drawTexturedModalRect(left + 1, top + 1, 147, 10, barWidth, 7);
			}
		}

		String label = "" + ((int) (props.getShield() * 10.0F)) / 10.0D;
		int labelX = left + 40 - font.getStringWidth(label) / 2;
		int labelY = top + 1;
		int textColor = 0xFFFF80;
		int shadowColor = 0x0000;

		font.drawString(label, labelX + 1, labelY, shadowColor);
		font.drawString(label, labelX - 1, labelY, shadowColor);
		font.drawString(label, labelX, labelY + 1, shadowColor);
		font.drawString(label, labelX, labelY - 1, shadowColor);
		font.drawString(label, labelX, labelY, textColor);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		GuiIngameForge.left_height += 10;
		Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
	}

	public static void renderScope(ScaledResolution res, ResourceLocation tex) {

		GlStateManager.enableBlend();
		//GlStateManager.disableDepth();
		//GlStateManager.depthMask(false);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableAlpha();

		Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		double w = res.getScaledWidth_double();
		double h = res.getScaledHeight_double();

		double smallest = Math.min(w, h);
		double divisor = smallest / (9D / 16D);
		smallest = 9D / 16D;
		double largest = Math.max(w, h) / divisor;

		double hMin = h < w ? 0.5 - smallest / 2D : 0.5 - largest / 2D;
		double hMax = h < w ? 0.5 + smallest / 2D : 0.5 + largest / 2D;
		double wMin = w < h ? 0.5 - smallest / 2D : 0.5 - largest / 2D;
		double wMax = w < h ? 0.5 + smallest / 2D : 0.5 + largest / 2D;

		double depth = -300D;

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		buffer.pos(0, h, depth).tex(wMin, hMax).endVertex();
		buffer.pos(w, h, depth).tex(wMax, hMax).endVertex();
		buffer.pos(w, 0, depth).tex(wMax, hMin).endVertex();
		buffer.pos(0, 0, depth).tex(wMin, hMin).endVertex();

		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	public enum Crosshair {
		NONE(0, 0, 0),
		CROSS(1, 55, 16),
		CIRCLE(19, 55, 16),
		SEMI(37, 55, 16),
		KRUCK(55, 55, 16),
		DUAL(1, 73, 16),
		SPLIT(19, 73, 16),
		CLASSIC(37, 73, 16),
		BOX(55, 73, 16),

		L_CROSS(0, 90, 32),
		L_KRUCK(32, 90, 32),
		L_CLASSIC(64, 90, 32),
		L_CIRCLE(96, 90, 32),
		L_SPLIT(0, 122, 32),
		L_ARROWS(32, 122, 32),
		L_BOX(64, 122, 32),
		L_CIRCUMFLEX(96, 122, 32),
		L_RAD(0, 154, 32),
		L_MODERN(32, 154, 32),
		L_BOX_OUTLINE(64, 154, 32);
		
		public int x;
		public int y;
		public int size;
		
		private Crosshair(int x, int y, int size) {
			this.x = x;
			this.y = y;
			this.size = size;
		}
	}
}
