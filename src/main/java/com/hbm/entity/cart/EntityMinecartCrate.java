package com.hbm.entity.cart;

import java.io.IOException;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.tool.ItemModMinecart;
import com.hbm.items.tool.ItemModMinecart.EnumCartBase;
import com.hbm.items.tool.ItemModMinecart.EnumMinecart;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "entity_ntm_cart_crate", trackingRange = 250, sendVelocityUpdates = false)
public class EntityMinecartCrate extends EntityMinecartContainerBase implements IGUIProvider {

    public EntityMinecartCrate(World world) {
        super(world);
    }

    public EntityMinecartCrate(World world, double x, double y, double z, EnumCartBase type, ItemStack stack) {
        super(world, x, y, z, type);
        if(stack.hasTagCompound()) {
            NBTTagCompound nbt = stack.getTagCompound();
            for(int i = 0; i < getSizeInventory(); i++) {
                setInventorySlotContents(i, new ItemStack(nbt.getCompoundTag("slot" + i)));
            }
        }
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, @NotNull EnumHand hand) {
        if(!this.world.isRemote) {
            player.openGui(MainRegistry.instance, 0, world, this.getEntityId(), 0, 0);
        }

        return true;
    }

    @Override
    public int getSizeInventory() {
        return 9 * 6;
    }

    @Override
    public void killMinecart(@NotNull DamageSource source) {
        this.setDead();
        ItemStack itemstack = ItemModMinecart.createCartItem(EnumCartBase.VANILLA, EnumMinecart.CRATE);

        NBTTagCompound nbt = new NBTTagCompound();

        for(int i = 0; i < getSizeInventory(); i++) {

            ItemStack stack = getStackInSlot(i);
            if(stack.isEmpty())
                continue;

            NBTTagCompound slot = new NBTTagCompound();
            stack.writeToNBT(slot);
            nbt.setTag("slot" + i, slot);
        }

        if(!nbt.isEmpty()) {
            itemstack.setTagCompound(nbt);
        }

        if(this.hasCustomName()) {
            itemstack.setStackDisplayName(this.getCustomNameTag());
        }

        try {
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(nbt, buffer);
            byte[] abyte = buffer.toByteArray();

            if(abyte.length > 6000) {
                world.newExplosion(this, posX, posY, posZ, 2F, true, true);
                this.entityDropItem(ItemModMinecart.createCartItem(EnumCartBase.VANILLA, EnumMinecart.CRATE), 0.0F);
            }

        } catch(IOException e) { }

        this.entityDropItem(itemstack, 0.0F);
    }

    @Override
    public @NotNull ItemStack getCartItem() {
        return ItemModMinecart.createCartItem(EnumCartBase.VANILLA, EnumMinecart.CRATE);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCartCrate(player.inventory, (EntityMinecartCrate) world.getEntityByID(x));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICartCrate(player.inventory, (EntityMinecartCrate) world.getEntityByID(x));
    }

    public static class ContainerCartCrate extends Container {

        private final IInventory crate;

        public ContainerCartCrate(InventoryPlayer invPlayer, IInventory crate) {
            this.crate = crate;

            for(int i = 0; i < 6; i++) {
                for(int j = 0; j < 9; j++) {
                    this.addSlotToContainer(new Slot(crate, j + i * 9, 8 + j * 18, 18 + i * 18));
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
                if(slotIndex < crate.getSizeInventory()) {
                    if(!this.mergeItemStack(stack, crate.getSizeInventory(), this.inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if(!this.mergeItemStack(stack, 0, crate.getSizeInventory(), false)) {
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
            return crate.isUsableByPlayer(player);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class GUICartCrate extends GuiContainer {

        private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_steel.png");
        private final IInventory crate;

        public GUICartCrate(InventoryPlayer invPlayer, EntityMinecartCrate crate) {
            super(new ContainerCartCrate(invPlayer, crate));
            this.crate = crate;
            this.xSize = 176;
            this.ySize = 222;
        }

        @Override
        protected void drawGuiContainerForegroundLayer(int i, int j) {
            String name = this.crate.hasCustomName() ? this.crate.getName() : I18nUtil.resolveKey(this.crate.getName());
            this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
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
