package com.hbm.inventory.control_panel;

import com.hbm.inventory.control_panel.nodes.Node;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class NodeTextBox extends NodeElement implements ITypableNode {

	public String name;

	public String value;
	public StringBuilder builder;
	public boolean isTyping;
	public Consumer<String> changed = null;

	public NodeTextBox(String name,Node p,int idx,String defaultVal,Consumer<String> func){
		super(p, idx);
		this.name = name;
		this.changed = func;
		value = defaultVal;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys){
		super.writeToNBT(tag, sys);
//		tag.setString("eleType", "connection");
		tag.setString("name", name);
		tag.setString("D", value);
		return tag;
	}
	
	public void readFromNBT(NBTTagCompound tag, NodeSystem sys){
		super.readFromNBT(tag, sys);
		name = tag.getString("name");
		value = tag.getString("D");
		builder = null;
		isTyping = false;
	}
	
	@Override
	public void resetOffset(){
		super.resetOffset();
		offsetY += parent.otherElements.size()*8;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void render(float mX, float mY){
		Minecraft.getMinecraft().getTextureManager().bindTexture(NodeSystem.node_tex);
		Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		float x = offsetX+38-40;
		float y = offsetY+8;
		float[] color = NTMRenderHelper.intersects2DBox(mX, mY, this.getValueBox()) && !isTyping ? new float[]{1, 1, 1} : new float[]{0.6F, 0.6F, 0.6F};
		NTMRenderHelper.drawGuiRectBatchedColor(x, y-1, 0, 0.203125F, 40, 6, 0.625F, 0.296875F, color[0], color[1], color[2], 1);
		Tessellator.getInstance().draw();

		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, 0);
		GL11.glScaled(0.4, 0.4, 0.4);
		GL11.glTranslated(-x, -y, 0);
		if(isTyping){
			String s = builder.toString();
			font.drawString(s + (Minecraft.getMinecraft().world.getTotalWorldTime()%20 > 10 ? "_" : ""), x+16, y+1F, 0xFFAFAFAF, false);
		} else {
			int hex =  0xFFAFAFAF;
			font.drawString(name, x+16, y+1F, hex, false);
			String s = value.toString();
			if(s.length() > 5){
				s = s.substring(0, 5);
			}
			font.drawString(s, x+94-font.getStringWidth(s), y+1, 0xFFAFAFAF, false);
		}
		GL11.glPopMatrix();
	}
	
	//minX, minY, maxX, maxY
	@SideOnly(Side.CLIENT)
	public float[] getPortBox(){
		float oX = offsetX;
		return new float[]{-2+oX, -2+offsetY+10, 2+oX, 2+offsetY+10};
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public float[] getValueBox(){
		return new float[]{3+offsetX, -3+offsetY+10, 37+offsetX, 3+offsetY+10};
	}

	public DataValue evaluate(){
		return new DataValueString(value);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isTyping() {
		return isTyping;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void startTyping(){
		isTyping = true;
		builder = new StringBuilder();
		builder.append(value.toString());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SideOnly(Side.CLIENT)
	@Override
	public void stopTyping(){
		value = builder.toString();
		builder = null;
		isTyping = false;
		changed.accept(value);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void keyTyped(char c, int key){
		if(key == Keyboard.KEY_BACK){
			if(builder.length() > 0)
				builder.deleteCharAt(builder.length()-1);
		} else if(key == Keyboard.KEY_RETURN){
			stopTyping();
		} else if (key != Keyboard.KEY_LSHIFT && key != Keyboard.KEY_RSHIFT) {
			builder.append(c);
		}
	}
}
