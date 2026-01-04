package com.hbm.blocks.network;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.machine.BlockContainerBakeable;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FluidPump extends BlockContainerBakeable implements INBTBlockTransformable, ILookOverlay, IGUIProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public FluidPump(Material material, String registryName) {
        super(material, registryName, new BlockBakeFrame("block_steel"));
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityFluidPump();
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        int i = MathHelper.floor((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int meta = switch (i) {
            case 0 -> 2;
            case 1 -> 5;
            case 2 -> 3;
            default -> 4;
        };
        worldIn.setBlockState(pos, this.getStateFromMeta(meta), 2);
    }
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        int i = MathHelper.floor((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        return this.getStateFromMeta(i == 0 ? 2 : i == 1 ? 5 : i == 2 ? 3 : 4);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }

        ItemStack heldStack = player.getHeldItem(hand);
        if (!heldStack.isEmpty() && heldStack.getItem() instanceof IItemFluidIdentifier identifier) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityFluidPump pump && !world.isRemote) {
                FluidType type = identifier.getType(world, pos.getX(), pos.getY(), pos.getZ(), heldStack);
                pump.tank[0].setTankType(type);
                pump.markDirty();

                TextComponentString message = new TextComponentString("Changed type to ");
                message.getStyle().setColor(TextFormatting.YELLOW);
                message.appendSibling(new TextComponentTranslation(type.getConditionalName()));
                message.appendSibling(new TextComponentString("!"));
                player.sendMessage(message);
            }
            return true;
        }

        if(world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIPump((TileEntityFluidPump) world.getTileEntity(new BlockPos(x, y, z)));
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (!(tile instanceof TileEntityFluidPump pump)) {
            return;
        }

        List<String> text = new ArrayList<>();
        text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + pump.tank[0].getTankType().getLocalizedName() + " (" + pump.tank[0].getPressure() + " PU): " + BobMathUtil.format(pump.bufferSize) + "mB/t" + TextFormatting.RED + " ->");
        text.add("Priority: " + TextFormatting.YELLOW + pump.priority.name());
        if (pump.tank[0].getFill() > 0) {
            text.add(BobMathUtil.format(pump.tank[0].getFill()) + "mB buffered");
        }
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = switch (meta) {
            case 3 -> EnumFacing.SOUTH;
            case 4 -> EnumFacing.WEST;
            case 5 -> EnumFacing.EAST;
            default -> EnumFacing.NORTH;
        };
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
            default -> 2;
        };
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
    @AutoRegister
    public static class TileEntityFluidPump extends TileEntityLoadedBase implements ITickable, IFluidStandardTransceiverMK2, IControlReceiver, SimpleComponent, CompatHandler.OCComponent {

        public int bufferSize = 100;
        public FluidTankNTM[] tank;
        public IEnergyReceiverMK2.ConnectionPriority priority = IEnergyReceiverMK2.ConnectionPriority.NORMAL;
        public boolean redstone = false;

        public TileEntityFluidPump() {
            this.tank = new FluidTankNTM[1];
            this.tank[0] = new FluidTankNTM(Fluids.NONE, bufferSize);
        }

        @Override
        public void update() {
            if (this.world == null || this.world.isRemote) {
                return;
            }

            if (this.bufferSize != this.tank[0].getMaxFill()) {
                int nextBuffer = Math.max(this.tank[0].getFill(), this.bufferSize);
                this.tank[0].changeTankSize(nextBuffer);
            }

            this.redstone = this.world.isBlockPowered(this.pos);

            IBlockState state = this.world.getBlockState(this.pos);
            ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
            ForgeDirection in = dir.getRotation(ForgeDirection.UP);
            ForgeDirection out = in.getOpposite();

            this.trySubscribe(tank[0].getTankType(), world, pos.getX() + in.offsetX, pos.getY(), pos.getZ() + in.offsetZ, in);
            if(!redstone) this.tryProvide(tank[0], world, pos.getX() + out.offsetX, pos.getY(), pos.getZ() + out.offsetZ, out);

            this.networkPackNT(15);
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            super.writeToNBT(compound);
            this.tank[0].writeToNBT(compound, "t");
            compound.setByte("p", (byte) this.priority.ordinal());
            compound.setInteger("buffer", this.bufferSize);
            return compound;
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            super.readFromNBT(compound);
            this.tank[0].readFromNBT(compound, "t");
            this.priority = EnumUtil.grabEnumSafely(IEnergyReceiverMK2.ConnectionPriority.class, compound.getByte("p"));
            this.bufferSize = compound.getInteger("buffer");
        }

        @Override
        public void serialize(ByteBuf buf) {
            super.serialize(buf);
            this.tank[0].serialize(buf);
            buf.writeByte((byte) this.priority.ordinal());
            buf.writeInt(this.bufferSize);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            super.deserialize(buf);
            this.tank[0].deserialize(buf);
            this.priority = EnumUtil.grabEnumSafely(IEnergyReceiverMK2.ConnectionPriority.class, buf.readByte());
            this.bufferSize = buf.readInt();
        }

        @Override
        public IEnergyReceiverMK2.ConnectionPriority getFluidPriority() {
            return this.priority;
        }

        @Override
        public FluidTankNTM[] getSendingTanks() {
            return this.redstone ? new FluidTankNTM[0] : this.tank;
        }

        @Override
        public FluidTankNTM[] getReceivingTanks() {
            return this.bufferSize < this.tank[0].getFill() ? new FluidTankNTM[0] : this.tank;
        }

        @Override
        public FluidTankNTM[] getAllTanks() {
            return this.tank;
        }

        @Override
        public boolean hasPermission(EntityPlayer player) {
            return player.getDistanceSq(this.pos) <= 128.0D;
        }

        @Override
        public void receiveControl(NBTTagCompound data) {
            if (data.hasKey("capacity")) {
                this.bufferSize = MathHelper.clamp(data.getInteger("capacity"), 0, 10_000);
            }
            if (data.hasKey("pressure")) {
                this.tank[0].withPressure(MathHelper.clamp(data.getByte("pressure"), 0, 5));
            }
            if (data.hasKey("priority")) {
                this.priority = EnumUtil.grabEnumSafely(IEnergyReceiverMK2.ConnectionPriority.class, data.getByte("priority"));
            }

            this.markDirty();
        }

        @Override
        @Optional.Method(modid = "opencomputers")
        public String getComponentName() {
            return "ntm_fluid_pump";
        }

        @Callback(direct = true, limit = 4)
        @Optional.Method(modid = "opencomputers")
        public Object[] getFluid(Context context, Arguments args) {
            return new Object[]{this.tank[0].getTankType().getTranslationKey()};
        }

        @Callback(direct = true, limit = 4)
        @Optional.Method(modid = "opencomputers")
        public Object[] getPressure(Context context, Arguments args) {
            return new Object[]{this.tank[0].getPressure()};
        }

        @Callback(direct = true, limit = 4)
        @Optional.Method(modid = "opencomputers")
        public Object[] getFlow(Context context, Arguments args) {
            return new Object[]{this.bufferSize};
        }

        @Callback(direct = true, limit = 4)
        @Optional.Method(modid = "opencomputers")
        public Object[] getPriority(Context context, Arguments args) {
            return new Object[]{this.getFluidPriority()};
        }

        @Callback(direct = true, limit = 4)
        @Optional.Method(modid = "opencomputers")
        public Object[] getInfo(Context context, Arguments args) {
            return new Object[]{
                    this.tank[0].getTankType().getTranslationKey(),
                    this.tank[0].getPressure(),
                    this.bufferSize,
                    this.getFluidPriority()
            };
        }

        @Callback(direct = true, limit = 4)
        @Optional.Method(modid = "opencomputers")
        public Object[] setPriority(Context context, Arguments args) {
            int num = args.checkInteger(0);
            switch (num) {
                case 0 -> this.priority = IEnergyReceiverMK2.ConnectionPriority.LOWEST;
                case 1 -> this.priority = IEnergyReceiverMK2.ConnectionPriority.LOW;
                case 2 -> this.priority = IEnergyReceiverMK2.ConnectionPriority.NORMAL;
                case 3 -> this.priority = IEnergyReceiverMK2.ConnectionPriority.HIGH;
                case 4 -> this.priority = IEnergyReceiverMK2.ConnectionPriority.HIGHEST;
                default -> {
                    return new Object[]{null, "Not a valid Priority."};
                }
            }
            return new Object[]{true};
        }

        @Callback(direct = true, limit = 4)
        @Optional.Method(modid = "opencomputers")
        public Object[] setFlow(Context context, Arguments args) {
            int input = args.checkInteger(0);
            if (input > 10_000 || input < 0) {
                return new Object[]{null, "Number outside of bounds."};
            }
            this.bufferSize = input;
            return new Object[]{true};
        }

        @Override
        @Optional.Method(modid = "opencomputers")
        public String[] methods() {
            return new String[]{
                    "getPriority",
                    "getPressure",
                    "getFluid",
                    "getFlow",
                    "getInfo",
                    "setPriority",
                    "setFlow"
            };
        }

        @Override
        @Optional.Method(modid = "opencomputers")
        public Object[] invoke(String method, Context context, Arguments args) throws Exception {
            return switch (method) {
                case "getPriority" -> this.getPriority(context, args);
                case "getPressure" -> this.getPressure(context, args);
                case "getFluid" -> this.getFluid(context, args);
                case "getFlow" -> this.getFlow(context, args);
                case "getInfo" -> this.getInfo(context, args);
                case "setPriority" -> this.setPriority(context, args);
                case "setFlow" -> this.setFlow(context, args);
                default -> throw new NoSuchMethodException();
            };
        }
    }

    @SideOnly(Side.CLIENT)
    public static class GUIPump extends GuiScreen {

        protected final TileEntityFluidPump pump;

        private GuiTextField textPlacementPriority;
        private GuiButton buttonPressure;
        private GuiButton buttonPriority;

        private int pressure;
        private int priority;

        public GUIPump(TileEntityFluidPump pump) {
            this.pump = pump;
            this.pressure = pump.tank[0].getPressure();
            this.priority = pump.priority.ordinal();
        }

        @Override
        public void initGui() {
            Keyboard.enableRepeatEvents(true);
            this.buttonList.clear();
            this.textPlacementPriority = new GuiTextField(2, this.fontRenderer, this.width / 2 - 150, 100, 90, 20);
            this.textPlacementPriority.setText(String.valueOf(this.pump.bufferSize));
            this.textPlacementPriority.setMaxStringLength(5);

            this.buttonPressure = new GuiButton(0, this.width / 2 - 50, 100, 90, 20, this.pressure + " PU");
            this.buttonPriority = new GuiButton(1, this.width / 2 + 50, 100, 90, 20, this.pump.priority.name());

            this.buttonList.add(this.buttonPressure);
            this.buttonList.add(this.buttonPriority);
        }

        @Override
        public void onGuiClosed() {
            Keyboard.enableRepeatEvents(false);

            NBTTagCompound data = new NBTTagCompound();
            data.setByte("pressure", (byte) this.pressure);
            data.setByte("priority", (byte) this.priority);
            try {
                data.setInteger("capacity", Integer.parseInt(this.textPlacementPriority.getText()));
            } catch (Exception ignored) {
            }

            PacketThreading.createSendToServerThreadedPacket(
                    new NBTControlPacket(data, this.pump.getPos().getX(), this.pump.getPos().getY(), this.pump.getPos().getZ()));
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            this.textPlacementPriority.updateCursorCounter();
        }

        @Override
        protected void actionPerformed(GuiButton button) {
            if (button.id == 0) {
                this.pressure = (this.pressure + 1) % 6;
                button.displayString = this.pressure + " PU";
                return;
            }
            if (button.id == 1) {
                this.priority++;
                if (this.priority >= IEnergyReceiverMK2.ConnectionPriority.VALUES.length) {
                    this.priority = 0;
                }
                button.displayString = EnumUtil.grabEnumSafely(IEnergyReceiverMK2.ConnectionPriority.class, this.priority).name();
            }
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            if (this.textPlacementPriority.textboxKeyTyped(typedChar, keyCode)) {
                return;
            }

            if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
                this.mc.player.closeScreen();
                return;
            }

            super.keyTyped(typedChar, keyCode);
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            this.textPlacementPriority.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();

            this.fontRenderer.drawString("Throughput:", this.width / 2 - 150, 80, 0xA0A0A0);
            this.fontRenderer.drawString("(max. 10,000mB)", this.width / 2 - 150, 90, 0xA0A0A0);
            this.textPlacementPriority.drawTextBox();

            this.fontRenderer.drawString("Pressure:", this.width / 2 - 50, 80, 0xA0A0A0);
            this.fontRenderer.drawString("Priority:", this.width / 2 + 50, 80, 0xA0A0A0);

            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean doesGuiPauseGame() {
            return false;
        }
    }
}
