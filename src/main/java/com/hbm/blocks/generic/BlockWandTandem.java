package com.hbm.blocks.generic;

import com.hbm.api.block.IBlockSideRotation;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockContainerBakeable;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.I18nUtil;
import com.hbm.world.gen.nbt.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * You're familiar with Billy Mitchell, World Video Game Champion? He could probably do it.
 * So I gotta find a way to harness his power. And I think I've found a way.
 *
 * THAT'S RIGHT, WE'RE GONNA CHEAT.
 *
 * NBTStructures have the inherent flaws of the vanilla structure system: Structures are composed
 * before terrain gen even kicks in, placement order of components are arbitrary and certain
 * connected parts will fall apart due to unexpected variance in the terrain. Not good.
 * The solution: Simply delay generation of parts using a tile entity that checks if the chunks
 * in front of it are loaded, and then places a random part from the chosen pool. When this happens,
 * the player is usually still far far away so they'll be none the wiser. Chunk load checks help
 * prevent forced chunk loading and all the lag that comes with that.
 *
 * The system is named after tandem shaped charges: Make a hole with the first charge, then deliver
 * the actual payload.
 *
 * @author hbm, Mellow
 */
public class BlockWandTandem extends BlockContainerBakeable implements IBlockSideRotation, INBTBlockTransformable, IGUIProvider, ILookOverlay {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockWandTandem(String regName) {
        super(Material.IRON, regName, new BlockBakeFrame(BlockBakeFrame.BlockForm.FULL_CUSTOM,
                "wand_tandem_top",
                "wand_tandem_top",
                "wand_tandem_back",
                "wand_tandem",
                "wand_tandem_side",
                "wand_tandem_side"
        ));
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityWandTandem();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        EnumFacing facing = player.getHorizontalFacing().getOpposite();
        world.setBlockState(pos, state.withProperty(FACING, facing), 2);
    }

