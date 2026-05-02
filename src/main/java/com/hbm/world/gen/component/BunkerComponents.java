package com.hbm.world.gen.component;

import com.hbm.blocks.ModBlocks;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsComponent;
import com.hbm.itempool.ItemPoolsLegacy;
import com.hbm.world.gen.ProceduralStructureStart;
import com.hbm.world.gen.ProceduralStructureStart.ProceduralComponent;
import com.hbm.world.gen.component.Component.ConcreteBricks;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.List;
import java.util.Random;

public class BunkerComponents {
	
	public static class BunkerStart extends ProceduralStructureStart {
		
		public BunkerStart() {}
		
		public BunkerStart(World world, Random rand, int chunkX, int chunkZ) {
			super(chunkX, chunkZ);
			
			this.sizeLimit = 7 + rand.nextInt(6);
			this.distanceLimit = 40;
			
			final int x = chunkX * 16 + 8;
			final int z = chunkZ * 16 + 8;
			
			Weight[] weights = new Weight[] {
				new Weight(6, 3, Corridor::findValidPlacement),
				new Weight(5, 4, BedroomL::findValidPlacement),
				new Weight(10, 3, FunJunction::findValidPlacement),
				new Weight(5, 2, BathroomL::findValidPlacement),
				new Weight(7, 2, Laboratory::findValidPlacement),
				new Weight(5, 1, PowerRoom::findValidPlacement),
			};
			
			StructureComponent starter = new StartingHub(rand, x, z);
			
			buildStart(world, rand, starter, weights);
			
			this.markAvailableHeight(world, rand, 20);
		}
		
	}
	
	public static void registerComponents() {
		MapGenStructureIO.registerStructureComponent(StartingHub.class, "NTMBStartingHub");
		MapGenStructureIO.registerStructureComponent(Corridor.class, "NTMBCorridor");
		MapGenStructureIO.registerStructureComponent(BedroomL.class, "NTMBBedroomL");
		MapGenStructureIO.registerStructureComponent(FunJunction.class, "NTMBFunJunction");
		MapGenStructureIO.registerStructureComponent(BathroomL.class, "NTMBBathroomL");
		MapGenStructureIO.registerStructureComponent(Laboratory.class, "NTMBLaboratory");
		MapGenStructureIO.registerStructureComponent(PowerRoom.class, "NTMBPowerRoom");
		//TODO more rooms for more variety
	}
	
	//why are we still doing this?
	private static ConcreteBricks ConcreteBricks = new ConcreteBricks();
	
	public static class StartingHub extends Component implements ProceduralComponent {
		
		private boolean[] paths = new boolean[3];
		
		public StartingHub() { }

		@Override
		protected void writeStructureToNBT(NBTTagCompound tagCompound) {
		}

		@Override
		protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {
		}

		public StartingHub(Random rand, int x, int z) {
			super(rand, x, 64, z, 7, 5, 7);
		}
		
		public StartingHub(int componentType, StructureBoundingBox box, int coordMode) {
			super(componentType);
			this.boundingBox = box;
			this.setCoordBaseMode(EnumFacing.byIndex(coordMode));
		}
		
		/** write to nbt */
		@Override
		protected void setToNBT(NBTTagCompound nbt) {
			super.setToNBT(nbt);
			for(int i = 0; i < paths.length; i++)
				nbt.setBoolean("p" + i, paths[i]);
		}
		
		/** read from nbt */
		@Override
		protected void getFromNBT(NBTTagCompound nbt) {
			super.getFromNBT(nbt);
			for(int i = 0; i < paths.length; i++)
				paths[i] = nbt.getBoolean("p" + i);
		}
		
		@Override
		public void buildComponent(ProceduralStructureStart start, Random rand) {
			paths[0] = this.getNextComponentEast(start, this, getCoordMode(), rand, 5, 1) != null;
			paths[1] = this.getNextComponentAntiNormal(start, this, getCoordMode(), rand, 4, 1) != null;
			paths[2] = this.getNextComponentWest(start, this, getCoordMode(), rand, 3, 1) != null;
		}
		
		@Override
		public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
			
			fillWithAir(world, box, 1, 1, 1, 6, 3, 6);
			//floor
			fillWithMetadataBlocks(world, box, 1, 0, 1, 6, 0, 6, ModBlocks.vinyl_tile, 1);
			//ceiling
			fillWithBlocks(world, box, 1, 4, 1, 6, 4, 6, ModBlocks.vinyl_tile);
			//upper shield
			fillWithBlocks(world, box, 1, 4, 4, 3, 4, 6, ModBlocks.reinforced_stone);
			fillWithBlocks(world, box, 0, 5, 0, 7, 5, 7, ModBlocks.reinforced_stone);
			//walls
			fillWithRandomizedBlocks(world, box, 0, 0, 0, 0, 4, 7, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 1, 0, 7, 6, 4, 7, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 7, 0, 0, 7, 4, 7, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 1, 0, 0, 6, 4, 0, rand, ConcreteBricks);
			//meh, fix the area later
			final int hpos = Component.getAverageHeight(world, boundingBox, box, boundingBox.maxY) - boundingBox.minY; 
			//top hatch
			setBlockState(world, ModBlocks.concrete_slab.getStateFromMeta(1), 0, hpos, 5, box);
			fillWithBlocks(world, box, 1, hpos, 4, 1, hpos, 6,
					ModBlocks.concrete_smooth_stairs.getStateFromMeta(getStairMeta(0)),
					ModBlocks.concrete_smooth_stairs.getStateFromMeta(getStairMeta(0)), false);
			setBlockState(world, ModBlocks.concrete_slab.getStateFromMeta(1), 2, hpos, 3, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(getStairMeta(2)), 2, hpos, 4, box);
			setBlockState(world, ModBlocks.trapdoor_steel.getStateFromMeta(getDecoModelMeta(8) >> 2), 2, hpos, 5, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(getStairMeta(3)), 2, hpos, 6, box);
			setBlockState(world, ModBlocks.concrete_slab.getStateFromMeta(1), 2, hpos, 7, box);
			fillWithBlocks(world, box, 3, hpos, 4, 3, hpos, 6,
					ModBlocks.concrete_smooth_stairs.getStateFromMeta(getStairMeta(1)),
					ModBlocks.concrete_smooth_stairs.getStateFromMeta(getStairMeta(1)), false);
			setBlockState(world, ModBlocks.concrete_slab.getStateFromMeta(1), 4, hpos, 5, box);
			//tunnel downwards
			fillWithBlocks(world, box, 1, 6, 4, 1, hpos - 1, 6, ModBlocks.reinforced_stone);
			fillWithBlocks(world, box, 2, 1, 6, 2, hpos - 1, 6, ModBlocks.reinforced_stone);
			fillWithBlocks(world, box, 3, 6, 4, 3, hpos - 1, 6, ModBlocks.reinforced_stone);
			fillWithBlocks(world, box, 2, 6, 4, 2, hpos - 1, 4, ModBlocks.reinforced_stone);
			fillWithMetadataBlocks(world, box, 2, 1, 5, 2, hpos - 1, 5, ModBlocks.ladder_sturdy, getDecoMeta(2)); //double check meta

