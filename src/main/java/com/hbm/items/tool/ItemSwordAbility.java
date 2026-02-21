package com.hbm.items.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.hbm.Tags;
import com.hbm.handler.ability.AvailableAbilities;
import com.hbm.handler.ability.IWeaponAbility;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemSwordAbility extends ItemSword implements IDynamicModels {

	private EnumRarity rarity = EnumRarity.COMMON;
	//was there a reason for this to be private?
	protected float damage;
	protected double attackSpeed;
	protected double movement;
	private AvailableAbilities abilities = new AvailableAbilities();
	String texturePath;

	public ItemSwordAbility(float damage, double attackSpeed, double movement, ToolMaterial material, String s) {
		super(material);
		this.damage = damage;
		this.movement = movement;
		this.attackSpeed = attackSpeed;
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.texturePath = s;
		INSTANCES.add(this);

		ModItems.ALL_ITEMS.add(this);
	}
	public ItemSwordAbility(float damage, double attackSpeed, double movement, ToolMaterial material, String s, boolean useBakedModel) {
		super(material);
		this.damage = damage;
		this.movement = movement;
		this.attackSpeed = attackSpeed;
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModItems.ALL_ITEMS.add(this);
	}

	public ItemSwordAbility(float damage, double movement, ToolMaterial material, String s, boolean useBakedModel) {
		this(damage, -2.4, movement, material, s, false);
	}

	public ItemSwordAbility(float damage, double movement, ToolMaterial material, String s) {
		this(damage, -2.4, movement, material, s);
	}

	@Override
	public void bakeModel(ModelBakeEvent event) {
		try {
			ResourceLocation templateModel = new ResourceLocation(Tags.MODID, "item/sword_template");
			IModel baseModel = ModelLoaderRegistry.getModel(templateModel);
			ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath);

			IModel retexturedModel = baseModel.retexture(
					ImmutableMap.of("layer0", spriteLoc.toString())
			);

			IBakedModel bakedModel = retexturedModel.bake(
					net.minecraftforge.common.model.TRSRTransformation.identity(),
					DefaultVertexFormats.ITEM,
					ModelLoader.defaultTextureGetter()
			);

			ModelResourceLocation bakedModelLocation = new ModelResourceLocation(spriteLoc, "inventory");
			event.getModelRegistry().putObject(bakedModelLocation, bakedModel);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void registerModel() {
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath), "inventory"));
	}

	@Override
	public void registerSprite(TextureMap map) {
		map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath));
	}

	public ItemSwordAbility addAbility(IWeaponAbility weaponAbility, int level) {
		this.abilities.addAbility(weaponAbility, level);
		return this;
	}

	// <insert obvious Rarity joke here>
	public ItemSwordAbility setRarity(EnumRarity rarity) {
		this.rarity = rarity;
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return this.rarity != EnumRarity.COMMON ? this.rarity : super.getRarity(stack);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		if(!attacker.world.isRemote && attacker instanceof EntityPlayer && canOperate(stack)) {

			//hacky hacky hack
			if(this == ModItems.mese_gavel)
				attacker.world.playSound(null, target.posX, target.posY, target.posZ, HBMSoundHandler.whack, SoundCategory.HOSTILE, 3.0F, 1.F);

			this.abilities.getWeaponAbilities().forEach((ability, level) -> {
				ability.onHit(level, attacker.world, (EntityPlayer) attacker, target, this);
			});
		}
		stack.damageItem(1, attacker);
		return super.hitEntity(stack, target, attacker);
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
		Multimap<String, AttributeModifier> map = HashMultimap.<String, AttributeModifier> create();
		if(slot == EntityEquipmentSlot.MAINHAND) {
			map.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"), "Tool modifier", movement, 1));
			map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double) this.damage, 0));
			map.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", this.attackSpeed, 0));
		}
		return map;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		abilities.addInformation(list);
	}

	protected boolean canOperate(ItemStack stack) {
		return true;
	}
}
