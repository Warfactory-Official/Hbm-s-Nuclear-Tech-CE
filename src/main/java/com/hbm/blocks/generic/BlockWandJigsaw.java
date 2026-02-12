package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
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
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockWandJigsaw extends BlockContainerBakeable implements IBlockSideRotation, INBTBlockTransformable, IGUIProvider, ILookOverlay {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    public BlockWandJigsaw(String regName) {
        this(regName, new BlockBakeFrame(BlockBakeFrame.BlockForm.FULL_CUSTOM,
                "wand_jigsaw_top",   // up
                "wand_jigsaw_top",   // down
                "wand_jigsaw_back",  // north (back)
                "wand_jigsaw",       // south (front)
                "wand_jigsaw_side",  // west
                "wand_jigsaw_side"   // east
        ));
    }

    public BlockWandJigsaw(String regName, BlockBakeFrame frame) {
        super(Material.IRON, regName, frame);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWandJigsaw();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta & 7);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        return facing.getIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing clickedFace, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing facing = EnumFacing.getDirectionFromEntityLiving(pos, placer);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        EnumFacing facing = EnumFacing.getDirectionFromEntityLiving(pos, player);
        world.setBlockState(pos, state.withProperty(FACING, facing), 2);
    }

    // Порт логики поворотов декоратора
    @Override
    public int getRotationFromSide(IBlockAccess world, BlockPos pos, EnumFacing efside) {
        int side = efside.getIndex();
        IBlockState state = world.getBlockState(pos);
        int meta = state.getValue(FACING).getIndex();

        if (side == 0) return IBlockSideRotation.topToBottom(getRotationFromSide(world, pos, efside));

        if (side == meta || IBlockSideRotation.isOpposite(side, meta)) return 0;

        if (meta == 0) return 0;
        if (meta == 1) return 3;

        if (side == 1) {
            switch (meta) {
                case 2:
                    return 3;
                case 3:
                    return 0;
                case 4:
                    return 1;
                case 5:
                    return 2;
            }
        }

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
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileEntityWandJigsaw)) return false;

        TileEntityWandJigsaw jigsaw = (TileEntityWandJigsaw) te;

        if (!player.isSneaking()) {
            ItemStack held = player.getHeldItem(hand);
            Block block = getBlock(worldIn, held);
            if (block == ModBlocks.wand_air) block = Blocks.AIR;

            if (block != null && block != ModBlocks.wand_jigsaw && block != ModBlocks.wand_loot) {
                jigsaw.replaceBlock = block;
                jigsaw.replaceMeta = held.getItemDamage();
                return true;
            }

            if (!held.isEmpty() && held.getItem() == ModItems.wand_s) return false;

            if (worldIn.isRemote) {
                FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }

        return false;
    }

    private Block getBlock(World world, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (!(stack.getItem() instanceof ItemBlock)) return null;
        return ((ItemBlock) stack.getItem()).getBlock();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GuiWandJigsaw((TileEntityWandJigsaw) world.getTileEntity(new BlockPos(x, y, z)));
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityWandJigsaw jigsaw)) return;

        List<String> text = new ArrayList<>();

        text.add(TextFormatting.GRAY + "Target pool: " + TextFormatting.RESET + jigsaw.pool);
        text.add(TextFormatting.GRAY + "Name: " + TextFormatting.RESET + jigsaw.name);
        text.add(TextFormatting.GRAY + "Target name: " + TextFormatting.RESET + jigsaw.target);
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(jigsaw.replaceBlock);
        text.add(TextFormatting.GRAY + "Turns into: " + TextFormatting.RESET + (key != null ? key.toString() : "minecraft:air"));
        text.add(TextFormatting.GRAY + "   with meta: " + TextFormatting.RESET + jigsaw.replaceMeta);
        text.add(TextFormatting.GRAY + "Selection/Placement priority: " + TextFormatting.RESET + jigsaw.selectionPriority + "/" + jigsaw.placementPriority);
        text.add(TextFormatting.GRAY + "Joint type: " + TextFormatting.RESET + (jigsaw.isRollable ? "Rollable" : "Aligned"));

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation(blockFrame.getBaseModel()));
            ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();
            blockFrame.putTextures(textureMap);

            IModel retexturedModel = baseModel.retexture(textureMap.build());
            IBakedModel[] models = new IBakedModel[6];

            for (EnumFacing facing : EnumFacing.VALUES) {
                IBakedModel baked = retexturedModel.bake(
                        ModelRotation.getModelRotation(BlockBakeFrame.getXRotationForFacing(facing), BlockBakeFrame.getYRotationForFacing(facing)),
                        DefaultVertexFormats.BLOCK,
                        ModelLoader.defaultTextureGetter()
                );
                models[facing.getIndex()] = baked;
            }

            ModelResourceLocation inv = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory");
            event.getModelRegistry().putObject(inv, models[EnumFacing.SOUTH.getIndex()]);

            for (EnumFacing facing : EnumFacing.VALUES) {
                ModelResourceLocation worldLoc = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "facing=" + facing.getName());
                event.getModelRegistry().putObject(worldLoc, models[facing.getIndex()]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @AutoRegister
    public static class TileEntityWandJigsaw extends TileEntityLoadedBase implements IControlReceiver, ITickable {

        private int selectionPriority = 0;
        private int placementPriority = 0;
        private String pool = "default";
        private String name = "default";
        private String target = "default";
        private Block replaceBlock = Blocks.AIR;
        private int replaceMeta = 0;
        private boolean isRollable = true;

        @Override
        public void update() {
            if (!world.isRemote) {
                networkPackNT(15);
            }
        }

        @Override
        public void serialize(ByteBuf buf) {
            buf.writeInt(selectionPriority);
            buf.writeInt(placementPriority);
            BufferUtil.writeString(buf, pool);
            BufferUtil.writeString(buf, name);
            BufferUtil.writeString(buf, target);
            buf.writeInt(Block.getIdFromBlock(replaceBlock));
            buf.writeInt(replaceMeta);
            buf.writeBoolean(isRollable);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            selectionPriority = buf.readInt();
            placementPriority = buf.readInt();
            pool = BufferUtil.readString(buf);
            name = BufferUtil.readString(buf);
            target = BufferUtil.readString(buf);
            replaceBlock = Block.getBlockById(buf.readInt());
            replaceMeta = buf.readInt();
            isRollable = buf.readBoolean();
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);

            nbt.setInteger("selection", selectionPriority);
            nbt.setInteger("placement", placementPriority);
            nbt.setString("pool", pool);
            nbt.setString("name", name);
            nbt.setString("target", target);

            ResourceLocation key = ForgeRegistries.BLOCKS.getKey(replaceBlock);
            nbt.setString("block", key != null ? key.toString() : "minecraft:air");
            nbt.setInteger("meta", replaceMeta);
            nbt.setBoolean("roll", isRollable);

            return nbt;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);

            selectionPriority = nbt.getInteger("selection");
            placementPriority = nbt.getInteger("placement");
            pool = nbt.getString("pool");
            name = nbt.getString("name");
            target = nbt.getString("target");

            if (nbt.hasKey("block")) {
                replaceBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("block")));
                if (replaceBlock == null) replaceBlock = Blocks.AIR;
            } else {
                replaceBlock = Blocks.AIR;
            }
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
    }

    public static class GuiWandJigsaw extends GuiScreen {

        private final TileEntityWandJigsaw jigsaw;

        private GuiTextField textPool;
        private GuiTextField textName;
        private GuiTextField textTarget;

        private GuiTextField textSelectionPriority;
        private GuiTextField textPlacementPriority;

        private GuiButton jointToggle;

        public GuiWandJigsaw(TileEntityWandJigsaw jigsaw) {
            this.jigsaw = jigsaw;
        }

        @Override
        public void initGui() {
            Keyboard.enableRepeatEvents(true);

            textPool = new GuiTextField(0, this.fontRenderer, this.width / 2 - 150, 50, 300, 20);
            textPool.setText(jigsaw.pool);

            textName = new GuiTextField(1, this.fontRenderer, this.width / 2 - 150, 100, 140, 20);
            textName.setText(jigsaw.name);

            textTarget = new GuiTextField(2, this.fontRenderer, this.width / 2 + 10, 100, 140, 20);
            textTarget.setText(jigsaw.target);

            textSelectionPriority = new GuiTextField(3, this.fontRenderer, this.width / 2 - 150, 150, 90, 20);
            textSelectionPriority.setText(Integer.toString(jigsaw.selectionPriority));

            textPlacementPriority = new GuiTextField(4, this.fontRenderer, this.width / 2 - 40, 150, 90, 20);
            textPlacementPriority.setText(Integer.toString(jigsaw.placementPriority));

            jointToggle = new GuiButton(0, this.width / 2 + 60, 150, 90, 20, jigsaw.isRollable ? "Rollable" : "Aligned");
            this.buttonList.add(jointToggle);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();

            this.drawString(this.fontRenderer, "Target pool:", this.width / 2 - 150, 37, 0xA0A0A0);
            textPool.drawTextBox();

            this.drawString(this.fontRenderer, "Name:", this.width / 2 - 150, 87, 0xA0A0A0);
            textName.drawTextBox();

            this.drawString(this.fontRenderer, "Target name:", this.width / 2 + 10, 87, 0xA0A0A0);
            textTarget.drawTextBox();

            this.drawString(this.fontRenderer, "Selection priority:", this.width / 2 - 150, 137, 0xA0A0A0);
            textSelectionPriority.drawTextBox();

            this.drawString(this.fontRenderer, "Placement priority:", this.width / 2 - 40, 137, 0xA0A0A0);
            textPlacementPriority.drawTextBox();

            this.drawString(this.fontRenderer, "Joint type:", this.width / 2 + 60, 137, 0xA0A0A0);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        public void onGuiClosed() {
            Keyboard.enableRepeatEvents(false);

            NBTTagCompound data = new NBTTagCompound();
            jigsaw.writeToNBT(data);

            data.setString("pool", textPool.getText());
            data.setString("name", textName.getText());
            data.setString("target", textTarget.getText());

            try {
                data.setInteger("selection", Integer.parseInt(textSelectionPriority.getText()));
            } catch (Exception ignored) {}
            try {
                data.setInteger("placement", Integer.parseInt(textPlacementPriority.getText()));
            } catch (Exception ignored) {}

            data.setBoolean("roll", "Rollable".equals(jointToggle.displayString));

            PacketThreading.createSendToServerThreadedPacket(
                    new NBTControlPacket(data, jigsaw.getPos().getX(), jigsaw.getPos().getY(), jigsaw.getPos().getZ()));
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            super.keyTyped(typedChar, keyCode);
            textPool.textboxKeyTyped(typedChar, keyCode);
            textName.textboxKeyTyped(typedChar, keyCode);
            textTarget.textboxKeyTyped(typedChar, keyCode);
            textSelectionPriority.textboxKeyTyped(typedChar, keyCode);
            textPlacementPriority.textboxKeyTyped(typedChar, keyCode);
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            textPool.mouseClicked(mouseX, mouseY, mouseButton);
            textName.mouseClicked(mouseX, mouseY, mouseButton);
            textTarget.mouseClicked(mouseX, mouseY, mouseButton);
            textSelectionPriority.mouseClicked(mouseX, mouseY, mouseButton);
            textPlacementPriority.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        protected void actionPerformed(GuiButton button) {
            if (button == jointToggle) {
                jointToggle.displayString = "Rollable".equals(jointToggle.displayString) ? "Aligned" : "Rollable";
            }
        }
    }
}
