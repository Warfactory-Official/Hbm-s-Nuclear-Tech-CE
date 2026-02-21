package com.hbm.tileentity;


import com.hbm.main.MainRegistry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGUIProvider {

	Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z);
	@SideOnly(Side.CLIENT)
	GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z);

    // for mod devs: override this to return your mod instance, this makes spectator inspection work
    default Object getModInstanceForGui() {
        return MainRegistry.instance;
    }
}
