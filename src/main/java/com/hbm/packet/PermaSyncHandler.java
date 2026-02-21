package com.hbm.packet;

import com.hbm.handler.ImpactWorldHandler;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionData;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.potion.HbmPotion;
import com.hbm.saveddata.TomSaveData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

/**
 * Utility for permanently synchronizing values every tick with a player in the given context of a world.
 * Uses the Byte Buffer directly instead of NBT to cut back on unnecessary data.
 * @author hbm
 */
public class PermaSyncHandler {
	
	public static IntOpenHashSet boykissers = new IntOpenHashSet();
	public static float[] pollution = new float[PollutionType.VALUES.length];

	public static void writePacket(ByteBuf buf, World world, EntityPlayerMP player) {
		
		/// TOM IMPACT DATA ///
		TomSaveData data = TomSaveData.forWorld(world);
		buf.writeFloat(data.fire);
		buf.writeFloat(data.dust);
		buf.writeBoolean(data.impact);
		buf.writeLong(data.time);
		/// TOM IMPACT DATA ///
		
		/// SHITTY MEMES ///
		IntArrayList ids = new IntArrayList();
		for(EntityPlayer p : world.playerEntities) {
            if(p.isPotionActive(HbmPotion.death)) {
				ids.add(p.getEntityId());
			}
		}
		buf.writeShort((short) ids.size());
        IntListIterator iterator = ids.iterator();
        while (iterator.hasNext()) {
            int id = iterator.nextInt();
            buf.writeInt(id);
        }
        /// SHITTY MEMES ///

		/// POLLUTION ///
		PollutionData pollution = PollutionHandler.getPollutionData(world, player.getPosition());
		if(pollution == null) pollution = new PollutionData();
		for(int i = 0; i < PollutionType.VALUES.length; i++) {
			buf.writeFloat(pollution.pollution[i]);
		}
		/// POLLUTION ///

		/// SATELLITES ///
		// Only syncs data required for rendering satellites on the client
		Int2ObjectOpenHashMap<Satellite> sats = SatelliteSavedData.getData(world).sats;
		buf.writeInt(sats.size());
        ObjectIterator<Int2ObjectMap.Entry<Satellite>> iter = sats.int2ObjectEntrySet().fastIterator();
        while (iter.hasNext()) {
            Int2ObjectMap.Entry<Satellite> entry = iter.next();
            buf.writeInt(entry.getIntKey());
            buf.writeInt(entry.getValue().getID());
        }
        /// SATELLITES ///

		/// RIDING DESYNC FIX ///
		if(player.getRidingEntity() != null) {
			buf.writeInt(player.getRidingEntity().getEntityId());
		} else {
			buf.writeInt(-1);
		}
		/// RIDING DESYNC FIX ///
	}
	
	public static void readPacket(ByteBuf buf, World world, EntityPlayer player) {

		/// TOM IMPACT DATA ///
		ImpactWorldHandler.lastSyncWorld = player.world;
		ImpactWorldHandler.fire = buf.readFloat();
		ImpactWorldHandler.dust = buf.readFloat();
		ImpactWorldHandler.impact = buf.readBoolean();
		ImpactWorldHandler.time = buf.readLong();
		/// TOM IMPACT DATA ///

		/// SHITTY MEMES ///
		boykissers.clear();
		int ids = buf.readShort();
		for(int i = 0; i < ids; i++) boykissers.add(buf.readInt());
		/// SHITTY MEMES ///

		/// POLLUTION ///
		for(int i = 0; i < PollutionType.VALUES.length; i++) {
			pollution[i] = buf.readFloat();
		}
		/// POLLUTION ///

		/// SATELLITES ///
		int satSize = buf.readInt();
		Int2ObjectOpenHashMap<Satellite> sats = new Int2ObjectOpenHashMap<>();
		for(int i = 0; i < satSize; i++) {
			sats.put(buf.readInt(), Satellite.create(buf.readInt()));
		}
		SatelliteSavedData.setClientSats(sats);
		/// SATELLITES ///

		/// RIDING DESYNC FIX ///
		int ridingId = buf.readInt();
		if(ridingId >= 0 && player.getRidingEntity() == null) {
			Entity entity = world.getEntityByID(ridingId);
			player.startRiding(entity);
		}
		/// RIDING DESYNC FIX ///
	}
}
