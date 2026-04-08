package com.hbm.inventory.control_panel;

import com.hbm.Tags;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

import java.util.Collection;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class ControlEventSystem {

	private static final Map<World, ControlEventSystem> systems = new Reference2ObjectOpenHashMap<>();
	
	private final ObjectOpenHashSet<IControllable> allControllables = new ObjectOpenHashSet<>();
	private final ObjectOpenHashSet<IControllable> tickables = new ObjectOpenHashSet<>();
	private final Map<String, Long2ObjectOpenHashMap<IControllable>> controllablesByEventName = new Object2ObjectOpenHashMap<>();
	private final Long2ObjectOpenHashMap<ObjectOpenHashSet<IControllable>> positionSubscriptions = new Long2ObjectOpenHashMap<>();
	
	public void addControllable(IControllable c){
		if(allControllables.contains(c))
			return;
        long controlPos = c.getControlPos().toLong();
		for(String s : c.getInEvents()){
			if(s.equals("tick")){
				tickables.add(c);
				continue;
			}
			Long2ObjectOpenHashMap<IControllable> map = controllablesByEventName.get(s);
			if(map == null){
				map = new Long2ObjectOpenHashMap<>();
				controllablesByEventName.put(s, map);
			}
			map.put(controlPos, c);
		}
		allControllables.add(c);
	}
	
	public void removeControllable(IControllable c){
        long controlPos = c.getControlPos().toLong();
		for(String s : c.getInEvents()){
			if(s.equals("tick")){
				tickables.remove(c);
				continue;
			}
			Long2ObjectOpenHashMap<IControllable> map = controllablesByEventName.get(s);
			if(map != null) {
				map.remove(controlPos);
				if(map.isEmpty()) {
					controllablesByEventName.remove(s);
				}
			}
		}
		// Keep subscribers for this target position so they survive ordinary unload/reload cycles.
		// Only remove the unloaded controllable from lists where it was itself the subscriber.
		for(var it = positionSubscriptions.long2ObjectEntrySet().fastIterator(); it.hasNext(); ) {
			var entry = it.next();
			ObjectOpenHashSet<IControllable> subscribers = entry.getValue();
			subscribers.remove(c);
			if(subscribers.isEmpty()) {
				it.remove();
			}
		}
		allControllables.remove(c);
	}
	
	public boolean isValid(IControllable c){
		return allControllables.contains(c);
	}
	
	public void subscribeTo(IControllable subscriber, IControllable target){
		subscribeTo(subscriber, target.getControlPos());
	}
	
	public void subscribeTo(IControllable subscriber, BlockPos target){
        long serializedTarget = target.toLong();
		ObjectOpenHashSet<IControllable> subscribers = positionSubscriptions.get(serializedTarget);
		if(subscribers == null){
			subscribers = new ObjectOpenHashSet<>();
			positionSubscriptions.put(serializedTarget, subscribers);
		}
		subscribers.add(subscriber);
	}
	
	public void unsubscribeFrom(IControllable subscriber, IControllable target){
		unsubscribeFrom(subscriber, target.getControlPos());
	}
	
	public void unsubscribeFrom(IControllable subscriber, BlockPos target){
        long serializedTarget = target.toLong();
		ObjectOpenHashSet<IControllable> subscribers = positionSubscriptions.get(serializedTarget);
		if(subscribers != null){
			subscribers.remove(subscriber);
			if(subscribers.isEmpty()) {
				positionSubscriptions.remove(serializedTarget);
			}
		}
	}
	
	public void broadcastEvent(BlockPos from, ControlEvent evt, BlockPos pos){
		Long2ObjectOpenHashMap<IControllable> map = controllablesByEventName.get(evt.name);
		if(map == null)
			return;
        IControllable c = map.get(pos.toLong());
		if(c != null) {
			c.receiveEvent(from, evt);
		}
	}
	
	public void broadcastEvent(BlockPos from, ControlEvent evt, Collection<BlockPos> positions){
		Long2ObjectOpenHashMap<IControllable> map = controllablesByEventName.get(evt.name);
		if(map == null)
			return;
		if(positions == null){
			for(IControllable c : map.values()){
				c.receiveEvent(from, evt);
			}
		} else {
			for(BlockPos pos : positions){
                IControllable c = map.get(pos.toLong());
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
        ObjectOpenHashSet<IControllable> subscribed = positionSubscriptions.get(ctrl.getControlPos().toLong());
		if(subscribed == null)
			return;
		for(IControllable sub : subscribed){
			sub.receiveEvent(ctrl.getControlPos(), evt);
		}
	}
	
	public static ControlEventSystem get(World w){
		ControlEventSystem system = systems.get(w);
		if(system == null) {
			system = new ControlEventSystem();
			systems.put(w, system);
		}
		return system;
	}
	
	@SubscribeEvent
	public static void tick(WorldTickEvent evt){
		if(evt.phase != Phase.START || evt.world.isRemote)
			return;
		ControlEventSystem s = systems.get(evt.world);
		if(s != null){
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
