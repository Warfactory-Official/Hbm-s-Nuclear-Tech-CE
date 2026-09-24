package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.StructureConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.gui.element.GuiFileList;
import com.hbm.items.IModelRegister;
import com.hbm.items.block.ItemBlockBase;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.I18nUtil;
import com.hbm.util.Tuple.Pair;
import com.hbm.world.gen.nbt.NBTStructure;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockWandStructure extends BlockContainer implements IBlockMulti, ICustomBlockItem, IGUIProvider, ILookOverlay {

    public static final int META_SAVE = 0;
    public static final int META_LOAD = 1;

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 1);

    public BlockWandStructure(String s) {
        super(Material.IRON);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(null);
        this.setDefaultState(this.blockState.getBaseState().withProperty(META, META_SAVE));

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntityWandStructure();
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, META);
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, rectify(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return this.getMetaFromState(state);
    }

    @Override
    public int getSubCount() {
        return 2;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return this.getTranslationKey() + (stack.getItemDamage() == META_LOAD ? ".load" : ".save");
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof TileEntityWandStructure structure)) return false;

        if (!player.isSneaking()) {
            ItemStack held = player.getHeldItem(hand);
            Block block = held.isEmpty() ? null : Block.getBlockFromItem(held.getItem());

            if (block != null && block != Blocks.AIR && !ModBlocks.isStructureBlock(block, true)) {
                Pair<Block, Integer> bm = new Pair<>(block, held.getItemDamage());

                if (structure.blacklist.contains(bm)) {
                    structure.blacklist.remove(bm);
                } else {
                    structure.blacklist.add(bm);
                }

                return true;
            }

            if (world.isRemote)
                FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());

            return true;
        }

        return false;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntityWandStructure structure = (TileEntityWandStructure) world.getTileEntity(pos);
        if (world.getBlockState(pos).getValue(META) == META_LOAD) return new GuiStructureLoad(structure);
        return new GuiStructureSave(structure);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {

        if (world.getBlockState(pos).getValue(META) != META_SAVE) return;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityWandStructure structure)) return;

        List<String> text = new ArrayList<>();

        text.add(TextFormatting.GRAY + "Name: " + TextFormatting.RESET + structure.name);
        text.add(TextFormatting.GRAY + "Size: " + TextFormatting.RESET + structure.sizeX + " / " + structure.sizeY + " / " + structure.sizeZ);

        text.add(TextFormatting.GRAY + "Blacklist:");
        for (Pair<Block, Integer> bm : structure.blacklist) {
            text.add(TextFormatting.RED + "- " + bm.getKey().getTranslationKey() + " : " + bm.getValue());
        }

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".save.name"), 0xffff00, 0x404000, text);
    }

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new ItemBlockWandStructure(this);
        itemBlock.setRegistryName(this.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    public static class ItemBlockWandStructure extends ItemBlockBase implements IModelRegister {

        public ItemBlockWandStructure(Block block) {
            super(block);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void registerModels() {
            ResourceLocation loc = this.block.getRegistryName();
            for (int meta = 0; meta < 2; meta++) {
                ModelLoader.setCustomModelResourceLocation(this, meta, new ModelResourceLocation(loc, "meta=" + meta));
            }
        }
    }

    @AutoRegister(name = "tileentity_wand_structure")
    public static class TileEntityWandStructure extends TileEntityLoadedBase implements IControlReceiver, ITickable {

        public String name = "";

        public int sizeX = 1;
        public int sizeY = 1;
        public int sizeZ = 1;

        public Set<Pair<Block, Integer>> blacklist = new HashSet<>();

        @Override
        public void update() {
            if (!world.isRemote) {
                networkPackNT(256);
            }
        }

        public void saveStructure(EntityPlayer player) {

            if (name.isEmpty()) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Could not save: invalid name"));
                return;
            }

            if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Could not save: invalid dimensions"));
                return;
            }

            Pair<Block, Integer> air = new Pair<>(Blocks.AIR, 0);
            blacklist.add(air);

            File file = NBTStructure.saveArea(name + ".nbt", world,
                    pos.getX(), pos.getY() + 1, pos.getZ(),
                    pos.getX() + sizeX - 1, pos.getY() + sizeY, pos.getZ() + sizeZ - 1, blacklist);

            blacklist.remove(air);

            if (file == null) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to save structure"));
                return;
            }

            TextComponentString fileText = new TextComponentString(file.getName());
            fileText.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getParentFile().getAbsolutePath()));
            fileText.getStyle().setUnderlined(true);

            player.sendMessage(new TextComponentString("Saved structure as ").appendSibling(fileText));
        }

        public void loadStructure(EntityPlayer player) {

            if (name.isEmpty()) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Could not load: no filename specified"));
                return;
            }

            File structureFile = new File(NBTStructure.getStructureDirectory(), name + ".nbt");

            boolean debug = !world.isBlockPowered(pos);
            boolean previousDebug = StructureConfig.debugStructures;
            StructureConfig.debugStructures = debug;

            try {
                NBTStructure structure = new NBTStructure(structureFile);

                sizeX = structure.getSizeX();
                sizeY = structure.getSizeY();
                sizeZ = structure.getSizeZ();

                structure.build(world, pos.getX(), pos.getY() + 1, pos.getZ(), 0, false, true);

                world.setBlockState(pos, ModBlocks.wand_structure.getDefaultState().withProperty(META, META_SAVE), 3);

                player.sendMessage(new TextComponentString("Structure loaded"));

            } catch (FileNotFoundException ex) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Could not load: file not found"));
            } finally {
                StructureConfig.debugStructures = previousDebug;
            }
        }

        @Override
        public boolean hasPermission(EntityPlayer player) {
            return player.getDistanceSq(pos) < 400;
        }

        @Override
        public void receiveControl(EntityPlayerMP player, NBTTagCompound nbt) {
            readFromNBT(nbt);
            markDirty();

            if (nbt.getBoolean("save")) {
                saveStructure(player);
            }

            if (nbt.getBoolean("load")) {
                loadStructure(player);
            }
        }

        @Override
        public void serialize(ByteBuf buf) {
            BufferUtil.writeString(buf, name);

            buf.writeInt(sizeX);
            buf.writeInt(sizeY);
            buf.writeInt(sizeZ);

            buf.writeInt(blacklist.size());
            for (Pair<Block, Integer> bm : blacklist) {
                buf.writeInt(Block.getIdFromBlock(bm.getKey()));
                buf.writeInt(bm.getValue());
            }
        }

        @Override
        public void deserialize(ByteBuf buf) {
            this.name = BufferUtil.readString(buf);

            this.sizeX = buf.readInt();
            this.sizeY = buf.readInt();
            this.sizeZ = buf.readInt();

            int count = buf.readInt();
            blacklist = new HashSet<>();
            for (int i = 0; i < count; i++) {
                Block block = Block.getBlockById(buf.readInt());
                int meta = buf.readInt();
                blacklist.add(new Pair<>(block, meta));
            }
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);

            this.name = nbt.getString("name");

            this.sizeX = nbt.getInteger("sizeX");
            this.sizeY = nbt.getInteger("sizeY");
            this.sizeZ = nbt.getInteger("sizeZ");

            int[] blocks = nbt.getIntArray("blocks");
            int[] metas = nbt.getIntArray("metas");

            blacklist = new HashSet<>();
            for (int i = 0; i < blocks.length; i++) {
                blacklist.add(new Pair<>(Block.getBlockById(blocks[i]), metas[i]));
            }
        }

        @Override
        public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            nbt.setString("name", name);

            nbt.setInteger("sizeX", sizeX);
            nbt.setInteger("sizeY", sizeY);
            nbt.setInteger("sizeZ", sizeZ);

            nbt.setIntArray("blocks", blacklist.stream().mapToInt(b -> Block.getIdFromBlock(b.getKey())).toArray());
            nbt.setIntArray("metas", blacklist.stream().mapToInt(Pair::getValue).toArray());

            return super.writeToNBT(nbt);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public @NotNull AxisAlignedBB getRenderBoundingBox() {
            return INFINITE_EXTENT_AABB;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public double getMaxRenderDistanceSquared() {
            return 65536.0D;
        }
    }

    @SideOnly(Side.CLIENT)
    public static class GuiStructureSave extends GuiScreen {

        private final TileEntityWandStructure tile;

        private GuiTextField textName;

        private GuiTextField textSizeX;
        private GuiTextField textSizeY;
        private GuiTextField textSizeZ;

        private GuiButton performAction;

        private boolean saveOnClose = false;

        public GuiStructureSave(TileEntityWandStructure tile) {
            this.tile = tile;
        }

        @Override
        public void initGui() {
            Keyboard.enableRepeatEvents(true);

            textName = new GuiTextField(0, fontRenderer, width / 2 - 150, 50, 300, 20);
            textName.setMaxStringLength(128);
            textName.setText(tile.name);

            textSizeX = new GuiTextField(1, fontRenderer, width / 2 - 150, 100, 50, 20);
            textSizeX.setText("" + tile.sizeX);
            textSizeY = new GuiTextField(2, fontRenderer, width / 2 - 100, 100, 50, 20);
            textSizeY.setText("" + tile.sizeY);
            textSizeZ = new GuiTextField(3, fontRenderer, width / 2 - 50, 100, 50, 20);
            textSizeZ.setText("" + tile.sizeZ);

            performAction = new GuiButton(0, width / 2 - 150, 150, 300, 20, "SAVE");
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float interp) {
            drawDefaultBackground();

            textName.drawTextBox();

            textSizeX.drawTextBox();
            textSizeY.drawTextBox();
            textSizeZ.drawTextBox();

            performAction.drawButton(mc, mouseX, mouseY, interp);

            super.drawScreen(mouseX, mouseY, interp);
        }

        @Override
        public void updateScreen() {
            textName.updateCursorCounter();
            textSizeX.updateCursorCounter();
            textSizeY.updateCursorCounter();
            textSizeZ.updateCursorCounter();
        }

        @Override
        public void onGuiClosed() {
            Keyboard.enableRepeatEvents(false);

            NBTTagCompound data = new NBTTagCompound();
            tile.writeToNBT(data);

            data.setString("name", textName.getText());

            try {
                data.setInteger("sizeX", Integer.parseInt(textSizeX.getText()));
            } catch (Exception ex) {
            }
            try {
                data.setInteger("sizeY", Integer.parseInt(textSizeY.getText()));
            } catch (Exception ex) {
            }
            try {
                data.setInteger("sizeZ", Integer.parseInt(textSizeZ.getText()));
            } catch (Exception ex) {
            }

            if (saveOnClose) data.setBoolean("save", true);

            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, tile.getPos()));
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            super.keyTyped(typedChar, keyCode);

            textName.textboxKeyTyped(typedChar, keyCode);

            textSizeX.textboxKeyTyped(typedChar, keyCode);
            textSizeY.textboxKeyTyped(typedChar, keyCode);
            textSizeZ.textboxKeyTyped(typedChar, keyCode);
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            textName.mouseClicked(mouseX, mouseY, mouseButton);

            textSizeX.mouseClicked(mouseX, mouseY, mouseButton);
            textSizeY.mouseClicked(mouseX, mouseY, mouseButton);
            textSizeZ.mouseClicked(mouseX, mouseY, mouseButton);

            if (performAction.mousePressed(mc, mouseX, mouseY)) {
                saveOnClose = true;

                mc.displayGuiScreen(null);
                mc.setIngameFocus();
            }
        }

        @Override
        public boolean doesGuiPauseGame() {
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    public static class GuiStructureLoad extends GuiScreen {

        private static String nameFilter = "";
        private static final FileFilter structureFilter = file -> {
            if (!file.isFile() || !file.getName().endsWith(".nbt")) return false;
            return nameFilter.isEmpty() || file.getName().contains(nameFilter);
        };
        private final TileEntityWandStructure tile;
        private GuiTextField textName;
        private GuiFileList fileList;
        private GuiButton performAction;
        private boolean loadOnClose = false;

        public GuiStructureLoad(TileEntityWandStructure tile) {
            this.tile = tile;
        }

        @Override
        public void initGui() {
            Keyboard.enableRepeatEvents(true);

            textName = new GuiTextField(0, fontRenderer, width / 2 - 150, 50, 300, 20);
            textName.setMaxStringLength(128);
            textName.setText(tile.name);
            nameFilter = tile.name;

            fileList = listFiles();

            performAction = new GuiButton(0, width / 2 - 150, height - 70, 300, 20, "LOAD");
        }

        private GuiFileList listFiles() {
            File[] files = NBTStructure.getStructureDirectory().listFiles(structureFilter);
            return new GuiFileList(mc, files, this::selectFile, nameFilter, width, height, 70, height - 90, 16);
        }

        public void selectFile(File file) {
            String fileName = file.getName();
            textName.setText(fileName.substring(0, fileName.length() - 4));
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float interp) {
            fileList.drawScreen(mouseX, mouseY, interp);

            textName.drawTextBox();

            performAction.drawButton(mc, mouseX, mouseY, interp);

            super.drawScreen(mouseX, mouseY, interp);
        }

        @Override
        public void updateScreen() {
            textName.updateCursorCounter();
        }

        @Override
        public void onGuiClosed() {
            Keyboard.enableRepeatEvents(false);

            NBTTagCompound data = new NBTTagCompound();
            tile.writeToNBT(data);

            data.setString("name", textName.getText());

            if (loadOnClose) data.setBoolean("load", true);

            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, tile.getPos()));
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            super.keyTyped(typedChar, keyCode);

            textName.textboxKeyTyped(typedChar, keyCode);

            if (!nameFilter.equals(textName.getText())) {
                nameFilter = textName.getText();
                fileList = listFiles();
            }
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            textName.mouseClicked(mouseX, mouseY, mouseButton);

            fileList.mouseClicked(mouseX, mouseY, mouseButton);

            fileList.select(textName.getText());

            if (performAction.mousePressed(mc, mouseX, mouseY)) {
                loadOnClose = true;

                mc.displayGuiScreen(null);
                mc.setIngameFocus();
            }
        }

        @Override
        protected void mouseReleased(int mouseX, int mouseY, int state) {
            super.mouseReleased(mouseX, mouseY, state);
            fileList.mouseReleased(mouseX, mouseY, state);
        }

        @Override
        public void handleMouseInput() throws IOException {
            super.handleMouseInput();
            fileList.handleMouseInput();
        }

        @Override
        public boolean doesGuiPauseGame() {
            return false;
        }
    }
}
