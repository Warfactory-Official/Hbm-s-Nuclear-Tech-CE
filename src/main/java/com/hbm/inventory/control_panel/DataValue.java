package com.hbm.inventory.control_panel;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagString;

public abstract class DataValue {
	public abstract float getNumber();
	public abstract boolean getBoolean();
	public abstract String toString();
	public abstract DataType getType();
	public abstract <E extends Enum<E>> E getEnum(Class<E> clazz);
	public abstract NBTBase writeToNBT();
	public abstract void readFromNBT(NBTBase nbt);
	
	public static DataValue newFromNBT(NBTBase base){
		DataValue val = null;
		try {
			if(base instanceof NBTTagCompound) {
				val = new DataValueEnum<>(null);
				val.readFromNBT(base);
			} else if(base instanceof NBTTagFloat) {
				val = new DataValueFloat(0);
				val.readFromNBT(base);
			} else if(base instanceof NBTTagString) {
				val = new DataValueString("");
				val.readFromNBT(base);
			}
		} catch(Exception x) {
			x.printStackTrace();
			return null;
		}
		return val;
	}
	
	public static enum DataType {
		GENERIC(new float[]{0.5F, 0.5F, 0.5F}),
		NUMBER(new float[]{0.4F, 0.6F, 0}),
		STRING(new float[]{0, 1, 1}),
		ENUM(new float[]{0.29F, 0, 0.5F});

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
