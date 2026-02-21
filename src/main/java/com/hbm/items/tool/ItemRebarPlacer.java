package com.hbm.items.tool;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockConcreteColoredExt.EnumConcreteType;
import com.hbm.blocks.generic.BlockRebar.TileEntityRebar;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.container.ContainerRebar;
import com.hbm.inventory.gui.GUIRebar;
import com.hbm.items.ItemBakedBase;
import com.hbm.items.ItemInventory;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.ChatBuilder;
import com.hbm.util.InventoryUtil;
import com.hbm.util.ItemStackUtil;
import com.hbm.util.Tuple.Pair;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import java.util.ArrayList;
import java.util.List;

public class ItemRebarPlacer extends ItemBakedBase implements IGUIProvider {

    public static final List<Pair<Block, Integer>> ACCEPTABLE_CONK = new ArrayList<>();

    public ItemRebarPlacer(String s) {
        super(s);
        setMaxStackSize(1);

        if (ACCEPTABLE_CONK.isEmpty()) {
            ACCEPTABLE_CONK.add(new Pair<>(ModBlocks.concrete, 0));
            ACCEPTABLE_CONK.add(new Pair<>(ModBlocks.concrete_rebar, 0));
            ACCEPTABLE_CONK.add(new Pair<>(ModBlocks.concrete_smooth, 0));
            ACCEPTABLE_CONK.add(new Pair<>(ModBlocks.concrete_pillar, 0));

            for (int i = 0; i < 16; i++) {
                ACCEPTABLE_CONK.add(new Pair<>(ModBlocks.concrete_colored, i));
            }
            for (int i = 0; i < EnumConcreteType.VALUES.length; i++) {
                ACCEPTABLE_CONK.add(new Pair<>(ModBlocks.concrete_colored_ext, i));
            }
        }
    }

    public static boolean isValidConk(Item item, int meta) {
        for (Pair<Block, Integer> conk : ACCEPTABLE_CONK) {
            if (item == Item.getItemFromBlock(conk.getKey()) && meta == conk.getValue()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        if (!stack.hasTagCompound()) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("pos")) {
            return;
        }

        ItemStack[] stored = ItemStackUtil.readStacksFromNBT(stack, 1);
        ItemStack theConk = stored != null && stored.length > 0 ? stored[0] : ItemStack.EMPTY;

        boolean held = isSelected;
        if (entity instanceof EntityPlayer player) {
            held = player.getHeldItemMainhand() == stack || player.getHeldItemOffhand() == stack;
        }

        if (!held || theConk.isEmpty() || !isValidConk(theConk.getItem(), theConk.getMetadata())) {
            tag.removeTag("pos");
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        // mlbv: maybe we should add off-hand support? currently our GuiHandler only support main hand
        if (handIn == EnumHand.OFF_HAND) return new ActionResult<>(EnumActionResult.PASS, stack);
        if (!worldIn.isRemote) playerIn.openGui(MainRegistry.instance, 0, worldIn, 0, 0, 0);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        ItemStack stack = player.getHeldItem(hand);
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
            ItemStackUtil.addStacksToNBT(stack, new ItemStack(ModBlocks.concrete_rebar));
        }

        ItemStack[] stored = ItemStackUtil.readStacksFromNBT(stack, 1);
        ItemStack theConk = stored != null && stored.length > 0 ? stored[0] : ItemStack.EMPTY;

        boolean hasConk = !theConk.isEmpty() && isValidConk(theConk.getItem(), theConk.getMetadata());

        if (!hasConk) {
            player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
                    .nextTranslation(getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
                    .next("] ").color(TextFormatting.DARK_AQUA)
                    .next("No valid concrete type set!").color(TextFormatting.RED).flush());
            return EnumActionResult.SUCCESS;
        }

        BlockPos placePos = pos.offset(facing);
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return EnumActionResult.SUCCESS;
        }

        if (!tag.hasKey("pos")) {
            tag.setIntArray("pos", new int[]{placePos.getX(), placePos.getY(), placePos.getZ()});
            return EnumActionResult.SUCCESS;
        }

        int rebarLeft;
        if (player.isCreative()) { // mlbv: added a creative check here, kinda weird why 1.7 doesn't have it
            rebarLeft = Integer.MAX_VALUE;
        } else {
            rebarLeft = InventoryUtil.countAStackMatches(player, new ComparableStack(ModBlocks.rebar), true);
            if (rebarLeft <= 0) {
                player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
                        .nextTranslation(getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
                        .next("] ").color(TextFormatting.DARK_AQUA)
                        .next("Out of rebar!").color(TextFormatting.RED).flush());
                tag.removeTag("pos");
                return EnumActionResult.SUCCESS;
            }
        }

        int[] cached = tag.getIntArray("pos");
        int iX = placePos.getX();
        int iY = placePos.getY();
        int iZ = placePos.getZ();

        int minX = Math.min(cached[0], iX);
        int maxX = Math.max(cached[0], iX);
        int minY = Math.min(cached[1], iY);
        int maxY = Math.max(cached[1], iY);
        int minZ = Math.min(cached[2], iZ);
        int maxZ = Math.max(cached[2], iZ);

        int rebarUsed = 0;
        BlockPos.MutableBlockPos place = new BlockPos.MutableBlockPos();
        outer:
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    if (rebarLeft <= 0) {
                        break outer;
                    }

                    place.setPos(x, y, z);
                    if (world.getBlockState(place).getBlock().isReplaceable(world, place) && player.canPlayerEdit(place, facing, stack)) {
                        world.setBlockState(place, ModBlocks.rebar.getDefaultState(), 3);
                        TileEntity tile = world.getTileEntity(place);
                        if (tile instanceof TileEntityRebar) {
                            ((TileEntityRebar) tile).setup(Block.getBlockFromItem(theConk.getItem()), theConk.getMetadata());
                        }
                        rebarUsed++;
                        rebarLeft--;
                    }
                }
            }
        }

        if (rebarUsed > 0 && !player.isCreative()) { //mlbv: added creative check
            PlayerMainInvWrapper wrapper = new PlayerMainInvWrapper(player.inventory);
            InventoryUtil.tryConsumeAStack(wrapper, 0, wrapper.getSlots() - 1, new ComparableStack(ModBlocks.rebar, rebarUsed));
        }

        player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
                .nextTranslation(getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
                .next("] ").color(TextFormatting.DARK_AQUA)
                .next("Placed " + rebarUsed + " rebar!").color(TextFormatting.GREEN).flush());

        tag.removeTag("pos");
        player.inventoryContainer.detectAndSendChanges();

        return EnumActionResult.SUCCESS;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerRebar(player.inventory, new InventoryRebar(player, player.getHeldItemMainhand()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIRebar(player.inventory, new InventoryRebar(player, player.getHeldItemMainhand()));
    }

    public static class InventoryRebar extends ItemInventory {
        public InventoryRebar(EntityPlayer player, ItemStack box) {
            super(player, box, 1);
        }
    }
}
