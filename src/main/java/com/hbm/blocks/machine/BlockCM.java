package com.hbm.blocks.machine;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.Arrays;
import java.util.Locale;

public class BlockCM<E extends Enum<E>> extends BlockEnumMeta<E> {

	public BlockCM(String registryName, E[] blockEnum) {
		super(Material.IRON, SoundType.METAL, registryName, blockEnum, true, true);
	}

	@Override
	protected BlockBakeFrame[] generateBlockFrames(String registryName) {
		return Arrays.stream(blockEnum)
				.map(Enum::name)
				.map(name -> registryName + "_" + name.toLowerCase(Locale.US))
				.map(BlockBakeFrame::cubeAll)
				.toArray(BlockBakeFrame[]::new);
	}
}
