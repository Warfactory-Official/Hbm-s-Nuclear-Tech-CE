package com.hbm.blocks.generic;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockContainerBakeable;
import com.hbm.config.StructureConfig;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.interfaces.AutoRegister;
import com.hbm.itempool.ItemPool;
import com.hbm.main.MainRegistry;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.DelayedTick;
import com.hbm.util.I18nUtil;
import com.hbm.util.LootGenerator;
import com.hbm.world.gen.nbt.INBTTileEntityTransformable;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BlockWandLoot extends BlockContainerBakeable implements ILookOverlay, IToolable, ITooltipProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockWandLoot(String s) {
        super(Material.IRON, s, new BlockBakeFrame("wand_loot_top", "wand_loot"));
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
        EnumFacing facing = player.getHorizontalFacing();
        world.setBlockState(pos, state.withProperty(FACING, facing), 2);

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityWandLoot)) return;
        ((TileEntityWandLoot) te).placedRotation = player.rotationYaw;
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof TileEntityWandLoot loot)) return;

        List<String> text = new ArrayList<>();
        text.add("Will replace with: " + loot.replaceBlock.getTranslationKey());
        text.add("   meta: " + loot.replaceMeta);
        text.add("Loot pool: " + loot.poolName);
        if (loot.replaceBlock != ModBlocks.deco_loot) {
            text.add("Minimum items: " + loot.minItems);
            text.add("Maximum items: " + loot.maxItems);
        }

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
        list.add("Define loot crates/piles in .nbt structures");
        list.add(TextFormatting.GOLD + "Use screwdriver to increase/decrease minimum loot");
        list.add(TextFormatting.GOLD + "Use hand drill to increase/decrease maximum loot");
        list.add(TextFormatting.GOLD + "Use defuser to cycle loot types");
        list.add(TextFormatting.GOLD + "Use container block to set the block that spawns with loot inside");
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof TileEntityWandLoot loot)) return false;

        if (!player.isSneaking()) {

            ItemStack held = player.getHeldItem(hand);
            Block block = getLootableBlock(world, held);

            if (block != null) {
                loot.replaceBlock = block;
                loot.replaceMeta = held.getMetadata();

                List<String> poolNames = loot.getPoolNames(block == ModBlocks.deco_loot);
                if (!poolNames.contains(loot.poolName)) {
                    loot.poolName = poolNames.get(0);
                }

                return true;
            }
        }

        return false;
    }

    private Block getLootableBlock(World world, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        if (stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) stack.getItem()).getBlock();

            if (block == ModBlocks.deco_loot) return block;

            IBlockState state = block.getStateFromMeta(stack.getMetadata());
            if (block.hasTileEntity(state)) {
                TileEntity te = block.createTileEntity(world, state);
                if (te instanceof IInventory) return block;
            }
        }

        return null;
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof TileEntityWandLoot loot)) return false;

        switch (tool) {
            case SCREWDRIVER -> {
                if (player.isSneaking()) {
                    loot.minItems--;
                    if (loot.minItems < 0) loot.minItems = 0;
                } else {
                    loot.minItems++;
                    loot.maxItems = Math.max(loot.minItems, loot.maxItems);
                }
                return true;
            }
            case HAND_DRILL -> {
                if (player.isSneaking()) {
                    loot.maxItems--;
                    if (loot.maxItems < 0) loot.maxItems = 0;
                    loot.minItems = Math.min(loot.minItems, loot.maxItems);
                } else {
                    loot.maxItems++;
                }
                return true;
            }
            case DEFUSER -> {
                List<String> poolNames = loot.getPoolNames(loot.replaceBlock == ModBlocks.deco_loot);
                int index = poolNames.indexOf(loot.poolName);
                index += player.isSneaking() ? -1 : 1;
                index = MathHelper.clamp(index, 0, poolNames.size() - 1);
                loot.poolName = poolNames.get(index);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWandLoot();
    }
    @AutoRegister
    public static class TileEntityWandLoot extends TileEntityLoadedBase implements INBTTileEntityTransformable, ITickable {

        private boolean triggerReplace;

        private Block replaceBlock = ModBlocks.deco_loot;
        private int replaceMeta;

        private String poolName = LootGenerator.LOOT_BOOKLET;
        private int minItems;
        private int maxItems = 1;

        private float placedRotation;

        private static final GameProfile FAKE_PROFILE = new GameProfile(UUID.fromString("839eb18c-50bc-400c-8291-9383f09763e7"), "[NTM]");
        private static FakePlayer fakePlayer;

        @Override
        public void update() {
            if (!world.isRemote) {
                if (triggerReplace) {
                    replace();
                } else {
                    networkPackNT(15);
                }
            }
        }

        private void replace() {
            if (!(world.getBlockState(pos).getBlock() instanceof BlockWandLoot)) {
                MainRegistry.logger.warn("Somehow the block at: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " isn't a loot block but we're doing a TE update as if it is, cancelling!");
                return;
            }

            WeightedRandomChestContentFrom1710[] pool = ItemPool.getPool(poolName);

            world.setBlockState(pos, replaceBlock.getStateFromMeta(replaceMeta), 2);

            TileEntity te = world.getTileEntity(pos);

            if (te == null || te instanceof TileEntityWandLoot) {
                MainRegistry.logger.warn("TE set incorrectly at: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ". If you're using some sort of world generation mod, report it to the author!");

                te = replaceBlock.createTileEntity(world, replaceBlock.getStateFromMeta(replaceMeta));
                world.setTileEntity(pos, te);
            }
            // Th3_Sl1ze: sometimes it does still output null in cases of vanilla chests, though it works seamlessly with crates for example
            // do I know why? nope..
            DelayedTick.nextWorldTickEnd(world, w -> applyLoot(w, w.getTileEntity(pos), pool));
        }

        private void applyLoot(World world, TileEntity te, WeightedRandomChestContentFrom1710[] pool) {
            IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

            if (handler instanceof IItemHandlerModifiable modHandler) {
                int count = minItems;
                if (maxItems - minItems > 0) count += world.rand.nextInt(maxItems - minItems);
                WeightedRandomChestContentFrom1710.generateChestContents(world.rand, pool, modHandler, count);
            } else if (te instanceof BlockLoot.TileEntityLoot) {
                LootGenerator.applyLoot(world, pos.getX(), pos.getY(), pos.getZ(), poolName);
            }

            if (!(world instanceof WorldServer)) return;

            try {
                if (fakePlayer == null || fakePlayer.world != world) {
                    fakePlayer = FakePlayerFactory.get((WorldServer) world, FAKE_PROFILE);
                }

                fakePlayer.rotationYaw = fakePlayer.rotationYawHead = placedRotation;

                ItemStack fakeStack = new ItemStack(replaceBlock, 1, replaceMeta);

                replaceBlock.onBlockPlacedBy(world, pos, world.getBlockState(pos), fakePlayer, fakeStack);
            } catch (Exception ex) {
                MainRegistry.logger.warn("Failed to correctly rotate loot block at: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                MainRegistry.logger.catching(ex);
            }
        }

        private List<String> getPoolNames(boolean loot) {
            if (loot) return Arrays.asList(LootGenerator.getLootNames());

            return new ArrayList<>(ItemPool.pools.keySet());
        }

        @Override
        public void transformTE(World world, int coordBaseMode) {
            triggerReplace = !StructureConfig.debugStructures;
            placedRotation = MathHelper.wrapDegrees(placedRotation + coordBaseMode * 90);
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            Block writeBlock = replaceBlock == null ? ModBlocks.deco_loot : replaceBlock;
            nbt.setString("block", writeBlock.getRegistryName().toString());
            nbt.setInteger("meta", replaceMeta);
            nbt.setInteger("min", minItems);
            nbt.setInteger("max", maxItems);
            nbt.setString("pool", poolName);
            nbt.setFloat("rot", placedRotation);

            nbt.setBoolean("trigger", triggerReplace);
            return super.writeToNBT(nbt);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            replaceBlock = Block.getBlockFromName(nbt.getString("block"));
            replaceMeta = nbt.getInteger("meta");
            minItems = nbt.getInteger("min");
            maxItems = nbt.getInteger("max");
            poolName = nbt.getString("pool");
            placedRotation = nbt.getFloat("rot");

            if (replaceBlock == null) replaceBlock = ModBlocks.deco_loot;

            triggerReplace = nbt.getBoolean("trigger");
        }

        @Override
        public void serialize(ByteBuf buf) {
            buf.writeInt(Block.getIdFromBlock(replaceBlock));
            buf.writeInt(replaceMeta);
            buf.writeInt(minItems);
            buf.writeInt(maxItems);
            BufferUtil.writeString(buf, poolName);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            replaceBlock = Block.getBlockById(buf.readInt());
            replaceMeta = buf.readInt();
            minItems = buf.readInt();
            maxItems = buf.readInt();
            poolName = BufferUtil.readString(buf);
        }

    }

}
