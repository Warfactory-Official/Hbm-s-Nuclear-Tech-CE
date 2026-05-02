package com.hbm.inventory.control_panel;

import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ItemList {

	public float posX;
	public float posY;
	public float width;
	public int textColor = 0xFFAFAFAF;
	public float r = 0.5F;
	public float g = 0.5F;
	public float b = 0.5F;
	public float alpha = 0.5F;
	
	public List<String> itemNames = new ArrayList<>();
	public Function<String, ItemList> action;
	
	public ItemList child;
	
	public boolean isClosed;
	
	public ItemList(float posX, float posY, float width, Function<String, ItemList> a){
		this.posX = posX;
		this.posY = posY;
		this.width = width;
		this.action = a;
		isClosed = false;
	}

    public ItemList addItems(String item) {
        itemNames.add(item);
        return this;
    }

    public ItemList addItems(String item1, String item2) {
        itemNames.add(item1);
        itemNames.add(item2);
        return this;
    }

	public ItemList addItems(String... items){
        Collections.addAll(itemNames, items);
		return this;
	}

    public ItemList addItems(Collection<String> items){
        itemNames.addAll(items);
        return this;
    }
	
	public void render(float mouseX, float mouseY){
		if(isClosed)
			return;
		if(child != null)
			child.render(mouseX, mouseY);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.white);
		NTMImmediate.INSTANCE.beginPositionTexColorQuads(1);
		NTMRenderHelper.drawGuiRectBatchedColor(posX, posY, 0, 0, width, itemNames.size()*6+4, 1, 1, r, g, b, alpha);
		int idx = getMouseoverIndex(mouseX, mouseY);
		if(idx != -1){
			NTMRenderHelper.drawGuiRectBatchedColor(posX, posY+idx*6+2, 0, 0, width, 5, 1, 1, r+0.1F, g+0.1F, b+0.1F, alpha);
		}
		NTMImmediate.INSTANCE.draw();
		GlStateManager.disableBlend();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY, 0);
		GL11.glScaled(0.4, 0.4, 1);
		GlStateManager.translate(-posX, -posY, 0);
		float y = posY + 7;
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		for(String s : itemNames){
			if(s.startsWith("{expandable}")){
				font.drawString(">", posX+width*2-6, y, textColor, false);
				s = s.substring(12);
			}
			font.drawString(s, posX+2, y, textColor, false);
			y += 15;
		}
		GlStateManager.popMatrix();
	}
	
	public boolean mouseClicked(float x, float y){
		if(isClosed)
			return false;
		boolean didAction = false;
		if(child != null && NTMRenderHelper.intersects2DBox(x, y, child.getBoundingBox())){
			return child.mouseClicked(x, y);
		} else {
			if(child != null){
				child.close();
				child = null;
			}
			float yPos = posY + 2;
			for(String s : itemNames){
				if(x > posX && x < posX+width && y > yPos && y < yPos + 6){
					ItemList newList = action.apply(s);
					if(newList != null){
						newList.posX = posX+width;
						newList.posY = yPos;
						child = newList;
					} else {
						didAction = true;
					}
				}
				yPos += 6;
			}
		}
		return didAction;
	}
	
	public int getMouseoverIndex(float x, float y){
		float yPos = posY + 2;
		for(int i = 0; i < itemNames.size(); i++){
			if(x > posX && x < posX+width && y > yPos && y < yPos + 5){
				return i;
			}
			yPos += 6;
		}
		return -1;
	}
	
	public float[] getBoundingBox(){
		if(child != null){
			float[] box = child.getBoundingBox();
			return new float[]{Math.min(posX, box[0]), Math.min(posY, box[1]), Math.max(posX+width, box[2]), Math.max(posY+itemNames.size()*6+4, box[3])};
		}
		return new float[]{posX, posY, posX+width, posY + itemNames.size()*6+4};
	}

	public void close(){
		isClosed = true;
	}
	
	public void open(){
		isClosed = false;
	}
}
