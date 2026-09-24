package com.hbm.entity.train;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.blocks.rail.IRailNTM.TrackGauge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.gui.GuiInfoContainer;
import com.hbm.Tags;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "entity_ntm_cargo_tram", trackingRange = 250, sendVelocityUpdates = false)
public class TrainCargoTram extends EntityRailCarElectric implements IGUIProvider {

    public TrainCargoTram(World world) {
        super(world);
        this.setSize(5F, 2F);
    }

    @Override public double getPoweredAcceleration() { return 0.01; }
    @Override public double getPassivBrake() { return 0.95; }
    @Override public boolean shouldUseEngineBrake(EntityPlayer player) { return Math.abs(this.engineSpeed) < 0.1; }
    @Override public double getMaxPoweredSpeed() { return 0.5; }
    @Override public double getMaxRailSpeed() { return 1; }

    @Override public TrackGauge getGauge() { return TrackGauge.STANDARD; }
    @Override public double getLengthSpan() { return 1.5; }
    @Override public double getCollisionSpan() { return 2.5; }
    @Override public Vec3d getRiderSeatPosition() { return new Vec3d(0.375, 2.375, 0.5); }
    @Override public boolean shouldRiderSit() { return false; }
    @Override public int getSizeInventory() { return 29; }
    @Override public @NotNull String getName() { return this.hasCustomName() ? this.getEntityName() : "container.trainTram"; }
    @Override public double getCouplingDist(TrainCoupling coupling) { return coupling != null ? 2.75 : 0; }

    @Override public int getMaxPower() { return this.getPowerConsumption() * 100; }
    @Override public int getPowerConsumption() { return 10; }
    @Override public boolean hasChargeSlot() { return true; }
    @Override public int getChargeSlot() { return 28; }

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
    public Vec3d[] getPassengerSeats() {
        return new Vec3d[] {
                new Vec3d(0.5, 1.75, -1.5),
                new Vec3d(-0.5, 1.75, -1.5)
        };
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerTrainCargoTram(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUITrainCargoTram(player.inventory, this);
    }

    public static class ContainerTrainCargoTram extends Container {
        private final TrainCargoTram train;
        public ContainerTrainCargoTram(InventoryPlayer invPlayer, TrainCargoTram train) {
            this.train = train;
            for(int i = 0; i < 4; i++) {
                for(int j = 0; j < 7; j++) {
                    this.addSlotToContainer(new Slot(train, i * 7 + j, 8 + j * 18, 18 + i * 18));
                }
            }
            this.addSlotToContainer(new Slot(train, 28, 152, 72));
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 9; j++) {
                    this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
                }
            }
            for(int i = 0; i < 9; i++) {
                this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 180));
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
                } else {

                    if(stackCopy.getItem() instanceof IBatteryItem) {
                        if(!this.mergeItemStack(stack, 28, 29, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if(!this.mergeItemStack(stack, 0, 28, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
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
    public static class GUITrainCargoTram extends GuiInfoContainer {
        private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/vehicles/gui_cargo_tram.png");
        private final TrainCargoTram train;
        public GUITrainCargoTram(InventoryPlayer invPlayer, TrainCargoTram train) {
            super(new ContainerTrainCargoTram(invPlayer, train));
            this.train = train;
            this.xSize = 176;
            this.ySize = 204;
        }

        @Override
        public void drawScreen(int x, int y, float interp) {
            super.drawScreen(x, y, interp);
            this.drawElectricityInfo(this, x, y, guiLeft + 152, guiTop + 18, 16, 52, train.getPower(), train.getMaxPower());
        }

        @Override
        protected void drawGuiContainerForegroundLayer(int i, int j) {
            String name = this.train.hasCustomName() ? this.train.getName() : I18nUtil.resolveKey(this.train.getName());
            this.fontRenderer.drawString(name, 140 / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xffffff);
            this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        }

        @Override
        protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

            int i = train.getPower() * 53 / train.getMaxPower();
            drawTexturedModalRect(guiLeft + 152, guiTop + 70 - i, 176, 52 - i, 16, i);

            if(train.getPower() > train.getPowerConsumption()) {
                drawTexturedModalRect(guiLeft + 156, guiTop + 4, 176, 52, 9, 12);
            }
        }
    }
}
