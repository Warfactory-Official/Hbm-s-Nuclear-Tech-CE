package com.hbm.util;

import com.hbm.Tags;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.packet.toserver.KeypadServerPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.render.WavefrontObjDisplayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;

@SideOnly(Side.CLIENT)
public class KeypadClient extends Keypad {

	public static final FloatBuffer AUX_GL_MATRIX = GLAllocation.createDirectFloatBuffer(16);

	public static int fullModel;
	public static int mainModel;
	public static int displayModel;
	public static int[] keyModels = new int[12];

	public Matrix4f transform;
	public AxisAlignedBB[] buttonBoxes = new AxisAlignedBB[12];
	//Is having a second set of boxes particularly efficient? No, but I'm too lazy to think of something else.
	public AxisAlignedBB[] pressedButtonBoxes = new AxisAlignedBB[12];
	public AxisAlignedBB mainBox;

	public KeypadClient(TileEntity te, Matrix4f transform) {
		super(te);
		//All these magic numbers are pulled out of the keypad model in blender so the box would exactly match the model
		this.transform = transform;
		for(int x = 0; x < 3; x++) {
			for(int y = 3; y >= 0; y--) {
				Vector4f pos1 = new Vector4f(0.3125F - 0.234375F * x, 0.042969F + 0.175781F * y, 0, 1);
				Vector4f pos2 = new Vector4f(0.15625F - 0.234375F * x, 0.160156F + 0.175781F * y, -0.0625F, 1);
				Vector4f pos2_pressed = new Vector4f(0.15625F - 0.234375F * x, 0.160156F + 0.175781F * y, -0.0125F, 1);
				Matrix4f.transform(transform, pos1, pos1);
				Matrix4f.transform(transform, pos2, pos2);
				Matrix4f.transform(transform, pos2_pressed, pos2_pressed);
				buttonBoxes[(3 - y) * 3 + x] = new AxisAlignedBB(pos1.x + 0.5F, pos1.y, pos1.z + 0.5F, pos2.x + 0.5F, pos2.y, pos2.z + 0.5F);
				pressedButtonBoxes[(3 - y) * 3 + x] = new AxisAlignedBB(pos1.x + 0.5F, pos1.y, pos1.z + 0.5F, pos2_pressed.x + 0.5F, pos2_pressed.y, pos2_pressed.z + 0.5F);
			}
		}
		Vector4f pos1 = new Vector4f(-0.375F, 0, 0, 1);
		Vector4f pos2 = new Vector4f(0.375F, 1, -0.0625F, 1);
		Matrix4f.transform(transform, pos1, pos1);
		Matrix4f.transform(transform, pos2, pos2);
		mainBox = new AxisAlignedBB(pos1.x + 0.5F, pos1.y, pos1.z + 0.5F, pos2.x + 0.5F, pos2.y, pos2.z + 0.5F);
	}

	public boolean playerClick(BlockPos pos) {
		int idx = rayTraceForButtonIndex(pos);
		if(idx >= 0) {
			Button b = buttons[idx];
			if(b.cooldown == 0) {
				PacketDispatcher.wrapper.sendToServer(new KeypadServerPacket(pos, 0, idx));
				return true;
			}
		}
		return false;
	}
	
	public AxisAlignedBB rayTrace(BlockPos pos){
		int idx = rayTraceForButtonIndex(pos);
		if(idx < 0){
			EntityPlayer p = Minecraft.getMinecraft().player;
			Vec3d vec1 = p.getPositionEyes(MainRegistry.proxy.partialTicks());
			Vec3d vec2 = vec1.add(p.getLook(MainRegistry.proxy.partialTicks()).scale(3));
			RayTraceResult r = mainBox.offset(pos).calculateIntercept(vec1, vec2);
			if(r != null && r.typeOfHit != Type.MISS){
				return mainBox.offset(pos);
			}
			return null;
		}
		if(buttons[idx].cooldown == 0){
			return buttonBoxes[idx].offset(pos);
		} else {
			return pressedButtonBoxes[idx].offset(pos);
		}
	}
	
	public int rayTraceForButtonIndex(BlockPos pos){
		EntityPlayer p = Minecraft.getMinecraft().player;
		Vec3d vec1 = p.getPositionEyes(MainRegistry.proxy.partialTicks());
		Vec3d vec2 = vec1.add(p.getLook(MainRegistry.proxy.partialTicks()).scale(3));
		int idx = -1;
		RayTraceResult hit = null;
		for(int i = 0; i < buttonBoxes.length; i ++){
			RayTraceResult r = buttonBoxes[i].offset(pos).calculateIntercept(vec1, vec2);
			if(r == null || r.typeOfHit == Type.MISS)
				continue;
			if(hit == null || r.hitVec.squareDistanceTo(vec1) < hit.hitVec.squareDistanceTo(vec1)){
				hit = r;
				idx = i;
			}
		}
		return idx;
	}
	
