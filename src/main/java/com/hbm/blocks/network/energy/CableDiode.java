package com.hbm.blocks.network.energy;

import com.hbm.Tags;
import com.hbm.api.energymk2.IEnergyConnectorBlock;
import com.hbm.api.energymk2.IEnergyConnectorMK2;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.energymk2.Nodespace;
import com.hbm.api.energymk2.Nodespace.PowerNode;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.gui.GUIDiode;
import com.hbm.items.IDynamicModels;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.model.CableDiodeBakedModel;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CableDiode extends BlockContainer implements IEnergyConnectorBlock, ILookOverlay, ITooltipProvider, IDynamicModels {
    public static final PropertyDirection FACING = BlockDirectional.FACING;
    public static final IUnlistedProperty<Integer> CONNECTION_MASK = new UnlistedPropertyInteger("connection_mask");

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite sprite;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite cableSprite;

    public CableDiode(Material materialIn, String s) {
        super(materialIn);

        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{FACING}, new IUnlistedProperty[]{CONNECTION_MASK});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public @NotNull IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @NotNull
    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public @NotNull IBlockState getExtendedState(@NotNull IBlockState state, @NotNull IBlockAccess world, BlockPos pos) {
        int mask = 0;
        if (Library.canConnect(world, pos.offset(EnumFacing.EAST), ForgeDirection.getOrientation(EnumFacing.EAST))) mask |= 1;
        if (Library.canConnect(world, pos.offset(EnumFacing.WEST), ForgeDirection.getOrientation(EnumFacing.WEST))) mask |= 1 << 1;
        if (Library.canConnect(world, pos.offset(EnumFacing.UP), ForgeDirection.getOrientation(EnumFacing.UP))) mask |= 1 << 2;
        if (Library.canConnect(world, pos.offset(EnumFacing.DOWN), ForgeDirection.getOrientation(EnumFacing.DOWN))) mask |= 1 << 3;
        if (Library.canConnect(world, pos.offset(EnumFacing.SOUTH), ForgeDirection.getOrientation(EnumFacing.SOUTH))) mask |= 1 << 4;
        if (Library.canConnect(world, pos.offset(EnumFacing.NORTH), ForgeDirection.getOrientation(EnumFacing.NORTH))) mask |= 1 << 5;
        return ((IExtendedBlockState) state).withProperty(CONNECTION_MASK, mask);
    }

    @Override
    public @NotNull IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
    }

    @Override
    public boolean canConnect(IBlockAccess world, BlockPos pos, ForgeDirection dir) {
        return true;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, List<String> list, @NotNull ITooltipFlag flagIn) {
        list.add(TextFormatting.GOLD + "Limits throughput and restricts flow direction");
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public void printHook(Pre event, World world, BlockPos pos) {

        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof TileEntityDiode diode)) return;

        List<String> text = new ArrayList<>();
        text.add("Max.: " + BobMathUtil.getShortNumber(diode.getMaxPower()) + "HE/t");
        text.add("Priority: " + diode.priority.name());

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntityDiode();
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos,
                                        @NotNull EnumFacing side) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        } else if (!player.isSneaking()) {
            TileEntityDiode entity = (TileEntityDiode) world.getTileEntity(pos);
            if (entity != null) {
                player.openGui(MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        // Item model: point to "inventory" variant
        Item item = Item.getItemFromBlock(this);
        ModelResourceLocation inv = new ModelResourceLocation(getRegistryName(), "inventory");
        ModelLoader.setCustomModelResourceLocation(item, 0, inv);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
                return new ModelResourceLocation(loc, "normal");
            }
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        this.sprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/" + getRegistryName().getPath()));
        this.cableSprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/cable_neo"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        ModelResourceLocation worldLoc = new ModelResourceLocation(getRegistryName(), "normal");
        ModelResourceLocation invLoc = new ModelResourceLocation(getRegistryName(), "inventory");

        IBakedModel worldModel = new CableDiodeBakedModel(sprite, cableSprite, false);
        IBakedModel itemModel = new CableDiodeBakedModel(sprite, cableSprite, true);

        event.getModelRegistry().putObject(worldLoc, worldModel);
        event.getModelRegistry().putObject(invLoc, itemModel);
    }

    @AutoRegister
    public static class TileEntityDiode extends TileEntityLoadedBase implements IEnergyReceiverMK2, IControlReceiver, IGUIProvider, ITickable {

        /**
         * Used as an intra-tick tracker for how much energy has been transmitted, resets to 0 each tick
         * and maxes out based on transfer
         */
        private long power;
        private boolean recursionBrake = false;
        private int pulses = 0;
        public ConnectionPriority priority = ConnectionPriority.NORMAL;
        public long limit = 1_000;

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            if(nbt.hasKey("level")) {
                this.limit = (long) Math.pow(10, nbt.getInteger("level"));
            } else {
                this.limit = nbt.getLong("limit");
            }
            priority = ConnectionPriority.values()[nbt.getByte("p")];
        }

        @Override
        public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            nbt.setLong("limit", limit);
            nbt.setByte("p", (byte) this.priority.ordinal());
            return super.writeToNBT(nbt);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            super.deserialize(buf);
            limit = buf.readLong();
            priority = ConnectionPriority.values()[buf.readByte()];
        }

        @Override
        public void serialize(ByteBuf buf) {
            super.serialize(buf);
            buf.writeLong(limit);
            buf.writeByte((byte)this.priority.ordinal());
        }

        private ForgeDirection getDir() {
            return ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
        }

        @Override
        public void update() {

            if (!world.isRemote) {
                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    if (dir == getDir()) continue;
                    this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
                }

                pulses = 0;
                this.setPower(0); // tick is over, reset our allowed transfer
                this.networkPackNT(15);
            }
        }

        @Override
        public boolean canConnect(ForgeDirection dir) {
            return dir != getDir();
        }

        @Override
        public long transferPower(long power, boolean simulate) {

            if (recursionBrake) return power;

            int effectivePulses = pulses + 1;
            if (this.getPower() >= this.getMaxPower() || effectivePulses > 10) return power; // if we have already maxed out transfer or max pulses, abort
            if (!simulate) pulses = effectivePulses;

            recursionBrake = true;

            ForgeDirection dir = getDir();
            PowerNode node = UniNodespace.getNode(world, new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ), Nodespace.THE_POWER_PROVIDER);
            TileEntity te = Compat.getTileStandard(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ);

            if (node != null && !node.expired && node.hasValidNet() && te instanceof IEnergyConnectorMK2 && ((IEnergyConnectorMK2) te).canConnect(
                    dir.getOpposite())) {
                long toTransfer = Math.min(power, this.getReceiverSpeed());
                long remainder = node.net.sendPowerDiode(toTransfer, simulate);
                long transferred = (toTransfer - remainder);
                if (!simulate) this.power += transferred;
                power -= transferred;

            } else if (te instanceof IEnergyReceiverMK2 rec && te != this) {
                if (rec.canConnect(dir.getOpposite())) {
                    long toTransfer = Math.min(power, rec.getReceiverSpeed());
                    long remainder = rec.transferPower(toTransfer, simulate);
                    long transferred = (toTransfer - remainder);
                    if (!simulate) this.power += transferred;
                    power -= transferred;
                    recursionBrake = false;
                    return power;
                }
            }

            recursionBrake = false;
            return power;
        }

        @Override
        public long getReceiverSpeed() {
            return this.getMaxPower() - this.getPower();
        }

        @Override public long getMaxPower() { return this.limit; }

        @Override
        public long getPower() {
            return Math.min(power, this.getMaxPower());
        }

        @Override
        public void setPower(long power) {
            this.power = power;
        }

        @Override
        public ConnectionPriority getPriority() {
            return this.priority;
        }

        @Override
        public boolean hasPermission(EntityPlayer player) {
            return player.getDistanceSq(this.pos) <= 128.0D;
        }

        @Override
        public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {
            if(data.hasKey("limit")) this.limit = data.getLong("limit");
            if(data.hasKey("priority")) this.priority = EnumUtil.grabEnumSafely(ConnectionPriority.class, data.getByte("priority"));
            if(limit < 0) limit = 0;
            if(limit > 10_000_000_000L) limit = 10_000_000_000L;
            this.markDirty();
        }

        @Override @SideOnly(Side.CLIENT) public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIDiode(this); }

        // Th3_Sl1ze: idk why it even requires a container on this exact TE
        @Override
        public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
            return new Container() {
                @Override public boolean canInteractWith(@NotNull EntityPlayer p) { return hasPermission(p); }
            };
        }
    }
}
