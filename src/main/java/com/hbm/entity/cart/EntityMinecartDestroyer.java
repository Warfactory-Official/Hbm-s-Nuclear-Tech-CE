package com.hbm.entity.cart;

import java.util.List;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.SlotPattern;
import com.hbm.items.tool.ItemModMinecart;
import com.hbm.items.tool.ItemModMinecart.EnumCartBase;
import com.hbm.items.tool.ItemModMinecart.EnumMinecart;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.entity.item.RenderNeoCart;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "entity_ntm_cart_destroyer", trackingRange = 250, sendVelocityUpdates = false)
public class EntityMinecartDestroyer extends EntityMinecartContainerBase implements IGUIProvider {

    public EntityMinecartDestroyer(World world) {
        super(world);
    }

    public EntityMinecartDestroyer(World world, double x, double y, double z, EnumCartBase type) {
        super(world, x, y, z, type);
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
        return 18;
    }

    @Override
    public boolean isItemValidForSlot(int slot, @NotNull ItemStack stack) {
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if(!world.isRemote && this.ticksExisted % 5 == 0) {

            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(
                    posX - 2.5,
                    posY - 1.5,
                    posZ - 2.5,
                    posX + 2.5,
                    posY + 2,
                    posZ + 2.5));

            boolean sound = false;

            outer: for(EntityItem item : items) {
                ItemStack stack = item.getItem();

                for(int i = 0; i < 9; i++) {
                    ItemStack match = this.slots.get(i);

                    if(!match.isEmpty() && match.getItem() == stack.getItem() && match.getItemDamage() == stack.getItemDamage()) {
                        item.setDead();
                        sound = true;
                        continue outer;
                    }
                }

                for(int i = 9; i < 18; i++) {
                    ItemStack match = this.slots.get(i);

                    if(!match.isEmpty() && match.getItem() == stack.getItem()) {
                        item.setDead();
                        sound = true;
                        continue outer;
                    }
                }
            }

            if(sound)
                world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.BLOCKS, 0.5F, 0.5F + world.rand.nextFloat() * 0.2F);
        }

        if(world.isRemote && this.ticksExisted % 5 == 0) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.75, posZ, 0.0, 0.01, 0.0);
        }
    }

    @Override
    public @NotNull ItemStack getCartItem() {
        return ItemModMinecart.createCartItem(this.getBase(), EnumMinecart.DESTROYER);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderSpecialContent(RenderNeoCart renderer) {
        renderer.bindTexture(ResourceManager.cart_destroyer_tex);
        ResourceManager.cart_destroyer.renderAll();
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCartDestroyer(player.inventory, (EntityMinecartDestroyer) world.getEntityByID(x));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICartDestroyer(player.inventory, (EntityMinecartDestroyer) world.getEntityByID(x));
    }

    public static class ContainerCartDestroyer extends Container {

        private final IInventory cart;

        public ContainerCartDestroyer(InventoryPlayer invPlayer, IInventory cart) {
            this.cart = cart;

            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 3; j++) {
                    this.addSlotToContainer(new SlotPattern(cart, j + i * 3, 10 + j * 18, 17 + i * 18));
                    this.addSlotToContainer(new SlotPattern(cart, j + i * 3 + 9, 114 + j * 18, 17 + i * 18));
                }
            }

            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 9; j++) {
                    this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
                }
            }

            for(int i = 0; i < 9; i++) {
                this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
            }
        }

        @Override
        public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canInteractWith(@NotNull EntityPlayer player) {
            return cart.isUsableByPlayer(player);
        }

        @Override
        public @NotNull ItemStack slotClick(int index, int button, @NotNull ClickType mode, @NotNull EntityPlayer player) {

            if(index < 0 || index >= cart.getSizeInventory()) {
                return super.slotClick(index, button, mode, player);
            }

            Slot slot = this.getSlot(index);

            ItemStack ret = ItemStack.EMPTY;
            ItemStack held = player.inventory.getItemStack();

            if(slot.getHasStack())
                ret = slot.getStack().copy();

            slot.putStack(held);

            return ret;
        }
    }

    @SideOnly(Side.CLIENT)
    public static class GUICartDestroyer extends GuiContainer {

        private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/cart/gui_destroyer.png");
        private final IInventory cart;

        public GUICartDestroyer(InventoryPlayer invPlayer, EntityMinecartDestroyer cart) {
            super(new ContainerCartDestroyer(invPlayer, cart));
            this.cart = cart;
            this.xSize = 176;
            this.ySize = 166;
        }

        @Override
        protected void drawGuiContainerForegroundLayer(int i, int j) {
            String name = this.cart.hasCustomName() ? this.cart.getName() : I18nUtil.resolveKey(this.cart.getName());
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
