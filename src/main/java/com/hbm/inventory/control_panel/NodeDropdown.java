package com.hbm.inventory.control_panel;

import com.hbm.inventory.control_panel.nodes.Node;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;

import java.util.function.Function;
import java.util.function.Supplier;

public class NodeDropdown extends NodeElement {

	public ItemList list;
	public Supplier<String> nameGetter;
	
	public NodeDropdown(Node parent, int idx, Function<String, ItemList> func, Supplier<String> nameGetter){
		super(parent, idx);
		list = new ItemList(0, 0, 32, func);
		list.alpha = 0.95F;
		list.r = list.g = list.b = 0.3F;
		list.textColor = 0xFFDFDFDF;
		list.close();
		resetOffset();
		this.nameGetter = nameGetter;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys){
		throw new RuntimeException("Dropdown node should not be serialized!");
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag, NodeSystem sys){
		throw new RuntimeException("Dropdown node should not be serialized!");
	}
	
	@Override
	public void resetOffset(){
		super.resetOffset();
		if(list != null){
			list.posX = this.offsetX+4;
			list.posY = this.offsetY+14;
		}
	}
	
	public void setOffset(float x, float y){
		this.offsetX = x;
		this.offsetY = y;
		list.posX = x+4;
		list.posY = y+14;
	}
	
	@Override
	public void render(float mX, float mY){
		Minecraft.getMinecraft().getTextureManager().bindTexture(NodeSystem.node_tex);
		NTMImmediate.INSTANCE.beginPositionTexColorQuads(1);
		float x = offsetX+4;
		float y = offsetY+8;
		NTMRenderHelper.drawGuiRectBatchedColor(x, y, 0F, 0.890625F, 32, 6, 0.609375F, 0.984375F, 1, 1, 1, 1);
		NTMImmediate.INSTANCE.draw();
		list.render(mX, mY);
		
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GL11.glScaled(0.35, 0.35, 0.35);
		GlStateManager.translate(-x, -y, 0);
		String s = nameGetter.get();
		font.drawString(s, x+43-font.getStringWidth(s)/2, y+5, 0xFF5F5F5F, false);
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		float width = font.getCharWidth('^');
		float height = font.FONT_HEIGHT;
		x = x+27.5F;
		y = y+2.5F;
		GlStateManager.translate(x+width*0.4F, y+height*0.2F, 0);
		GL11.glScaled(0.5, 0.5, 0.5);
		if(list.isClosed)
			GL11.glRotated(180, 0, 0, 1);
		GlStateManager.translate(-x-width*0.4F, -y-height*0.2F, 0);
		font.drawString("^", x, y, 0xFF5F5F5F, false);
		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean onClick(float x, float y){
		if(!list.isClosed && NTMRenderHelper.intersects2DBox(x, y, list.getBoundingBox())){
			if(list.mouseClicked(x, y)){
				list.close();
				return true;
			}
		}
		if(NTMRenderHelper.intersects2DBox(x, y, getBox())){
			if(list.isClosed){
				list.open();
			} else {
				list.close();
			}
			return true;
		}
		return false;
	}
	
	public float[] getBox(){
		return new float[]{3+offsetX, -3+offsetY+10, 37+offsetX, 3+offsetY+10};
	}
	
}
