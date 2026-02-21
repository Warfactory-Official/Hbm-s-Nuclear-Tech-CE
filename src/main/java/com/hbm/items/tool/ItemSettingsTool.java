package com.hbm.items.tool;

import com.hbm.capability.HbmCapability;
import com.hbm.handler.HbmKeybinds;
import com.hbm.interfaces.ICopiable;
import com.hbm.items.ItemBakedBase;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacketLegacy;
import com.hbm.util.ChatBuilder;
import com.hbm.util.Either;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemSettingsTool extends ItemBakedBase {

    public ItemSettingsTool(String s) {
        super(s);
    }


    //Causes small visual bug when switching to form one settings tool to another, but thats so minor that I don't see this as worth fixing
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (slotChanged) return true;
        return oldStack.getItem() != newStack.getItem();
    }
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (!(entity instanceof EntityPlayerMP playerMP)) {
            return;
        }

        ItemStack held = playerMP.getHeldItemMainhand();

        if (!held.isEmpty() && held == stack && stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) {
                return;
            }

            int delay = tag.getInteger("inputDelay") + 1;
            NBTTagList displayInfo = tag.getTagList("displayInfo", Constants.NBT.TAG_COMPOUND);

            HbmCapability.IHBMData props = HbmCapability.getData(playerMP);
            if (props.getKeyPressed(HbmKeybinds.EnumKeybind.TOOL_ALT) && delay > 4) {
                int index = tag.getInteger("copyIndex") + 1;
                if (index > displayInfo.tagCount() - 1) {
                    index = 0;
                }
                tag.setInteger("copyIndex", index);
                delay = 0;
            }

            tag.setInteger("inputDelay", delay);
            if (world.getTotalWorldTime() % 5 != 0) {
                return;
            }

            if (displayInfo.tagCount() > 0) {
                int copyIndex = tag.getInteger("copyIndex");
                for (int j = 0; j < displayInfo.tagCount(); j++) {
                    NBTTagCompound infoTag = displayInfo.getCompoundTagAt(j);
                    TextFormatting format = copyIndex == j ? TextFormatting.AQUA : TextFormatting.YELLOW;
                    PacketDispatcher.wrapper.sendTo(
                            new PlayerInformPacketLegacy(
                                    ChatBuilder.startTranslation(infoTag.getString("info"))
                                            .color(format)
                                            .flush(),
                                    897 + j,
                                    4000
                            ),
                            playerMP
                    );
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Can copy the settings (filters, fluid ID, etc) of machines");
        tooltip.add("Shift right-click to copy, right click to paste");
        tooltip.add("Ctrl click on pipes to paste settings to multiple pipes");
        if (stack.hasTagCompound()) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null) {
                if (nbt.hasKey("tileName")) {
                    tooltip.add(TextFormatting.BLUE + I18nUtil.resolveKey(nbt.getString("tileName") + ".name"));
                } else {
                    tooltip.add(TextFormatting.RED + " None ");
                }
            }
        }
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        Either<TileEntity, Block> schrodinger = getCopyInfoSource(world, pos);
        if (schrodinger == null) {
            return EnumActionResult.PASS;
        }

        ICopiable copiable = schrodinger.cast();
        ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            NBTTagCompound settings = copiable.getSettings(world, pos.getX(), pos.getY(), pos.getZ());
            stack.setTagCompound(settings);

            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null) {
                tag.setString("tileName", copiable.getSettingsSourceID(schrodinger));
                tag.setInteger("copyIndex", 0);
                tag.setInteger("inputDelay", 0);

                String[] info = copiable.infoForDisplay(world, pos.getX(), pos.getY(), pos.getZ());
                if (info != null) {
                    NBTTagList displayInfo = new NBTTagList();
                    for (String str : info) {
                        NBTTagCompound infoTag = new NBTTagCompound();
                        infoTag.setString("info", str);
                        displayInfo.appendTag(infoTag);
                    }
                    tag.setTag("displayInfo", displayInfo);
                }

                if (world.isRemote) {
                    player.sendMessage(
                            ChatBuilder.start("[")
                                    .color(TextFormatting.DARK_AQUA)
                                    .nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
                                    .next("] ").color(TextFormatting.DARK_AQUA)
                                    .next("Copied settings of " + copiable.getSettingsSourceDisplay(schrodinger)).color(TextFormatting.AQUA)
                                    .flush()
                    );
                }
            } else if (!world.isRemote) {
                player.sendMessage(
                        ChatBuilder.start("[")
                                .color(TextFormatting.DARK_AQUA)
                                .nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
                                .next("] ").color(TextFormatting.DARK_AQUA)
                                .next("Copy failed, machine has no settings tool support: " + copiable.getSettingsSourceDisplay(schrodinger)).color(TextFormatting.RED)
                                .flush()
                );
            }
        } else if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null) {
                int index = tag.getInteger("copyIndex");
                copiable.pasteSettings(tag, index, world, player, pos.getX(), pos.getY(), pos.getZ());
            }
        }

        return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
    }

    @Nullable
    private Either<TileEntity, Block> getCopyInfoSource(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ICopiable) {
            return Either.left(te);
        }

        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof ICopiable) {
            return Either.right(block);
        }

        return null;
    }
}
