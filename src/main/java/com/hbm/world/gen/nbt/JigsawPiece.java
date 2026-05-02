package com.hbm.world.gen.nbt;

import net.minecraft.block.Block;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;

import java.util.HashMap;
import java.util.Map;

// Assigned to a Component to build
public class JigsawPiece {

    // Translates a given name into a jigsaw piece, for serialization
    protected static Map<String, JigsawPiece> jigsawMap = new HashMap<>();

	public final String name;
	public final NBTStructure structure;

	// Block modifiers, for randomization and terrain matching
	public Map<Block, BlockSelector> blockTable;
	public boolean conformToTerrain = false; // moves every single column to the terrain (digging out trenches, natural formations)
	public boolean alignToTerrain = false; // aligns this component y-level individually, without moving individual columns (village houses)
	public int heightOffset; // individual offset for the structure
    public BlockSelector platform; // fills the air gap beneath a piece when it needs structural support
    public int instanceLimit = 0; // maximum allowed instances of this piece in one structure
    public boolean required = false; // extends generation past normal limits until this piece appears at least once

	public JigsawPiece(String name, NBTStructure structure) {
		this(name, structure, 0);
	}

	public JigsawPiece(String name, NBTStructure structure, int heightOffset) {
		if(name == null) throw new IllegalStateException("A severe error has occurred in NBTStructure! A jigsaw piece has been registered without a valid name!");
		if(jigsawMap.containsKey(name)) throw new IllegalStateException("A severe error has occurred in NBTStructure! A jigsaw piece has been registered with the same name as another: " + name);

		this.name = name;
		this.structure = structure;
		jigsawMap.put(name, this);

		this.heightOffset = heightOffset;
	}

}
