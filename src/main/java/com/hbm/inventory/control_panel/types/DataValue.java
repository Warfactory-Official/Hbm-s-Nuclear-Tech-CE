package com.hbm.inventory.control_panel.types;

import com.hbm.main.MainRegistry;
import net.minecraft.nbt.*;

import java.util.Collections;
import java.util.Set;

public abstract class DataValue {
	public abstract float getNumber();
	public abstract boolean getBoolean();
	public abstract String toString();
	public String getValueOf(String key) {
		return "ERROR";
	}
	public void setValueOf(String key,String value) { }
	public Set<String> values() {
		return Collections.emptySet();
	}
	public abstract DataType getType();
	public abstract <E extends Enum<E>> E getEnum(Class<E> clazz);
	public abstract DataValue copy();
	public abstract NBTBase writeToNBT();
	public abstract void readFromNBT(NBTBase nbt);
	
	public static DataValue newFromNBT(NBTBase base){
		DataValue val = null;
		try {
			if(base instanceof NBTTagCompound tag) {
				if (tag.hasKey("ordinal") && tag.getTag("ordinal") instanceof NBTTagInt) {
					val = new DataValueEnum<>(null);
				} else {
					val = new DataValueComposite();
				}
				val.readFromNBT(base);
			} else if(base instanceof NBTTagFloat) {
				val = new DataValueFloat(0);
				val.readFromNBT(base);
			} else if(base instanceof NBTTagString) {
				val = new DataValueString("");
				val.readFromNBT(base);
			}
		} catch(Exception x) {
			MainRegistry.logger.error("Failed to deserialize control-panel data value from NBT type {}", base.getClass().getName(), x);
			return null;
		}
		return val;
	}
	
	public static enum DataType {
		GENERIC(new float[]{0.5F, 0.5F, 0.5F}),
		NUMBER(new float[]{0.4F, 0.6F, 0}),
		STRING(new float[]{0, 1, 1}),
		ENUM(new float[]{0.5F, 0.29F, 0}),
		COMPOSITE(new float[]{1F,0.399F,0.842F});

        public static final DataType[] VALUES = values();
		private float[] color;
		
		private DataType(float[] color){
			this.color = color;
		}
		
		public float[] getColor(){
			return color;
		}
	}
}
