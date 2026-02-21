package com.hbm.world.feature;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SchistStratum {

    private final IBlockState b;
    private final double scale;
    private final double threshold;
    private final double thickness;
    private final int height;
    private NoiseGeneratorPerlin noise;
    private final MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public SchistStratum(IBlockState s, double scale, double threshold, double thickness, int height) {
        this.b = s;
        this.scale = scale;
        this.threshold = threshold;
        this.thickness = thickness;
        this.height = height;
    }

    @SubscribeEvent
    public void onDecorate(DecorateBiomeEvent.Pre event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        if (world.provider.getDimension() != 0) return;

        if (this.noise == null) {
            this.noise = new NoiseGeneratorPerlin(event.getRand(), 4);
        }
        ChunkPos chunkPos = event.getChunkPos();
        int cX = chunkPos.getXStart();
        int cZ = chunkPos.getZStart();
        MutableBlockPos pos = this.mutablePos;

        int minY = 1;
        int maxY = world.getHeight() - 1;

        for (int x = cX; x < cX + 16; x++) {
            for (int z = cZ; z < cZ + 16; z++) {
                double n = noise.getValue(x * scale, z * scale);
                if (n <= threshold) continue;

                double range = (n - threshold) * (thickness * 0.5 - 1);

                if (range > thickness * 0.5) range = thickness - range;
                if (range <= 0) continue;

                int r = (int) range;

                int yStart = Math.max(minY, height - r);
                int yEnd = Math.min(maxY, height + r);

                for (int y = yStart; y <= yEnd; y++) {
                    pos.setPos(x, y, z);
                    IBlockState target = world.getBlockState(pos);

                    if (target.isNormalCube() && target.getMaterial() == Material.ROCK) {
                        world.setBlockState(pos, b, 2 | 16);
                    }
                }
            }
        }
    }
}
