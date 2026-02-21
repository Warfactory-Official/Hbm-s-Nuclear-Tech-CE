package com.hbm.inventory.control_panel;

import com.hbm.inventory.control_panel.DataValue.DataType;
import com.hbm.inventory.control_panel.nodes.Node;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nonnull;

public class NodeConnection extends NodeElement implements ITypableNode {

	public String name;
	//These should always be null and -1 for output nodes.
	//Bad code design? Maybe, but I didn't realize it until I was almost done with the connection system.
	public Node connection;
	public int connectionIndex;
	
	public DataValue defaultValue;
	public boolean drawsLine = false;
	public boolean isInput;
	public DataType type;
	public StringBuilder builder;
	public boolean isTyping;
	
	public NodeDropdown enumSelector = null;
	
	public NodeConnection(String name, Node p, int idx, boolean isInput, DataType type, @Nonnull DataValue defaultVal){
		super(p, idx);
		this.name = name;
		this.connection = null;
		setDefault(defaultVal);
		this.connectionIndex = -1;
		this.isInput = isInput;
		this.type = type;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys){
		super.writeToNBT(tag, sys);
//		tag.setString("eleType", "connection");
		tag.setString("name", name);
		tag.setInteger("Ci", connectionIndex);
		tag.setInteger("Ni", sys.nodes.indexOf(connection));
		tag.setBoolean("in", isInput);
		tag.setInteger("T", type.ordinal());
		tag.setTag("D", defaultValue.writeToNBT());
		tag.setBoolean("DL", drawsLine);
		return tag;
	}
	
