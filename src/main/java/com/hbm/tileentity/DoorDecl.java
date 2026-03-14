package com.hbm.tileentity;

import com.hbm.Tags;
import com.hbm.animloader.AnimatedModel;
import com.hbm.animloader.Animation;
import com.hbm.interfaces.IDoor.DoorState;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.loader.IModelCustomNamed;
import com.hbm.render.tileentity.door.*;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Clock;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.function.Consumer;

public abstract class DoorDecl {

	/// For keeping addon support
	public static class DefaultSkins {
		public static final ResourceLocation pheo_fire_door_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/fire_door.png");
		public static final ResourceLocation pheo_fire_door_black_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/fire_door_black.png");
		public static final ResourceLocation pheo_fire_door_orange_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/fire_door_orange.png");
		public static final ResourceLocation pheo_fire_door_yellow_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/fire_door_yellow.png");
		public static final ResourceLocation pheo_fire_door_trefoil_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/fire_door_trefoil.png");
		public static final ResourceLocation pheo_airlock_door_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/airlock_door.png");
		public static final ResourceLocation pheo_airlock_door_clean_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/airlock_door_clean.png");
		public static final ResourceLocation pheo_airlock_door_green_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/airlock_door_green.png");
		public static final ResourceLocation pheo_blast_door_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/blast_door.png");
		public static final ResourceLocation pheo_containment_door_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/containment_door.png");
		public static final ResourceLocation pheo_containment_door_trefoil_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/containment_door_trefoil.png");
		public static final ResourceLocation pheo_containment_door_trefoil_yellow_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/containment_door_trefoil_yellow.png");
		public static final ResourceLocation pheo_seal_door_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/seal_door.png");
		public static final ResourceLocation pheo_secure_door_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/secure_door.png");
		public static final ResourceLocation pheo_secure_door_grey_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/secure_door_grey.png");
		public static final ResourceLocation pheo_secure_door_yellow_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/secure_door_yellow.png");
		public static final ResourceLocation pheo_secure_door_black_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/secure_door_black.png");
		public static final ResourceLocation pheo_sliding_door_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/sliding_door.png");
		public static final ResourceLocation pheo_vehicle_door_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/vehicle_door.png");
		public static final ResourceLocation pheo_water_door_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/water_door.png");
		public static final ResourceLocation pheo_water_door_clean_tex = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/water_door_clean.png");
		public static final ResourceLocation pheo_vault_door_3 = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/vault/vault_door_3.png");
		public static final ResourceLocation pheo_vault_door_4 = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/vault/vault_door_4.png");
		public static final ResourceLocation pheo_vault_door_s = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/vault/vault_door_s.png");
		public static final ResourceLocation pheo_label_2 = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/vault/label_2.png");
		public static final ResourceLocation pheo_label_81 = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/vault/label_81.png");
		public static final ResourceLocation pheo_label_87 = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/vault/label_87.png");
		public static final ResourceLocation pheo_label_99 = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/vault/label_99.png");
		public static final ResourceLocation pheo_label_101 = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/vault/label_101.png");
		public static final ResourceLocation pheo_label_106 = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/vault/label_106.png");
		public static final ResourceLocation pheo_label_111 = new ResourceLocation(Tags.MODID, "textures/models/pheodoors/vault/label_111.png");
	}

	public DoorDecl() {
		if (hasSkins())
			addSkins(getDefaultSkins());
	}

