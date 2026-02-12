package com.hbm.blocks.machine;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockICFLaserComponent.EnumICFPart;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.machine.TileEntityICFController;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

public class MachineICFController extends BlockBase implements ITileEntityProvider, ILookOverlay {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    private static final HashMap<BlockPos, IBlockState> assembly = new HashMap<>();
    private static final HashSet<BlockPos> casings = new HashSet<>();
    private static final HashSet<BlockPos> ports = new HashSet<>();
    private static final HashSet<BlockPos> cells = new HashSet<>();
    private static final HashSet<BlockPos> emitters = new HashSet<>();
    private static final HashSet<BlockPos> capacitors = new HashSet<>();
    private static final HashSet<BlockPos> turbochargers = new HashSet<>();
    private static final int maxSize = 1024;
    private static boolean errored;
    public MachineICFController() {
        super(Material.IRON, "icf_controller");
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityICFController();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX
            , float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        } else if (!player.isSneaking()) {
            TileEntityICFController controller = (TileEntityICFController) world.getTileEntity(pos);
            if (controller != null && !controller.assembled) {
                assemble(world, pos, player);
            }
            return true;
        }
        return false;
    }

    public void assemble(World world, BlockPos pos, EntityPlayer player) {
        assembly.clear();
        casings.clear();
        ports.clear();
        cells.clear();
        emitters.clear();
        capacitors.clear();
        turbochargers.clear();
        assembly.put(pos, world.getBlockState(pos));

        IBlockState controllerState = world.getBlockState(pos);
        EnumFacing dir = controllerState.getValue(FACING);

        errored = false;
        floodFill(world, pos.offset(dir.getOpposite()), player);
        assembly.remove(pos);

        TileEntityICFController controller = (TileEntityICFController) world.getTileEntity(pos);

        if (!errored && controller != null) {
            for (Entry<BlockPos, IBlockState> entry : assembly.entrySet()) {
                BlockPos partPos = entry.getKey();
                IBlockState originalPartState = entry.getValue();
                boolean isPort = ports.contains(partPos);
                IBlockState placeholderState = ModBlocks.icf_block.getDefaultState().withProperty(BlockICF.IO_ENABLED, isPort);
                world.setBlockState(partPos, placeholderState, 3);
                BlockICF.TileEntityBlockICF icfTE = (BlockICF.TileEntityBlockICF) world.getTileEntity(partPos);
                if (icfTE != null) {
                    icfTE.originalBlockState = originalPartState;
                    icfTE.setCore(pos);
                    icfTE.markDirty();
                }
            }

            controller.setup(ports, cells, emitters, capacitors, turbochargers);
            controller.markDirty();
        }

        if (controller != null) {
            controller.assembled = !errored;
        }

        assembly.clear();
        casings.clear();
        ports.clear();
        cells.clear();
        emitters.clear();
        capacitors.clear();
        turbochargers.clear();
    }

    private void floodFill(World world, BlockPos pos, EntityPlayer player) {
        if (assembly.containsKey(pos)) return;
        if (assembly.size() >= maxSize) {
            errored = true;
            sendError(world, pos, "Max size exceeded", player);
            return;
        }

        IBlockState state = world.getBlockState(pos);
        boolean validCasing = false;
        boolean validCore = false;

        if (state.getBlock() == ModBlocks.icf_laser_component) {
            EnumICFPart part = (EnumICFPart) ((BlockICFLaserComponent) state.getBlock()).getEnumFromState(state);
            switch (part) {
                case CASING -> {
                    casings.add(pos);
                    validCasing = true;
                }
                case PORT -> {
                    ports.add(pos);
                    validCasing = true;
                }
                case CELL -> {
                    cells.add(pos);
                    validCore = true;
                }
                case EMITTER -> {
                    emitters.add(pos);
                    validCore = true;
                }
                case CAPACITOR -> {
                    capacitors.add(pos);
                    validCore = true;
                }
                case TURBO -> {
                    turbochargers.add(pos);
                    validCore = true;
                }
            }
        }

        if (validCasing) {
            assembly.put(pos, state);
            return;
        }

        if (validCore) {
            assembly.put(pos, state);
            for (EnumFacing facing : EnumFacing.VALUES) {
                floodFill(world, pos.offset(facing), player);
            }
            return;
        }

        sendError(world, pos, "Invalid block", player);
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
            PacketThreading.createSendToThreadedPacket(new AuxParticlePacketNT(data, pos.getX(), pos.getY(), pos.getZ()), (EntityPlayerMP) player);
        }
    }

    @Override
    public void printHook(Pre event, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityICFController icf)) return;
        List<String> text = new ArrayList<>();
        text.add(BobMathUtil.getShortNumber(icf.getPower()) + "/" + BobMathUtil.getShortNumber(icf.getMaxPower()) + " HE");
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
}
