package com.hbm.inventory.control_panel.types;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DataValueComposite extends DataValue {
	Map<String,String> dataMap = new Object2ObjectOpenHashMap<>();
	@Override
	public float getNumber() {
		return 0;
	}
	@Override
	public boolean getBoolean() {
		return false;
	}
	@Override
	public String toString() {
		return "< PACKED >";
	}
	@Override
	public String getValueOf(String key) {
		return dataMap.getOrDefault(key,"");
	}
	@Override
	public void setValueOf(String key,String value) {
		if (key.isBlank()) return;
		if (value.isBlank())
			dataMap.remove(key);
		else
			dataMap.put(key,value);
	}
	@Override
	public Set<String> values() {
		return dataMap.keySet();
	}
	@Override
	public DataType getType() {
		return DataType.COMPOSITE;
	}
	@Override
	public <E extends Enum<E>> E getEnum(Class<E> clazz) {
		throw new UnsupportedOperationException("DataValueComposite has no enum representation");
	}
	@Override
	public DataValue copy() {
		DataValueComposite data = new DataValueComposite();
		data.dataMap.putAll(dataMap);
		return data;
	}
	@Override
	public NBTBase writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		for (Entry<String,String> entry : dataMap.entrySet())
			compound.setString(entry.getKey(),entry.getValue());
		return compound;
	}
	@Override
	public void readFromNBT(NBTBase nbt) {
		if (nbt instanceof NBTTagCompound compound) {
			dataMap.clear();
			for (String s : compound.getKeySet())
				dataMap.put(s,compound.getString(s));
		}
	}

    public Map<String,String> snapshot() {
		return new HashMap<>(dataMap);
	}

    public static DataValueComposite fromLuaTable(Map<?,?> table) {
		DataValueComposite value = new DataValueComposite();
		for (Entry<?,?> e : table.entrySet()) {
			value.setValueOf(e.getKey().toString(), e.getValue().toString());
		}
		return value;
	}
}
