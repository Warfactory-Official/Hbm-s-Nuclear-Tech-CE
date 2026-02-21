package com.hbm.items.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hbm.Tags;
import com.hbm.api.item.IDepthRockTool;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBedrockOre;
import com.hbm.blocks.generic.BlockBedrockOreTE;
import com.hbm.handler.HbmKeybinds;
import com.hbm.handler.ability.*;
import com.hbm.inventory.gui.GUIScreenToolAbility;
import com.hbm.items.IDynamicModels;
import com.hbm.items.IItemControlReceiver;
import com.hbm.items.IKeybindReceiver;
import com.hbm.items.ModItems;
import com.hbm.lib.internal.MethodHandleHelper;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacketLegacy;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.*;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemToolAbility extends ItemTool implements IDepthRockTool, IGUIProvider, IItemControlReceiver, IKeybindReceiver, IDynamicModels {

	protected boolean isShears = false;

	private EnumToolType toolType;
	private EnumRarity rarity = EnumRarity.COMMON;
	//was there a reason for this to be private?
	protected float damage;
	protected double movement;
	protected AvailableAbilities availableAbilities = new AvailableAbilities().addToolAbilities();
	protected boolean rockBreaker = false;
    
	
	public enum EnumToolType {
		
		PICKAXE(
				Sets.newHashSet(Material.IRON, Material.ANVIL, Material.ROCK),
				Sets.newHashSet(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.DOUBLE_STONE_SLAB, Blocks.GOLDEN_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE)
		),
		AXE(
				Sets.newHashSet(Material.WOOD, Material.PLANTS, Material.VINE),
				Sets.newHashSet(Blocks.PLANKS, Blocks.BOOKSHELF, Blocks.LOG, Blocks.LOG2, Blocks.CHEST, Blocks.PUMPKIN, Blocks.LIT_PUMPKIN, Blocks.MELON_BLOCK, Blocks.LADDER, Blocks.WOODEN_BUTTON, Blocks.WOODEN_PRESSURE_PLATE)
		),
		SHOVEL(
				Sets.newHashSet(Material.CLAY, Material.SAND, Material.GROUND, Material.SNOW, Material.CRAFTED_SNOW),
				Sets.newHashSet(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SNOW_LAYER, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.CONCRETE_POWDER)
		),
		MINER(
				Sets.newHashSet(Material.GRASS, Material.IRON, Material.ANVIL, Material.ROCK, Material.CLAY, Material.SAND, Material.GROUND, Material.SNOW, Material.CRAFTED_SNOW)
		);
		
		private EnumToolType(Set<Material> materials) {
			this.materials = materials;
		}
		
		private EnumToolType(Set<Material> materials, Set<Block> blocks) {
			this.materials = materials;
			this.blocks = blocks;
		}

		public Set<Material> materials = new HashSet<Material>();
		public Set<Block> blocks = new HashSet<Block>();
	}

	public ItemToolAbility setShears() {
		this.isShears = true;
		return this;
	}

	String texturePath;
	public ItemToolAbility(float damage, float attackSpeedIn, double movement, ToolMaterial material, EnumToolType type, String s) {
		super(0, attackSpeedIn, material, type.blocks);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.damage = damage;
		this.movement = movement;
		this.toolType = type;
		if(type == EnumToolType.MINER){
			this.setHarvestLevel("shovel", material.getHarvestLevel());
			this.setHarvestLevel("pickaxe", material.getHarvestLevel());
		} else {
			this.setHarvestLevel(type.toString().toLowerCase(), material.getHarvestLevel());
		}
		this.texturePath = s;
		INSTANCES.add(this);
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public void bakeModel(ModelBakeEvent event) {
		try {
			IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/handheld"));
			ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath);
			IModel retexturedModel = baseModel.retexture(
					ImmutableMap.of(
							"layer0", spriteLoc.toString()
					)

			);
			IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
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

	public ItemToolAbility addAbility(IBaseAbility ability, int level) {
		this.availableAbilities.addAbility(ability, level);

		return this;
	}

	public ItemToolAbility setDepthRockBreaker() {
		this.rockBreaker = true;
		return this;
	}
	
	//<insert obvious Rarity joke here>
	//Drillgon200: What?
	public ItemToolAbility setRarity(EnumRarity rarity) {
		this.rarity = rarity;
		return this;
	}
	
	//Drillgon200: Dang it bob, override annotations matter!
	@SuppressWarnings("deprecation")
	@Override
    public EnumRarity getRarity(ItemStack stack) {
        return this.rarity != EnumRarity.COMMON ? this.rarity : super.getRarity(stack);
    }
	
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		if(!attacker.world.isRemote && attacker instanceof EntityPlayer && canOperate(stack)) {

			this.availableAbilities.getWeaponAbilities().forEach((ability, level) -> {
				ability.onHit(level, attacker.world, (EntityPlayer) attacker, target, this);
			});
    	}
		stack.damageItem(2, attacker);
        return true;
	}

	// Should be safe, considering the AoE ability does a similar trick already.
	// If not, wrap this in a ThreadLocal or something...
	public static int dropX, dropY, dropZ;
	
	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
		World world = player.world;
    	IBlockState state = world.getBlockState(pos);
    	Block block = state.getBlock();

		/*
		 * The original implementation of this always returned FALSE which uses the vanilla block break code.
		 * This one now returns TRUE when an ability applies and instead relies on breakExtraBlock, which has the minor
		 * issue of only running on the sever, while the client uses the vanilla implementation. breakExtraBlock was only
		 * meant to be used for AoE or vein miner and not for the block that's being mined, hence break EXTRA block.
		 * The consequence was that the server would fail to break keyholes since breakExtraBlock is supposed to exclude
		 * them, while the client happily removes the block, causing a desync.
		 *
		 * Since keyholes aren't processable and exempt from silk touch anyway, we just default to the vanilla implementation in every case.
		 */
		if(block == ModBlocks.stone_keyhole || block == ModBlocks.stone_keyhole_meta) return false;

		if(!world.isRemote && (canHarvestBlock(state, stack) || canShearBlock(block, stack, world, pos.getX(), pos.getY(), pos.getZ())) && canOperate(stack)) {
			Configuration config = getConfiguration(stack);
			ToolPreset preset = config.getActivePreset();

			dropX = pos.getX();
			dropY = pos.getY();
			dropZ = pos.getZ();

			preset.harvestAbility.preHarvestAll(preset.harvestAbilityLevel, world, player);

			boolean skipRef = preset.areaAbility.onDig(preset.areaAbilityLevel, world, pos, player, this);

			if(!skipRef) {
				breakExtraBlock(world, pos.getX(), pos.getY(), pos.getZ(), player, pos.getX(), pos.getY(), pos.getZ());
			}

			preset.harvestAbility.postHarvestAll(preset.harvestAbilityLevel, world, player);

			return true;
		}
    	
    	return false;
	}

	public boolean canOperate(ItemStack stack) {
		return true;
	}
	
	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state) {
		if(!canOperate(stack))
    		return 1;
    	
    	if(toolType == null)
            return super.getDestroySpeed(stack, state);
    	
    	if(toolType.blocks.contains(state.getBlock()) || toolType.materials.contains(state.getMaterial()))
    		return this.efficiency;
    	
        return super.getDestroySpeed(stack, state);
	}
	
	@Override
	public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
		if(!canOperate(stack)) return false;

		if(isForbiddenBlock(state.getBlock())) return false;

		if(this.getConfiguration(stack).getActivePreset().harvestAbility == IToolHarvestAbility.SILK)
			return true;
		
    	return getDestroySpeed(stack, state) > 1;
	}

	@Override
	public boolean canBreakRock(World world, EntityPlayer player, ItemStack tool, IBlockState block, BlockPos pos){
		return canOperate(tool) && this.rockBreaker;
	}

	public boolean canShearBlock(Block block, ItemStack stack, World world, int x, int y, int z) {
		return this.isShears(stack) && block instanceof IShearable && ((IShearable) block).isShearable(stack, world, new BlockPos(x, y, z));
	}

	public boolean isShears(ItemStack stack) {
		return this.isShears;
	}

	public static boolean isForbiddenBlock(Block b){
		return (b == Blocks.BARRIER || b == Blocks.BEDROCK || b == Blocks.COMMAND_BLOCK || b == Blocks.CHAIN_COMMAND_BLOCK || b == Blocks.REPEATING_COMMAND_BLOCK || b == ModBlocks.ore_bedrock_oil || b instanceof BlockBedrockOre || b instanceof BlockBedrockOreTE );
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
		Multimap<String, AttributeModifier> map = HashMultimap.<String, AttributeModifier>create();
		if(slot == EntityEquipmentSlot.MAINHAND){
			map.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"), "Tool modifier", movement, 1));
			map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", this.damage, 0));
			map.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", this.attackSpeed, 0));
		}
        return map;
	}

	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return stack.isItemEnchanted() || !getConfiguration(stack).getActivePreset().isNone();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag advanced) {

		availableAbilities.addInformation(list);

		if(this.rockBreaker) {
			list.add("");
			list.add(TextFormatting.RED + "Can break depth rock!");
		}
	}

	public void breakExtraBlock(World world, int x, int y, int z, EntityPlayer playerEntity, int refX, int refY, int refZ) {

		BlockPos pos = new BlockPos(x, y, z);
		if (world.isAirBlock(pos))
			return;

		if (!(playerEntity instanceof EntityPlayerMP))
			return;

		EntityPlayerMP player = (EntityPlayerMP) playerEntity;
		ItemStack stack = player.getHeldItemMainhand();

		if (stack.isEmpty()) {
			return;
		}

		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);

		if (!(canHarvestBlock(state, stack) || canShearBlock(block, stack, world, x, y, z)) || (state.getBlockHardness(world, pos) == -1.0F && state.getPlayerRelativeBlockHardness(player, world, pos) == 0.0F)
				|| block == ModBlocks.stone_keyhole)
			return;

		BlockPos refPos = new BlockPos(refX, refY, refZ);
		IBlockState refState = world.getBlockState(refPos);

		float refStrength = refState.getPlayerRelativeBlockHardness(player, world, refPos);
		float strength = state.getPlayerRelativeBlockHardness(player, world, pos);

		if (!ForgeHooks.canHarvestBlock(state.getBlock(), player, world, pos) || strength <= 0.0F || refStrength / strength > 10f || refState.getPlayerRelativeBlockHardness(player, world, refPos) < 0
		)
			return;

		int exp = ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, pos);
		if (exp == -1)
			return;

		Configuration config = getConfiguration(stack);
		ToolPreset preset = config.getActivePreset();

		preset.harvestAbility.onHarvestBlock(preset.harvestAbilityLevel, world, x, y, z, player, block, meta);
	}

	/** Assumes a canShearBlock check has passed, will most likely crash otherwise! */
	// welp, it's not used for now anyway..
	/* public static void shearBlock(World world, int x, int y, int z, Block block, EntityPlayer player) {

		ItemStack held = player.getHeldItem();

		IShearable target = (IShearable) block;
		if(target.isShearable(held, player.worldObj, x, y, z)) {
			ArrayList<ItemStack> drops = target.onSheared(held, player.worldObj, x, y, z, EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, held));
			Random rand = new Random();

			for(ItemStack stack : drops) {
				float f = 0.7F;
				double d = (double) (rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
				double d1 = (double) (rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
				double d2 = (double) (rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
				EntityItem entityitem = new EntityItem(player.worldObj, (double) dropX + d, (double) dropY + d1, (double) dropZ + d2, stack);
				entityitem.delayBeforeCanPickup = 10;
				player.worldObj.spawnEntityInWorld(entityitem);
			}

			held.damageItem(1, player);
			player.addStat(StatList.mineBlockStatArray[Block.getIdFromBlock(block)], 1);
		}
	} */

	private static final MethodHandle blockCaptureDrops = MethodHandleHelper.findVirtual(Block.class, "captureDrops", MethodType.methodType(NonNullList.class, boolean.class));

	public static void standardDigPost(World world, int x, int y, int z, EntityPlayerMP player) {

		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		int l = block.getMetaFromState(state);

		world.playEvent(player, 2001, pos, Block.getStateId(state));

		boolean removedByPlayer = false;

		if (player.capabilities.isCreativeMode) {
			removedByPlayer = removeBlock(world, x, y, z, false, player);
			player.connection.sendPacket(new SPacketBlockChange(world, pos));
		} else {
			ItemStack itemstack = player.getHeldItemMainhand();
			boolean canHarvest = ForgeHooks.canHarvestBlock(block, player, world, pos);

			removedByPlayer = removeBlock(world, x, y, z, canHarvest, player);

			if (!itemstack.isEmpty()) {
				itemstack.onBlockDestroyed(world, state, pos, player);

				if (itemstack.getCount() == 0) {
					player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
				}
			}

			if (removedByPlayer && canHarvest) {
				try {
                    //noinspection unchecked
					NonNullList<ItemStack> ignored = (NonNullList<ItemStack>) blockCaptureDrops.invokeExact(block, true);
					block.harvestBlock(world, player, pos, state, world.getTileEntity(pos), itemstack);
					//noinspection unchecked
					List<ItemStack> drops = (NonNullList<ItemStack>) blockCaptureDrops.invokeExact(block, false);
					for (ItemStack stack : drops) {
						Block.spawnAsEntity(world, new BlockPos(dropX, dropY, dropZ), stack);
					}
				} catch (Throwable e) {
                    MainRegistry.logger.error("Failed to capture drops for block {}", block, e);
                    throw new RuntimeException(e);
                }
            }
		}
	}

	public static boolean removeBlock(World world, int x, int y, int z, boolean canHarvest, EntityPlayerMP player) {
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		block.onBlockHarvested(world, pos, state, player);
		boolean flag = block.removedByPlayer(state, world, pos, player, canHarvest);

		if (flag) {
			block.onPlayerDestroy(world, pos, state);
		}

		return flag;
	}

	public static class Configuration {
		public List<ToolPreset> presets;
		public int currentPreset;

		public Configuration() {
			this.presets = null;
			this.currentPreset = 0;
		}

		public Configuration(List<ToolPreset> presets, int currentPreset) {
			this.presets = presets;
			this.currentPreset = currentPreset;
		}

		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setInteger("ability", currentPreset);

			NBTTagList nbtPresets = new NBTTagList();

			for(ToolPreset preset : presets) {
				NBTTagCompound nbtPreset = new NBTTagCompound();
				preset.writeToNBT(nbtPreset);
				nbtPresets.appendTag(nbtPreset);
			}

			nbt.setTag("abilityPresets", nbtPresets);
		}

		public void readFromNBT(NBTTagCompound nbt) {
			currentPreset = nbt.getInteger("ability");

			NBTTagList nbtPresets = nbt.getTagList("abilityPresets", 10);
			int numPresets = Math.min(nbtPresets.tagCount(), 99);

			presets = new ArrayList<ToolPreset>(numPresets);

			for(int i = 0; i < numPresets; i++) {
				NBTTagCompound nbtPreset = nbtPresets.getCompoundTagAt(i);
				ToolPreset preset = new ToolPreset();
				preset.readFromNBT(nbtPreset);
				presets.add(preset);
			}

			currentPreset = Math.max(0, Math.min(currentPreset, presets.size() - 1));
		}

		public void reset(AvailableAbilities availableAbilities) {
			currentPreset = 0;

			presets = new ArrayList<ToolPreset>(availableAbilities.size());
			presets.add(new ToolPreset());

			availableAbilities.getToolAreaAbilities().forEach((ability, level) -> {
				if (ability == IToolAreaAbility.NONE)
					return;
				presets.add(new ToolPreset(ability, level, IToolHarvestAbility.NONE, 0));
			});

			availableAbilities.getToolHarvestAbilities().forEach((ability, level) -> {
				if (ability == IToolHarvestAbility.NONE)
					return;
				presets.add(new ToolPreset(IToolAreaAbility.NONE, 0, ability, level));
			});

			presets.sort(
					Comparator
							.comparing((ToolPreset p) -> p.harvestAbility)
							.thenComparingInt(p -> p.harvestAbilityLevel)
							.thenComparing(p -> p.areaAbility)
							.thenComparingInt(p -> p.areaAbilityLevel)
			);
		}

		public void restrictTo(AvailableAbilities availableAbilities) {
			for (ToolPreset preset : presets) {
				preset.restrictTo(availableAbilities);
			}
		}

		public ToolPreset getActivePreset() {
			return presets.get(currentPreset);
		}
	}

	public Configuration getConfiguration(ItemStack stack) {
		Configuration config = new Configuration();

		if(stack == null || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("ability") || !stack.getTagCompound().hasKey("abilityPresets")) {
			config.reset(availableAbilities);
			return config;
		}

		config.readFromNBT(stack.getTagCompound());
		config.restrictTo(availableAbilities);
		return config;
	}

	public void setConfiguration(ItemStack stack, Configuration config) {
		if (stack == null) {
			return;
		}

		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}

		config.writeToNBT(stack.getTagCompound());
	}

	@Override
	public void receiveControl(ItemStack stack, NBTTagCompound data) {
		Configuration config = new Configuration();
		config.readFromNBT(data);
		config.restrictTo(availableAbilities);
		setConfiguration(stack, config);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIScreenToolAbility(this.availableAbilities);
	}

	@Override
	public boolean canHandleKeybind(EntityPlayer player, ItemStack stack, HbmKeybinds.EnumKeybind keybind) {
		if(player.world.isRemote) return keybind == HbmKeybinds.EnumKeybind.ABILITY_ALT;
		return keybind == HbmKeybinds.EnumKeybind.ABILITY_CYCLE;
	}

	@Override
	public void handleKeybind(EntityPlayer player, ItemStack stack, HbmKeybinds.EnumKeybind keybind, boolean state) {

		if(keybind == HbmKeybinds.EnumKeybind.ABILITY_CYCLE && state) {

			World world = player.world;
			if(!canOperate(stack)) return;

			Configuration config = getConfiguration(stack);
			if(config.presets.size() < 2 || world.isRemote) return;

			if(player.isSneaking()) {
				config.currentPreset = 0;
			} else {
				config.currentPreset = (config.currentPreset + 1) % config.presets.size();
			}

			setConfiguration(stack, config);
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacketLegacy(config.getActivePreset().getMessage(), 11), (EntityPlayerMP) player);
			world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.25F, config.getActivePreset().isNone() ? 0.75F : 1.25F);
		}
	}

	@Override
	public void handleKeybindClient(EntityPlayer player, ItemStack stack, HbmKeybinds.EnumKeybind keybind, boolean state) {
		if(state) player.openGui(MainRegistry.instance, 0, player.world, 0, 0, 0);
	}
}
