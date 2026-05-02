package com.hbm.render.loader;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IModelCustom
{
	String getType();
    void renderAll();
    void renderOnly(String... groupNames);
    void renderPart(String partName);
    void renderAllExcept(String... excludedGroupNames);
}