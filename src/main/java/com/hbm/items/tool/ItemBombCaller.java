package com.hbm.items.tool;

import com.hbm.Tags;
import com.hbm.entity.logic.EntityBomber;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ItemBakedBase;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.util.I18nUtil;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemBombCaller extends ItemBakedBase implements IDynamicModels {
  String texturePath;

  public ItemBombCaller(String name) {
    super(name, name);
    this.texturePath = name;
    this.setHasSubtypes(true);
  }

  public enum EnumCallerType {
    CARPET,
    NAPALM,
    POISON,
    ORANGE,
    ATOMIC,
    STINGER,
    PIP,
    CLOUD,
    NONE;

      public static final EnumCallerType[] VALUES = values();
  }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
    return enchantment != Enchantments.UNBREAKING
        && enchantment != Enchantments.MENDING
        && super.canApplyAtEnchantingTable(stack, enchantment);
  }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
    if (book.getItem() == Items.ENCHANTED_BOOK) {
      Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(book);
      if (enchantments.containsKey(Enchantments.MENDING)
          || enchantments.containsKey(Enchantments.UNBREAKING)) {
        return false;
      }
    }
    return super.isBookEnchantable(stack, book);
  }

  @Override
  public void addInformation(
      ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
    final String desc = TextFormatting.GRAY + I18nUtil.resolveKey("desc.type") + " ";
    list.add(TextFormatting.GRAY + I18nUtil.resolveKey("desc.airstrike"));

    switch (getTypeByIndex(stack.getItemDamage())) {
      case CARPET:
        list.add(desc + TextFormatting.YELLOW + I18nUtil.resolveKey("type.carpet"));
        break;
      case NAPALM:
        list.add(desc + TextFormatting.GOLD + I18nUtil.resolveKey("type.napalm"));
        break;
      case POISON:
        list.add(desc + TextFormatting.GREEN + I18nUtil.resolveKey("type.poison"));
        break;
      case ORANGE:
        list.add(desc + TextFormatting.GOLD + I18nUtil.resolveKey("type.orange"));
        break;
      case ATOMIC:
        list.add(
            desc
                + TextFormatting.DARK_RED
                + TextFormatting.BOLD
                + I18nUtil.resolveKey("type.atomic"));
        break;
      case STINGER:
        list.add(desc + TextFormatting.AQUA + I18nUtil.resolveKey("type.stinger"));
        break;
      case PIP:
        list.add(desc + TextFormatting.AQUA + I18nUtil.resolveKey("type.pip"));
        break;
      case CLOUD:
        list.add(desc + TextFormatting.AQUA + I18nUtil.resolveKey("type.cloud"));
        break;
      default:
        break;
    }
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(
      World world, EntityPlayer playerIn, EnumHand handIn) {
    RayTraceResult trace = Library.rayTrace(playerIn, 500, 1);
    ItemStack stack = playerIn.getHeldItem(handIn);
    boolean success = false;
    if (trace.typeOfHit != Type.MISS && !world.isRemote) {

      int x = trace.getBlockPos().getX();
      int y = trace.getBlockPos().getY();
      int z = trace.getBlockPos().getZ();

      switch (getTypeByIndex(stack.getItemDamage())) {
        case CARPET:
          success = world.spawnEntity(EntityBomber.statFacCarpet(world, x, y, z));
          break;
        case NAPALM:
          success = world.spawnEntity(EntityBomber.statFacNapalm(world, x, y, z));
          break;
        case POISON:
          success = world.spawnEntity(EntityBomber.statFacChlorine(world, x, y, z));
          break;
        case ORANGE:
          success = world.spawnEntity(EntityBomber.statFacOrange(world, x, y, z));
          break;
        case ATOMIC:
          success = world.spawnEntity(EntityBomber.statFacABomb(world, x, y, z));
          break;
        case STINGER:
          success = world.spawnEntity(EntityBomber.statFacStinger(world, x, y, z));
          break;
        case PIP:
          success = world.spawnEntity(EntityBomber.statFacBoxcar(world, x, y, z));
          break;
        case CLOUD:
          success = world.spawnEntity(EntityBomber.statFacPC(world, x, y, z));
          break;
        default:
          break;
      }
      if (success) {
        playerIn.sendMessage(new TextComponentTranslation("chat.callas"));
        if (!playerIn.capabilities.isCreativeMode) stack.shrink(1);
      }
      world.playSound(
          playerIn.posX,
          playerIn.posY,
          playerIn.posZ,
          HBMSoundHandler.techBoop,
          SoundCategory.PLAYERS,
          1.0F,
          1.0F,
          true);
    }
    return new ActionResult<>(
        success ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack.copy());
  }

  private static EnumCallerType getTypeByIndex(int index) {
    EnumCallerType[] values = EnumCallerType.VALUES;
    if (index < 0 || index >= values.length) {
      return EnumCallerType.NONE;
    }
    return values[index];
  }

  @Override
  public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
    if (tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH)
      for (int i = 0; i < EnumCallerType.VALUES.length - 4; i++) {
        ItemStack stack = new ItemStack(this, 1, i);
        items.add(stack);
      }
  }

  @Override
  public boolean hasEffect(ItemStack stack) {
    return stack.getItemDamage() >= 4;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerModel() {
    for (int i = 0; i < EnumCallerType.VALUES.length - 1; i++) {
      ModelLoader.setCustomModelResourceLocation(
          this,
          i,
          new ModelResourceLocation(
              new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath), "inventory"));
    }
  }
}