    @Override
    public int getRotationFromSide(IBlockAccess world, BlockPos pos, EnumFacing sideEF) {
        int side = sideEF.getIndex();
        if (side == 0) return IBlockSideRotation.topToBottom(getRotationFromSide(world, pos, sideEF));

        IBlockState state = world.getBlockState(pos);
        int meta = state.getValue(FACING).getIndex();
        if (side == meta || IBlockSideRotation.isOpposite(side, meta)) return 0;

        // top (and bottom) is rotated fairly normally
        if (side == 1) {
            switch (meta) {
                case 2 -> {
                    return 3;
                }
                case 3 -> {
                    return 0;
                }
                case 4 -> {
                    return 1;
                }
                case 5 -> {
                    return 2;
                }
            }
        }

        // you know what I aint explaining further, it's a fucking mess here
        if (meta == 2) return side == 4 ? 2 : 1;
        if (meta == 3) return side == 4 ? 1 : 2;
        if (meta == 4) return side == 2 ? 1 : 2;
        if (meta == 5) return side == 2 ? 2 : 1;

        return 0;
    }

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityWandTandem jigsaw)) return false;

        ItemStack held = player.getHeldItem(hand);

        if (!held.isEmpty() && held.getItem() == Items.PAPER) {
            TileEntityWandTandem.copyMode = true;
            if (!held.hasTagCompound()) {
                NBTTagCompound tag = new NBTTagCompound();
                jigsaw.writeToNBT(tag);
                held.setTagCompound(tag);
            } else {
                jigsaw.readFromNBT(held.getTagCompound());
                jigsaw.markDirty();
            }
            TileEntityWandTandem.copyMode = false;
            return true;
        }

        if (!player.isSneaking()) {
            Block block = getBlock(held);
            if (block == ModBlocks.wand_air) block = Blocks.AIR;

            if (block != null && block != ModBlocks.wand_jigsaw && block != ModBlocks.wand_loot) {
                jigsaw.replaceBlock = block;
                jigsaw.replaceMeta = held.getItemDamage();
                jigsaw.markDirty();
                return true;
            }

            if (!held.isEmpty() && held.getItem() == ModItems.wand_s) return false;

            if (world.isRemote) {
                player.openGui(MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }

        return false;
    }

    private Block getBlock(ItemStack stack) {
        if (stack == null) return null;
        if (!(stack.getItem() instanceof ItemBlock)) return null;
        return ((ItemBlock) stack.getItem()).getBlock();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GuiWandTandem((TileEntityWandTandem) world.getTileEntity(new BlockPos(x, y, z)));
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityWandTandem jigsaw)) return;

        List<String> text = new ArrayList<>();

        text.add(TextFormatting.GRAY + "Target pool: " + TextFormatting.RESET + jigsaw.pool);
        text.add(TextFormatting.GRAY + "Target name: " + TextFormatting.RESET + jigsaw.target);
        text.add(TextFormatting.GRAY + "Turns into: " + TextFormatting.RESET + jigsaw.replaceBlock.getRegistryName());
        text.add(TextFormatting.GRAY + "   with meta: " + TextFormatting.RESET + jigsaw.replaceMeta);
        text.add(TextFormatting.GRAY + "Joint type: " + TextFormatting.RESET + (jigsaw.isRollable ? "Rollable" : "Aligned"));

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta);
        if (facing.getAxis().isVertical()) facing = EnumFacing.NORTH;
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }
    @AutoRegister
    public static class TileEntityWandTandem extends TileEntityLoadedBase implements IControlReceiver, ITickable {

        public static boolean copyMode = false;

        private String pool = "default";
        private String target = "default";
        private Block replaceBlock = Blocks.AIR;
        private int replaceMeta = 0;
        private boolean isRollable = true; // sets joint type, rollable joints can be placed in any orientation for vertical jigsaw connections

        private boolean isArmed = false;
        private SpawnCondition structure;

        @Override
        public void update() {
            if (!world.isRemote) {
                tryGenerate();
                networkPackNT(15);
            }
        }

        private void tryGenerate() {
            if (!this.isArmed || target == null || target.isEmpty() || pool == null || pool.isEmpty()) return;

            JigsawPool pool;
            try {
                pool = structure.getPool(this.pool);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            if (pool == null) return;

            JigsawPiece nextPiece = pool.get(world.rand);
            if (nextPiece == null) return;

            EnumFacing dir = world.getBlockState(pos).getValue(FACING);

            List<NBTStructure.JigsawConnection> connectionPool = nextPiece.structure.getConnectionPool(dir, target);
            if (connectionPool == null) return;

            NBTStructure.JigsawConnection toConnection = connectionPool.get(world.rand.nextInt(connectionPool.size()));
            int nextCoordBase = directionOffsetToCoordBase(dir.getOpposite(), toConnection.dir);

            BlockPos npos = pos.offset(dir);

            // offset the starting point to the connecting point
            int ox = nextPiece.structure.rotateX(toConnection.pos.x, toConnection.pos.z, nextCoordBase);
            int oy = toConnection.pos.y;
            int oz = nextPiece.structure.rotateZ(toConnection.pos.x, toConnection.pos.z, nextCoordBase);

            nextPiece.structure.build(world, nextPiece, npos.getX() - ox, npos.getY() - oy, npos.getZ() - oz, nextCoordBase, structure.name);

            IBlockState replaceState = replaceBlock.getStateFromMeta(replaceMeta);
            world.setBlockState(pos, replaceState, 2);
        }

        private int directionOffsetToCoordBase(EnumFacing from, EnumFacing to) {
            for (int i = 0; i < 4; i++) {
                if (from == to) return i % 4;
                from = from.rotateYCCW();
            }
            return 0;
        }

        @Override
        public void serialize(ByteBuf buf) {
            BufferUtil.writeString(buf, pool);
            BufferUtil.writeString(buf, target);
            buf.writeInt(Block.getIdFromBlock(replaceBlock));
            buf.writeInt(replaceMeta);
            buf.writeBoolean(isRollable);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            pool = BufferUtil.readString(buf);
            target = BufferUtil.readString(buf);
            replaceBlock = Block.getBlockById(buf.readInt());
            replaceMeta = buf.readInt();
            isRollable = buf.readBoolean();
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            if (!copyMode) {
                super.writeToNBT(nbt);
                nbt.setInteger("direction", world.getBlockState(pos).getValue(FACING).getIndex());
                if (isArmed) {
                    nbt.setBoolean("isArmed", true);
                    nbt.setString("structure", structure.name);
                }
            }

            nbt.setString("pool", pool);
            nbt.setString("target", target);
            nbt.setString("block", String.valueOf(replaceBlock.getRegistryName()));
            nbt.setInteger("meta", replaceMeta);
            nbt.setBoolean("roll", isRollable);

            return nbt;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            if (!copyMode) {
                super.readFromNBT(nbt);
                isArmed = nbt.getBoolean("isArmed");
                structure = NBTStructure.getStructure(nbt.getString("structure"));
            }

            pool = nbt.getString("pool");
            target = nbt.getString("target");
            replaceBlock = Block.getBlockFromName(nbt.getString("block"));
            replaceMeta = nbt.getInteger("meta");
            isRollable = nbt.getBoolean("roll");
        }

        @Override
        public boolean hasPermission(EntityPlayer player) {
            return true;
        }

        @Override
        public void receiveControl(NBTTagCompound nbt) {
            readFromNBT(nbt);
            markDirty();
        }

        public void arm(SpawnCondition structure) {
            isArmed = true;
            this.structure = structure;
        }

    }

    public static class GuiWandTandem extends GuiScreen {

        private final TileEntityWandTandem jigsaw;

        private GuiTextField textPool;
        private GuiTextField textTarget;

        private GuiButton jointToggle;

        public GuiWandTandem(TileEntityWandTandem jigsaw) {
            this.jigsaw = jigsaw;
        }

        @Override
        public void initGui() {
            Keyboard.enableRepeatEvents(true);

            textPool = new GuiTextField(0, this.fontRenderer, this.width / 2 - 150, 50, 300, 20);
            textPool.setText(jigsaw.pool);

            textTarget = new GuiTextField(1, this.fontRenderer, this.width / 2 + 10, 100, 140, 20);
            textTarget.setText(jigsaw.target);

            jointToggle = new GuiButton(0, this.width / 2 + 60, 150, 90, 20, jigsaw.isRollable ? "Rollable" : "Aligned");
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();

            this.drawString(this.fontRenderer, "Target pool:", this.width / 2 - 150, 37, 0xA0A0A0);
            textPool.drawTextBox();

            this.drawString(this.fontRenderer, "Target name:", this.width / 2 + 10, 87, 0xA0A0A0);
            textTarget.drawTextBox();

            this.drawString(this.fontRenderer, "Joint type:", this.width / 2 + 60, 137, 0xA0A0A0);
            jointToggle.drawButton(this.mc, mouseX, mouseY, partialTicks);

            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        public void onGuiClosed() {
            Keyboard.enableRepeatEvents(false);

            NBTTagCompound data = new NBTTagCompound();
            jigsaw.writeToNBT(data);

            data.setString("pool", textPool.getText());
            data.setString("target", textTarget.getText());
            data.setBoolean("roll", "Rollable".equals(jointToggle.displayString));

            PacketThreading.createSendToServerThreadedPacket(
                    new NBTControlPacket(data, jigsaw.getPos().getX(), jigsaw.getPos().getY(), jigsaw.getPos().getZ()));
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            super.keyTyped(typedChar, keyCode);
            textPool.textboxKeyTyped(typedChar, keyCode);
            textTarget.textboxKeyTyped(typedChar, keyCode);
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            textPool.mouseClicked(mouseX, mouseY, mouseButton);
            textTarget.mouseClicked(mouseX, mouseY, mouseButton);

            if (jointToggle.mousePressed(this.mc, mouseX, mouseY)) {
                jointToggle.displayString = "Rollable".equals(jointToggle.displayString) ? "Aligned" : "Rollable";
            }
        }

        @Override public boolean doesGuiPauseGame() { return false; }
    }
}