			/* DECO */
			//lamps
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 2, 5, 2, box);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 5, 5, 2, box);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 5, 5, 5, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 2, 4, 2, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 5, 4, 2, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 5, 4, 5, box);
			//machine
			setBlockState(world, ModBlocks.deco_tungsten.getStateFromMeta(0), 3, 1, 6, box);
			generateInvContents(world, box, rand, Blocks.CHEST, getDecoMeta(3), 4, 1, 6, ItemPool.getPool(ItemPoolsLegacy.POOL_ANTENNA)/*TODO change */, 5);
			setBlockState(world, ModBlocks.deco_tungsten.getStateFromMeta(0), 5, 1, 6, box);
			fillWithBlocks(world, box, 3, 2, 6, 5, 2, 6, ModBlocks.concrete_smooth_stairs.getStateFromMeta(getStairMeta(2) | 4), ModBlocks.concrete_smooth_stairs.getStateFromMeta(getStairMeta(2) | 4), false);
			fillWithBlocks(world, box, 3, 3, 6, 5, 3, 6, ModBlocks.tape_recorder.getStateFromMeta(getDecoMeta(2)), ModBlocks.tape_recorder.getStateFromMeta(getDecoMeta(2)), false);
			//desk
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(getStairMeta(1) | 4), 3, 1, 4, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(getStairMeta(3) | 4), 4, 1, 4, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(getStairMeta(0) | 4), 5, 1, 4, box);
			setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(getDecoModelMeta(1)), 4, 2, 4, box);
			//clear out entryways based on path
			if(paths[0]) fillWithAir(world, box, 7, 1, 2, 7, 2, 3);
			if(paths[1]) fillWithAir(world, box, 3, 1, 0, 4, 2, 0);
			if(paths[2]) fillWithAir(world, box, 0, 1, 2, 0, 2, 3);

			return true;
		}
	}
	
	public static class Corridor extends Component implements ProceduralComponent {
		
		private boolean path;
		private int[] decorations = new int[2];
		
		public Corridor() { }

		@Override
		protected void writeStructureToNBT(NBTTagCompound tagCompound) {

		}

		@Override
		protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {

		}

		public Corridor(int componentType, StructureBoundingBox box, int coordMode, Random rand) {
			super(componentType);
			this.boundingBox = box;
			this.setCoordBaseMode(EnumFacing.byIndex(coordMode));
			
			decorations[0] = rand.nextInt(6);
			decorations[1] = rand.nextInt(6);
		}
		
		/** write to nbt */
		@Override
		protected void setToNBT(NBTTagCompound nbt) {
			super.setToNBT(nbt);
			nbt.setBoolean("p", path);
			nbt.setIntArray("d", decorations);
		}
		
		/** read from nbt */
		@Override
		protected void getFromNBT(NBTTagCompound nbt) {
			super.getFromNBT(nbt);
			path = nbt.getBoolean("p");
			decorations = nbt.getIntArray("d");
		}
		
		@Override
		public void buildComponent(ProceduralStructureStart start, Random rand) {
			path = this.getNextComponentNormal(start, this, getCoordMode(), rand, 3, 1) != null;
		}
		
		@Override
		public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
			
			fillWithAir(world, box, 1, 1, 1, 4, 3, 5);
			//floor
			fillWithMetadataBlocks(world, box, 1, 0, 1, 4, 0, 5, ModBlocks.vinyl_tile, 1);
			//ceiling
			fillWithBlocks(world, box, 1, 4, 1, 4, 4, 5, ModBlocks.vinyl_tile);
			//upper shield
			fillWithBlocks(world, box, 0, 5, 0, 5, 5, 6, ModBlocks.reinforced_stone);
			//walls
			fillWithRandomizedBlocks(world, box, 0, 0, 0, 0, 4, 6, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 1, 0, 6, 4, 4, 6, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 5, 0, 0, 5, 4, 6, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 1, 0, 0, 4, 4, 0, rand, ConcreteBricks);
			
			//TODO different deco types? maybe plants or vending machines?
			//save it to nbt either way
			/* DECO */
			//lamps
			fillWithBlocks(world, box, 2, 5, 3, 3, 5, 3, ModBlocks.reinforced_lamp_off);
			fillWithBlocks(world, box, 2, 4, 3, 3, 4, 3, ModBlocks.fan);
			//deco misc
			final int stairMetaW = getStairMeta(0);
			final int stairMetaE = getStairMeta(1);
			final int stairMetaN = getStairMeta(2);
			final int stairMetaS = getStairMeta(3);
			final int decoMetaE = getDecoMeta(4);
			final int decoMetaW = getDecoMeta(5);

			for (int i = 0; i <= 1; i++) {
				final int x = 1 + i * 3;
				switch (decorations[i]) {
					default: // table w/ chairs
						setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaS), x, 1, 2, box);
						setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaN), x, 1, 4, box);
						setBlockState(world, Blocks.OAK_FENCE.getDefaultState(), x, 1, 3, box);
						setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getStateFromMeta(1), x, 2, 3, box);
						break;

					case 1: // desk w/ computer
						setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaS | 4), x, 1, 2, box);
						setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaN), x, 1, 4, box);
						setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(getDecoModelMeta(1)), x, 2, 2, box);
						break;

					case 2: // couch
						setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaS), x, 1, 2, box);
						setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(i < 1 ? stairMetaE : stairMetaW), x, 1, 3, box);
						setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaN), x, 1, 4, box);
						break;

					case 3:
						setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaS | 4), x, 1, 2, box);
						setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta((i < 1 ? stairMetaE : stairMetaW) | 4), x, 1, 3, box);
						setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaN | 4), x, 1, 4, box);
						setBlockState(world, Blocks.FLOWER_POT.getDefaultState(), x, 2, 2, box);
						break;

					case 4:
						fillWithBlocks(world, box, x, 1, 1, x, 3, 1,
								ModBlocks.deco_tungsten.getDefaultState(), ModBlocks.deco_tungsten.getDefaultState(), false);
						setBlockState(world, ModBlocks.deco_tungsten.getDefaultState(), x, 1, 3, box);
						fillWithBlocks(world, box, x, 3, 2, x, 3, 4,
								ModBlocks.concrete_smooth_stairs.getStateFromMeta(i < 1 ? stairMetaE : stairMetaW),
								ModBlocks.concrete_smooth_stairs.getStateFromMeta(i < 1 ? stairMetaE : stairMetaW), false);
						fillWithBlocks(world, box, x, 1, 5, x, 3, 5,
								ModBlocks.deco_tungsten.getDefaultState(), ModBlocks.deco_tungsten.getDefaultState(), false);
						fillWithBlocks(world, box, x, 1, 2, x, 2, 2,
								ModBlocks.tape_recorder.getStateFromMeta(i < 1 ? decoMetaW : decoMetaE),
								ModBlocks.tape_recorder.getStateFromMeta(i < 1 ? decoMetaW : decoMetaE), false);
						fillWithBlocks(world, box, x, 1, 4, x, 2, 4,
								ModBlocks.tape_recorder.getStateFromMeta(i < 1 ? decoMetaW : decoMetaE),
								ModBlocks.tape_recorder.getStateFromMeta(i < 1 ? decoMetaW : decoMetaE), false);
						setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(i < 1 ? getDecoModelMeta(3) : getDecoModelMeta(2)), x, 2, 3, box);
						break;

					case 5:
						setBlockState(world, Blocks.OAK_FENCE.getDefaultState(), x, 1, 1, box);
						setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getStateFromMeta(0), x, 2, 1, box);
						setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaS | 4), x, 1, 3, box);
						setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaN | 4), x, 1, 4, box);
						setBlockState(world, ModBlocks.radiorec.getStateFromMeta(i < 1 ? decoMetaE : decoMetaW), x, 2, 3, box);
						break;
				}
			}
			//doors
			placeDoor(world, box, ModBlocks.door_bunker, 1, true, rand.nextBoolean(), 2, 1, 0);
			placeDoor(world, box, ModBlocks.door_bunker, 1, false, rand.nextBoolean(), 3, 1, 0);
			if(path) fillWithAir(world, box, 2, 1, 6, 3, 2, 6);
			
			return true;
		}
		
		public static StructureComponent findValidPlacement(List components, Random rand, int x, int y, int z, int coordMode, int type) {
			StructureBoundingBox box = ProceduralStructureStart.getComponentToAddBoundingBox(x, y, z, -3, -1, 0, 6, 6, 7, coordMode);
			return box.minY > 10 && StructureComponent.findIntersecting(components, box) == null ? new Corridor(type, box, coordMode, rand) : null;
		}
	}
	
	public static class BedroomL extends Component implements ProceduralComponent {
		
		private boolean path;
		
		public BedroomL() { }

		@Override
		protected void writeStructureToNBT(NBTTagCompound tagCompound) {

		}

		@Override
		protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {

		}

		public BedroomL(int componentType, StructureBoundingBox box, int coordMode) {
			super(componentType);
			this.boundingBox = box;
			this.setCoordBaseMode(EnumFacing.byIndex(coordMode));
		}
		
		/** write to nbt */
		@Override
		protected void setToNBT(NBTTagCompound nbt) {
			super.setToNBT(nbt);
			nbt.setBoolean("p", path);
		}
		
		/** read from nbt */
		@Override
		protected void getFromNBT(NBTTagCompound nbt) {
			super.getFromNBT(nbt);
			path = nbt.getBoolean("p");
		}
		
		@Override
		public void buildComponent(ProceduralStructureStart start, Random rand) {
			path = this.getNextComponentWest(start, this, getCoordMode(), rand, 9, 1) != null;
		}
		
		@Override
		public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
			
			fillWithAir(world, box, 4, 1, 1, 8, 3, 4);
			fillWithAir(world, box, 1, 1, 5, 8, 3, 9);
			//floor
			fillWithMetadataBlocks(world, box, 4, 0, 1, 8, 0, 4, ModBlocks.vinyl_tile, 1);
			fillWithMetadataBlocks(world, box, 1, 0, 5, 8, 0, 9, ModBlocks.vinyl_tile, 1);
			//ceiling
			fillWithBlocks(world, box, 4, 4, 1, 8, 4, 4, ModBlocks.vinyl_tile);
			fillWithBlocks(world, box, 1, 4, 5, 8, 4, 9, ModBlocks.vinyl_tile);
			//upper shield
			fillWithBlocks(world, box, 3, 5, 0, 9, 5, 3, ModBlocks.reinforced_stone);
			fillWithBlocks(world, box, 0, 5, 4, 9, 5, 10, ModBlocks.reinforced_stone);
			//walls
			fillWithRandomizedBlocks(world, box, 0, 0, 4, 0, 4, 10, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 1, 0, 10, 8, 4, 10, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 9, 0, 0, 9, 4, 10, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 4, 0, 0, 8, 4, 0, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 3, 0, 0, 3, 4, 4, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 1, 0, 4, 2, 4, 4, rand, ConcreteBricks);
			
			/* DECO */
			//lamps
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 3, 5, 7, box);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 6, 5, 7, box);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 6, 5, 3, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 3, 4, 7, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 6, 4, 7, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 6, 4, 3, box);
			//Beds w/ table
			final int stairMetaW = getStairMeta(0);
			final int stairMetaE = getStairMeta(1);
			final int stairMetaN = getStairMeta(2);
			final int stairMetaS = getStairMeta(3);
			placeBed(world, box, 1, 5, 1, 1);
			placeBed(world, box, 1, 5, 1, 3);
			placeBed(world, box, 2, 3, 1, 6);
			placeBed(world, box, 2, 1, 1, 6);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaE | 4), 4, 1, 2, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaE | 4), 4, 1, 4, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaS | 4), 4, 1, 5, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaS | 4), 2, 1, 5, box);
			setBlockState(world, ModBlocks.radiorec.getStateFromMeta(getDecoMeta(4)), 4, 2, 4, box);
			//table w/ microwave
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaS | 4), 8, 1, 3, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaN | 4), 8, 1, 4, box);
			setBlockState(world, Blocks.NOTEBLOCK.getStateFromMeta(0), 8, 1, 5, box);
			setBlockState(world, ModBlocks.machine_microwave.getStateFromMeta(getDecoMeta(4)), 8, 2, 4, box);
			//desk w/ computer
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaW | 4), 6, 1, 9, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaN | 4), 5, 1, 9, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaE | 4), 4, 1, 9, box);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaS), 5, 1, 8, box);
			setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(getDecoModelMeta(0)), 5, 2, 9, box);
			generateInvContents(world, box, rand, ModBlocks.filing_cabinet, getDecoModelMeta(0), 3, 1, 9, ItemPool.getPool(ItemPoolsComponent.POOL_FILING_CABINET), 5);
			//lockers
			generateInvContents(world, box, rand, Blocks.CHEST, getDecoMeta(4), 8, 1, 7, ItemPool.getPool(ItemPoolsComponent.POOL_VAULT_LOCKERS), 3);
			generateInvContents(world, box, rand, Blocks.CHEST, getDecoMeta(4), 8, 2, 7, ItemPool.getPool(ItemPoolsComponent.POOL_VAULT_LOCKERS), 5);
			fillWithBlocks(world, box, 8, 1, 8, 8, 2, 8, ModBlocks.deco_tungsten);
			generateInvContents(world, box, rand, Blocks.CHEST, getDecoMeta(4), 8, 1, 9, ItemPool.getPool(ItemPoolsComponent.POOL_VAULT_LOCKERS), 4);
			generateInvContents(world, box, rand, Blocks.CHEST, getDecoMeta(4), 8, 2, 9, ItemPool.getPool(ItemPoolsComponent.POOL_VAULT_LOCKERS), 5);
			fillWithMetadataBlocks(world, box, 8, 3, 7, 8, 3, 9, Blocks.TRAPDOOR, getDecoModelMeta(2) >> 2);
			//doors
			placeDoor(world, box, ModBlocks.door_bunker, 1, true, rand.nextBoolean(), 7, 1, 0);
			placeDoor(world, box, ModBlocks.door_bunker, 1, false, rand.nextBoolean(), 8, 1, 0);
			if(path) fillWithAir(world, box, 0, 1, 8, 0, 2, 9);
			
			return true;
		}
		
		public static StructureComponent findValidPlacement(List components, Random rand, int x, int y, int z, int coordMode, int type) {
			StructureBoundingBox box = ProceduralStructureStart.getComponentToAddBoundingBox(x, y, z, -8, -1, 0, 10, 6, 11, coordMode);
			return box.minY > 10 && StructureComponent.findIntersecting(components, box) == null ? new BedroomL(type, box, coordMode) : null;
		}
	}
	
	public static class FunJunction extends Component implements ProceduralComponent {
		
		private boolean[] paths = new boolean[2];
		
		public FunJunction() { }

		@Override
		protected void writeStructureToNBT(NBTTagCompound tagCompound) {

		}

		@Override
		protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {

		}

		public FunJunction(int componentType, StructureBoundingBox box, int coordMode) {
			super(componentType);
			this.boundingBox = box;
			this.setCoordBaseMode(EnumFacing.byIndex(coordMode));
		}
		
		/** write to nbt */
		@Override
		protected void setToNBT(NBTTagCompound nbt) {
			super.setToNBT(nbt);
			for(int i = 0; i < paths.length; i++)
				nbt.setBoolean("p" + i, paths[i]);
		}
		
		/** read from nbt */
		@Override
		protected void getFromNBT(NBTTagCompound nbt) {
			super.getFromNBT(nbt);
			for(int i = 0; i < paths.length; i++)
				paths[i] = nbt.getBoolean("p" + i);
		}
		
		@Override
		public void buildComponent(ProceduralStructureStart start, Random rand) {
			paths[0] = this.getNextComponentEast(start, this, getCoordMode(), rand, 6, 1) != null;
			paths[1] = this.getNextComponentNormal(start, this, getCoordMode(), rand, 5, 1) != null;
		}
		
		@Override
		public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
			
			fillWithAir(world, box, 1, 1, 1, 6, 3, 10);
			//floor
			fillWithMetadataBlocks(world, box, 1, 0, 1, 6, 0, 10, ModBlocks.vinyl_tile, 1);
			//ceiling
			fillWithBlocks(world, box, 1, 4, 1, 6, 4, 10, ModBlocks.vinyl_tile);
			//upper shield
			fillWithBlocks(world, box, 0, 5, 0, 7, 5, 11, ModBlocks.reinforced_stone);
			//walls
			fillWithRandomizedBlocks(world, box, 0, 0, 0, 0, 4, 11, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 1, 0, 11, 6, 4, 11, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 7, 0, 0, 7, 4, 11, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 1, 0, 0, 6, 4, 0, rand, ConcreteBricks);
			
			/* DECO */
			//lamps
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 2, 5, 3, box);
			fillWithBlocks(world, box, 5, 5, 5, 5, 5, 6, ModBlocks.reinforced_lamp_off);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 2, 5, 8, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 2, 4, 3, box);
			fillWithBlocks(world, box, 5, 4, 5, 5, 4, 6, ModBlocks.fan);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 2, 4, 8, box);
			//couches w/ tables
			final int stairMetaW = getStairMeta(0);
			final int stairMetaE = getStairMeta(1);
			final int stairMetaN = getStairMeta(2);
			final int stairMetaS = getStairMeta(3);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaE), 1, 1, 1, box);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaS), 2, 1, 1, box);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaW), 3, 1, 1, box);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaS), 1, 1, 4, box);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaE), 1, 1, 5, box);
			fillWithMetadataBlocks(world, box, 1, 1, 6, 2, 1, 6, Blocks.OAK_STAIRS, stairMetaN);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaW), 3, 1, 6, box);
			setBlockState(world, Blocks.OAK_FENCE.getStateFromMeta(0), 1, 1, 3, box);
			setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getStateFromMeta(0), 1, 2, 3, box);
			setBlockState(world, Blocks.OAK_FENCE.getStateFromMeta(0), 3, 1, 4, box);
			setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getStateFromMeta(0), 3, 2, 4, box);
			//table & chest
			setBlockState(world, Blocks.OAK_FENCE.getStateFromMeta(0), 6, 1, 2, box);
			setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getStateFromMeta(0), 6, 2, 2, box);
			generateInvContents(world, box, rand, Blocks.CHEST, getDecoMeta(4), 6, 1, 3, ItemPool.getPool(ItemPoolsComponent.POOL_VAULT_LOCKERS), 8);
			//desk w/ computer + bobblehead
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaS | 4), 1, 1, 8, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaE | 4), 1, 1, 9, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaN | 4), 1, 1, 10, box);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaS), 2, 1, 8, box);
			setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(getDecoModelMeta(3)), 1, 2, 9, box);
			if(rand.nextBoolean()) placeRandomBobble(world, box, rand, 1, 2, 8);
			//jukebox
			fillWithBlocks(world, box, 6, 1, 8, 6, 2, 8, Blocks.NOTEBLOCK);
			setBlockState(world, ModBlocks.deco_tungsten.getStateFromMeta(0), 6, 1, 9, box);
			setBlockState(world, ModBlocks.tape_recorder.getStateFromMeta(getDecoMeta(4)), 6, 2, 9, box);
			fillWithBlocks(world, box, 6, 3, 8, 6, 3, 9, ModBlocks.concrete_slab);
			placeLever(world, box, 2, rand.nextBoolean(), 5, 1, 9);
			//doors
			placeDoor(world, box, ModBlocks.door_bunker, 1, true, rand.nextBoolean(), 4, 1, 0);
			placeDoor(world, box, ModBlocks.door_bunker, 1, false, rand.nextBoolean(), 5, 1, 0);
			if(paths[0]) fillWithAir(world, box, 7, 1, 5, 7, 2, 6);
			if(paths[1]) fillWithAir(world, box, 4, 1, 11, 5, 2, 11);
			
			return true;
		}
		
		public static StructureComponent findValidPlacement(List components, Random rand, int x, int y, int z, int coordMode, int type) {
			StructureBoundingBox box = ProceduralStructureStart.getComponentToAddBoundingBox(x, y, z, -5, -1, 0, 8, 6, 12, coordMode);
			return box.minY > 10 && StructureComponent.findIntersecting(components, box) == null ? new FunJunction(type, box, coordMode) : null;
		}
	}
	
	public static class BathroomL extends Component implements ProceduralComponent {
		
		private boolean path;
		
		public BathroomL() { }

		@Override
		protected void writeStructureToNBT(NBTTagCompound tagCompound) {

		}

		@Override
		protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {

		}

		public BathroomL(int componentType, StructureBoundingBox box, int coordMode) {
			super(componentType);
			this.boundingBox = box;
			this.setCoordBaseMode(EnumFacing.byIndex(coordMode));
		}
		
		/** write to nbt */
		@Override
		protected void setToNBT(NBTTagCompound nbt) {
			super.setToNBT(nbt);
			nbt.setBoolean("p", path);
		}
		
		/** read from nbt */
		@Override
		protected void getFromNBT(NBTTagCompound nbt) {
			super.getFromNBT(nbt);
			path = nbt.getBoolean("p");
		}
		
		@Override
		public void buildComponent(ProceduralStructureStart start, Random rand) {
			path = this.getNextComponentEast(start, this, getCoordMode(), rand, 3, 1) != null;
		}
		
		@Override
		public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
			
			fillWithAir(world, box, 1, 1, 1, 7, 3, 9);
			//floor
			fillWithMetadataBlocks(world, box, 1, 0, 1, 7, 0, 9, ModBlocks.vinyl_tile, 1);
			//ceiling
			fillWithBlocks(world, box, 1, 4, 1, 7, 4, 9, ModBlocks.vinyl_tile);
			//upper shield
			fillWithBlocks(world, box, 0, 5, 0, 8, 5, 10, ModBlocks.reinforced_stone);
			//walls
			fillWithRandomizedBlocks(world, box, 0, 0, 0, 0, 4, 10, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 1, 0, 10, 7, 4, 10, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 8, 0, 0, 8, 4, 10, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 1, 0, 0, 7, 4, 0, rand, ConcreteBricks);
			
			/* DECO */
			//lamps
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 2, 5, 3, box);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 2, 5, 7, box);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 5, 5, 7, box);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 5, 5, 3, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 2, 4, 3, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 2, 4, 7, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 5, 4, 7, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 5, 4, 3, box);
			//sinks
			for(int i = 2; i <= 8; i += 2) {
				setBlockState(world, Blocks.CAULDRON.getStateFromMeta(rand.nextInt(4)), 1, 1, i, box);
				setBlockState(world, ModBlocks.concrete_slab.getStateFromMeta(8), 1, 1, i + 1, box);
				setBlockState(world, Blocks.TRIPWIRE_HOOK.getStateFromMeta(getTripwireMeta(3)), 1, 2, i, box);
			}
			//hand-dryers (industrial-strength)
			setBlockState(world, ModBlocks.steel_beam.getStateFromMeta(3), 4, 1, 9, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(getDecoMeta(2)), 4, 2, 9, box);
			setBlockState(world, Blocks.STONE_BUTTON.getStateFromMeta(getButtonMeta(2)), 3, 2, 9, box); //TODO button meta
			setBlockState(world, ModBlocks.steel_beam.getStateFromMeta(3), 6, 1, 9, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(getDecoMeta(2)), 6, 2, 9, box);
			setBlockState(world, Blocks.STONE_BUTTON.getStateFromMeta(getButtonMeta(1)), 7, 2, 9, box);
			//stalls w/ toilets
			for(int i = 1; i <= 5; i += 2) {
				placeDoor(world, box, ModBlocks.door_metal, 0, false, rand.nextBoolean(), 5, 1, i);
				fillWithMetadataBlocks(world, box, 5, 1, i + 1, 5, 2, i + 1, ModBlocks.steel_corner, getDecoMeta(2));
				fillWithMetadataBlocks(world, box, 6, 1, i + 1, 7, 2, i + 1, ModBlocks.steel_wall, getDecoMeta(2));
				setBlockState(world, ModBlocks.deco_pipe_rim.getStateFromMeta(0), 7, 1, i, box);
				setBlockState(world, Blocks.TRAPDOOR.getStateFromMeta(getDecoModelMeta(2) >> 2), 7, 2, i, box);
			}
			//doors
			placeDoor(world, box, ModBlocks.door_bunker, 1, true, rand.nextBoolean(), 2, 1, 0);
			placeDoor(world, box, ModBlocks.door_bunker, 1, false, rand.nextBoolean(), 3, 1, 0);
			if(path) fillWithAir(world, box, 8, 1, 7, 8, 2, 8);
			
			return true;
		}
		
		public static StructureComponent findValidPlacement(List components, Random rand, int x, int y, int z, int coordMode, int type) {
			StructureBoundingBox box = ProceduralStructureStart.getComponentToAddBoundingBox(x, y, z, -3, -1, 0, 9, 6, 11, coordMode);
			return box.minY > 10 && StructureComponent.findIntersecting(components, box) == null ? new BathroomL(type, box, coordMode) : null;
		}
	}
	
	public static class Laboratory extends Component implements ProceduralComponent {
		
		private boolean[] paths = new boolean[2];
		
		public Laboratory() { }

		@Override
		protected void writeStructureToNBT(NBTTagCompound tagCompound) {

		}

		@Override
		protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {

		}

		public Laboratory(int componentType, StructureBoundingBox box, int coordMode) {
			super(componentType);
			this.boundingBox = box;
			this.setCoordBaseMode(EnumFacing.byIndex(coordMode));
		}
		
		/** write to nbt */
		@Override
		protected void setToNBT(NBTTagCompound nbt) {
			super.setToNBT(nbt);
			for(int i = 0; i < paths.length; i++)
				nbt.setBoolean("p" + i, paths[i]);
		}
		
		/** read from nbt */
		@Override
		protected void getFromNBT(NBTTagCompound nbt) {
			super.getFromNBT(nbt);
			for(int i = 0; i < paths.length; i++)
				paths[i] = nbt.getBoolean("p" + i);
		}
		
		@Override
		public void buildComponent(ProceduralStructureStart start, Random rand) {
			paths[0] = this.getNextComponentWest(start, this, getCoordMode(), rand, 3, 1) != null;
			paths[1] = this.getNextComponentNormal(start, this, getCoordMode(), rand, 6, 1) != null;
		}
		
		@Override
		public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
			
			fillWithAir(world, box, 1, 1, 1, 7, 3, 11);
			//floor
			fillWithMetadataBlocks(world, box, 1, 0, 1, 7, 0, 11, ModBlocks.vinyl_tile, 1);
			//ceiling
			fillWithBlocks(world, box, 1, 4, 1, 7, 4, 11, ModBlocks.vinyl_tile);
			//upper shield
			fillWithBlocks(world, box, 0, 5, 0, 8, 5, 12, ModBlocks.reinforced_stone);
			//walls
			fillWithBlocks(world, box, 0, 0, 0, 0, 4, 12, ModBlocks.brick_concrete);
			fillWithBlocks(world, box, 1, 0, 12, 7, 4, 12, ModBlocks.brick_concrete);
			fillWithBlocks(world, box, 8, 0, 0, 8, 4, 12, ModBlocks.brick_concrete);
			fillWithBlocks(world, box, 1, 0, 0, 7, 4, 0, ModBlocks.brick_concrete);
			
			/* DECO */
			//lamps
			for(int x = 3; x <= 5; x += 2) {
				for(int z = 3; z <= 9; z += 3) {
					setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), x, 5, z, box);
					setBlockState(world, ModBlocks.fan.getStateFromMeta(0), x, 4, z, box);
				}
			}
			//couch w/ table
			final int stairMetaW = getStairMeta(0);
			final int stairMetaE = getStairMeta(1);
			final int stairMetaN = getStairMeta(2);
			final int stairMetaS = getStairMeta(3);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaE), 1, 1, 1, box);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaS), 2, 1, 1, box);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaW), 3, 1, 1, box);
			setBlockState(world, Blocks.OAK_FENCE.getStateFromMeta(0), 4, 1, 1, box);
			setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getStateFromMeta(0), 4, 2, 1, box);
			//big ole wall machine
			final int decoMetaE = getDecoMeta(4);
			final int decoMetaW = getDecoMeta(5);
			final int decoModelMetaW = getDecoModelMeta(2);
			final int decoModelMetaE = getDecoModelMeta(3);
			fillWithBlocks(world, box, 1, 1, 5, 1, 3, 5, ModBlocks.deco_tungsten);
			setBlockState(world, ModBlocks.deco_steel.getStateFromMeta(0), 1, 1, 6, box);
			setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(decoModelMetaE), 1, 2, 6, box);
			setBlockState(world, ModBlocks.tape_recorder.getStateFromMeta(decoMetaW), 1, 3, 6, box);
			fillWithMetadataBlocks(world, box, 1, 1, 7, 1, 3, 7, ModBlocks.tape_recorder, decoMetaW);
			fillWithBlocks(world, box, 1, 1, 8, 1, 3, 8, ModBlocks.deco_tungsten);
			fillWithMetadataBlocks(world, box, 1, 1, 9, 1, 1, 10, ModBlocks.tape_recorder, decoMetaW);
			fillWithMetadataBlocks(world, box, 1, 2, 9, 1, 2, 10, ModBlocks.concrete_smooth_stairs, stairMetaE | 4);
			fillWithMetadataBlocks(world, box, 1, 3, 9, 1, 3, 10, ModBlocks.tape_recorder, decoMetaW);
			fillWithBlocks(world, box, 1, 1, 11, 1, 3, 11, ModBlocks.deco_tungsten);
			//desks w/ computers
			generateInvContents(world, box, rand, Blocks.CHEST, getDecoMeta(2), 3, 1, 4, ItemPool.getPool(ItemPoolsComponent.POOL_MACHINE_PARTS), 6);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaS | 4), 3, 1, 5, box);
			fillWithMetadataBlocks(world, box, 4, 1, 5, 4, 1, 7, ModBlocks.concrete_smooth_stairs, stairMetaW | 4);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaN), 3, 1, 7, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaS | 4), 3, 1, 9, box);
			fillWithMetadataBlocks(world, box, 4, 1, 9, 4, 1, 11, ModBlocks.concrete_smooth_stairs, stairMetaW | 4);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaN), 3, 1, 11, box);
			setBlockState(world, Blocks.FLOWER_POT.getStateFromMeta(0), 3, 2, 5, box);
			setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(decoModelMetaW), 4, 2, 6, box);
			setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(decoModelMetaW), 4, 2, 10, box);
			//lever wall machine
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaW | 4), 7, 1, 3, box);
			setBlockState(world, ModBlocks.deco_red_copper.getStateFromMeta(0), 7, 2, 3, box);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaW), 7, 3, 3, box);
			placeLever(world, box, 2, rand.nextBoolean(), 6, 2, 3);
			fillWithMetadataBlocks(world, box, 7, 1, 4, 7, 2, 4, ModBlocks.steel_poles, decoMetaE);
			setBlockState(world, ModBlocks.deco_steel.getStateFromMeta(0), 7, 3, 4, box);
			setBlockState(world, ModBlocks.deco_tungsten.getStateFromMeta(0), 7, 1, 5, box);
			setBlockState(world, ModBlocks.tape_recorder.getStateFromMeta(decoMetaE), 7, 1, 6, box);
			setBlockState(world, ModBlocks.deco_tungsten.getStateFromMeta(0), 7, 1, 7, box);
			fillWithMetadataBlocks(world, box, 7, 2, 5, 7, 2, 7, ModBlocks.concrete_smooth_stairs, stairMetaW | 4);
			fillWithMetadataBlocks(world, box, 7, 3, 5, 7, 3, 7, ModBlocks.tape_recorder, decoMetaE);
			//table w/ chest
			setBlockState(world, Blocks.OAK_FENCE.getStateFromMeta(0), 7, 1, 9, box);
			setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getStateFromMeta(0), 7, 2, 9, box);
			generateInvContents(world, box, rand, Blocks.CHEST, getDecoMeta(4), 7, 1, 10, ItemPool.getPool(ItemPoolsComponent.POOL_VAULT_LAB), 8);
			//doors
			placeDoor(world, box, ModBlocks.door_bunker, 1, true, rand.nextBoolean(), 5, 1, 0);
			placeDoor(world, box, ModBlocks.door_bunker, 1, false, rand.nextBoolean(), 6, 1, 0);
			if(paths[0]) fillWithAir(world, box, 0, 1, 2, 0, 2, 3);
			if(paths[1]) fillWithAir(world, box, 5, 1, 12, 6, 2, 12);
			
			return true;
		}
		
		public static StructureComponent findValidPlacement(List components, Random rand, int x, int y, int z, int coordMode, int type) {
			StructureBoundingBox box = ProceduralStructureStart.getComponentToAddBoundingBox(x, y, z, -6, -1, 0, 9, 6, 12, coordMode);
			return box.minY > 10 && StructureComponent.findIntersecting(components, box) == null ? new Laboratory(type, box, coordMode) : null;
		}
	}
	
	public static class PowerRoom extends Component implements ProceduralComponent {
		
		private boolean path;
		
		private int powerType;
		
		public PowerRoom() { }

		@Override
		protected void writeStructureToNBT(NBTTagCompound tagCompound) {

		}

		@Override
		protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {

		}

		public PowerRoom(int componentType, StructureBoundingBox box, int coordMode, Random rand) {
			super(componentType);
			this.boundingBox = box;
			this.setCoordBaseMode(EnumFacing.byIndex(coordMode));
			
			float chance = rand.nextFloat();
			powerType = chance < 0.2 ? 2 : chance < 0.6 ? 1 : 0;
		}
		
		/** write to nbt */
		@Override
		protected void setToNBT(NBTTagCompound nbt) {
			super.setToNBT(nbt);
			nbt.setBoolean("p", path);
		}
		
		/** read from nbt */
		@Override
		protected void getFromNBT(NBTTagCompound nbt) {
			super.getFromNBT(nbt);
			path = nbt.getBoolean("p");
		}
		
		@Override
		public void buildComponent(ProceduralStructureStart start, Random rand) {
			path = this.getNextComponentEast(start, this, getCoordMode(), rand, 4, 1) != null;
		}
		
		@Override
		public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
			
			fillWithAir(world, box, 1, 1, 1, 10, 3, 10);
			//floor
			fillWithMetadataBlocks(world, box, 1, 0, 1, 10, 0, 10, ModBlocks.vinyl_tile, 1);
			//ceiling
			fillWithBlocks(world, box, 1, 4, 1, 10, 4, 10, ModBlocks.vinyl_tile);
			//upper shield
			fillWithBlocks(world, box, 0, 5, 0, 11, 5, 11, ModBlocks.reinforced_stone);
			//walls
			fillWithRandomizedBlocks(world, box, 0, 0, 0, 11, 4, 0, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 0, 0, 1, 0, 4, 10, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 0, 0, 11, 11, 4, 11, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 11, 0, 1, 11, 4, 10, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 5, 1, 1, 5, 3, 6, rand, ConcreteBricks);
			fillWithRandomizedBlocks(world, box, 6, 1, 6, 10, 3, 6, rand, ConcreteBricks);
			
			/* DECO */
			//lamps
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 3, 5, 2, box);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 3, 5, 5, box);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 3, 5, 8, box);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 6, 5, 8, box);
			setBlockState(world, ModBlocks.reinforced_lamp_off.getStateFromMeta(0), 9, 5, 8, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 3, 4, 2, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 3, 4, 5, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 3, 4, 8, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 6, 4, 8, box);
			setBlockState(world, ModBlocks.fan.getStateFromMeta(0), 9, 4, 8, box);
			//power room stuff
			fillWithBlocks(world, box, 7, 2, 6, 9, 2, 6, ModBlocks.reinforced_glass);
			int decoMetaE = getDecoMeta(5);
			int decoMetaW = getDecoMeta(4);
			int decoMetaN = getDecoMeta(3);
			int decoMetaS = getDecoMeta(2);
			
			int stairMetaS = getStairMeta(3);
			int stairMetaN = getStairMeta(2);
			int stairMetaW = getStairMeta(1);
			int stairMetaE = getStairMeta(0);
			
			switch(this.powerType) {
			default:
				fillWithBlocks(world, box, 6, 1, 1, 6, 3, 1, ModBlocks.deco_pipe_framed_rusted);
				for(int i = 7; i <= 9; i += 2) {
					setBlockState(world, ModBlocks.machine_electric_furnace_off.getStateFromMeta(decoMetaN), i, 1, 1, box);
					setBlockState(world, ModBlocks.steel_beam.getStateFromMeta(2), i, 2, 1, box);
					setBlockState(world, ModBlocks.machine_electric_furnace_off.getStateFromMeta(decoMetaN), i, 3, 1, box);
				}
				setBlockState(world, ModBlocks.deco_red_copper.getStateFromMeta(0), 8, 1, 1, box);
				setBlockState(world, ModBlocks.concrete_colored_ext.getStateFromMeta(5), 8, 2, 1, box);
				setBlockState(world, ModBlocks.deco_red_copper.getStateFromMeta(0), 8, 3, 1, box);
				placeLever(world, box, 3, rand.nextBoolean(), 8, 2, 2);
				for(int i = 1; i <= 3; i += 2) {
					setBlockState(world, ModBlocks.deco_steel.getStateFromMeta(0), 10, i, 1, box);
					fillWithMetadataBlocks(world, box, 10, i, 2, 10, i, 4, ModBlocks.deco_pipe_quad_rusted, getPillarMeta(8));
					setBlockState(world, ModBlocks.deco_steel.getStateFromMeta(0), 10, i, 5, box);
				}
				setBlockState(world, ModBlocks.deco_pipe_framed_rusted.getStateFromMeta(0), 10, 2, 1, box);
				setBlockState(world, ModBlocks.fluid_duct_gauge.getStateFromMeta(decoMetaW), 10, 2, 5, box);
				setBlockState(world, ModBlocks.barrel_plastic.getStateFromMeta(0), 6, 1, 5, box);
				//chests
				generateInvContents(world, box, rand, Blocks.CHEST, decoMetaS, 7, 1, 5, ItemPool.getPool(ItemPoolsComponent.POOL_SOLID_FUEL), 5);
				generateInvContents(world, box, rand, Blocks.CHEST, decoMetaS, 9, 1, 5, ItemPool.getPool(ItemPoolsComponent.POOL_SOLID_FUEL), 6);
				break;
			case 1:
				setBlockState(world, ModBlocks.concrete_colored_ext.getStateFromMeta(5), 6, 1, 1, box);
				setBlockState(world, ModBlocks.cable_detector.getStateFromMeta(0), 6, 2, 1, box);
				setBlockState(world, ModBlocks.concrete_colored_ext.getStateFromMeta(5), 6, 3, 1, box);
				placeLever(world, box, 3, false, 6, 2, 2);
				for(int i = 7; i <= 9; i += 2) {
					setBlockState(world, ModBlocks.steel_scaffold.getStateFromMeta(8), i, 1, 1, box); //i'm not making another fucking meta method
					setBlockState(world, ModBlocks.machine_diesel.getStateFromMeta(decoMetaE), i, 2, 1, box);
				}
				setBlockState(world, ModBlocks.deco_pipe_rim_rusted.getStateFromMeta(getPillarMeta(4)), 8, 2, 1, box);
				setBlockState(world, ModBlocks.deco_pipe_rim_rusted.getStateFromMeta(getPillarMeta(4)), 8, 2, 1, box);
				fillWithMetadataBlocks(world, box, 7, 3, 1, 9, 3, 1, ModBlocks.concrete_smooth_stairs, stairMetaS);
				fillWithBlocks(world, box, 10, 1, 1, 10, 1, 3, ModBlocks.deco_steel);
				setBlockState(world, ModBlocks.deco_red_copper.getStateFromMeta(0), 10, 2, 1, box);
				setBlockState(world, ModBlocks.deco_steel.getStateFromMeta(0), 10, 3, 1, box);
				setBlockState(world, ModBlocks.steel_grate.getStateFromMeta(7), 10, 2, 2, box);
				setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(getDecoModelMeta(2)), 10, 3, 2, box);
				fillWithMetadataBlocks(world, box, 10, 2, 3, 10, 3, 3, ModBlocks.tape_recorder, decoMetaW);
				fillWithMetadataBlocks(world, box, 9, 1, 2, 9, 1, 3, ModBlocks.steel_grate, 7);
				fillWithBlocks(world, box, 9, 1, 5, 10, 1, 5, ModBlocks.barrel_iron);
				setBlockState(world, ModBlocks.barrel_iron.getStateFromMeta(0), 10, 2, 5, box);
				fillWithBlocks(world, box, 6, 1, 5, 6, 2, 5, ModBlocks.barrel_iron);
				setBlockState(world, ModBlocks.barrel_iron.getStateFromMeta(0), 6, 1, 2, box);
				break;
			case 2:
				for(int i = 7; i <= 9; i += 2) {
					fillWithBlocks(world, box, i, 1, 2, i, 1, 4, ModBlocks.deco_lead);
					fillWithBlocks(world, box, i, 2, 2, i, 2, 4, ModBlocks.block_lead);
					fillWithBlocks(world, box, i, 3, 2, i, 3, 4, ModBlocks.deco_lead);
				}
				setBlockState(world, ModBlocks.concrete_colored_ext.getStateFromMeta(5), 8, 1, 4, box);
				setBlockState(world, Blocks.REDSTONE_LAMP.getStateFromMeta(0), 8, 2, 4, box);
				setBlockState(world, ModBlocks.concrete_colored_ext.getStateFromMeta(5), 8, 3, 4, box);
				placeLever(world, box, 3, rand.nextBoolean(), 8, 2, 5);
				setBlockState(world, ModBlocks.pwr_fuelrod.getStateFromMeta(0), 8, 1, 3, box);
				setBlockState(world, ModBlocks.pwr_control.getStateFromMeta(0), 8, 2, 3, box);
				setBlockState(world, ModBlocks.pwr_fuelrod.getStateFromMeta(0), 8, 3, 3, box);
				setBlockState(world, ModBlocks.block_copper.getStateFromMeta(0), 8, 1, 2, box);
				setBlockState(world, ModBlocks.block_lead.getStateFromMeta(0), 8, 2, 2, box);
				setBlockState(world, ModBlocks.block_copper.getStateFromMeta(0), 8, 3, 2, box);
				setBlockState(world, ModBlocks.pwr_channel.getStateFromMeta(0), 8, 1, 1, box);
				setBlockState(world, ModBlocks.machine_turbine.getStateFromMeta(0), 8, 2, 1, box);
				setBlockState(world, ModBlocks.pwr_channel.getStateFromMeta(0), 8, 3, 1, box);
				fillWithBlocks(world, box, 9, 1, 1, 9, 3, 1, ModBlocks.deco_steel);
				setBlockState(world, ModBlocks.steel_grate.getStateFromMeta(7), 10, 1, 1, box);
				setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(getDecoModelMeta(1)), 10, 2, 1, box);
				setBlockState(world, ModBlocks.tape_recorder.getStateFromMeta(decoMetaN), 10, 3, 1, box);
				fillWithMetadataBlocks(world, box, 6, 1, 1, 7, 1, 1, ModBlocks.deco_pipe_quad_rusted, getPillarMeta(4));
				setBlockState(world, ModBlocks.deco_pipe_quad_rusted.getStateFromMeta(getPillarMeta(4)), 7, 3, 1, box);
				setBlockState(world, ModBlocks.fluid_duct_gauge.getStateFromMeta(decoMetaN), 6, 3, 1, box);
				//chest
				generateInvContents(world, box, rand, Blocks.CHEST, decoMetaN, 6, 1, 2, ItemPool.getPool(ItemPoolsComponent.POOL_NUKE_FUEL), 8);
				break;
			}
			//transformer
			fillWithMetadataBlocks(world, box, 1, 1, 1, 1, 1, 5, ModBlocks.concrete_smooth_stairs, stairMetaW | 4);
			fillWithBlocks(world, box, 1, 1, 6, 1, 3, 6, ModBlocks.concrete_pillar);
			fillWithMetadataBlocks(world, box, 1, 3, 1, 1, 3, 5, ModBlocks.concrete_smooth_stairs, stairMetaW);
			setBlockState(world, ModBlocks.machine_transformer.getStateFromMeta(0), 1, 2, 1, box);
			setBlockState(world, ModBlocks.cable_diode.getStateFromMeta(decoMetaN), 1, 2, 2, box);
			setBlockState(world, ModBlocks.machine_battery.getStateFromMeta(decoMetaE), 1, 2, 3, box);
			setBlockState(world, ModBlocks.deco_red_copper.getStateFromMeta(0), 1, 2, 4, box);
			setBlockState(world, ModBlocks.cable_switch.getStateFromMeta(0), 1, 2, 5, box);
			//machine
			for(int i = 1; i <= 5; i += 4) {
				setBlockState(world, ModBlocks.deco_beryllium.getStateFromMeta(0), i, 1, 10, box);
				setBlockState(world, ModBlocks.steel_scaffold.getStateFromMeta(0), i, 2, 10, box);
				setBlockState(world, ModBlocks.deco_beryllium.getStateFromMeta(0), i, 3, 10, box);
			}
			setBlockState(world, ModBlocks.steel_scaffold.getStateFromMeta(0), 2, 1, 10, box);
			setBlockState(world, ModBlocks.deco_tungsten.getStateFromMeta(0), 3, 1, 10, box);
			setBlockState(world, ModBlocks.steel_scaffold.getStateFromMeta(0), 4, 1, 10, box);
			setBlockState(world, ModBlocks.tape_recorder.getStateFromMeta(decoMetaS), 2, 2, 10, box);
			setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(getDecoModelMeta(0)), 3, 2, 10, box);
			setBlockState(world, ModBlocks.tape_recorder.getStateFromMeta(decoMetaS), 4, 2, 10, box);
			fillWithMetadataBlocks(world, box, 2, 3, 10, 4, 3, 10, ModBlocks.tape_recorder, decoMetaS);
			//desk
			fillWithMetadataBlocks(world, box, 8, 1, 10, 10, 1, 10, ModBlocks.concrete_smooth_stairs, stairMetaN | 4);
			setBlockState(world, ModBlocks.concrete_smooth_stairs.getStateFromMeta(stairMetaE | 4), 10, 1, 9, box);
			setBlockState(world, Blocks.OAK_STAIRS.getStateFromMeta(stairMetaS), 9, 1, 9, box);
			setBlockState(world, Blocks.FLOWER_POT.getStateFromMeta(0), 8, 2, 10, box);
			setBlockState(world, ModBlocks.deco_computer.getStateFromMeta(getDecoModelMeta(0)), 9, 2, 10, box);
			//loot
			generateInvContents(world, box, rand, Blocks.CHEST, decoMetaE, 1, 1, 7, ItemPool.getPool(ItemPoolsComponent.POOL_MACHINE_PARTS), 6);
			generateInvContents(world, box, rand, ModBlocks.filing_cabinet, getDecoModelMeta(0), 7, 1, 10, ItemPool.getPool(ItemPoolsComponent.POOL_FILING_CABINET), 4);
			//doors
			placeDoor(world, box, ModBlocks.door_bunker, 1, true, rand.nextBoolean(), 3, 1, 0);
			placeDoor(world, box, ModBlocks.door_bunker, 1, false, rand.nextBoolean(), 4, 1, 0);
			placeDoor(world, box, ModBlocks.door_bunker, 0, false, false, 5, 1, 3);
			if(path) fillWithAir(world, box, 11, 1, 7, 11, 2, 8);
			
			return true;
		}
		
		public static StructureComponent findValidPlacement(List components, Random rand, int x, int y, int z, int coordMode, int type) {
			StructureBoundingBox box = ProceduralStructureStart.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 12, 6, 12, coordMode);
			return box.minY > 10 && StructureComponent.findIntersecting(components, box) == null ? new PowerRoom(type, box, coordMode, rand) : null;
		}
	}
}