	public boolean isPlayerMouseingOver(BlockPos pos){
		EntityPlayer p = Minecraft.getMinecraft().player;
		Vec3d vec1 = p.getPositionEyes(MainRegistry.proxy.partialTicks());
		Vec3d vec2 = vec1.add(p.getLook(MainRegistry.proxy.partialTicks()).scale(3));
		RayTraceResult r = mainBox.offset(pos).calculateIntercept(vec1, vec2);
		if(r != null && r.typeOfHit != Type.MISS){
			return true;
		}
		return false;
	}
	
	public static void load(){
		WavefrontObjDisplayList model = new WavefrontObjDisplayList(new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/keypad.obj")));
		mainModel = model.getListForName("Keypad");
		displayModel = model.getListForName("Display");
		for(int i = 0; i < 9; i++){
			keyModels[i] = model.getListForName("K" + (i+1));
		}
		keyModels[9] = model.getListForName("KReset");
		keyModels[10] = model.getListForName("K0");
		keyModels[11] = model.getListForName("KReturn");
		fullModel = GL11.glGenLists(1);
		GL11.glNewList(fullModel, GL11.GL_COMPILE);
		model.renderAll();
		GL11.glEndList();
	}

	//Garbage render code, should have thought of a better system.
	public void render() {
		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.keypad_tex);
		GlStateManager.enableRescaleNormal();
		transform.store(AUX_GL_MATRIX);
		AUX_GL_MATRIX.rewind();
		GL11.glMultMatrix(AUX_GL_MATRIX);
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		String disp = "";
		if(isActive()) {
			if(isSettingCode){
				Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.keypad_tex);
			} else if(successColorTicks > 0){
				Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.keypad_success_tex);
			} else if(failColorTicks  > 0){
				Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.keypad_error_tex);
			}
			GL11.glCallList(mainModel);
			GL11.glCallList(displayModel);
			for(int i = 0; i < buttons.length; i ++){
				GlStateManager.pushMatrix();
				if(buttons[i].cooldown > 0){
					GlStateManager.translate(0, 0, 0.05);
					Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.keypad_success_tex);
				} else {
					Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.keypad_tex);
				}
				GL11.glCallList(keyModels[i]);
				GlStateManager.popMatrix();
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		} else {
			GL11.glCallList(fullModel);
		}
		
		
		GlStateManager.enablePolygonOffset();
		GlStateManager.doPolygonOffset(-1.0F, -1.0F);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.275, 0.67, -0.0625);
		float s = 0.02F;
		GL11.glScaled(s*0.5, -s*0.5, s);
		GL11.glRotated(180, 0, 1, 0);
		for(int i = 0; i < 12; i ++){
			switch(i){
			case 9:
				disp = "R";
				break;
			case 10:
				disp = "0";
				break;
			case 11:
				disp = "E";
				break;
			default:
				disp = ""+(i+1);
				break;
			}
			GlStateManager.pushMatrix();
			GlStateManager.translate((i%3)*0.234375F*100, (i/3)*0.175781F*100, 0);
			if(buttons[i].cooldown > 0){
				GlStateManager.translate(0, 0, -0.05*50);
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.color(1, 0, 0);
			font.drawString(disp, 0, 0, 0xFF404040);
			GlStateManager.popMatrix();
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
		
		int code = buildIntCode();
		
		if(code < 0){
			GlStateManager.translate(0.3, 1-0.08, 0.03125);
			GL11.glRotated(180, 0, 1, 0);
			GlStateManager.color(1, 0, 0);
			if(isSettingCode){
				GL11.glScaled(s*0.5F, -s*0.5, s);
				font.drawString("Enter New", 0, 0, 0xFFFFEE00);
				GlStateManager.translate(0, 8, 0);
				font.drawString("Code:", 0, 0, 0xFFFFEE00);
			} else if(successColorTicks > 0){
				GL11.glScaled(s*0.5F, -s*0.5, s);
				font.drawString("Access", 0, 0, 0xFF15FF00);
				GlStateManager.translate(0, 8, 0);
				font.drawString("Granted", 0, 0, 0xFF15FF00);
			} else if(failColorTicks  > 0){
				GL11.glScaled(s*0.5F, -s*0.5, s);
				font.drawString("Access", 0, 0, 0xFFFF0800);
				GlStateManager.translate(0, 8, 0);
				font.drawString("Denied", 0, 0, 0xFFFF0800);
			} else {
				GlStateManager.translate(0, -0.035, 0);
				GL11.glScaled(s*0.5F, -s*0.5, s);
				font.drawString("Enter Code:", 0, 0, 0xFFFFFFFF);
			}
		} else {
			GlStateManager.translate(0.3, 1-0.09, 0.03125);
			GL11.glScaled(s*0.85, -s*0.9, s);
			GL11.glRotated(180, 0, 1, 0);
			GlStateManager.color(1, 0, 0);
			if(isSettingCode){
				font.drawString("" + code, 0, 0, 0xFFFFEE00);
			} else {
				font.drawString("" + code, 0, 0, 0xFFFFFFFF);
			}
		}
		
		GlStateManager.disablePolygonOffset();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}
}
