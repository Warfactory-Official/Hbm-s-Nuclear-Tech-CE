package com.hbm.items.machine;

import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;

import java.util.List;
import java.util.Objects;

public class ItemCassette extends Item implements IDynamicModels {

    @Override
    public IItemColor getItemColorHandler() {
         return (stack, tintIndex) -> {
             if (tintIndex == 1) {
                 int j = ItemCassette.TrackType.getEnum(stack.getItemDamage()).getColor();
                 if (j < 0) j = 0xFFFFFF;
                 return j;
             }
             return 0xFFFFFF;
         };
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {

    }

    @Override
    public void registerModel() {
            for (int i = 0; i < ItemCassette.TrackType.values().length; i++) {
                ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(Objects.requireNonNull(this.getRegistryName()), "inventory"));
            }

    }

    @Override
    public void registerSprite(TextureMap map) {

    }

    public enum TrackType {

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
			TrackType track = VALUES.get(i);
			return track != null ? track : NULL;
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
        IDynamicModels.INSTANCES.add(this);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if(tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH) {
			for (TrackType track : TrackType.VALUES.values()) {
				if (track != TrackType.NULL) {
					items.add(new ItemStack(this, 1, track.getId()));
				}
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
