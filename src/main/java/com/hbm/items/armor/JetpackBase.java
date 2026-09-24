package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.ArmorUtil;
import com.hbm.main.MainRegistry;
import com.hbm.render.model.ModelJetPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

public abstract class JetpackBase extends ItemArmorMod {

    private ModelJetPack model;


    public JetpackBase(String registryName) {
        super(ArmorModHandler.plate_only, false, true, false, false, registryName);
    }

    public static int getFuel(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
            return 0;
        }

        return stack.getTagCompound().getInteger("fuel");

    }

    public static void setFuel(ItemStack stack, int i) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().setInteger("fuel", i);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("");
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(TextFormatting.GOLD + "Can be worn on its own!");
    }

    @Override
    public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {

        ItemStack jetpack = ArmorModHandler.pryMods(armor)[ArmorModHandler.plate_only];

        if (jetpack == null)
            return;

        list.add(TextFormatting.RED + "  " + stack.getDisplayName());
    }

    @Override
    public void modUpdate(EntityLivingBase entity, ItemStack armor) {

        if (!(entity instanceof EntityPlayer))
            return;

        ItemStack jetpack = ArmorModHandler.pryMods(armor)[ArmorModHandler.plate_only];

        if (jetpack == null)
            return;

        onArmorTick(entity.world, (EntityPlayer) entity, jetpack);
        ArmorUtil.resetFlightTime((EntityPlayer) entity);

        ArmorModHandler.applyMod(armor, jetpack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void modRender(Pre event, ItemStack armor) {

        ModelBiped modelJetpack = getArmorModel(event.getEntityLiving(), null, EntityEquipmentSlot.CHEST, null);

        EntityPlayer player = event.getEntityPlayer();

        ModelBiped model = event.getRenderer().getMainModel();
        modelJetpack.isSneak = model.isSneak;

        float pitch = player.rotationPitch;

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(this.getArmorTexture(armor, event.getEntity(), this.getEquipmentSlot(armor), null)));

        GlStateManager.pushMatrix();
        modelJetpack.render(player, 0.0F, 0.0F, 0.0F, 0.0F, pitch, 0.0625F);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
        return armorType == EntityEquipmentSlot.CHEST;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        if (armorSlot == EntityEquipmentSlot.CHEST) {
            if (model == null) {
                this.model = new ModelJetPack();
            }
            return this.model;
        }

        return null;
    }

    protected void useUpFuel(EntityPlayer player, ItemStack stack, int rate) {

        if (player.ticksExisted % rate == 0)
            setFuel(stack, getFuel(stack) - 1);
    }
}