	public static final DoorDecl TRANSITION_SEAL = new DoorDecl(){
		
		@Override
		public SoundEvent getOpenSoundStart() {
			return HBMSoundHandler.transitionSealOpen;
		};
		
		@Override
		public float getSoundVolume(){
			return 6;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void getTranslation(String partName, float openTicks, boolean child, float[] trans) {
			if(!partName.equals("base")){
				set(trans, 0, 3.5F*getNormTime(openTicks), 0);
			} else {
				super.getTranslation(partName, openTicks, child, trans);
			}
		};
		
		@Override
		@SideOnly(Side.CLIENT)
		public void doOffsetTransform() {
			GlStateManager.translate(0, 0, 0.5);
		};
		
		@Override
		@SideOnly(Side.CLIENT)
		public double[][] getClippingPlanes() {
			return super.getClippingPlanes();
		};
		
		@Override
		public int timeToOpen() {
			return 480;
		};
		
		@Override
		public int[][] getDoorOpenRanges(){
			//3 is tall
			//4 is wide
			return new int[][]{{-9, 2, 0, 20, 20, 1}};
		}

		@Override
		public int[] getDimensions(){
			return new int[]{23, 0, 0, 0, 13, 12};
		}
		
		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			return super.getBlockBound(relPos, open);
		};

		@Override
		@SideOnly(Side.CLIENT)
		public ResourceLocation getTextureForPart(String partName){
			return ResourceManager.transition_seal_tex;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Animation getAnim() {
			return ResourceManager.transition_seal_anim;
		};
		
		@Override
		@SideOnly(Side.CLIENT)
		public AnimatedModel getAnimatedModel() {
			return ResourceManager.transition_seal;
		};
		
		@Override
		@SideOnly(Side.CLIENT)
		public IModelCustomNamed getModel(){
			return null;
		}
	};

	public static final DoorDecl VAULT_DOOR = new DoorDecl() {

		@Override
		public IRenderDoors getSEDNARenderer() {
			return RenderVaultDoor.INSTANCE;
		}

		@Override
		public BusAnimationSedna getBusAnimation(DoorState state, byte skinIndex) {
			if(state == DoorState.OPENING) return new BusAnimationSedna()
					.addBus("PULL", new BusAnimationSequenceSedna().setPos(0, 0, 0).addPos(0, 0, 1, 2_000, IType.SIN_FULL))
					.addBus("SLIDE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 2_000).addPos(1, 0, 0, 4_000));
			if(state == DoorState.CLOSING) return new BusAnimationSedna()
					.addBus("PULL", new BusAnimationSequenceSedna().setPos(0, 0, 1).addPos(0, 0, 1, 4_000).addPos(0, 0, 0, 2_000, IType.SIN_FULL))
					.addBus("SLIDE", new BusAnimationSequenceSedna().setPos(1, 0, 0).addPos(0, 0, 0, 4_000));
			return null;
		}

		@Override
		public boolean hasSkins() {
			return true;
		}

		@Override
		protected ResourceLocation[] getDefaultSkins() {
			return new ResourceLocation[] {
					DefaultSkins.pheo_label_101,
					DefaultSkins.pheo_label_87,
					DefaultSkins.pheo_label_106,
					DefaultSkins.pheo_label_81,
					DefaultSkins.pheo_label_111,
					DefaultSkins.pheo_label_2,
					DefaultSkins.pheo_label_99
			};
		}

		@Override public int timeToOpen() { return 120; }
		@Override public int[][] getDoorOpenRanges() { return new int[][] { { -1, 1, 0, 3, 3, 2 } }; }
		@Override public int[] getDimensions() { return new int[] { 4, 0, 0, 0, 2, 2 }; }
		@Override public int[][] getExtraDimensions() { return new int[][] { { 0, 0, 1, -1, 2, 2 } }; }

		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			if(!open || relPos.getY() == 0) return Block.FULL_BLOCK_AABB;
			return super.getBlockBound(relPos, open);
		}

		public Consumer<TileEntityDoorGeneric> onUpdate = door -> {
			if(door.getWorld().isRemote) return;

			if(door.state == DoorState.OPENING) {
				if(door.openTicks == 0) door.getWorld().playSound(null, door.getPos(), HBMSoundHandler.vaultScrapeNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
				for(int i = 45; i <= 115; i += 10)
					if(door.openTicks == i) door.getWorld().playSound(null, door.getPos(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
			} else if(door.state == DoorState.CLOSING) {
				if(door.openTicks == 30) door.getWorld().playSound(null, door.getPos(), HBMSoundHandler.vaultScrapeNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
				for(int i = 45; i <= 115; i += 10)
					if(door.openTicks == i) door.getWorld().playSound(null, door.getPos(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		};

		@Override public Consumer<TileEntityDoorGeneric> onDoorUpdate() { return onUpdate; }
	};
	
	public static final DoorDecl SLIDING_SEAL_DOOR = new DoorDecl(){
		
		@Override
		public SoundEvent getOpenSoundEnd() {
			return HBMSoundHandler.nullMine;
		};
		@Override
		public SoundEvent getOpenSoundStart() {
			return HBMSoundHandler.sliding_seal_open;
		};
		
		public float getSoundVolume(){
			return 1;
		}

		@Override
		public IRenderDoors getSEDNARenderer() {
			return RenderSealDoor.INSTANCE;
		}

		@Override
		public BusAnimationSedna getBusAnimation(DoorState state,byte skinIndex) {
			if(state == DoorState.OPENING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 0, 0).addPos(0, 1, 0, this.timeToOpen() * 50));
			if(state == DoorState.CLOSING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 1, 0).addPos(0, 0, 0, this.timeToOpen() * 50));
			return null;
		}

		@Override public int timeToOpen() { return 20; };

		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			if(open){
				if(relPos.getY() == 0)
					return new AxisAlignedBB(0, 0, 1-0.25, 1, 0.125, 1);
				return super.getBlockBound(relPos, open);
			} else {
				return new AxisAlignedBB(0, 0, 1-0.25, 1, 1, 1);
			}
		};

		@Override public int[][] getDoorOpenRanges() { return new int[][] { { 0, 0, 0, 1, 2, 2 } }; }
		@Override public int[] getDimensions() { return new int[] { 1, 0, 0, 0, 0, 0 }; }
	};

	public static final DoorDecl SLIDING_GATE_DOOR = new DoorDecl(){
		
		@Override
		public SoundEvent getOpenSoundEnd() {
			return HBMSoundHandler.sliding_seal_stop;
		};
		@Override
		public SoundEvent getOpenSoundStart() {
			return HBMSoundHandler.sliding_seal_open;
		};
		
		public float getSoundVolume(){
			return 3;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void getTranslation(String partName, float openTicks, boolean child, float[] trans) {
			if(partName.startsWith("door")){
				set(trans, 0, 0, Library.smoothstep(getNormTime(openTicks), 0, 1));
			} else {
				set(trans, 0, 0, 0);
			}
		};
		
		@Override
		@SideOnly(Side.CLIENT)
		public double[][] getClippingPlanes() {
			return new double[][]{{0, 0, -1, 0.5001}};
		};
		
		@Override
		@SideOnly(Side.CLIENT)
		public void doOffsetTransform() {
			GlStateManager.translate(0.375, 0, 0);
		};
		
		@Override
		public int timeToOpen() {
			return 28;
		};
		
		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			if(open){
				if(relPos.getY() == 0)
					return new AxisAlignedBB(0, 0, 1-0.25, 1, 0.125, 1);
				return super.getBlockBound(relPos, open);
			} else {
				return new AxisAlignedBB(0, 0, 1-0.25, 1, 1, 1);
			}
		};
		
		@Override
		public int[][] getDoorOpenRanges(){
			return new int[][]{{0, 0, 0, 1, 2, 2}};
		}

		@Override
		public int[] getDimensions(){
			return new int[]{1, 0, 0, 0, 0, 0};
		}

		@Override
		@SideOnly(Side.CLIENT)
		public ResourceLocation getTextureForPart(String partName){
			return ResourceManager.sliding_gate_door_tex;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IModelCustomNamed getModel(){
			return ResourceManager.sliding_seal_door;
		}
	};
	
	public static final DoorDecl SECURE_ACCESS_DOOR = new DoorDecl(){
		
		@Override
		public SoundEvent getOpenSoundEnd() {
			return HBMSoundHandler.garage_stop;
		};
		
		@Override
		public SoundEvent getOpenSoundLoop() {
			return HBMSoundHandler.garage;
		};
		
		public float getSoundVolume(){
			return 2;
		}

		@Override
		public IRenderDoors getSEDNARenderer() {
			return RenderSecureDoor.INSTANCE;
		}

		@Override
		public BusAnimationSedna getBusAnimation(DoorState state,byte skinIndex) {
			if(state == DoorState.OPENING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 0, 0).addPos(0, 1, 0, this.timeToOpen() * 50));
			if(state == DoorState.CLOSING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 1, 0).addPos(0, 0, 0, this.timeToOpen() * 50));
			return null;
		}

		@Override public int timeToOpen() { return 120; };
		@Override public int[][] getDoorOpenRanges() { return new int[][] { { -2, 1, 0, 4, 5, 1 } }; }
		@Override public int[] getDimensions() { return new int[] { 4, 0, 0, 0, 2, 2 }; }
		
		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			if(!open){
				if(relPos.getY() > 0){
					return new AxisAlignedBB(0, 0, 0.375, 1, 1, 0.625);
				}
				return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
			}
			if(relPos.getY() == 1) {
				return new AxisAlignedBB(0, 0, 0, 1, 0.0625, 1);
			} else if(relPos.getY() == 4){
				return new AxisAlignedBB(0, 0.5, 0.15, 1, 1, 0.85);
			} else {
				return super.getBlockBound(relPos, open);
			}
		};

		@Override public ResourceLocation[] getDefaultSkins() {
			return new ResourceLocation[] {
					DefaultSkins.pheo_secure_door_tex,
					DefaultSkins.pheo_secure_door_grey_tex,
					DefaultSkins.pheo_secure_door_black_tex,
					DefaultSkins.pheo_secure_door_yellow_tex,
			};
		}

		@Override
		public boolean hasSkins() {
			return true;
		}
	};
	
	public static final DoorDecl ROUND_AIRLOCK_DOOR = new DoorDecl(){
		
		@Override
		public SoundEvent getOpenSoundEnd() {
			return HBMSoundHandler.garage_stop;
		};
		
		@Override
		public SoundEvent getOpenSoundLoop() {
			return HBMSoundHandler.garage;
		};
		
		public float getSoundVolume(){
			return 2;
		}

		@Override
		public IRenderDoors getSEDNARenderer() {
			return RenderAirlockDoor.INSTANCE;
		}

		@Override
		public BusAnimationSedna getBusAnimation(DoorState state, byte skinIndex) {
			if(state == DoorState.OPENING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 0, 0).addPos(0, 1, 0, this.timeToOpen() * 50));
			if(state == DoorState.CLOSING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 1, 0).addPos(0, 0, 0, this.timeToOpen() * 50));
			return null;
		}

		@Override
		protected ResourceLocation[] getDefaultSkins() {
			return new ResourceLocation[]{
					DefaultSkins.pheo_airlock_door_tex,
					DefaultSkins.pheo_airlock_door_clean_tex,
					DefaultSkins.pheo_airlock_door_green_tex
			};
		}

		@Override
		public boolean hasSkins() {
			return true;
		}

		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			if(!open)
				return super.getBlockBound(relPos, open);
			if(relPos.getZ() == 1){
				return new AxisAlignedBB(0.4, 0, 0, 1, 1, 1);
			} else if(relPos.getZ() == -2){
				return new AxisAlignedBB(0, 0, 0, 0.6, 1, 1);
			} else if(relPos.getY() == 3){
				return new AxisAlignedBB(0, 0.5, 0, 1, 1, 1);
			} else if(relPos.getY() == 0){
				return new AxisAlignedBB(0, 0, 0, 1, 0.0625, 1);
			}
			return super.getBlockBound(relPos, open);
		};

		@Override
		public int timeToOpen() {
			return 60;
		};

		@Override
		public int[][] getDoorOpenRanges(){
			return new int[][]{{0, 0, 0, -2, 4, 2}, {0, 0, 0, 3, 4, 2}};
		}

		@Override
		public int[] getDimensions() {
			return new int[]{3, 0, 0, 0, 2, 1};
		};
	};
	
