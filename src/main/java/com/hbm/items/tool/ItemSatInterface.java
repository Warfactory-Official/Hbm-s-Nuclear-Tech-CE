package com.hbm.items.tool;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.gui.GUIScreenSatCoord;
import com.hbm.inventory.gui.GUIScreenSatInterface;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.SatPanelPacket;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSatInterface extends ItemSatellite implements IGUIProvider {

	@SideOnly(Side.CLIENT)
	public static Satellite currentSat;
	
	public ItemSatInterface(String s) {
		super(s);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
		if(world.isRemote) {
			BlockPos pos = player.getPosition();
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
		}
		
		return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(handIn));
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		//Drillgon200: what in the world
		/*if(!world.isRemote) {
		    SatelliteSavedData data = (SatelliteSavedData)entity.world.perWorldStorage.loadData(SatelliteSavedData.class, "satellites");
			
		    if(data != null) {
			    for(int j = 0; j < data.satellites.size(); j++) {
			    	PacketDispatcher.wrapper.sendToAll(new SatPanelPacket(data.satellites.get(j)));
			    }
		    }
		}*/
		if(world.isRemote || !(entity instanceof EntityPlayerMP))
    		return;
    	
    	if(((EntityPlayerMP)entity).getHeldItemMainhand() != stack)
    		return;
    	
    	Satellite sat = SatelliteSavedData.getData(world).getSatFromFreq(getFreq(stack));
    	
    	if(sat != null && entity.ticksExisted % 2 == 0) {
    		PacketThreading.createSendToThreadedPacket(new SatPanelPacket(sat), (EntityPlayerMP) entity);
    	}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(this == ModItems.sat_interface) return new GUIScreenSatInterface(player);
		else return new GUIScreenSatCoord(player);
	}
}
