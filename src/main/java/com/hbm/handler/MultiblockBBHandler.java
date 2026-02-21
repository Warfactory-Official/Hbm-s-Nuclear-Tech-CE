package com.hbm.handler;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class MultiblockBBHandler {

	public static final MultiblockBounds FENSU_BOUNDS = load(new ResourceLocation(Tags.MODID, "multiblock_bounds/bb_fensu0.mbb"));
	
	public static final Map<Block, MultiblockBounds> REGISTRY = new HashMap<>();
	
	public static MultiblockBounds load(ResourceLocation loc){
		try {
			InputStream s = MainRegistry.class.getResourceAsStream("/assets/"+loc.getNamespace()+"/"+loc.getPath());
			return parse(ByteBuffer.wrap(IOUtils.toByteArray(s)));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static MultiblockBounds parse(ByteBuffer buf){
		buf.order(ByteOrder.LITTLE_ENDIAN);
		int version = buf.getInt();
		int offsetX = buf.getInt();
		int offsetY = buf.getInt();
		int offsetZ = buf.getInt();
		AxisAlignedBB[] boundingBoxes = new AxisAlignedBB[buf.getInt()];
		int numBlocks = buf.getInt();
		
		Map<BlockPos, AxisAlignedBB[]> blocks = new HashMap<>();
		
		for(int i = 0; i < boundingBoxes.length; i ++){
			boundingBoxes[i] = new AxisAlignedBB(buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat());
		}
		for(int i = 0; i < numBlocks; i ++){
			BlockPos pos = new BlockPos(buf.getFloat(), buf.getFloat(), buf.getFloat());
			AxisAlignedBB[] blockBoxes = new AxisAlignedBB[buf.getInt()];
			for(int j = 0; j < blockBoxes.length; j ++){
				blockBoxes[j] = new AxisAlignedBB(buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat());
			}
			blocks.put(pos, blockBoxes);
		}
		
		return new MultiblockBounds(boundingBoxes, blocks);
	}
	
	public static void init(){
		REGISTRY.put(ModBlocks.machine_fensu, FENSU_BOUNDS);
	}
	
	public static class MultiblockBounds {
		public AxisAlignedBB[] boxes;
		public Map<BlockPos, AxisAlignedBB[]> blocks;
		
		public MultiblockBounds(AxisAlignedBB[] boxes, Map<BlockPos, AxisAlignedBB[]> blocks) {
			this.boxes = boxes;
			this.blocks = blocks;
		}
	}
	
}
