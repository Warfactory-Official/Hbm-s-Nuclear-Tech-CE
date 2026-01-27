package com.hbm.blocks.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MachineCapacitor extends BlockContainer implements ILookOverlay, IPersistentInfoProvider, ITooltipProvider {
    public static final PropertyDirection FACING = BlockDirectional.FACING;

    protected long power;
    String name;

    public MachineCapacitor(long power, String name, String s) {
        super(Material.IRON);

        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setSoundType(SoundType.METAL);
        this.power = power;
        this.name = name;
        ModBlocks.ALL_BLOCKS.add(this);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return ((EnumFacing)state.getValue(FACING)).getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
    }


    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCapacitor(this.power);
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);

        if(!(te instanceof TileEntityCapacitor))
            return;

        TileEntityCapacitor battery = (TileEntityCapacitor) te;
        List<String> text = new ArrayList<>();
        text.add(BobMathUtil.getShortNumber(battery.getPower()) + " / " + BobMathUtil.getShortNumber(battery.getMaxPower()) + "HE");

        double percent = (double) battery.getPower() / (double) battery.getMaxPower();
        int charge = (int) Math.floor(percent * 10_000D);
        int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int)(0xFF * percent) << 8);
        text.add("&[" + color + "&]" + (charge / 100D) + "%");
        text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + "+" + BobMathUtil.getShortNumber(battery.powerReceived) + "HE/t");
        text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + "-" + BobMathUtil.getShortNumber(battery.powerSent) + "HE/t");

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List list, boolean ext) {
        list.add(ChatFormatting.GOLD + "Stores up to "+ BobMathUtil.getShortNumber(this.power) + "HE");
        list.add(ChatFormatting.GOLD + "Charge speed: "+ BobMathUtil.getShortNumber(this.power / 200) + "HE");
        list.add(ChatFormatting.GOLD + "Discharge speed: "+ BobMathUtil.getShortNumber(this.power / 600) + "HE");
        list.add(ChatFormatting.YELLOW + "" + BobMathUtil.getShortNumber(persistentTag.getLong("power")) + "/" + BobMathUtil.getShortNumber(persistentTag.getLong("maxPower")) + "HE");
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {

        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            for(String s : I18nUtil.resolveKeyArray("tile.capacitor.desc")) tooltip.add(ChatFormatting.YELLOW + s);
        } else {
            tooltip.add(ChatFormatting.DARK_GRAY + "" + ChatFormatting.ITALIC +"Hold <" +
                    ChatFormatting.YELLOW + "" + ChatFormatting.ITALIC + "LSHIFT" +
                    ChatFormatting.DARK_GRAY + "" + ChatFormatting.ITALIC + "> to display more info");
        }
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        IPersistentNBT.onBlockPlacedBy(world, pos, stack);
    }
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
//        if(!player.capabilities.isCreativeMode) {
//            harvesters.set(player);
//            this.dropBlockAsItem(worldIn, pos, state, 0);
//            harvesters.set(null);
//        }
        // mlbv: wtf? why did you mutate harvesters?
        IPersistentNBT.onBlockHarvested(worldIn, pos, player);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        IPersistentNBT.breakBlock(worldIn, pos, state);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        player.addStat(Objects.requireNonNull(StatList.getBlockStats(this)), 1);
        player.addExhaustion(0.025F);
    }

    @AutoRegister
    public static class TileEntityCapacitor extends TileEntityLoadedBase implements IEnergyProviderMK2, IEnergyReceiverMK2, IPersistentNBT, ITickable, CompatHandler.OCComponent {

        public long power;
        protected long maxPower;
        public long powerReceived;
        public long powerSent;
        public long lastPowerReceived;
        public long lastPowerSent;
        private boolean destroyedByCreativePlayer = false;

        public TileEntityCapacitor() { }

        public TileEntityCapacitor(long maxPower) {
            this.maxPower = maxPower;
        }

        @Override
        public void update() {
            if(!world.isRemote) {

                ForgeDirection opp = ForgeDirection.getOrientation(this.getBlockMetadata());
                ForgeDirection dir = opp.getOpposite();

                BlockPos pos = new BlockPos(this.pos.add(dir.offsetX, dir.offsetY, dir.offsetZ));

                boolean didStep = false;
                ForgeDirection last = null;

                while(world.getBlockState(pos).getBlock() == ModBlocks.capacitor_bus) {
                    ForgeDirection current = ForgeDirection.getOrientation(world.getBlockState(pos).getValue(FACING).getIndex());
                    if(!didStep) last = current;
                    didStep = true;

                    if(last != current) {
                        pos = null;
                        break;
                    }

                    pos = pos.offset(current.toEnumFacing());
                }

                if(pos != null && last != null) {
                    this.tryUnsubscribe(world, pos.getX(), pos.getY(), pos.getZ());
                    this.tryProvide(world, pos.getX(), pos.getY(), pos.getZ(), last);
                }

                this.trySubscribe(world, this.pos.getX() + opp.offsetX, this.pos.getY() + opp.offsetY, this.pos.getZ() + opp.offsetZ, opp);

                networkPackNT(15);

                this.lastPowerSent = powerSent;
                this.lastPowerReceived = powerReceived;
                this.powerSent = 0;
                this.powerReceived = 0;
            }
        }

        @Override
        public void serialize(ByteBuf buf) {
            buf.writeLong(power);
            buf.writeLong(maxPower);
            buf.writeLong(powerReceived);
            buf.writeLong(powerSent);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            power = buf.readLong();
            maxPower = buf.readLong();
            powerReceived = buf.readLong();
            powerSent = buf.readLong();
        }

        @Override
        public long transferPower(long power, boolean simulate) {
            if(power + this.getPower() <= this.getMaxPower()) {
                this.setPower(power + this.getPower());
                this.powerReceived += power;
                return 0;
            }
            long capacity = this.getMaxPower() - this.getPower();
            long overshoot = power - capacity;
            this.powerReceived += (this.getMaxPower() - this.getPower());
            this.setPower(this.getMaxPower());
            return overshoot;
        }

        @Override
        public void usePower(long power) {
            this.powerSent += Math.min(this.getPower(), power);
            this.setPower(this.getPower() - power);
        }

        @Override
        public long getPower() {
            return power;
        }

        @Override
        public long getMaxPower() {
            return maxPower;
        }

        @Override public long getProviderSpeed() {
            return this.getMaxPower() / 300;
        }

        @Override public long getReceiverSpeed() {
            return this.getMaxPower() / 100;
        }

        @Override
        public ConnectionPriority getPriority() {
            return ConnectionPriority.LOW;
        }

        @Override
        public void setPower(long power) {
            this.power = power;
        }

        @Override
        public boolean canConnect(ForgeDirection dir) {
            return dir == ForgeDirection.getOrientation(world.getBlockState(pos).getValue(FACING).getIndex());
        }

        @Override
        public void setDestroyedByCreativePlayer() {
            destroyedByCreativePlayer = true;
        }

        @Override
        public boolean isDestroyedByCreativePlayer() {
            return destroyedByCreativePlayer;
        }

        @Override
        public void writeNBT(NBTTagCompound nbt) {
            NBTTagCompound data = new NBTTagCompound();
            data.setLong("power", power);
            data.setLong("maxPower", maxPower);
            nbt.setTag(NBT_PERSISTENT_KEY, data);
        }

        @Override
        public void readNBT(NBTTagCompound nbt) {
            NBTTagCompound data = nbt.getCompoundTag(NBT_PERSISTENT_KEY);
            this.power = data.getLong("power");
            this.maxPower = data.getLong("maxPower");
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            this.power = nbt.getLong("power");
            this.maxPower = nbt.getLong("maxPower");
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            nbt.setLong("power", power);
            nbt.setLong("maxPower", maxPower);
            return super.writeToNBT(nbt);
        }

        // opencomputer
        @Override
        @Optional.Method(modid = "opencomputers")
        public String getComponentName() {
            return "capacitor";
        }

        @Callback(direct = true)
        @Optional.Method(modid = "opencomputers")
        public Object[] getEnergy(Context context, Arguments args) {
            return new Object[] {power};
        }

        @Callback(direct = true)
        @Optional.Method(modid = "opencomputers")
        public Object[] getMaxEnergy(Context context, Arguments args) {
            return new Object[] {maxPower};
        }

        @Callback(direct = true)
        @Optional.Method(modid = "opencomputers")
        public Object[] getEnergySent(Context context, Arguments args) {
            return new Object[] {lastPowerReceived};
        }

        @Callback(direct = true)
        @Optional.Method(modid = "opencomputers")
        public Object[] getEnergyReceived(Context context, Arguments args) { return new Object[] {lastPowerSent}; }

        @Callback(direct = true)
        @Optional.Method(modid = "opencomputers")
        public Object[] getInfo(Context context, Arguments args) {
            return new Object[] {power, maxPower, lastPowerReceived, lastPowerSent};
        }

        @Override
        @Optional.Method(modid = "opencomputers")
        public String[] methods() {
            return new String[] {
                    "getEnergy",
                    "getMaxEnergy",
                    "getEnergySent",
                    "getEnergyReceived",
                    "getInfo"
            };
        }
        @Override
        @Optional.Method(modid = "opencomputers")
        public Object[] invoke(String method, Context context, Arguments args) throws Exception {
            switch(method) {
                case ("getEnergy"):
                    return getEnergy(context, args);
                case ("getMaxEnergy"):
                    return getMaxEnergy(context, args);
                case ("getEnergySent"):
                    return getEnergySent(context, args);
                case ("getEnergyReceived"):
                    return getEnergyReceived(context, args);
                case ("getInfo"):
                    return getEnergyReceived(context, args);
            }
            throw new NoSuchMethodException();
        }
    }


}