	public static final DoorDecl HATCH = new DoorDecl(){

		@Override
		public SoundEvent getOpenSoundStart() {
			return HBMSoundHandler.hatch_open;
		};
		
		public float getSoundVolume(){
			return 2;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void getRotation(String partName, float openTicks, float[] rot) {
			if(partName.equals("hatch")){
				set(rot, Library.smoothstep(getNormTime(openTicks, 15, 30), 0, 1)*90-90, 0, 0);
				return;
			} else if(partName.equals("spinny")){
				set(rot, 0, 0, Library.smoothstep(getNormTime(openTicks, 0, 15), 0, 1)*360);
				return;
			}
			set(rot, 0, 0, 0);
		};
		
		@Override
		@SideOnly(Side.CLIENT)
		public void getOrigin(String partName, float[] orig) {
			if(partName.equals("hatch")){
				set(orig, 0, 1.03157F, 0.591647F);
				return;
			} else if(partName.equals("spinny")){
				set(orig, 0, 1.62322F, 0.434233F);
				return;
			}
			super.getOrigin(partName, orig);
		};
		
		@Override
		@SideOnly(Side.CLIENT)
		public String[] getChildren(String partName) {
			if(partName.equals("hatch")){
				return new String[]{"spinny"};
			}
			return super.getChildren(partName);
		};
		
		@Override
		@SideOnly(Side.CLIENT)
		public boolean doesRender(String partName, boolean child) {
			if(partName.equals("spinny")){
				return child;
			} else {
				return true;
			}
		};
		
		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			if(open){
				return new AxisAlignedBB(0, 0, 0, 1, 1, 0.0625);
			}
			return super.getBlockBound(relPos, open);
		};
		
		@Override
		public int timeToOpen() {
			return 30;
		};
		
		@Override
		public boolean isLadder(boolean open) {
			return open;
		};
		
		@Override
		@SideOnly(Side.CLIENT)
		public void doOffsetTransform() {
			GlStateManager.rotate(-90, 0, 1, 0);
		};
		
		@Override
		public int[][] getDoorOpenRanges(){
			return new int[][]{{0, 0, 0, 1, 1, 1}};
		}

		@Override
		public int[] getDimensions(){
			return new int[]{0, 0, 0, 0, 0, 0};
		}

		@Override
		@SideOnly(Side.CLIENT)
		public ResourceLocation getTextureForPart(String partName){
			return ResourceManager.small_hatch_tex;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IModelCustomNamed getModel(){
			return ResourceManager.small_hatch;
		}
		
	};
	
	public static final DoorDecl FIRE_DOOR = new DoorDecl(){
		
		@Override
		public SoundEvent getOpenSoundEnd() {
			return HBMSoundHandler.wgh_stop;
		};
		@Override
		public SoundEvent getOpenSoundLoop() {
			return HBMSoundHandler.wgh_start;
		};
		@Override
		public SoundEvent getSoundLoop2() {
			return HBMSoundHandler.alarm6;
		};


		@Override
		public IRenderDoors getSEDNARenderer() {
			return RenderFireDoor.INSTANCE;
		}

		@Override
		public BusAnimationSedna getBusAnimation(DoorState state,byte skinIndex) {
			if(state == DoorState.OPENING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 0, 0).addPos(0, 1, 0, this.timeToOpen() * 50));
			if(state == DoorState.CLOSING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 1, 0).addPos(0, 0, 0, this.timeToOpen() * 50));
			return null;
		}

