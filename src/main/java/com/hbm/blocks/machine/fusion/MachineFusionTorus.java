package com.hbm.blocks.machine.fusion;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.fusion.TileEntityFusionTorus;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MachineFusionTorus extends BlockDummyable implements ITooltipProvider {
    public MachineFusionTorus(String s) {
        super(Material.IRON, s);
    }

    public static final int[][][] layout = new int[][][] {

            new int[][] {
                    new int[] {0,0,0,0,3,3,3,3,3,3,3,0,0,0,0},
                    new int[] {0,0,0,3,1,1,1,1,1,1,1,3,0,0,0},
                    new int[] {0,0,3,1,1,1,1,1,1,1,1,1,3,0,0},
                    new int[] {0,3,1,1,1,1,1,1,1,1,1,1,1,3,0},
                    new int[] {3,1,1,1,1,3,3,3,3,3,1,1,1,1,3},
                    new int[] {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    new int[] {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    new int[] {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    new int[] {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    new int[] {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    new int[] {3,1,1,1,1,3,3,3,3,3,1,1,1,1,3},
                    new int[] {0,3,1,1,1,1,1,1,1,1,1,1,1,3,0},
                    new int[] {0,0,3,1,1,1,1,1,1,1,1,1,3,0,0},
                    new int[] {0,0,0,3,1,1,1,1,1,1,1,3,0,0,0},
                    new int[] {0,0,0,0,3,3,3,3,3,3,3,0,0,0,0},
            },
            new int[][] {
                    new int[] {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
                    new int[] {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
                    new int[] {0,0,1,1,2,2,2,2,2,2,2,1,1,0,0},
                    new int[] {0,1,1,2,1,1,1,1,1,1,1,2,1,1,0},
                    new int[] {1,1,2,1,1,1,1,1,1,1,1,1,2,1,1},
                    new int[] {1,1,2,1,1,3,3,3,3,3,1,1,2,1,1},
                    new int[] {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    new int[] {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    new int[] {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    new int[] {1,1,2,1,1,3,3,3,3,3,1,1,2,1,1},
                    new int[] {1,1,2,1,1,1,1,1,1,1,1,1,2,1,1},
                    new int[] {0,1,1,2,1,1,1,1,1,1,1,2,1,1,0},
                    new int[] {0,0,1,1,2,2,2,2,2,2,2,1,1,0,0},
                    new int[] {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
                    new int[] {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
            },
            new int[][] {
                    new int[] {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
                    new int[] {0,0,0,1,2,2,2,2,2,2,2,1,0,0,0},
                    new int[] {0,0,1,2,2,2,2,2,2,2,2,2,1,0,0},
                    new int[] {0,1,2,2,2,2,2,2,2,2,2,2,2,1,0},
                    new int[] {1,2,2,2,1,1,1,1,1,1,1,2,2,2,1},
                    new int[] {1,2,2,2,1,3,3,3,3,3,1,2,2,2,1},
                    new int[] {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    new int[] {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    new int[] {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    new int[] {1,2,2,2,1,3,3,3,3,3,1,2,2,2,1},
                    new int[] {1,2,2,2,1,1,1,1,1,1,1,2,2,2,1},
                    new int[] {0,1,2,2,2,2,2,2,2,2,2,2,2,1,0},
                    new int[] {0,0,1,2,2,2,2,2,2,2,2,2,1,0,0},
                    new int[] {0,0,0,1,2,2,2,2,2,2,2,1,0,0,0},
                    new int[] {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
            }
    };

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        if(meta >= 12) return new TileEntityFusionTorus();
        if(meta >= 6) return new TileEntityProxyCombo().inventory().power().fluid();

        return null;
    }
    @Override
    public boolean onBlockActivated(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        return super.standardOpenBehavior(world, pos, player, 0);
    }

    @Override
    public int[] getDimensions() {
        return new int[] { 4, 0, 7, 7, 7, 7 };
    }

    @Override
    public int getOffset() {
        return 7;
    }

    @Override
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
        if (!super.checkRequirement(world, x, y, z, dir, o)) return false;

        x += dir.offsetX * o;
        z += dir.offsetZ * o;

        for(int iy = 0; iy < 5; iy++) {

            int l = iy > 2 ? 4 - iy : iy;
            int[][] layer = layout[l];

            for(int ix = 0; ix < layer.length; ix++) {

                for(int iz = 0; iz < layer.length; iz++) {

                    int ex = ix - layer.length / 2;
                    int ez = iz - layer.length / 2;
                    BlockPos pos = new BlockPos(x + ex, y + iy, z + ez);
                    if(layout[l][ix][iz] > 0 && !world.getBlockState(pos).getBlock().canPlaceBlockAt(world, pos)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {

        x = x + dir.offsetX * o;
        z = z + dir.offsetZ * o;

        for(int iy = 0; iy < 5; iy++) {

            int l = iy > 2 ? 4 - iy : iy;
            int[][] layer = layout[l];

            for(int ix = 0; ix < layer.length; ix++) {

                for(int iz = 0; iz < layer[0].length; iz++) {

                    int ex = ix - layer.length / 2;
                    int ez = iz - layer.length / 2;

                    int meta;

                    if(iy > 0) {
                        meta = ForgeDirection.UP.ordinal();
                    } else if(ex < 0) {
                        meta = ForgeDirection.WEST.ordinal();
                    } else if(ex > 0) {
                        meta = ForgeDirection.EAST.ordinal();
                    } else if(ez < 0) {
                        meta = ForgeDirection.NORTH.ordinal();
                    } else if(ez > 0) {
                        meta = ForgeDirection.SOUTH.ordinal();
                    } else {
                        continue;
                    }

                    if(layout[l][ix][iz] > 0)
                        world.setBlockState(new BlockPos(x + ex, y + iy, z + ez), this.getDefaultState().withProperty(META, meta), 3);
                }
            }
        }

        // is that enough ports?
        this.makeExtra(world, x, y + 4, z);

        this.makeExtra(world, x + 6, y, z);
        this.makeExtra(world, x + 6, y + 4, z);
        this.makeExtra(world, x + 6, y, z + 2);
        this.makeExtra(world, x + 6, y + 4, z + 2);
        this.makeExtra(world, x + 6, y, z - 2);
        this.makeExtra(world, x + 6, y + 4, z - 2);

        this.makeExtra(world, x - 6, y, z);
        this.makeExtra(world, x - 6, y + 4, z);
        this.makeExtra(world, x - 6, y, z + 2);
        this.makeExtra(world, x - 6, y + 4, z + 2);
        this.makeExtra(world, x - 6, y, z - 2);
        this.makeExtra(world, x - 6, y + 4, z - 2);

        this.makeExtra(world, x, y, z + 6);
        this.makeExtra(world, x, y + 4, z + 6);
        this.makeExtra(world, x + 2, y, z + 6);
        this.makeExtra(world, x + 2, y + 4, z + 6);
        this.makeExtra(world, x - 2, y, z + 6);
        this.makeExtra(world, x - 2, y + 4, z + 6);

        this.makeExtra(world, x, y, z - 6);
        this.makeExtra(world, x, y + 4, z - 6);
        this.makeExtra(world, x + 2, y, z - 6);
        this.makeExtra(world, x + 2, y + 4, z - 6);
        this.makeExtra(world, x - 2, y, z - 6);
        this.makeExtra(world, x - 2, y + 4, z - 6);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World player, @NotNull List<String> tooltip, @NotNull ITooltipFlag advanced) {
        addStandardInfo(tooltip);
    }


}

