package com.hbm.items.tool;

import com.hbm.inventory.container.ContainerToolBox;
import com.hbm.inventory.gui.GUIToolBox;
import com.hbm.items.ItemInventory;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.ItemStackUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemToolBox extends Item implements IGUIProvider {
    public ItemToolBox(String s) {
        this.setMaxStackSize(1);
        this.setRegistryName(s);
        this.setTranslationKey(s);
        this.addPropertyOverride(new ResourceLocation("open"), (stack, worldIn, entityIn) -> {
            if (stack.hasTagCompound()) {
                if (stack.getTagCompound().hasKey("isOpen") && stack.getTagCompound().getBoolean("isOpen")) {
                    return 1;
                }
            }
            return 0;
        });

        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GRAY + "Click with the toolbox to swap hotbars in/out of the toolbox.");
        tooltip.add(TextFormatting.GRAY + "Shift-click with the toolbox to open the toolbox.");
    }

    // Finds active rows in the toolbox (rows with items inside them).
    public List<Integer> getActiveRows(ItemStack box) {
        ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(box, 24);
        if(stacks == null)
            return new ArrayList<>();
        List<Integer> activeRows = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int slot = 0; slot < 8; slot++) {
                if(stacks[row * 8 + slot] != null && !stacks[row * 8 + slot].isEmpty()) {
                    activeRows.add(row);
                    break;
                }
            }
        }
        return activeRows;
    }

    public void moveRows(ItemStack box, EntityPlayer player) {
        List<Integer> activeRows = getActiveRows(box);
        List<List<Integer>> swaps = new ArrayList<>(8);
        ItemStack[] boxInv = ItemStackUtil.readStacksFromNBT(box, 24);
        if (boxInv == null || activeRows.isEmpty()) return;

        boolean extraToolboxWarn = false;
        int playerSlotIndex = -1;
        for (int i = 0; i < 8; i++) { // Create swaps list
            playerSlotIndex++;
            if (i == player.inventory.currentItem) playerSlotIndex++;

            ItemStack hotbarStack = player.inventory.getStackInSlot(playerSlotIndex);

            if (hotbarStack.getItem() instanceof ItemToolBox) { // Warn if you're trying to toolbox a toolbox, and drop the extra toolbox
                if (!extraToolboxWarn) player.sendMessage(new TextComponentString(I18n.format("item.toolbox.error_toolbox_toolbox")).setStyle(new Style().setColor(TextFormatting.RED)));
                extraToolboxWarn = true;
                player.dropItem(hotbarStack, true, true);
                player.inventory.removeStackFromSlot(playerSlotIndex);
            }

            List<Integer> swap = new ArrayList<>(activeRows.size() + 1);

            swap.add(playerSlotIndex);

            if (activeRows.contains(0)) swap.add(i);
            if (activeRows.contains(1)) swap.add(i + 8);
            if (activeRows.contains(2)) swap.add(i + 16);

            swaps.add(swap);
        }

        ItemStack[] boxInvCopy = new ItemStack[24];
        System.arraycopy(boxInv, 0, boxInvCopy, 0, boxInv.length);

        for (List<Integer> swap : swaps) { // Swap hotbars
            ItemStack[] buffer = new ItemStack[swap.size()]; // Create buffer [player|box1|box2|box3]

            buffer[0] = player.inventory.getStackInSlot(swap.get(0));

            for (int i = 1; i < swap.size(); i++) {
                buffer[i] = boxInv[swap.get(i)];
            }

            for (int i = 0; i < swap.size(); i++) { // Actually swap items
                if (i == 0) { // Put player hotbar in last row of toolbox
                    boxInvCopy[swap.get(swap.size() - 1)] = buffer[0];
                }
                else if (i == 1) { // Put first row of toolbox in player hotbar
                    if (buffer[1] == null || buffer[1].isEmpty()) player.inventory.removeStackFromSlot(swap.get(0));
                    else player.inventory.setInventorySlotContents(swap.get(0), buffer[1]);
                }
                else { // Move other rows in toolbox up
                    boxInvCopy[swap.get(i - 1)] = buffer[i];
                }
            }
        }

        System.arraycopy(boxInvCopy, 0, boxInv, 0, boxInv.length);

        ItemStackUtil.addStacksToNBT(box, boxInv); // Update box inventory

        NBTTagCompound nbt = box.getTagCompound(); // idk what this really is for but ill add it here just in case
        if (nbt != null && !nbt.isEmpty()) {
            Random random = new Random();

            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                CompressedStreamTools.writeCompressed(nbt, stream);
                byte[] nbtBytes = stream.toByteArray();

                if (nbtBytes.length > 6000) {
                    player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "Warning: Container NBT exceeds 6kB, contents will be ejected!"));
                    ItemStack[] stacks1 = ItemStackUtil.readStacksFromNBT(box, 24 /* Toolbox inv size. */);
                    if (stacks1 == null)
                        return;
                    for (ItemStack itemstack : stacks1) {

                        if (itemstack != null) {
                            float f = random.nextFloat() * 0.8F + 0.1F;
                            float f1 = random.nextFloat() * 0.8F + 0.1F;
                            float f2 = random.nextFloat() * 0.8F + 0.1F;

                            while (itemstack.getCount() > 0) {
                                int j1 = random.nextInt(21) + 10;

                                if (j1 > itemstack.getCount()) {
                                    j1 = itemstack.getCount();
                                }

                                itemstack.shrink(j1);
                                EntityItem entityitem = new EntityItem(player.world, player.posX + f, player.posY + f1, player.posZ + f2, new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));

                                if (itemstack.hasTagCompound()) {
                                    entityitem.getItem().setTagCompound(itemstack.getTagCompound().copy());
                                }

                                float f3 = 0.05F;
                                entityitem.motionX = (float) random.nextGaussian() * f3 + player.motionX;
                                entityitem.motionY = (float) random.nextGaussian() * f3 + 0.2F + player.motionY;
                                entityitem.motionZ = (float) random.nextGaussian() * f3 + player.motionZ;
                                player.world.spawnEntity(entityitem);
                            }
                        }
                    }

                    box.setTagCompound(new NBTTagCompound()); // Reset.
                }
            } catch (IOException ignored) {}
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
        if (handIn == EnumHand.OFF_HAND) return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItemOffhand());
        ItemStack stack = player.getHeldItemMainhand();
        if(!world.isRemote) {
            if (!player.isSneaking()) {
                moveRows(stack, player);
                player.inventoryContainer.detectAndSendChanges();
            } else {
                if(stack.getTagCompound() == null)
                    stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setBoolean("isOpen", true);
                player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerToolBox(player.inventory, new InventoryToolBox(player, player.getHeldItemMainhand()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIToolBox(player.inventory, new InventoryToolBox(player, player.getHeldItemMainhand()));
    }

    public static class InventoryToolBox extends ItemInventory {
        public InventoryToolBox(EntityPlayer player, ItemStack box) {
            super(player, box, 24);
            this.player = player;
            this.target = box;

            if(!box.hasTagCompound())
                box.setTagCompound(new NBTTagCompound());

            ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(box, getSlots());

            if(fromNBT != null) {
                for (int i = 0; i < getSlots(); i++) {
                    fromNBT[i] = getStackInSlot(i);
                }
            }
        }

        @Override
        public void closeInventory() {
            super.closeInventory();

            if (!this.target.hasTagCompound()) {
                this.target.setTagCompound(new NBTTagCompound());
            }

            this.target.getTagCompound().setBoolean("isOpen", false);
            this.target.getTagCompound().setInteger("rand", player.world.rand.nextInt()); // a boolean changing isn't sufficient to detect the change
            player.inventoryContainer.detectAndSendChanges();
        }

        public boolean hasCustomName() {
            return this.target.hasDisplayName();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return !(stack.getItem() instanceof ItemToolBox);
        }
    }
}
