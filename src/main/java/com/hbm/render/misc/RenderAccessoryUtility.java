package com.hbm.render.misc;

import com.hbm.Tags;
import com.hbm.main.MainRegistry;
import com.hbm.util.ShadyUtil;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class RenderAccessoryUtility {

	private static ResourceLocation hbm = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeHbm.png");
	private static ResourceLocation hbm2 = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeHbm2.png");
	private static ResourceLocation drillgon = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeDrillgon.png");
	private static ResourceLocation dafnik = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeDafnik.png");
	private static ResourceLocation lpkukin = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeShield.png");
	private static ResourceLocation vertice = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeVertice_2.png");
	private static ResourceLocation red = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeRed.png");
	private static ResourceLocation ayy = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeAyy.png");
	private static ResourceLocation nostalgia = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeNostalgia.png");
	private static ResourceLocation sam = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeSam.png");
	private static ResourceLocation hoboy = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeHoboy.png");
	private static ResourceLocation master = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeMaster.png");
	private static ResourceLocation mek = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeMek.png");
	private static ResourceLocation test = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeTest.png");
	private static ResourceLocation swiggs = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeSweatySwiggs.png");
	private static ResourceLocation doctor17 = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeDoctor17.png");
	private static ResourceLocation shimmeringblaze = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeBlaze.png");
	private static ResourceLocation wiki = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeWiki.png");
	private static ResourceLocation leftnugget = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeLeftNugget.png");
	private static ResourceLocation rightnugget = new ResourceLocation(Tags.MODID + ":textures/models/capes/CapeRightNugget.png");
	private static ResourceLocation alcater = new ResourceLocation(Tags.MODID + ":textures/models/capes/capealcater.png");
	private static ResourceLocation jame = new ResourceLocation(Tags.MODID + ":textures/models/capes/capejame.png");
	private static ResourceLocation golem = new ResourceLocation(Tags.MODID + ":textures/models/capes/capegolem.png");
	
	public static ResourceLocation getCloakFromPlayer(EntityPlayer player) {
		UUID uuid = player.getUniqueID();
		String name = player.getDisplayName().getUnformattedText();
		if(uuid.equals(ShadyUtil.HbMinecraft)) {

			if(MainRegistry.polaroidID == 11)
				return hbm;
			else
				return hbm2;
		}
		if(uuid.equals(ShadyUtil.Drillgon)) {
			return drillgon;
		}
		if(uuid.equals(ShadyUtil.Dafnik)) {
			return dafnik;
		}
		if(uuid.equals(ShadyUtil.LPkukin)) {
			return lpkukin;
		}
		if(uuid.equals(ShadyUtil.LordVertice)) {
			return vertice;
		}
		if(uuid.equals(ShadyUtil.CodeRed_)) {
			return red;
		}
		if(uuid.equals(ShadyUtil.dxmaster769)) {
			return ayy;
		}
		if(uuid.equals(ShadyUtil.Dr_Nostalgia)) {
			return nostalgia;
		}
		if(uuid.equals(ShadyUtil.Samino2)) {
			return sam;
		}
		if(uuid.equals(ShadyUtil.Hoboy03new)) {
			return hoboy;
		}
		if(uuid.equals(ShadyUtil.Dragon59MC)) {
			return master;
		}
		if(uuid.equals(ShadyUtil.Steelcourage)) {
			return mek;
		}
		if(uuid.equals(ShadyUtil.SweatySwiggs)) {
			return swiggs;
		}
		if(uuid.equals(ShadyUtil.Doctor17) || uuid.equals(ShadyUtil.Doctor17PH)) {
			return doctor17;
		}
		if(uuid.equals(ShadyUtil.ShimmeringBlaze)) {
			return shimmeringblaze;
		}
		if(uuid.equals(ShadyUtil.FifeMiner)) {
			return leftnugget;
		}
		if(uuid.equals(ShadyUtil.lag_add)) {
			return rightnugget;
		}
		if(uuid.equals(ShadyUtil.Alcater)) {
			return alcater;
		}
		if(uuid.equals(ShadyUtil.ege444)) {
			return jame;
		}
		if(uuid.equals(ShadyUtil.Golem)) {
			return golem;
		}
		if(ShadyUtil.contributors.contains(uuid)) {
			return wiki;
		}
		if(name.startsWith("Player")) {
			return test;
		}
		return null;
	}

	public static void loadCape(NetworkPlayerInfo info, ResourceLocation rl) {
		info.playerTextures.put(Type.CAPE, rl);
	}
}
