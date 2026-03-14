package com.hbm.inventory.control_panel;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagFloat;

import java.util.Objects;

public class DataValueFloat extends DataValue {

	public float num;
	
	public DataValueFloat(float f) {
		num = f;
	}

	public DataValueFloat(boolean b) {
		num = (b)? 1.0F : 0.0F;
	}

	@Override
	public float getNumber() {
		return num;
	}

	@Override
	public boolean getBoolean() {
		return num != 0;
	}

	@Override
	public String toString() {
		return Float.toString(num);
	}

	@Override
	public DataType getType(){
		return DataType.NUMBER;
	}
	
	
	@Override
	public <E extends Enum<E>> E getEnum(Class<E> clazz){
		int i = (int)num;
		E[] enums = clazz.getEnumConstants();
		if(i >= 0 && i < enums.length){
			return enums[i];
		}
		return enums[0];
	}

	@Override
	public DataValue copy() {
		return new DataValueFloat(num);
	}

	@Override
	public NBTBase writeToNBT(){
		return new NBTTagFloat(num);
	}

	@Override
	public void readFromNBT(NBTBase nbt){
		NBTTagFloat f = (NBTTagFloat)nbt;
		num = f.getFloat();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof DataValueFloat other)) return false;
		return Float.compare(other.num, num) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(num);
	}

}
