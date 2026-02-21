package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.modular.INodeLoader;
import com.hbm.inventory.control_panel.modular.NTMControlPanelRegistry;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {

	public float posX;
	public float posY;
	public float size;
	
	public List<NodeElement> otherElements = new ArrayList<>();
	public List<NodeConnection> inputs = new ArrayList<>();
	public List<NodeConnection> outputs = new ArrayList<>();

	public boolean cacheValid = false;
	public DataValue[] evalCache = null;
	
	public abstract DataValue evaluate(int idx);
	public abstract float[] getColor();
	public abstract String getDisplayName();
	
	public Node(float x, float y){
		setPosition(x, y);
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys){
		NBTTagCompound inputs = new NBTTagCompound();
		for(int i = 0; i < this.inputs.size(); i ++){
			inputs.setTag("con"+i, this.inputs.get(i).writeToNBT(new NBTTagCompound(), sys));
		}
		tag.setTag("in", inputs);
		
		NBTTagCompound outputs = new NBTTagCompound();
		for(int i = 0; i < this.outputs.size(); i ++){
			outputs.setTag("con"+i, this.outputs.get(i).writeToNBT(new NBTTagCompound(), sys));
		}
		tag.setTag("out", outputs);

		tag.setFloat("X", posX);
		tag.setFloat("Y", posY);
		tag.setFloat("S", size);
		return tag;
	}
	
	public void readFromNBT(NBTTagCompound tag, NodeSystem sys){
		this.inputs.clear();
		this.outputs.clear();
		NBTTagCompound inputs = tag.getCompoundTag("in");
		for(int i = 0; i < inputs.getKeySet().size(); i ++){
			NodeConnection c = new NodeConnection(null, this, 0, false, null, new DataValueFloat(0));
			c.readFromNBT(inputs.getCompoundTag("con"+i), sys);
			this.inputs.add(c);
		}
		
		NBTTagCompound outputs = tag.getCompoundTag("out");
		for(int i = 0; i < outputs.getKeySet().size(); i ++){
			NodeConnection c = new NodeConnection(null, this, 0, false, null, new DataValueFloat(0));
			c.readFromNBT(outputs.getCompoundTag("con"+i), sys);
			this.outputs.add(c);
		}
		
		this.posX = tag.getFloat("X");
		this.posY = tag.getFloat("Y");
		this.size = tag.getFloat("S");
		cacheValid = false;
	}
	
	public static Node nodeFromNBT(NBTTagCompound tag, NodeSystem sys){
		for (INodeLoader loader : NTMControlPanelRegistry.nbtNodeLoaders) {
			Node node = loader.nodeFromNBT(tag,sys);
			if (node != null)
				return node;
		}
		return null;
	}
	
	public void setPosition(float x, float y){
		this.posX = x;
		this.posY = y;
		for(NodeElement c : otherElements)
			c.resetOffset();
		for(NodeConnection c : inputs)
			c.resetOffset();
		for(NodeConnection c : outputs)
			c.resetOffset();
	}
	
	public void render(float mX, float mY, boolean isActive, boolean isSelected){
		float[] color;
		Minecraft.getMinecraft().getTextureManager().bindTexture(NodeSystem.node_tex);
		if(isActive || isSelected){
			color = isActive ? new float[]{0.7F, 0.7F, 0.7F} : new float[]{1F, 0.5F, 0.1F};
			float edge = 0.5F;
			Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			NTMRenderHelper.drawGuiRectBatchedColor(posX-edge, posY-edge, 0, 0.875F, 40+edge*2, 6+edge*2, 0.625F, 0.96875F, color[0], color[1], color[2], 1);
			NTMRenderHelper.drawGuiRectBatchedColor(posX-edge, posY+6, 0, 0.96875F, 40+edge*2, size+edge, 0.625F, 0.984375F, color[0], color[1], color[2], 1);
			NTMRenderHelper.drawGuiRectBatchedColor(posX-edge, posY+6+size, 0, 0.984375F, 40+edge*2, 1+edge, 0.625F, 1, color[0], color[1], color[2], 1);
			Tessellator.getInstance().draw();
		}
		color = getColor();
		Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		NTMRenderHelper.drawGuiRectBatchedColor(posX, posY, 0, 0, 40, 6, 0.625F, 0.09375F, 1, 1, 1, 1);
		NTMRenderHelper.drawGuiRectBatchedColor(posX, posY+6, 0, 0.09375F, 40, size, 0.625F, 0.109375F, 1, 1, 1, 1);
		NTMRenderHelper.drawGuiRectBatchedColor(posX, posY+6+size, 0, 0.109375F, 40, 1, 0.625F, 0.125F, 1, 1, 1, 1);
		NTMRenderHelper.drawGuiRectBatchedColor(posX, posY+1, 0, 0.125F, 40, 5, 0.625F, 0.203125F, color[0], color[1], color[2], 1);
		Tessellator.getInstance().draw();
		
		for(int i = inputs.size()-1; i >= 0; i --)
			inputs.get(i).render(mX, mY);
		for(int i = outputs.size()-1; i >= 0; i --)
			outputs.get(i).render(mX, mY);
		for(int i = otherElements.size()-1; i >= 0; i --)
			otherElements.get(i).render(mX, mY);

		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY, 0);
		GL11.glScaled(0.5, 0.5, 0.5);
		GlStateManager.translate(-posX, -posY, 0);
		font.drawString(getDisplayName(), posX+4, posY+3, 0xFF2F2F2F, false);
		GlStateManager.popMatrix();
	}
	
	public void drawConnections(float mX, float mY){
		for(NodeConnection n : inputs)
			n.drawLine(mX, mY);
		for(NodeConnection n : outputs)
			n.drawLine(mX, mY);
	}
	
	public void recalcSize(){
		this.size = (otherElements.size() + inputs.size() + outputs.size())*8+2;
		this.setPosition(posX, posY);
	}
	
	public boolean onClick(float x, float y){
		for(NodeElement n : otherElements){
			if(n.onClick(x, y))
				return true;
		}
		return false;
	}

	public float[] getBoundingBox(){
		return new float[]{posX, posY, posX+40, posY+6+size};
	}
	
	public float[] getExtendedBoundingBox(){
		return new float[]{posX-2, posY, posX+42, posY+6+size};
	}
	
}
