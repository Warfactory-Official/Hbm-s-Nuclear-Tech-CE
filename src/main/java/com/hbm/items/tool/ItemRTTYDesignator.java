package com.hbm.items.tool;

import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.inventory.gui.GUIRTTYDesignator;
import com.hbm.items.IItemControlReceiver;
import com.hbm.items.ItemBakedBase;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacketLegacy;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.util.EnumUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRTTYDesignator extends ItemBakedBase implements IItemControlReceiver, IGUIProvider {

    public static final String KEY_CHANNEL = "chan";
    public static final double RANGE = 300D;
    //same limit as the pager and the radio torches
    public static final int MAX_CHAN_LENGTH = 10;
    public static final int ID_DESIGNATOR_INFORM = 1100;
    public static final int COOLDOWN_TICKS = 10;

    public ItemRTTYDesignator(String s) {
        super(s);
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            if (world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, hand.ordinal(), -1, 0);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        if (world.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, stack);

        String chan = getChannel(stack);
        if (chan.isEmpty()) return new ActionResult<>(EnumActionResult.FAIL, stack);

        RayTraceResult hit = Library.rayTrace(player, RANGE, 1F);
        //a miss must not broadcast the last air block of the ray
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) return new ActionResult<>(EnumActionResult.FAIL, stack);

        //the block in front of the face that was hit
        BlockPos target = hit.getBlockPos().offset(hit.sideHit);
        String coords = target.getX() + IRORInteractive.PARAM_SEPARATOR + target.getY() + IRORInteractive.PARAM_SEPARATOR + target.getZ();
        RTTYSystem.broadcast(world, chan, coords);

        player.getCooldownTracker().setCooldown(this, COOLDOWN_TICKS);
        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.PLAYERS, 1.0F, 1.0F);
        if (player instanceof EntityPlayerMP) {
            String message = TextFormatting.GOLD + "[ " + chan + " ] " + TextFormatting.YELLOW + "Designated " + target.getX() + " " + target.getY() + " " + target.getZ();
            PacketDispatcher.wrapper.sendTo(new PlayerInformPacketLegacy(new TextComponentString(message), ID_DESIGNATOR_INFORM, 3_000), (EntityPlayerMP) player);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static String getChannel(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString(KEY_CHANNEL) : "";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        String chan = getChannel(stack);
        if (chan.isEmpty()) {
            list.add(TextFormatting.RED + "No channel set!");
        } else {
            list.add(TextFormatting.YELLOW + "Channel: " + chan);
        }
    }

    @Override
    public void receiveControl(ItemStack stack, NBTTagCompound data) {
        if (!data.hasKey(KEY_CHANNEL)) return;
        String chan = data.getString(KEY_CHANNEL);
        //don't trust the length from the client
        if (chan.length() > MAX_CHAN_LENGTH) chan = chan.substring(0, MAX_CHAN_LENGTH);
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString(KEY_CHANNEL, chan);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        EnumHand hand = x >= 0 && x < EnumUtil.HANDS.length ? EnumUtil.HANDS[x] : EnumHand.MAIN_HAND;
        return new GUIRTTYDesignator(player.getHeldItem(hand));
    }
}
