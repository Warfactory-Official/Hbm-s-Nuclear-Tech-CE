package com.hbm.render.anim;

import com.hbm.animloader.AnimationWrapper;
import com.hbm.animloader.AnimationWrapper.EndResult;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
// Th3_Sl1ze: for now I'll leave it because of good old Drillgon guns. Maybe it can become sophisticated and in a single class,
// but I'm a lazy fucker so I'll create die Neuen HbmAnimations for sedna weapons.
public class HbmAnimations {

	//in flans mod and afaik also MW, there's an issue that there is only one
	//single animation timer for each client. this is fine for the most part,
	//but once you reload and switch weapons while the animation plays, the
	//other weapon will too play the animation, even though it is not reloading.
	//my approach adds 9 timers, one for every inventory slot (Drillgon200: +1 for left hand). you can still
	//"trick" the system by putting a weapon into a different slot while an
	//animation is playing, though this will cancel the animation entirely.
	public static final Animation[] hotbar = new Animation[10];

	public static enum AnimType {
		RELOAD,		//animation for every reload cycle
		CYCLE,		//animation for every firing cycle
		ALT_CYCLE,	//animation for alt fire cycles
		SPINUP,		//animation for actionstart
		SPINDOWN,	//animation for actionend
		EQUIP;

		public static final AnimType[] VALUES = values();
	}

	public static class Animation {

		//the "name" of the animation slot. if the item has a different key than
		//the animation, the animation will be canceled.
		public String key;
		//the starting time of the animation
		public long startMillis;
		//the animation bus
		public BusAnimation animation;

		public Animation(String key, long startMillis, BusAnimation animation) {
			this.key = key;
			this.startMillis = startMillis;
			this.animation = animation;
		}
	}
	
	//Drillgon200: So we're just going to piggyback off of this system because it would be weird to make another one.
	public static class BlenderAnimation extends Animation {

		public AnimationWrapper wrapper;
		
		public BlenderAnimation(String key, long startMillis, float scale, com.hbm.animloader.Animation animation, EndResult end) {
			super(key, startMillis, null);
			wrapper = new AnimationWrapper(startMillis, scale, animation).onEnd(end);
		}
		
	}

	public static AnimationWrapper getRelevantBlenderAnim(EnumHand hand){
		Animation anim = getRelevantAnim(hand);
		if(anim instanceof BlenderAnimation){
			return ((BlenderAnimation) anim).wrapper;
		}
		return AnimationWrapper.EMPTY;
	}
	
	public static Animation getRelevantAnim(EnumHand hand) {

		EntityPlayer player = Minecraft.getMinecraft().player;
		int slot = player.inventory.currentItem;
		if(hand == EnumHand.OFF_HAND)
			slot = 9;
		ItemStack stack = player.getHeldItem(hand);

		if(stack == null || stack.isEmpty())
			return null;

		if(hotbar[slot] == null)
			return null;

		if(hotbar[slot].key.equals(stack.getItem().getTranslationKey())) {
			return hotbar[slot];
		}

		return null;
	}
	
	public static double[] getRelevantTransformation(String bus, EnumHand hand) {

		Animation anim = HbmAnimations.getRelevantAnim(hand);

		if(anim != null && anim.animation != null) {

			BusAnimation buses = anim.animation;
			int millis = (int)(System.currentTimeMillis() - anim.startMillis);

			BusAnimationSequence seq = buses.getBus(bus);

			if(seq != null) {
				double[] trans = seq.getTransformation(millis);

				if(trans != null)
					return trans;
			}
		}
		return new double[] {0, 0, 0};
	}
	
	public static float getTimeDifference(String bus, EnumHand hand){
		Animation anim = HbmAnimations.getRelevantAnim(hand);
		if(anim != null && anim.animation != null) {
			return ((float)(System.currentTimeMillis() - anim.startMillis))/1000F;
		}
		return 0;
	}

}
