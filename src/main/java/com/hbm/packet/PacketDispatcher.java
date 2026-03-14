package com.hbm.packet;

import com.hbm.Tags;
import com.hbm.api.network.IPacketRegisterListener;
import com.hbm.main.NetworkHandler;
import com.hbm.packet.toclient.*;
import com.hbm.packet.toserver.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

public class PacketDispatcher {
	
	public static final NetworkHandler wrapper = new NetworkHandler(Tags.MODID);
	public static final List<IPacketRegisterListener> LISTENERS = new ArrayList<>();
	
	public static void registerPackets(){
		int i = 0;

		//Send chunk radiation packet to individual players
		wrapper.registerMessage(SurveyPacket.Handler.class, SurveyPacket.class, i++, Side.CLIENT);
		//Packet for rendering of rubble
		wrapper.registerMessage(ParticleBurstPacket.Handler.class, ParticleBurstPacket.class, i++, Side.CLIENT);
		//Sounds packets
		wrapper.registerMessage(LoopedSoundPacket.Handler.class, LoopedSoundPacket.class, i++, Side.CLIENT);
		//Particle packet
		wrapper.registerMessage(AuxParticlePacket.Handler.class, AuxParticlePacket.class, i++, Side.CLIENT);
		//Universal package for machine gauges and states but for longs
		wrapper.registerMessage(AuxLongPacket.Handler.class, AuxLongPacket.class, i++, Side.CLIENT);
		//Universal button packet
		wrapper.registerMessage(AuxButtonPacket.Handler.class, AuxButtonPacket.class, i++, Side.SERVER);
		//Packet for sending designator data to server
		wrapper.registerMessage(ItemDesignatorPacket.Handler.class, ItemDesignatorPacket.class, i++, Side.SERVER);
		//New particle packet
		wrapper.registerMessage(EnumParticlePacket.Handler.class, EnumParticlePacket.class, i++, Side.CLIENT);
		//Gun firing packet
		wrapper.registerMessage(GunButtonPacket.Handler.class, GunButtonPacket.class, i++, Side.SERVER);
		// <Insert good comment here>
		wrapper.registerMessage(RailgunCallbackPacket.Handler.class, RailgunCallbackPacket.class, i++, Side.CLIENT);
		// Sets last fire time for railgun
		wrapper.registerMessage(RailgunFirePacket.Handler.class, RailgunFirePacket.class, i++, Side.CLIENT);
		//Siren packet for looped sounds
		wrapper.registerMessage(TESirenPacket.Handler.class, TESirenPacket.class, i++, Side.CLIENT);
		//Door packet for animations and stuff
		wrapper.registerMessage(TEVaultPacket.Handler.class, TEVaultPacket.class, i++, Side.CLIENT);
		//Machine type for marker rendering
		wrapper.registerMessage(TEStructurePacket.Handler.class, TEStructurePacket.class, i++, Side.CLIENT);
		//Packet to send missile multipart information to TEs
		wrapper.registerMessage(TEMissileMultipartPacket.Handler.class, TEMissileMultipartPacket.class, i++, Side.CLIENT);
		//Signals server to consume items and create template
		wrapper.registerMessage(ItemFolderPacket.Handler.class, ItemFolderPacket.class, i++, Side.SERVER);
		//Signals server to buy offer from bobmazon
		wrapper.registerMessage(ItemBobmazonPacket.Handler.class, ItemBobmazonPacket.class, i++, Side.SERVER);
		//Update packet for force field
		wrapper.registerMessage(TEFFPacket.Handler.class, TEFFPacket.class, i++, Side.CLIENT);
		//Packet for updating entities being zapped
		wrapper.registerMessage(TETeslaPacket.Handler.class, TETeslaPacket.class, i++, Side.CLIENT);
		//Aux Particle Packet, New Technology: like the APP but with NBT
		wrapper.registerMessage(AuxParticlePacketNT.Handler.class, AuxParticlePacketNT.class, i++, Side.CLIENT);
		//Packet to send ByteBuf data to tile entities(faster than NBT one but not really convenient)
		wrapper.registerMessage(BufPacket.Handler.class, BufPacket.class, i++, Side.CLIENT);
		//Packet to send sat info to players
		wrapper.registerMessage(SatPanelPacket.Handler.class, SatPanelPacket.class, i++, Side.CLIENT);
		//Signals server to do coord based satellite stuff
		wrapper.registerMessage(SatCoordPacket.Handler.class, SatCoordPacket.class, i++, Side.SERVER);
		//Triggers gun animations of the client
		wrapper.registerMessage(GunAnimationPacketSedna.Handler.class, GunAnimationPacketSedna.class, i++, Side.CLIENT);
		//Signals server to perform orbital strike, among other things
		wrapper.registerMessage(SatLaserPacket.Handler.class, SatLaserPacket.class, i++, Side.SERVER);
		//Sets the gun animation on server because there is no client side nbt tag
		wrapper.registerMessage(SetGunAnimPacket.Handler.class, SetGunAnimPacket.class, i++, Side.SERVER);
		//Triggers gun animations of the client
		wrapper.registerMessage(GunAnimationPacket.Handler.class, GunAnimationPacket.class, i++, Side.CLIENT);
		//Unhooks the entity when the player jumps
		wrapper.registerMessage(MeathookJumpPacket.Handler.class, MeathookJumpPacket.class, i++, Side.SERVER);
		//Resets any sideways acceleration when the meathook unhooks
		wrapper.registerMessage(MeathookResetStrafePacket.Handler.class, MeathookResetStrafePacket.class, i++, Side.CLIENT);
		//Gernal packet for sending door states
		wrapper.registerMessage(TEDoorAnimationPacket.Handler.class, TEDoorAnimationPacket.class, i++, Side.CLIENT);
		//Does ExVNT standard player knockback
		wrapper.registerMessage(ExplosionKnockbackPacket.Handler.class, ExplosionKnockbackPacket.class, i++, Side.CLIENT);
		//just go fuck yourself already (no I won't lol, doing the port at 4:30 am)
		wrapper.registerMessage(ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket.Handler.class, ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket.class, i++, Side.CLIENT);
		//Packet to send NBT data from clients to the serverside held item
		wrapper.registerMessage(NBTItemControlPacket.Handler.class, NBTItemControlPacket.class, i++, Side.SERVER);
		//Packets for syncing the keypad
		wrapper.registerMessage(KeypadServerPacket.Handler.class, KeypadServerPacket.class, i++, Side.SERVER);
		wrapper.registerMessage(KeypadClientPacket.Handler.class, KeypadClientPacket.class, i++, Side.CLIENT);
		//Sends a funi text to display like a music disc announcement
		wrapper.registerMessage(PlayerInformPacket.Handler.class, PlayerInformPacket.class, i++, Side.CLIENT);
		//An alert from 1.7.10 (like if your lungs are going dead from coal)
		wrapper.registerMessage(PlayerInformPacketLegacy.Handler.class, PlayerInformPacketLegacy.class, i++, Side.CLIENT);
		//Activates particle effects or animations without the need for an entity
		wrapper.registerMessage(GunFXPacket.Handler.class, GunFXPacket.class, i++, Side.CLIENT);
		//Handles custom death animations (like the gluon gun disintegration effect)
		wrapper.registerMessage(PacketSpecialDeath.Handler.class, PacketSpecialDeath.class, i++, Side.CLIENT);
		//Universal keybind packet
		wrapper.registerMessage(KeybindPacket.Handler.class, KeybindPacket.class, i++, Side.SERVER);
		//To tell the server to cut a mob for the cutting swords
		wrapper.registerMessage(PacketMobSlicer.Handler.class, PacketMobSlicer.class, i++, Side.SERVER);
		//Sync packet for jetpack data
		wrapper.registerMessage(JetpackSyncPacket.Handler.class, JetpackSyncPacket.class, i++, Side.SERVER);
		wrapper.registerMessage(JetpackSyncPacket.Handler.class, JetpackSyncPacket.class, i-1, Side.CLIENT);
		wrapper.registerMessage(ExtPropPacket.Handler.class, ExtPropPacket.class, i++, Side.CLIENT);
		wrapper.registerMessage(NBTControlPacket.Handler.class, NBTControlPacket.class, i++, Side.SERVER);
		wrapper.registerMessage(AnvilCraftPacket.Handler.class, AnvilCraftPacket.class, i++, Side.SERVER);
		wrapper.registerMessage(ControlPanelUpdatePacket.Handler.class, ControlPanelUpdatePacket.class, i++, Side.CLIENT);
// 		wrapper.registerMessage(ControlPanelLinkerServerPacket.Handler.class, ControlPanelUpdatePacket.class, i++, Side.SERVER);
//		wrapper.registerMessage(ControlPanelLinkerClientPacket.Handler.class, ControlPanelUpdatePacket.class, i++, Side.CLIENT);
		wrapper.registerMessage(HbmCapabilityPacket.Handler.class, HbmCapabilityPacket.class, i++, Side.CLIENT);
		wrapper.registerMessage(SerializableRecipePacket.Handler.class, SerializableRecipePacket.class, i++, Side.CLIENT);
		wrapper.registerMessage(PlayerSoundPacket.Handler.class, PlayerSoundPacket.class, i++, Side.CLIENT);
		wrapper.registerMessage(ModFXCollidePacket.Handler.class, ModFXCollidePacket.class, i++, Side.SERVER);
		wrapper.registerMessage(BiomeSyncPacket.Handler.class, BiomeSyncPacket.class, i++, Side.CLIENT);
        wrapper.registerMessage(PermaSyncPacket.Handler.class, PermaSyncPacket.class, i++, Side.CLIENT);
		//Syncs muzzle flashes of SEDNA guns for clients from other entities/players
		wrapper.registerMessage(MuzzleFlashPacket.Handler.class, MuzzleFlashPacket.class, i++, Side.CLIENT);

		for (IPacketRegisterListener listener : LISTENERS) {
			i = listener.registerPackets(i);
		}
	}
	
	public static void sendTo(IMessage message, EntityPlayerMP player){
		if(player != null)
			wrapper.sendTo(message, player);
	}
}
