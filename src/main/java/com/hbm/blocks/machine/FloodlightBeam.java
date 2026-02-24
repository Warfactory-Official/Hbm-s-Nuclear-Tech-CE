package com.hbm.blocks.machine;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class FloodlightBeam extends BlockBeamBase {

    public FloodlightBeam(String name) {
        super(name);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityFloodlightBeam();
    }

    public static class TileEntityFloodlightBeam extends TileEntity implements ITickable {

        public Floodlight.TileEntityFloodlight cache;
        public int sourceX;
        public int sourceY;
        public int sourceZ;
        public int index;

        @Override
        public void update() {

            if(!world.isRemote && world.getTotalWorldTime() % 5 == 0) {

                if(cache == null) {

                    if(world.getChunkProvider().isChunkGeneratedAt(sourceX >> 4, sourceZ >> 4)) {
                        TileEntity tile = world.getTileEntity(new BlockPos(sourceX, sourceY, sourceZ));
                        if(tile instanceof Floodlight.TileEntityFloodlight) {
                            cache = (Floodlight.TileEntityFloodlight) tile; // chunk is loaded, tile exists -> cache
                        } else {
                            world.setBlockState(getPos(), Blocks.AIR.getDefaultState(), 2); // chunk is loaded, tile does not exist -> delete self
                        }
                    }
                }

                if((cache != null && (cache.isInvalid() || !cache.isOn || !getPos().equals(cache.lightPos[index]))) || sourceY == 0) {
                    world.setBlockState(getPos(), Blocks.AIR.getDefaultState(), 2);
                }
            }
        }

        public void setSource(Floodlight.TileEntityFloodlight floodlight, int x, int y, int z, int i) {
            cache = floodlight;
            sourceX = x;
            sourceY = y;
            sourceZ = z;
            index = i;
        }

        @Override
        public void readFromNBT(@NotNull NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            this.sourceX = nbt.getInteger("sourceX");
            this.sourceY = nbt.getInteger("sourceY");
            this.sourceZ = nbt.getInteger("sourceZ");
            this.index = nbt.getInteger("index");
        }

        @Override
        public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setInteger("sourceX", sourceX);
            nbt.setInteger("sourceY", sourceY);
            nbt.setInteger("sourceZ", sourceZ);
            nbt.setInteger("index", index);
            return nbt;
        }
    }
}