	public void readFromNBT(NBTTagCompound tag, NodeSystem sys){
		super.readFromNBT(tag, sys);
		name = tag.getString("name");
		connectionIndex = tag.getInteger("Ci");
		int nodeIdx = tag.getInteger("Ni");
		if(nodeIdx == -1){
			connection = null;
		} else {
			connection = sys.nodes.get(nodeIdx);
		}
		isInput = tag.getBoolean("in");
		type = DataType.VALUES[tag.getInteger("T") % DataType.VALUES.length];
		defaultValue = DataValue.newFromNBT(tag.getTag("D"));
		if(defaultValue == null){
			type = DataType.NUMBER;
			defaultValue = new DataValueFloat(0);
		}
		drawsLine = tag.getBoolean("DL");
		builder = null;
		isTyping = false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setDefault(@Nonnull DataValue defaultVal){
		this.defaultValue = defaultVal;
		type = defaultVal.getType();
		if(type == DataType.ENUM){
			enumSelector = new NodeDropdown(parent, this.index, s -> {
				Enum<?>[] vals = ((DataValueEnum<?>)defaultVal).getPossibleValues();
				for(Enum<?> e : vals){
					if(e.name().equals(s)){
						defaultValue = new DataValueEnum(e);
						return null;
					}
				}
				return null;
			}, ()->defaultValue.toString());
			Enum<?>[] vals = ((DataValueEnum<?>)defaultVal).getPossibleValues();
			for(Enum<?> e : vals)
				enumSelector.list.addItems(e.name());
			enumSelector.setOffset(offsetX, offsetY);
		} else {
			enumSelector = null;
		}
	}
	
	public NodeConnection removeConnection(){
		//Will only run for input nodes as well, since the output node doesn't maintain a connection
		if(connection != null){
			NodeConnection n = connection.outputs.get(connectionIndex);
			drawsLine = false;
			connection = null;
			connectionIndex = -1;
			return n;
		}
		return null;
	}

	public NodeConnection setData(Node connection, int connectionIndex, boolean drawsLine) {
		this.connection = connection;
		this.connectionIndex = connectionIndex;
		this.drawsLine = drawsLine;
		return this;
	}
	
	@Override
	public void resetOffset(){
		super.resetOffset();
		offsetY += parent.otherElements.size()*8;
		if(isInput){
			offsetY += parent.outputs.size()*8;
		}
		if(enumSelector != null){
			enumSelector.setOffset(offsetX, offsetY);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void render(float mX, float mY){
		float[] color = type.getColor();
		Minecraft.getMinecraft().getTextureManager().bindTexture(NodeSystem.node_tex);
		Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		float x = offsetX+38 + (isInput ? -40 : 0);
		float y = offsetY+8;
		NTMRenderHelper.drawGuiRectBatchedColor(x, y, 0.625F, 0, 4, 4, 0.6875F, 0.0625F, color[0], color[1], color[2], 1);
		color = NTMRenderHelper.intersects2DBox(mX, mY, this.getValueBox()) && !isTyping ? new float[]{1, 1, 1} : new float[]{0.6F, 0.6F, 0.6F};
		if(isInput){
			if(enumSelector != null){
				Tessellator.getInstance().draw();
				enumSelector.render(mX, mY);
				return;
			}
			if(connection == null){
				NTMRenderHelper.drawGuiRectBatchedColor(x, y-1, 0, 0.203125F, 40, 6, 0.625F, 0.296875F, color[0], color[1], color[2], 1);
			}
		}
		Tessellator.getInstance().draw();
		
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GL11.glScaled(0.4, 0.4, 0.4);
		GlStateManager.translate(-x, -y, 0);
		if(isTyping){
			String s = builder.toString();
			font.drawString(s + (Minecraft.getMinecraft().world.getTotalWorldTime()%20 > 10 ? "_" : ""), x+(isInput ? 16 : -font.getStringWidth(name)-1), y+1F, 0xFFAFAFAF, false);
		} else {
			int hex = isInput ? 0xFFAFAFAF : 0xFF2F2F2F;
			font.drawString(name, x+(isInput ? 16 : -font.getStringWidth(name)-1), y+1F, hex, false);
			if(isInput && connection == null){
				String s = defaultValue.toString();
				if(s.length() > 5){
					s = s.substring(0, 5);
				}
				font.drawString(s, x+94-font.getStringWidth(s), y+1, 0xFFAFAFAF, false);
			}
		}
		GlStateManager.popMatrix();
	}
	
	@SideOnly(Side.CLIENT)
	public void drawLine(float mouseX, float mouseY){
		if(drawsLine){
			BufferBuilder buf = Tessellator.getInstance().getBuffer();
			buf.pos(offsetX + (isInput ? 0 : 40), offsetY+10, 0).endVertex();
			if(connectionIndex == -1 || !isInput){
				buf.pos(mouseX, mouseY, 0).endVertex();
			} else {
				NodeConnection pair = (isInput ? connection.outputs : connection.inputs).get(connectionIndex);
				buf.pos(pair.offsetX + (pair.isInput ? 0 : 40), pair.offsetY+10, 0).endVertex();
			}
		}
	}
	
	//minX, minY, maxX, maxY
	@SideOnly(Side.CLIENT)
	public float[] getPortBox(){
		float oX = offsetX;
		if(!isInput)
			oX += 40;
		return new float[]{-2+oX, -2+offsetY+10, 2+oX, 2+offsetY+10};
	}
	
	@SideOnly(Side.CLIENT)
	public float[] getValueBox(){
		if(enumSelector != null)
			return enumSelector.getBox();
		return new float[]{3+offsetX, -3+offsetY+10, 37+offsetX, 3+offsetY+10};
	}

	public DataValue evaluate(){
		if(connection == null || connectionIndex == -1){
		} else {
			return connection.evaluate(connectionIndex);
		}
		return defaultValue;
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
		builder.append(defaultValue.toString());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SideOnly(Side.CLIENT)
	@Override
	public void stopTyping(){
		DataValue val = new DataValueString(builder.toString());
		builder = null;
		isTyping = false;
		switch(type){
			case GENERIC:
			case STRING:
				defaultValue = val;
				break;
			case ENUM:
				DataValueEnum<?> def = (DataValueEnum<?>)defaultValue;
				Enum<?>[] possibleVals = def.getPossibleValues();
				for(Enum<?> e : possibleVals){
					if(e.name().equalsIgnoreCase(val.toString())){
						defaultValue = new DataValueEnum(e);
						break;
					}
				}
				int idx = Math.abs(((int)val.getNumber()))%possibleVals.length;
				defaultValue = new DataValueEnum(possibleVals[idx]);
				break;
			case NUMBER:
				defaultValue = new DataValueFloat(val.getNumber());
		}
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
