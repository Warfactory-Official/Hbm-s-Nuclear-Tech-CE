package com.hbm.items.block;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.generic.BlockMetalFence;
import com.hbm.tileentity.IPersistentNBT;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemBlockBase extends ItemBlock {

    public ItemBlockBase(Block block) {
        super(block);

        if (block instanceof IBlockMulti) {
            this.setMaxDamage(0);
            this.setHasSubtypes(true);
        }
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        if (IPersistentNBT.carriesContents(stack)) return 1;
        return super.getItemStackLimit(stack);
    }

    @Override
    public int getMetadata(int meta) {
        if (this.block instanceof IBlockMulti)
            return meta;
        else
            return super.getMetadata(meta);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {

        if (this.block instanceof IBlockMulti) {
            return ((IBlockMulti) this.block).getTranslationKey(stack);
        } else if (this.block instanceof BlockMetalFence) {
            return this.block.getTranslationKey();
        } else {
            return super.getTranslationKey(stack);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        if (this.block instanceof IBlockMulti) {
            String override = ((IBlockMulti) this.block).getOverrideDisplayName(stack);
            if (override != null) {
                return override;
            }
        }
        return I18n.format(this.getTranslationKey(stack) + ".name").trim();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {

        if (this.block instanceof ITooltipProvider) {
            this.block.addInformation(stack, worldIn, list, flagIn);
        }

        if (this.block instanceof IPersistentInfoProvider && stack.hasTagCompound() && stack.getTagCompound().hasKey(IPersistentNBT.NBT_PERSISTENT_KEY)) {
            NBTTagCompound data = stack.getTagCompound().getCompoundTag(IPersistentNBT.NBT_PERSISTENT_KEY);
            EntityPlayer player = Minecraft.getMinecraft().player;
            boolean adv = flagIn.isAdvanced();
            ((IPersistentInfoProvider) this.block).addInformation(stack, data, player, list, adv);
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {

        if (this.block instanceof ITooltipProvider) {
            return ((ITooltipProvider) this.block).getRarity(stack);
        }

        return EnumRarity.COMMON;
    }
}
