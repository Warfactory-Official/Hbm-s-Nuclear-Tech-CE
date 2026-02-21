package com.hbm.blocks.machine;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockPWR.TileEntityBlockPWR;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.render.block.RotatableStateMapper;
import com.hbm.tileentity.machine.TileEntityPWRController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MachinePWRController extends BlockContainerBakeable implements ITooltipProvider {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    private final HashMap<BlockPos, IBlockState> assembly = new HashMap<>();
    private final HashMap<BlockPos, IBlockState> fuelRods = new HashMap<>();
    private final HashMap<BlockPos, IBlockState> sources = new HashMap<>();
    private boolean errored;
    private static final int MAX_SIZE = 4096;

    public MachinePWRController(String name) {
        super(Material.IRON, name, BlockBakeFrame.simpleSouthRotatable("pwr_casing_blank", "pwr_controller"));
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public @NotNull IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withProperty(FACING, mirrorIn.mirror(state.getValue(FACING)));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, @NotNull BlockPos pos, IBlockState state, EntityLivingBase placer, @NotNull ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        return new TileEntityPWRController();
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        if (!player.isSneaking()) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityPWRController controller) {
                if (!controller.assembled) {
                    assemble(world, pos, state, player);
                } else {
                    FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
                }
            }
            return true;
        }
        return false;
    }

    public void assemble(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        assembly.clear();
        fuelRods.clear();
        sources.clear();
        errored = false;

        assembly.put(pos, state);
        EnumFacing dir = state.getValue(FACING).getOpposite();
        floodFill(world, pos.offset(dir), player);

        if (fuelRods.isEmpty()) {
            sendError(world, pos, "Fuel rods required", player);
            errored = true;
        }
        if (sources.isEmpty()) {
            sendError(world, pos, "Neutron sources required", player);
            errored = true;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityPWRController controller) {
            if (!errored) {
                for (Map.Entry<BlockPos, IBlockState> entry : assembly.entrySet()) {
                    BlockPos partPos = entry.getKey();
                    IBlockState originalState = entry.getValue();
                    Block block = originalState.getBlock();

                    if (block != this) {
                        IBlockState replacementState;
                        if (block == ModBlocks.pwr_port) {
                            replacementState =  ModBlocks.pwr_block.getDefaultState().withProperty(BlockPWR.IO_ENABLED, true);
                        } else {
                            replacementState = ModBlocks.pwr_block.getDefaultState().withProperty(BlockPWR.IO_ENABLED, false);
                        }
                        world.setBlockState(partPos, replacementState, 3);

                        TileEntity partTile = world.getTileEntity(partPos);
                        if (partTile instanceof TileEntityBlockPWR pwr) {
                            pwr.originalBlockState = originalState;
                            pwr.corePos = pos;
                            pwr.markDirty();
                        }
                    }
                }
                controller.setup(assembly, fuelRods);
            }
            controller.assembled = !errored;
            controller.markDirty();
        }
    }

    private void floodFill(World world, BlockPos pos, EntityPlayer player) {
        if (assembly.containsKey(pos) || errored) return;
        if (assembly.size() >= MAX_SIZE) {
            sendError(world, pos, "Max size exceeded (" + MAX_SIZE + ")", player);
            errored = true;
            return;
        }

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (isValidCasing(block)) {
            assembly.put(pos, state);
            return;
        }

        if (isValidCore(block)) {
            assembly.put(pos, state);
            if (block == ModBlocks.pwr_fuelrod) fuelRods.put(pos, state);
            if (block == ModBlocks.pwr_neutron_source) sources.put(pos, state);

            for (EnumFacing facing : EnumFacing.VALUES) {
                floodFill(world, pos.offset(facing), player);
            }
            return;
        }
        sendError(world, pos, "Invalid block in structure: " + block.getLocalizedName(), player);
        errored = true;
    }

    private void sendError(World world, BlockPos pos, String message, EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "marker");
            data.setInteger("color", 0xff0000);
            data.setInteger("expires", 5_000);
            data.setDouble("dist", 128D);
            if (message != null) data.setString("label", message);
            PacketThreading.createSendToThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), (EntityPlayerMP) player);
        }
    }

    private boolean isValidCore(Block block) {
        return block == ModBlocks.pwr_fuelrod || block == ModBlocks.pwr_control || block == ModBlocks.pwr_channel ||
                block == ModBlocks.pwr_heatex || block == ModBlocks.pwr_heatsink || block == ModBlocks.pwr_neutron_source;
    }

    private boolean isValidCasing(Block block) {
        return block == ModBlocks.pwr_casing || block == ModBlocks.pwr_reflector || block == ModBlocks.pwr_port;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, @NotNull ITooltipFlag flag) {
        this.addStandardInfo(tooltip);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel() {
        for(var facing :EnumFacing.HORIZONTALS)
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), facing.getHorizontalIndex(), new ModelResourceLocation(Objects.requireNonNull(this.getRegistryName()), "facing=" +facing.getName()));

        super.registerModel();
    }


    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new RotatableStateMapper(loc);
    }
}