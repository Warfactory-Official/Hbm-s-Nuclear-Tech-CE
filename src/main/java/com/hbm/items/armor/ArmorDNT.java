package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmCapability.IHBMData;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.ResourceManager;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorDNT;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.render.util.ViewModelPositonDebugger;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ArmorDNT extends ArmorFSBPowered implements IItemRendererProvider {
    @SideOnly(Side.CLIENT)
    ModelArmorDNT[] models;

    @SideOnly(Side.CLIENT)
    protected ViewModelPositonDebugger offsets;

	public ArmorDNT(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, long maxPower, long chargeRate, long consumption, long drain, String s) {
		super(material, layer, slot, texture, maxPower, chargeRate, consumption, drain, s);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(@NotNull EntityLivingBase entityLiving, @NotNull ItemStack itemStack, @NotNull EntityEquipmentSlot armorSlot, @NotNull ModelBiped _default){
		if(models == null) {
			models = new ModelArmorDNT[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorDNT(i);
		}

		return models[armorSlot.getIndex()];
	}
	
	private static final UUID speed = UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

		super.onArmorTick(world, player, stack);
		
		if(this != ModItems.dns_plate)
			return;

		IHBMData props = HbmCapability.getData(player);
		
		// SPEED //
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(EntityEquipmentSlot.CHEST, stack);
		multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(speed, "DNT SPEED", 0.25, 0));
		player.getAttributeMap().removeAttributeModifiers(multimap);
		
		if(player.isSprinting()) {
			player.getAttributeMap().applyAttributeModifiers(multimap);
		}

        if(!world.isRemote) {
            // JET //
            if(hasFSBArmor(player) && (props.isJetpackActive() || (!player.onGround && !player.isSneaking() && props.getEnableBackpack()))) {
                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "jetpack_dns");
                data.setInteger("player", player.getEntityId());
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, player.posX, player.posY, player.posZ), new NetworkRegistry.TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 100));
            }
        }

		if(hasFSBArmor(player)) {
			
			ArmorUtil.resetFlightTime(player);

			if(props.isJetpackActive()) {

				if(player.motionY < 0.6D)
					player.motionY += 0.2D;

				player.fallDistance = 0;

				if(world.getTotalWorldTime() % 4 == 0)
					world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.immolatorShoot, SoundCategory.PLAYERS, 0.125F, 1.5F);

			} else if(!player.isSneaking() && !player.onGround && props.getEnableBackpack()) {
				player.fallDistance = 0;
				
				if(player.motionY < -1)
					player.motionY += 0.4D;
				else if(player.motionY < -0.1)
					player.motionY += 0.2D;
				else if(player.motionY < 0)
					player.motionY = 0;

				player.motionX *= 1.05D;
				player.motionZ *= 1.05D;
				
				if(player.moveForward != 0) {
					player.motionX += player.getLookVec().x * 0.25 * player.moveForward;
					player.motionZ += player.getLookVec().z * 0.25 * player.moveForward;
				}
				if(world.getTotalWorldTime() % 4 == 0)
					world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.immolatorShoot, SoundCategory.PLAYERS, 0.125F, 1.5F);
			}
			
			if(player.isSneaking() && !player.onGround) {
				player.motionY -= 0.1D;
			}
		}
	}
	
	@Override
	public void handleAttack(LivingAttackEvent event) {
		EntityLivingBase e = event.getEntityLiving();
		if(e instanceof EntityPlayer player && ArmorFSB.hasFSBArmor(player)) {
			if(event.getSource().isExplosion()) return;
			HbmCapability.plink(player, SoundEvents.ENTITY_ITEM_BREAK, 5F, 1.0F + e.getRNG().nextFloat() * 0.5F);
			event.setCanceled(true);
		}
	}
	
	@Override
	public void handleHurt(LivingHurtEvent event) {

		EntityLivingBase e = event.getEntityLiving();

		if(e instanceof EntityPlayer player && hasFSBArmor(player)) {
			if(event.getSource().isExplosion()) {
				event.setAmount(event.getAmount()*0.001F);
				return;
			}
			event.setAmount(0);
		}
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        list.add("Charge: " + BobMathUtil.getShortNumber(getCharge(stack)) + " / " + BobMathUtil.getShortNumber(this.getMaxCharge(stack)));

        list.add(ChatFormatting.GOLD + I18nUtil.resolveKey("armor.fullSetBonus"));

        if(!effects.isEmpty()) {
            for(PotionEffect effect : effects) {
                list.add(ChatFormatting.AQUA + "  " + I18nUtil.resolveKey(effect.getEffectName()));
            }
        }

        list.add(ChatFormatting.RED + "  " + I18nUtil.resolveKey("armor.vats"));
        list.add(ChatFormatting.RED + "  " + I18nUtil.resolveKey("armor.thermal"));
        list.add(ChatFormatting.RED + "  " + I18nUtil.resolveKey("armor.hardLanding"));
        list.add(ChatFormatting.AQUA + "  " + I18nUtil.resolveKey("armor.rocketBoots"));
        list.add(ChatFormatting.AQUA + "  " + I18nUtil.resolveKey("armor.fastFall"));
        list.add(ChatFormatting.AQUA + "  " + I18nUtil.resolveKey("armor.sprintBoost"));
	}

    @Override
    public Item getItemForRenderer() {
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase( ) {
            public void renderInventory() {
                if(armorType == EntityEquipmentSlot.MAINHAND) {
                    GlStateManager.translate(0, -1, 0);
                }

                setupRenderInv();
            }
            public void renderNonInv() {
                setupRenderNonInv();
            }

            public void renderCommon() {
                if (offsets == null)
                    offsets = new ViewModelPositonDebugger()
                            .get(ItemCameraTransforms.TransformType.GUI)
                            .setScale(1.0F).setPosition(-1.2, 0.0, 1.0).setRotation(255, -36, -143)
                            .getHelper()
                            .get(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
                            .setPosition(-1.00, -31.30, -4.95).setRotation(-23, -139, 85)
                            .getHelper()
                            .get(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND)
                            .setPosition(-0.5, 3, -2.75).setRotation(610, -115, -100)
                            .getHelper()
                            .get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
                            .setScale(0.7F).setPosition(-0.25, -3.6, -1.25).setRotation(5, -90, 340)
                            .getHelper()
                            .get(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND)
                            .setPosition(-8, -5.50, -1.00).setRotation(0, 330, 180)
                            .getHelper()
                            .get(ItemCameraTransforms.TransformType.GROUND)
                            .setScale(1F).setPosition(0, 1, 0).setRotation(0, 0, 180)
                            .getHelper();

                offsets.apply(type);

                renderStandard(ResourceManager.armor_dnt, armorType, ResourceManager.dnt_helmet, ResourceManager.dnt_chest, ResourceManager.dnt_arm, ResourceManager.dnt_leg, "Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
            }};
    }
}