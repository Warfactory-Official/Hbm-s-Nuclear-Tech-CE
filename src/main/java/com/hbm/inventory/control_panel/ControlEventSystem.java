package com.hbm.inventory.control_panel;

import com.hbm.Tags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

import java.util.*;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class ControlEventSystem {

	private static Map<World, ControlEventSystem> systems = new HashMap<>();
	
	private Set<IControllable> allControllables = new HashSet<>();
	private Set<IControllable> tickables = new HashSet<>();
	private Map<String, Map<BlockPos, IControllable>> controllablesByEventName = new HashMap<>();
	private Map<BlockPos, Set<IControllable>> positionSubscriptions = new HashMap<>();
	
	public void addControllable(IControllable c){
		if(allControllables.contains(c))
			return;
		for(String s : c.getInEvents()){
			if(s.equals("tick")){
				tickables.add(c);
				continue;
			}
			if(!controllablesByEventName.containsKey(s)){
				controllablesByEventName.put(s, new HashMap<>());
			}
			controllablesByEventName.get(s).put(c.getControlPos(), c);
		}
		allControllables.add(c);
	}
	
	public void removeControllable(IControllable c){
		for(String s : c.getInEvents()){
			if(s.equals("tick")){
				tickables.remove(c);
				continue;
			}
			controllablesByEventName.get(s).remove(c);
		}
		allControllables.remove(c);
	}
	
	public boolean isValid(IControllable c){
		return allControllables.contains(c);
	}
	
	public void subscribeTo(IControllable subscriber, IControllable target){
		if(!positionSubscriptions.containsKey(target.getControlPos())){
			positionSubscriptions.put(target.getControlPos(), new HashSet<>());
		}
		if(!positionSubscriptions.get(target).contains(subscriber))
			positionSubscriptions.get(target.getControlPos()).add(subscriber);
	}
	
	public void subscribeTo(IControllable subscriber, BlockPos target){
		if(!positionSubscriptions.containsKey(target)){
			positionSubscriptions.put(target, new HashSet<>());
		}
		if(!positionSubscriptions.get(target).contains(subscriber))
			positionSubscriptions.get(target).add(subscriber);
	}
	
	public void unsubscribeFrom(IControllable subscriber, IControllable target){
		if(positionSubscriptions.containsKey(target.getControlPos())){
			positionSubscriptions.get(target.getControlPos()).remove(subscriber);
			if(positionSubscriptions.get(target.getControlPos()).isEmpty())
				positionSubscriptions.remove(target.getControlPos());
		}
	}
	
	public void unsubscribeFrom(IControllable subscriber, BlockPos target){
		if(positionSubscriptions.containsKey(target)){
			positionSubscriptions.get(target).remove(subscriber);
			if(positionSubscriptions.get(target).isEmpty())
				positionSubscriptions.remove(target);
		}
	}
	
	public void broadcastEvent(BlockPos from, ControlEvent evt, BlockPos pos){
		Map<BlockPos, IControllable> map = controllablesByEventName.get(evt.name);
		if(map == null)
			return;
		IControllable c = map.get(pos);
		if(c != null) {
			c.receiveEvent(from, evt);
		}
	}
	
	public void broadcastEvent(BlockPos from, ControlEvent evt, Collection<BlockPos> positions){
		Map<BlockPos, IControllable> map = controllablesByEventName.get(evt.name);
		if(map == null)
			return;
		if(positions == null){
			for(IControllable c : map.values()){
				c.receiveEvent(from, evt);
			}
		} else {
			for(BlockPos pos : positions){
				IControllable c = map.get(pos);
				if(c != null){
					c.receiveEvent(from, evt);
				}
			}
		}
	}
	
	public void broadcastEvent(BlockPos from, ControlEvent c){
		broadcastEvent(from, c, (Collection<BlockPos>)null);
	}
	
	public void broadcastToSubscribed(IControllable ctrl, ControlEvent evt){
		Set<IControllable> subscribed = positionSubscriptions.get(ctrl.getControlPos());
		if(subscribed == null)
			return;
		for(IControllable sub : subscribed){
			sub.receiveEvent(ctrl.getControlPos(), evt);
		}
	}
	
	public static ControlEventSystem get(World w){
		if(!systems.containsKey(w))
			systems.put(w, new ControlEventSystem());
		return systems.get(w);
	}
	
	@SubscribeEvent
	public static void tick(WorldTickEvent evt){
		if(systems.containsKey(evt.world)){
			ControlEventSystem s = systems.get(evt.world);
			for(IControllable c : s.tickables){
				c.receiveEvent(c.getControlPos(), ControlEvent.newEvent("tick").setVar("time", evt.world.getTotalWorldTime()));
			}
		}
	}
	
	@SubscribeEvent
	public static void worldUnload(WorldEvent.Unload evt){
		systems.remove(evt.getWorld());
	}
}
