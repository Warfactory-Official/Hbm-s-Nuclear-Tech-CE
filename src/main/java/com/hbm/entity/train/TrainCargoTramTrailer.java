package com.hbm.entity.train;

import com.hbm.blocks.rail.IRailNTM.TrackGauge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.gui.GuiInfoContainer;
import com.hbm.Tags;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "entity_ntm_cargo_tram_trailer", trackingRange = 250, sendVelocityUpdates = false)
public class TrainCargoTramTrailer extends EntityRailCarCargo implements IGUIProvider {

    public TrainCargoTramTrailer(World world) {
        super(world);
        this.setSize(5F, 2F);
    }

    @Override public double getMaxRailSpeed() { return 1; }
    @Override public TrackGauge getGauge() { return TrackGauge.STANDARD; }
    @Override public double getLengthSpan() { return 1.5; }
    @Override public double getCollisionSpan() { return 2.5; }
    @Override public int getSizeInventory() { return 45; }
    @Override public @NotNull String getName() { return this.hasCustomName() ? this.getEntityName() : "container.trainTramTrailer"; }
    @Override public double getCouplingDist(TrainCoupling coupling) { return coupling != null ? 2.75 : 0; }
    @Override public double getCurrentSpeed() { return 0; }

    @Override
    public DummyConfig[] getDummies() {
        return new DummyConfig[] {
                new DummyConfig(2F, 1F, new Vec3d(0, 0, 1.5)),
                new DummyConfig(2F, 1F, new Vec3d(0, 0, 0)),
                new DummyConfig(2F, 1F, new Vec3d(0, 0, -1.5))
        };
    }

    @Override
    public boolean attackEntityFrom(@NotNull DamageSource source, float amount) {
        if(!this.world.isRemote && !this.isDead) {
            this.setDead();
        }

        return true;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, @NotNull EnumHand hand) {
        if(super.processInitialInteract(player, hand)) return false;

        if(!this.world.isRemote) {
            player.openGui(MainRegistry.instance, 0, world, this.getEntityId(), 0, 0);
        }

        return true;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerTrainCargoTramTrailer(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUITrainCargoTramTrailer(player.inventory, this);
    }

    public static class ContainerTrainCargoTramTrailer extends Container {
        private final TrainCargoTramTrailer train;
        public ContainerTrainCargoTramTrailer(InventoryPlayer invPlayer, TrainCargoTramTrailer train) {
            this.train = train;
            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 9; j++) {
                    this.addSlotToContainer(new Slot(train, i * 9 + j, 8 + j * 18, 18 + i * 18));
                }
            }
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 9; j++) {
                    this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));
                }
            }
            for(int i = 0; i < 9; i++) {
                this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 198));
            }
        }

        @Override
        public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int slotIndex) {
            ItemStack stackCopy = ItemStack.EMPTY;
            Slot slot = this.inventorySlots.get(slotIndex);
            if(slot != null && slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                stackCopy = stack.copy();
                if(slotIndex < train.getSizeInventory()) {
                    if(!this.mergeItemStack(stack, train.getSizeInventory(), this.inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if(!this.mergeItemStack(stack, 0, 45, false)) {
                    return ItemStack.EMPTY;
                }
                if(stack.isEmpty()) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
                    slot.onSlotChanged();
                }
            }
            return stackCopy;
        }

        @Override
        public boolean canInteractWith(@NotNull EntityPlayer player) {
            return train.isUsableByPlayer(player);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class GUITrainCargoTramTrailer extends GuiInfoContainer {
        private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/vehicles/gui_cargo_tram_trailer.png");
        private final TrainCargoTramTrailer train;
        public GUITrainCargoTramTrailer(InventoryPlayer invPlayer, TrainCargoTramTrailer train) {
            super(new ContainerTrainCargoTramTrailer(invPlayer, train));
            this.train = train;
            this.xSize = 176;
            this.ySize = 222;
        }

        @Override
        protected void drawGuiContainerForegroundLayer(int i, int j) {
            String name = this.train.hasCustomName() ? this.train.getName() : I18nUtil.resolveKey(this.train.getName());
            this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xffffff);
            this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        }

        @Override
        protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        }
    }
}
