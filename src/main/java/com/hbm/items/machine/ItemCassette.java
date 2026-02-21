package com.hbm.items.machine;

import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class ItemCassette extends Item {
	@SuppressWarnings("unused")
    public static class TrackType {
        public static final Int2ObjectMap<TrackType> VALUES = new Int2ObjectArrayMap<>(20);

		public static final TrackType NULL = new TrackType(" ", null, SoundType.SOUND, 0, 0, 0);
        public static final TrackType HATCH = new TrackType("Hatch Siren", HBMSoundHandler.alarmHatch, SoundType.LOOP, 3358839, 250, 1);
        public static final TrackType AUTOPILOT = new TrackType("Autopilot Disconnected", HBMSoundHandler.alarmAutopilot, SoundType.LOOP, 11908533, 50, 2);
        public static final TrackType AMS_SIREN = new TrackType("AMS Siren", HBMSoundHandler.alarmAMSSiren, SoundType.LOOP, 15055698, 50, 3);
        public static final TrackType BLAST_DOOR = new TrackType("Blast Door Alarm", HBMSoundHandler.alarmBlastDoor, SoundType.LOOP, 11665408, 50, 4);
        public static final TrackType APC_LOOP = new TrackType("APC Siren", HBMSoundHandler.alarmAPCLoop, SoundType.LOOP, 3565216, 50, 5);
        public static final TrackType KLAXON = new TrackType("Klaxon", HBMSoundHandler.alarmKlaxon, SoundType.LOOP, 8421504, 50, 6);
        public static final TrackType KLAXON_A = new TrackType("Vault Door Alarm", HBMSoundHandler.alarmFoKlaxonA, SoundType.LOOP, 0x8c810b, 50, 7);
        public static final TrackType KLAXON_B = new TrackType("Security Alert", HBMSoundHandler.alarmFoKlaxonB, SoundType.LOOP, 0x76818e, 50, 8);
        public static final TrackType SIREN = new TrackType("Standard Siren", HBMSoundHandler.alarmRegular, SoundType.LOOP, 6684672, 100, 9);
        public static final TrackType CLASSIC = new TrackType("Classic Siren", HBMSoundHandler.alarmClassic, SoundType.LOOP, 0xc0cfe8, 100, 10);
        public static final TrackType BANK_ALARM = new TrackType("Bank Alarm", HBMSoundHandler.alarmBank, SoundType.LOOP, 3572962, 100, 11);
        public static final TrackType BEEP_SIREN = new TrackType("Beep Siren", HBMSoundHandler.alarmBeep, SoundType.LOOP, 13882323, 100, 12);
        public static final TrackType CONTAINER_ALARM = new TrackType("Container Alarm", HBMSoundHandler.alarmContainer, SoundType.LOOP, 14727839, 100, 13);
        public static final TrackType SWEEP_SIREN = new TrackType("Sweep Siren", HBMSoundHandler.alarmSweep, SoundType.LOOP, 15592026, 500, 14);
        public static final TrackType STRIDER_SIREN = new TrackType("Missile Silo Siren", HBMSoundHandler.alarmStrider, SoundType.LOOP, 11250586, 500, 15);
        public static final TrackType AIR_RAID = new TrackType("Air Raid Siren", HBMSoundHandler.alarmAirRaid, SoundType.LOOP, 0xDF3795, 500, 16);
        public static final TrackType NOSTROMO_SIREN = new TrackType("Nostromo Self Destruct", HBMSoundHandler.alarmNostromo, SoundType.LOOP, 0x5dd800, 100, 17);
        public static final TrackType EAS_ALARM = new TrackType("EAS Alarm Screech", HBMSoundHandler.alarmEas, SoundType.LOOP, 0xb3a8c1, 50, 18);
        public static final TrackType APC_PASS = new TrackType("APC Pass", HBMSoundHandler.alarmAPCPass, SoundType.PASS, 3422163, 50, 19);
        public static final TrackType RAZORTRAIN = new TrackType("Razortrain Horn", HBMSoundHandler.alarmRazorTrain, SoundType.SOUND, 7819501, 250, 20);

        private static final AtomicInteger nextId = new AtomicInteger(21); // AtomicInteger to make sure no id collisions happen because of threading (i know its overkill but its not like this is a major performance bottleneck)

		// Name of the track shown in GUI
		private final String title;
		// Location of the sound
		private final SoundEvent location;
		// Sound type, whether the sound should be repeated or not
		private final SoundType type;
		// Color of the cassette
		private final int color;
		// Range where the sound can be heard
		private final int volume;

        private final int id;

        private TrackType(String name, SoundEvent loc, SoundType sound, int color, int volume, int id) {
			this.title = name;
			this.location = loc;
			this.type = sound;
			this.color = color;
			this.volume = volume;
            this.id = id;
            //Vidarin: If some ee user manages to break things even though it should be impossible (or my code is bad)
            if (VALUES.containsKey(id)) MainRegistry.logger.error("ID collision when registering siren tracks! (id: {}, old track: \"{}\", new track: \"{}\")", id, VALUES.get(id).title, name);
            VALUES.put(id, this);
		}

        public static TrackType register(String name, SoundEvent loc, SoundType sound, int color, int volume) {
            return new TrackType(name, loc, sound, color, volume, nextId.getAndIncrement());
        }

		public String getTrackTitle() { return title; }
		public SoundEvent getSoundLocation() { return location; }
		public SoundType getType() { return type; }
		public int getColor() { return color; }
		public int getVolume() { return volume; }
        public int getId() { return id; }

		public static TrackType byIndex(int i) {
			if (i < VALUES.size())
				return VALUES.get(i);
			else
				return NULL;
		}
	}

	public enum SoundType {
		LOOP, PASS, SOUND;
	}

	public ItemCassette(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);

		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if(tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH) {
			for(int i = 1; i < TrackType.VALUES.size(); ++i) {
				items.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(!(stack.getItem() instanceof ItemCassette))
			return;

		tooltip.add("[CREATED USING TEMPLATE FOLDER]");
		tooltip.add("");

		tooltip.add("Siren sound cassette:");
		tooltip.add("   Name: " + TrackType.byIndex(stack.getItemDamage()).getTrackTitle());
		tooltip.add("   Type: " + TrackType.byIndex(stack.getItemDamage()).getType().name());
		tooltip.add("   Volume: " + TrackType.byIndex(stack.getItemDamage()).getVolume());
	}

	public static TrackType getType(ItemStack stack) {
		if(stack != null && stack.getItem() instanceof ItemCassette)
			return TrackType.byIndex(stack.getItemDamage());
		else
			return TrackType.NULL;
	}

}
