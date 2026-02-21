package com.hbm.render.loader;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IModelCustom
{
	public String getType();
    public void renderAll();
    public void renderOnly(String... groupNames);
    public void renderPart(String partName);
    public void renderAllExcept(String... excludedGroupNames);

}