		@Override public ResourceLocation[] getDefaultSkins() {
			return new ResourceLocation[] {
					DefaultSkins.pheo_fire_door_tex,
					DefaultSkins.pheo_fire_door_black_tex,
					DefaultSkins.pheo_fire_door_orange_tex,
					DefaultSkins.pheo_fire_door_yellow_tex,
					DefaultSkins.pheo_fire_door_trefoil_tex
			};
		}

		@Override
		public boolean hasSkins() {
			return true;
		}

		@Override public int timeToOpen() { return 160; }
		@Override public int[][] getDoorOpenRanges() { return new int[][] { { -1, 0, 0, 3, 4, 1 } }; }
		@Override public int[] getDimensions() { return new int[] { 2, 0, 0, 0, 2, 1 }; }

		@Override
		public float getSoundVolume(){
			return 2;
		}
		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			if(!open)
				return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
			if(relPos.getZ() == 1){
				return new AxisAlignedBB(0.5, 0, 0, 1, 1, 1);
			} else if(relPos.getZ() == -2){
				return new AxisAlignedBB(0, 0, 0, 0.5, 1, 1);
			} else if(relPos.getY() > 1){
				return new AxisAlignedBB(0, 0.75, 0, 1, 1, 1);
			} else if(relPos.getY() == 0) {
				return new AxisAlignedBB(0, 0, 0, 1, 0.1, 1);
			} else {
				return super.getBlockBound(relPos, open);
			}
		};
	};
	
	public static final DoorDecl QE_SLIDING = new DoorDecl(){
		
		@Override
		public SoundEvent getOpenSoundEnd() {
			return HBMSoundHandler.qe_sliding_opened;
		};
		@Override
		public SoundEvent getCloseSoundEnd() {
			return HBMSoundHandler.qe_sliding_shut;
		};
		@Override
		public SoundEvent getOpenSoundLoop() {
			return HBMSoundHandler.qe_sliding_opening;
		};
		
		public float getSoundVolume(){
			return 2;
		}
		@Override public int timeToOpen() { return 10; };

		@Override
		public IRenderDoors getSEDNARenderer() {
			return RenderSlidingDoor.INSTANCE;
		}

		@Override
		public BusAnimationSedna getBusAnimation(DoorState state,byte skinIndex) {
			if(state == DoorState.OPENING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 0, 0).addPos(0, 1, 0, this.timeToOpen() * 50));
			if(state == DoorState.CLOSING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 1, 0).addPos(0, 0, 0, this.timeToOpen() * 50));
			return null;
		}

		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			if(open){
				if(relPos.getZ() == 0){
					return new AxisAlignedBB(1-0.125, 0, 1-0.125, 1, 1, 1);
				} else {
					return new AxisAlignedBB(0, 0, 1-0.125, 0.125, 1, 1);
				}
			} else {
				return new AxisAlignedBB(0, 0, 1-0.125, 1, 1, 1);
			}
		};

		@Override public int[][] getDoorOpenRanges() { return new int[][] { { 0, 0, 0, 2, 2, 2 } }; }
		@Override public int[] getDimensions() { return new int[] { 1, 0, 0, 0, 1, 0 }; }
	};
	
	public static final DoorDecl QE_CONTAINMENT = new DoorDecl(){

		@Override
		public SoundEvent getOpenSoundEnd() {
			return HBMSoundHandler.wgh_stop;
		};
		@Override
		public SoundEvent getOpenSoundLoop() {
			return HBMSoundHandler.wgh_start;
		};
		
		@Override
		public float getSoundVolume(){
			return 2;
		}

		@Override
		public IRenderDoors getSEDNARenderer() {
			return RenderContainmentDoor.INSTANCE;
		}

		@Override
		public BusAnimationSedna getBusAnimation(DoorState state,byte skinIndex) {
			if(state == DoorState.OPENING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 0, 0).addPos(0, 1, 0, this.timeToOpen() * 50));
			if(state == DoorState.CLOSING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 1, 0).addPos(0, 0, 0, this.timeToOpen() * 50));
			return null;
		}

		@Override public ResourceLocation[] getDefaultSkins() {
			return new ResourceLocation[] {
					DefaultSkins.pheo_containment_door_tex,
					DefaultSkins.pheo_containment_door_trefoil_tex,
					DefaultSkins.pheo_containment_door_trefoil_yellow_tex
			};
		}

		@Override
		public boolean hasSkins() {
			return true;
		}

		@Override public int timeToOpen() { return 160; };
		@Override public int[][] getDoorOpenRanges() { return new int[][] { { -1, 0, 0, 3, 3, 1 } }; }
		@Override public int[] getDimensions() { return new int[] { 2, 0, 0, 0, 1, 1 }; }

		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			if(!open)
				return new AxisAlignedBB(0, 0, 0.5, 1, 1, 1);
			if(relPos.getY() > 1)
				return new AxisAlignedBB(0, 0.5, 0.5, 1, 1, 1);
			else if(relPos.getY() == 0)
				return new AxisAlignedBB(0, 0, 0.5, 1, 0.1, 1);
			return super.getBlockBound(relPos, open);
		};

	};
	
	public static final DoorDecl WATER_DOOR = new DoorDecl(){
		
		@Override
		public SoundEvent getOpenSoundEnd() {
			return HBMSoundHandler.wgh_big_stop;
		};
		@Override
		public SoundEvent getOpenSoundLoop() {
			return HBMSoundHandler.wgh_big_start;
		};
		@Override
		public SoundEvent getOpenSoundStart() {
			return HBMSoundHandler.door_spinny;
		};
		@Override
		public SoundEvent getCloseSoundStart() {
			return null;
		};
		@Override
		public SoundEvent getCloseSoundEnd() {
			return HBMSoundHandler.door_spinny;
		};

		@Override
		public float getSoundVolume(){
			return 2;
		}


		@Override
		public IRenderDoors getSEDNARenderer() {
			return RenderWaterDoor.INSTANCE;
		}

		@Override
		public BusAnimationSedna getBusAnimation(DoorState state,byte skinIndex) {
			if(state == DoorState.OPENING) return new BusAnimationSedna()
					.addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 0, 0).addPos(0, 0, 0, 1500).addPos(0, 1, 0, 1500, IType.SIN_FULL))
					.addBus("BOLT", new BusAnimationSequenceSedna().setPos(0, 0, 0).addPos(0, 0, 1, 1500, IType.SIN_FULL));
			if(state == DoorState.CLOSING) return new BusAnimationSedna()
					.addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 1, 0).addPos(0, 0, 0, 1500, IType.SIN_FULL))
					.addBus("BOLT", new BusAnimationSequenceSedna().setPos(0, 0, 1).addPos(0, 0, 1, 1200).addPos(0, 0, 0, 1500, IType.SIN_FULL));
			return null;
		}

		@Override public ResourceLocation[] getDefaultSkins() {
			return  new ResourceLocation[] {
					DefaultSkins.pheo_water_door_tex,
					DefaultSkins.pheo_water_door_clean_tex
			};
		}

		@Override
		public boolean hasSkins() {
			return true;
		}

		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			if(!open){
				return new AxisAlignedBB(0, 0, 0.75, 1, 1, 1);
			} else if(relPos.getY() > 1) {
				return new AxisAlignedBB(0, 0.85, 0.75, 1, 1, 1);
			} else if(relPos.getY() == 0){
				return  new AxisAlignedBB(0, 0, 0.75, 1, 0.15, 1);
			}
			return super.getBlockBound(relPos, open);
		};

		@Override public int timeToOpen() { return 60; };
		@Override public int[][] getDoorOpenRanges() { return new int[][] { { 1, 0, 0, -3, 3, 2 } }; }
		@Override public float getDoorRangeOpenTime(int ticks, int idx) { return getNormTime(ticks, 35, 40); };
		@Override public int[] getDimensions() { return new int[] { 2, 0, 0, 0, 1, 1 }; }
	};
	
	public static final DoorDecl LARGE_VEHICLE_DOOR = new DoorDecl(){
		
		@Override
		public SoundEvent getOpenSoundEnd() {
			return HBMSoundHandler.garage_stop;
		};
		
		@Override
		public SoundEvent getOpenSoundLoop() {
			return HBMSoundHandler.garage;
		};
		
		public float getSoundVolume(){
			return 2;
		}

		@Override
		public IRenderDoors getSEDNARenderer() {
			return RenderVehicleDoor.INSTANCE;
		}

		@Override
		public BusAnimationSedna getBusAnimation(DoorState state,byte skinIndex) {
			if(state == DoorState.OPENING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 0, 0).addPos(0, 1, 0, this.timeToOpen() * 50));
			if(state == DoorState.CLOSING) return new BusAnimationSedna().addBus("DOOR", new BusAnimationSequenceSedna().setPos(0, 1, 0).addPos(0, 0, 0, this.timeToOpen() * 50));
			return null;
		}

		@Override
		public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open) {
			if(!open)
				return super.getBlockBound(relPos, open);
			if(relPos.getZ() == 3){
				return new AxisAlignedBB(0.4, 0, 0, 1, 1, 1);
			} else if(relPos.getZ() == -3){
				return new AxisAlignedBB(0, 0, 0, 0.6, 1, 1);
			}
			return super.getBlockBound(relPos, open);
		};

		@Override public int timeToOpen() { return 60; };
		@Override public int[][] getDoorOpenRanges() { return new int[][] { { 0, 0, 0, -4, 6, 2 }, { 0, 0, 0, 4, 6, 2 } }; }
		@Override public int[] getDimensions() { return new int[] { 5, 0, 0, 0, 3, 3 }; };
	};
    public static final DoorDecl SILO_HATCH = new DoorDecl() {

        @Override public SoundEvent getOpenSoundEnd() { return HBMSoundHandler.wgh_big_stop; };
        @Override public SoundEvent getOpenSoundLoop() { return HBMSoundHandler.wgh_big_start; };
        @Override public SoundEvent getOpenSoundStart() { return null; };
        @Override public SoundEvent getCloseSoundStart() { return null; };
        @Override public SoundEvent getCloseSoundEnd() { return HBMSoundHandler.wgh_big_stop; };
        @Override public float getSoundVolume() { return 2; }
        @Override public boolean remoteControllable() { return true; }

        @Override
        @SideOnly(Side.CLIENT)
        public void getTranslation(String partName, float openTicks, boolean child, float[] trans) {
            if("Hatch".equals(partName)) {
                set(trans, 0, 0.25F * Library.smoothstep(getNormTime(openTicks, 0, 10), 0, 1), 0);
            } else {
                set(trans, 0, 0, 0);
            }
        };

        @Override
        @SideOnly(Side.CLIENT)
        public void getOrigin(String partName, float[] orig) {
            if("Hatch".equals(partName)) {
                set(orig, 0F, 0.875F, -1.875F);
                return;
            }
            set(orig, 0, 0, 0);
            super.getOrigin(partName, orig);
        };

        @Override
        @SideOnly(Side.CLIENT)
        public void getRotation(String partName, float openTicks, float[] rot) {
            if("Hatch".equals(partName)) {
                set(rot, Library.smoothstep(getNormTime(openTicks, 20, 100), 0, 1) * -240, 0, 0);
                return;
            }
            super.getRotation(partName, openTicks, rot);
        };

        @Override
        @SideOnly(Side.CLIENT)
        public boolean doesRender(String partName, boolean child) {
            return true;
        };

        @Override public int timeToOpen() { return 60; };
        @Override public int[][] getDoorOpenRanges() { return new int[][] { { 1, 0, 1, -3, 3, 0 }, { 0, 0, 1, -3, 3, 0 }, { -1, 0, 1, -3, 3, 0 } }; }
        @Override public float getDoorRangeOpenTime(int ticks, int idx) { return getNormTime(ticks, 20, 20); };


        @Override public int getBlockOffset() { return 2; }
        @Override public int[] getDimensions() { return new int[] { 0, 0, 2, 2, 2, 2 }; }
        @Override @SideOnly(Side.CLIENT) public ResourceLocation getTextureForPart(String partName) { return ResourceManager.silo_hatch_tex; }
        @Override @SideOnly(Side.CLIENT) public IModelCustomNamed getModel() { return ResourceManager.silo_hatch; }

    };

    public static final DoorDecl SILO_HATCH_LARGE = new DoorDecl() {
        @Override public SoundEvent getOpenSoundEnd() { return HBMSoundHandler.wgh_big_stop; };
        @Override public SoundEvent getOpenSoundLoop() { return HBMSoundHandler.wgh_big_start; };
        @Override public SoundEvent getOpenSoundStart() { return null; };
        @Override public SoundEvent getCloseSoundStart() { return null; };
        @Override public SoundEvent getCloseSoundEnd() { return HBMSoundHandler.wgh_big_stop; };
        @Override public float getSoundVolume() { return 2; }
        @Override public boolean remoteControllable() { return true; }

        @Override
        @SideOnly(Side.CLIENT)
        public void getTranslation(String partName, float openTicks, boolean child, float[] trans) {
            if("Hatch".equals(partName)) {
                set(trans, 0, 0.25F * Library.smoothstep(getNormTime(openTicks, 0, 10), 0, 1), 0);
            } else {
                set(trans, 0, 0, 0);
            }
        };

        @Override
        @SideOnly(Side.CLIENT)
        public void getOrigin(String partName, float[] orig) {
            if("Hatch".equals(partName)) {
                set(orig, 0F, 0.875F, -2.875F);
                return;
            }
            set(orig, 0, 0, 0);
            super.getOrigin(partName, orig);
        };

        @Override
        @SideOnly(Side.CLIENT)
        public void getRotation(String partName, float openTicks, float[] rot) {
            if("Hatch".equals(partName)) {
                set(rot, Library.smoothstep(getNormTime(openTicks, 20, 100), 0, 1) * -240, 0, 0);
                return;
            }
            super.getRotation(partName, openTicks, rot);
        };

        @Override
        @SideOnly(Side.CLIENT)
        public boolean doesRender(String partName, boolean child) {
            return true;
        };

        @Override public int timeToOpen() { return 60; };
        @Override public int[][] getDoorOpenRanges() { return new int[][] { { 2, 0, 1, -3, 3, 0 }, { 1, 0, 2, -5, 3, 0 }, { 0, 0, 2, -5, 3, 0 }, { -1, 0, 2, -5, 3, 0 }, { -2, 0, 1, -3, 3, 0 } }; }
        @Override public float getDoorRangeOpenTime(int ticks, int idx) { return getNormTime(ticks, 20, 20); };


        @Override public int getBlockOffset() { return 3; }
        @Override public int[] getDimensions() { return new int[] { 0, 0, 3, 3, 3, 3 }; }
        @Override @SideOnly(Side.CLIENT) public ResourceLocation getTextureForPart(String partName) { return ResourceManager.silo_hatch_large_tex; }
        @Override @SideOnly(Side.CLIENT) public IModelCustomNamed getModel() { return ResourceManager.silo_hatch_large; }

    };

	//Format: x, y, z, tangent amount 1 (how long the door would be if it moved up), tangent amount 2 (door places blocks in this direction), axis (0-x, 1-y, 2-z)


    public boolean remoteControllable() { return false; }

	public abstract int[][] getDoorOpenRanges();
	
	public abstract int[] getDimensions();

	public int[][] getExtraDimensions() { return null; }
	
	public float getDoorRangeOpenTime(int ticks, int idx){
		return getNormTime(ticks);
	}
	
	public int timeToOpen(){
		return 20;
	}
	
	public float getNormTime(float time){
		return getNormTime(time, 0, timeToOpen());
	}
	
	public float getNormTime(float time, float min, float max){
		return BobMathUtil.remap01_clamp(time, min, max);
	}
	
	@SideOnly(Side.CLIENT)
	public ResourceLocation getTextureForPart(String partName) { return null; }
	
	@SideOnly(Side.CLIENT)
	public IModelCustomNamed getModel() { return null; }
	
	@SideOnly(Side.CLIENT)
	public AnimatedModel getAnimatedModel(){
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public Animation getAnim(){
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public void getTranslation(String partName, float openTicks, boolean child, float[] trans){
		set(trans, 0, 0, 0);
	}
	
	@SideOnly(Side.CLIENT)
	public void getRotation(String partName, float openTicks, float[] rot){
		set(rot, 0, 0, 0);
	}
	
	@SideOnly(Side.CLIENT)
	public void getOrigin(String partName, float[] orig){
		set(orig, 0, 0, 0);
	}
	
	@SideOnly(Side.CLIENT)
	public boolean doesRender(String partName, boolean child){
		return true;
	}
	
	private static final String[] nothing = new String[]{};
	
	@SideOnly(Side.CLIENT)
	public String[] getChildren(String partName){
		return nothing;
	}
	
	@SideOnly(Side.CLIENT)
	public double[][] getClippingPlanes(){
		return new double[][]{};
	}
	
	@SideOnly(Side.CLIENT)
	public void doOffsetTransform(){
	}
	
	public AxisAlignedBB getBlockBound(BlockPos relPos, boolean open){
		return open ? Library.EMPTY_AABB : Block.FULL_BLOCK_AABB;
	}
	
	public boolean isLadder(boolean open){
		return false;
	}
	
	public SoundEvent getOpenSoundLoop(){
		return null;
	}

    public int getBlockOffset() {
        return 0;
    }


    //Hack
	public SoundEvent getSoundLoop2(){
		return null;
	}
	
	public SoundEvent getCloseSoundLoop(){
		return getOpenSoundLoop();
	}
	
	public SoundEvent getOpenSoundStart(){
		return null;
	}
	
	public SoundEvent getCloseSoundStart(){
		return getOpenSoundStart();
	}
	
	public SoundEvent getOpenSoundEnd(){
		return null;
	}
	
	public SoundEvent getCloseSoundEnd(){
		return getOpenSoundEnd();
	}
	
	public float getSoundVolume(){
		return 1;
	}
	
	public float[] set(float[] f, float x, float y, float z){f[0] = x; f[1] = y; f[2] = z; return f;};

	// NEW DOORS

	/// Do NOT access this directly. Use getSEDNASkins()
	private List<ResourceLocation> skins;

	/// A little modification shouldn't hurt
	/// I made this List instead of arrays for easier modification with add-ons.
	/// -Leafia
	public final List<ResourceLocation> getSEDNASkins() {
		if (hasSkins() && skins == null)
			skins = new ArrayList<>();
		return skins;
	}

	/// Override this to enable skins.
	public boolean hasSkins() { return false; }

	/// Utility method for initializing skins. Called by constructor.
	/// Made public for addons.
	public final void addSkins(ResourceLocation... skins) {
		getSEDNASkins().addAll(Arrays.asList(skins));
	}

	/// Override this method to initialize skins. Called by constructor.
	protected ResourceLocation[] getDefaultSkins() {
		return new ResourceLocation[0];
	}

	/// screw it
	/// Override hasSkins to enable skins, otherwise this will always return 0.
	public final int getSkinCount() {
		if (!hasSkins())
			return 0;
		List<ResourceLocation> skins = getSEDNASkins();
		if (skins == null) return 0;
		return skins.size();
	}

	/// For item rendering
	public ResourceLocation getCyclingSkins() {
		List<ResourceLocation> skins = this.getSEDNASkins();
		if (skins == null) return null;
		int index = (int) ((Clock.get_ms() % (skins.size() * 1000)) / 1000);
		return skins.get(index);
	}

	public ResourceLocation getSkinFromIndex(int index) {
		List<ResourceLocation> skins = this.getSEDNASkins();
		return skins.get(Math.abs(index) % skins.size());
	}

	// keyframe animation system sneakily stitched into the door decl
	public IRenderDoors getSEDNARenderer() { return null; }
	public BusAnimationSedna getBusAnimation(DoorState state,byte skinIndex) { return null; }

	public HbmAnimationsSedna.Animation getSEDNAAnim(DoorState state,byte skinIndex) {
		BusAnimationSedna anim = this.getBusAnimation(state, skinIndex);
		if(anim != null) return new HbmAnimationsSedna.Animation("DOOR_ANIM", System.currentTimeMillis(), anim);
		return null;
	}

	public Consumer<TileEntityDoorGeneric> onDoorUpdate() { return null; }
}
