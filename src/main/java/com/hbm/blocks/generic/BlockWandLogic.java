package com.hbm.blocks.generic;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockContainerBakeable;
import com.hbm.config.StructureConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.ICopiable;
import com.hbm.main.MainRegistry;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.I18nUtil;
import com.hbm.world.gen.nbt.INBTTileEntityTransformable;
import com.hbm.world.gen.util.LogicBlockActions;
import com.hbm.world.gen.util.LogicBlockConditions;
import com.hbm.world.gen.util.LogicBlockInteractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockWandLogic extends BlockContainerBakeable implements ILookOverlay, IToolable, ITooltipProvider, IBomb {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockWandLogic(String s) {
            super(Material.IRON, s, new BlockBakeFrame("wand_logic_top", "wand_logic"));
            this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        }

        @Override
        public TileEntity createNewTileEntity(World worldIn, int meta) {
            return new TileEntityWandLogic();
        }

        @Override
        public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
            EnumFacing facing = player.getHorizontalFacing();
            world.setBlockState(pos, state.withProperty(FACING, facing), 2);

            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityWandLogic) {
                ((TileEntityWandLogic) te).placedRotation = facing.getIndex();
            }
        }

        @Override
        public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                EnumFacing side, float fX, float fY, float fZ) {

            ItemStack stack = player.getHeldItem(hand);

            if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock ib && !player.isSneaking()) {
                Block block = ib.getBlock();

                IBlockState disguiseState = block.getStateFromMeta(stack.getMetadata());
                if (disguiseState.isFullBlock() && block != this) {

                    TileEntity tile = world.getTileEntity(pos);

                    if (tile instanceof TileEntityWandLogic logic) {
                        logic.disguise = block;
                        logic.disguiseMeta = stack.getMetadata() & 15;
                        return true;
                    }
                }
            }
            return super.onBlockActivated(world, pos, state, player, hand, side, fX, fY, fZ);
        }

        @Override
        public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand,
                               ToolType tool) {
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity te = world.getTileEntity(pos);

            if (!(te instanceof TileEntityWandLogic logic)) return false;

            switch (tool) {
                case SCREWDRIVER -> {
                    List<String> actionNames = LogicBlockActions.getActionNames();
                    int indexA = actionNames.indexOf(logic.actionID);

                    indexA += player.isSneaking() ? -1 : 1;
                    indexA = MathHelper.clamp(indexA, 0, actionNames.size() - 1);

                    logic.actionID = actionNames.get(indexA);
                    return true;
                }
                case DEFUSER -> {
                    List<String> conditionNames = LogicBlockConditions.getConditionNames();
                    int indexC = conditionNames.indexOf(logic.conditionID);

                    indexC += player.isSneaking() ? -1 : 1;
                    indexC = MathHelper.clamp(indexC, 0, conditionNames.size() - 1);

                    logic.conditionID = conditionNames.get(indexC);

                    return true;
                }
                case HAND_DRILL -> {
                    List<String> interactionNames = LogicBlockInteractions.getInteractionNames();
                    int indexI = interactionNames.indexOf(logic.interactionID);

                    indexI += player.isSneaking() ? -1 : 1;
                    indexI = MathHelper.clamp(indexI, 0, interactionNames.size() - 1);

                    logic.interactionID = interactionNames.get(indexI);

                    return true;
                }
                default -> {
                    return false;
                }
            }
        }

        @Override
        public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
            TileEntity te = world.getTileEntity(pos);

            if (!(te instanceof TileEntityWandLogic logic)) return;

            List<String> text = new ArrayList<>();
            text.add("Action: " + logic.actionID);
            text.add("Condition: " + logic.conditionID);
            text.add("Interaction: " + (logic.interactionID != null ? logic.interactionID : "None"));

            String block;

            if (logic.disguise != null && logic.disguise != Blocks.AIR)
                block = I18nUtil.resolveKey(logic.disguise.getTranslationKey() + ".name");
            else
                block = "None";

            text.add("Disguise Block: " + block);

            ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
        }

        @Override
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
            list.add(TextFormatting.GOLD + "Use screwdriver to cycle forwards through the action list, shift click to go back");
            list.add(TextFormatting.GOLD + "Use defuser to cycle forwards through the condition list, shift click to go back");
            list.add(TextFormatting.GOLD + "Use hand drill to cycle forwards through the interaction list, shift click to go back");
            list.add(TextFormatting.YELLOW + "Use a detonator to transform");
        }

        @Override
        public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {
            TileEntity te = world.getTileEntity(pos);

            if (!(te instanceof TileEntityWandLogic)) return null;

            ((TileEntityWandLogic) te).triggerReplace = true;

            return BombReturnCode.TRIGGERED;
        }

        // state/mapping

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
        @AutoRegister
        public static class TileEntityWandLogic extends TileEntityLoadedBase implements INBTTileEntityTransformable, ICopiable, ITickable {

            private boolean triggerReplace;

            public int placedRotation;

            Block disguise;
            int disguiseMeta = -1;

            public String actionID = "FODDER_WAVE";
            public String conditionID = "PLAYER_CUBE_5";
            public String interactionID;

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
                if (!(world.getBlockState(pos).getBlock() instanceof BlockWandLogic)) {
                    MainRegistry.logger.warn("Somehow the block at: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " isn't a logic block but we're doing a TE update as if it is, cancelling!");
                    return;
                }
                world.setBlockState(pos, ModBlocks.logic_block.getDefaultState(), 2);

                TileEntity te = world.getTileEntity(pos);

                if (te == null || te instanceof BlockWandLoot.TileEntityWandLoot) {
                    MainRegistry.logger.warn("TE for logic block set incorrectly at: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ". If you're using some sort of world generation mod, report it to the author!");
                    te = ModBlocks.logic_block.createTileEntity(world, ModBlocks.logic_block.getDefaultState());
                    world.setTileEntity(pos, te);
                }

                if (te instanceof LogicBlock.TileEntityLogicBlock logic) {
                    logic.actionID = actionID;
                    logic.conditionID = conditionID;
                    logic.interactionID = interactionID;
                    logic.direction = EnumFacing.byIndex(placedRotation);
                    logic.disguise = disguise;
                    logic.disguiseMeta = disguiseMeta;
                }
            }

            @Override
            public void transformTE(World world, int coordBaseMode) {
                triggerReplace = !StructureConfig.debugStructures;
            }

            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                nbt.setString("actionID", actionID);
                nbt.setString("conditionID", conditionID);
                if (interactionID != null) {
                    nbt.setString("interactionID", interactionID);
                }
                nbt.setInteger("rotation", placedRotation);
                if (disguise != null) {
                    nbt.setString("disguise", disguise.getRegistryName().toString());
                    nbt.setInteger("disguiseMeta", disguiseMeta);
                }
                return super.writeToNBT(nbt);
            }

            @Override
            public void readFromNBT(NBTTagCompound nbt) {
                super.readFromNBT(nbt);
                actionID = nbt.getString("actionID");
                conditionID = nbt.getString("conditionID");
                if (nbt.hasKey("interactionID"))
                    interactionID = nbt.getString("interactionID");
                placedRotation = nbt.getInteger("rotation");
                if (nbt.hasKey("disguise")) {
                    disguise = Block.getBlockFromName(nbt.getString("disguise"));
                    disguiseMeta = nbt.getInteger("disguiseMeta");
                }
            }

            @Override
            public void serialize(ByteBuf buf) {
                buf.writeInt(placedRotation);
                BufferUtil.writeString(buf, actionID == null ? "" : actionID);
                BufferUtil.writeString(buf, conditionID == null ? "" : conditionID);
                BufferUtil.writeString(buf, interactionID == null ? "" : interactionID);
                buf.writeInt(Block.getIdFromBlock(disguise == null ? Objects.requireNonNull(Blocks.AIR) : disguise));
                buf.writeInt(Math.max(disguiseMeta, 0));
            }

            @Override
            public void deserialize(ByteBuf buf) {
                placedRotation = buf.readInt();
                actionID = BufferUtil.readString(buf);
                if (actionID.isEmpty()) actionID = "FODDER_WAVE";
                conditionID = BufferUtil.readString(buf);
                if (conditionID.isEmpty()) conditionID = "PLAYER_CUBE_5";
                interactionID = BufferUtil.readString(buf);
                if (interactionID.isEmpty()) interactionID = null;
                disguise = Block.getBlockById(buf.readInt());
                disguiseMeta = buf.readInt();
            }

            @Override
            public NBTTagCompound getSettings(World world, int x, int y, int z) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("actionID", actionID);
                nbt.setString("conditionID", conditionID);
                if (interactionID != null)
                    nbt.setString("interactionID", interactionID);
                if (disguise != null) {
                    nbt.setString("disguise", disguise.getRegistryName().toString());
                    nbt.setInteger("disguiseMeta", disguiseMeta);
                }

                return nbt;
            }

            @Override
            public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
                actionID = nbt.getString("actionID");
                conditionID = nbt.getString("conditionID");
                interactionID = nbt.getString("interactionID");
                if (nbt.hasKey("disguise")) {
                    disguise = Block.getBlockFromName(nbt.getString("disguise"));
                    disguiseMeta = nbt.getInteger("disguiseMeta");
                }
            }
        }
    }
