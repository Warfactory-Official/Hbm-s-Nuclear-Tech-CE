package com.hbm.items.tool;

import java.util.List;

import com.hbm.inventory.gui.GUIScreenPager;
import com.hbm.items.IItemControlReceiver;
import com.hbm.items.ItemBakedBase;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacketLegacy;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.tileentity.network.RTTYSystem.RTTYChannel;
import com.hbm.util.EnumUtil;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemRTTYPager extends ItemBakedBase implements IItemControlReceiver, IGUIProvider {

    public static final String KEY_CHANNEL = "chan";
    public static final int ID_PAGER_DYN = 1000;

    public ItemRTTYPager(String s) {
        super(s);
        setMaxStackSize(1);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(KEY_CHANNEL)) return;
        if (!(entity instanceof EntityPlayerMP) || world.isRemote) return;

        String channelFreq = stack.getTagCompound().getString(KEY_CHANNEL);
        RTTYChannel chan = RTTYSystem.listen(world, channelFreq);

        if (chan != null && chan.timeStamp >= world.getTotalWorldTime() - 1) {
            int alive = entity.ticksExisted % 1000;
            String message = TextFormatting.GOLD + "[ " + channelFreq + " (" + alive + ") ] " + TextFormatting.YELLOW + chan.signal;
            PacketDispatcher.wrapper.sendTo(new PlayerInformPacketLegacy(new TextComponentString(message), ID_PAGER_DYN + slot, 5_000), (EntityPlayerMP) entity);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, hand.ordinal(), -1, 0);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(KEY_CHANNEL) || stack.getTagCompound().getString(KEY_CHANNEL).isEmpty()) {
            list.add(TextFormatting.RED + "No channel set!");
        } else {
            list.add(TextFormatting.YELLOW + "Channel: " + stack.getTagCompound().getString(KEY_CHANNEL));
        }
    }

    @Override
    public void receiveControl(ItemStack stack, NBTTagCompound data) {
        if (data.hasKey(KEY_CHANNEL)) {
            if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString(KEY_CHANNEL, data.getString(KEY_CHANNEL));
        }
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        EnumHand hand = x >= 0 && x < EnumUtil.HANDS.length ? EnumUtil.HANDS[x] : EnumHand.MAIN_HAND;
        return new GUIScreenPager(player.getHeldItem(hand));
    }
}
