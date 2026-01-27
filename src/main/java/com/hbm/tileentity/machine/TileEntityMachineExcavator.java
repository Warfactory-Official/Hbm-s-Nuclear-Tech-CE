package com.hbm.tileentity.machine;

import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.gas.BlockGasBase;
import com.hbm.blocks.generic.BlockBedrockOreTE.TileEntityBedrockOre;
import com.hbm.blocks.generic.BlockDepth;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineExcavator;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineExcavator;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.ItemEnums.EnumDrillType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemDrillbit;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.items.special.ItemBedrockOreBase;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import com.hbm.util.InventoryUtil;
import com.hbm.util.ItemStackUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@AutoRegister
public class TileEntityMachineExcavator extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardReceiver, ITickable, IControlReceiver, IGUIProvider, IUpgradeInfoProvider, IFluidCopiable {

    public static final long maxPower = 10_000_000;
    private final long baseConsumption = 10_000L;
    private final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    private final HashSet<BlockPos> recursionBrake = new HashSet<>();
    public long power;
    public boolean enableDrill = false;
    public boolean enableCrusher = false;
    public boolean enableWalling = false;
    public boolean enableVeinMiner = false;
    public boolean enableSilkTouch = false;
    public float drillRotation = 0F;
    public float prevDrillRotation = 0F;
    public float drillExtension = 0F;
    public float prevDrillExtension = 0F;
    public float crusherRotation = 0F;
    public float prevCrusherRotation = 0F;
    public int chuteTimer = 0;
    public double speed = 1.0D;
    public long consumption = baseConsumption;
    public FluidTankNTM tank;
    private boolean operational = false;
    private boolean hasNullifier = false;
    private int ticksWorked = 0;
    private int targetDepth = 0; //0 is the first block below null position
    private boolean bedrockDrilling = false;
    private int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
    private AxisAlignedBB bb = null;

    public TileEntityMachineExcavator() {
        super(14, true, true);
        this.tank = new FluidTankNTM(Fluids.NONE, 16_000);
    }

    // 1.7 = isOre(int x ,int y, int z, Block b)
    private static boolean isOreDictOre(Block b) {

        /* doing this isn't terribly accurate but just for figuring out if there's OD it works */
        Item blockItem = Item.getItemFromBlock(b);

        if (blockItem != Items.AIR) {
            List<String> names = ItemStackUtil.getOreDictNames(new ItemStack(blockItem));

            for (String name : names) {
                if (name.startsWith("ore")) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String getDefaultName() {
        return "container.machineExcavator";
    }

    @Override
    public void update() {
        //needs to happen on client too for GUI rendering
        upgradeManager.checkSlots(inventory, 2, 3);
        int speedLevel = Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 10);
        int powerLevel = Math.min(upgradeManager.getLevel(UpgradeType.POWER), 3);
        hasNullifier = upgradeManager.getLevel(UpgradeType.NULLIFIER) > 0;

        consumption = baseConsumption * (1 + speedLevel);
        consumption /= (1 + powerLevel);

        if (!world.isRemote) {

            this.tank.setType(1, inventory);

            if (world.getTotalWorldTime() % 20 == 0) {
                tryEjectBuffer();

                for (DirPos posDir : getConPos()) {
                    this.trySubscribe(world, posDir.getPos().getX(), posDir.getPos().getY(), posDir.getPos().getZ(), posDir.getDir());
                    this.trySubscribe(tank.getTankType(), world, posDir.getPos().getX(), posDir.getPos().getY(), posDir.getPos().getZ(), posDir.getDir());
                }
            }

            if (chuteTimer > 0) chuteTimer--;

            this.power = Library.chargeTEFromItems(inventory, 0, this.getPower(), this.getMaxPower());
            this.operational = false;
            int radiusLevel = Math.min(upgradeManager.getLevel(UpgradeType.EFFECT), 3);

            EnumDrillType type = this.getInstalledDrill();
            if (this.enableDrill && type != null && this.power >= this.getPowerConsumption()) {

                operational = true;
                this.power -= this.getPowerConsumption();

                this.speed = type.speed;
                this.speed *= (1 + speedLevel / 2D);

                int maxDepth = this.pos.getY() - 4;

                if ((bedrockDrilling || targetDepth <= maxDepth) && tryDrill(1 + radiusLevel * 2)) {
                    targetDepth++;

                    if (targetDepth > maxDepth) {
                        this.enableDrill = false;
                    }
                }
            } else {
                this.targetDepth = 0;
            }

            this.networkPackNT(150);

        } else {

            this.prevDrillExtension = this.drillExtension;

            if (this.drillExtension != this.targetDepth) {
                float diff = Math.abs(this.drillExtension - this.targetDepth);
                float speed = Math.max(0.15F, diff / 10F);

                if (diff <= speed) {
                    this.drillExtension = this.targetDepth;
                } else {
                    float sig = Math.signum(this.drillExtension - this.targetDepth);
                    this.drillExtension -= sig * speed;
                }
            }

            this.prevDrillRotation = this.drillRotation;
            this.prevCrusherRotation = this.crusherRotation;

            if (this.operational) {
                this.drillRotation += 15F;

                if (this.enableCrusher) {
                    this.crusherRotation += 15F;
                }
            }

            if (this.drillRotation >= 360F) {
                this.drillRotation -= 360F;
                this.prevDrillRotation -= 360F;
            }

            if (this.crusherRotation >= 360F) {
                this.crusherRotation -= 360F;
                this.prevCrusherRotation -= 360F;
            }
        }
    }

    protected DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[]{
                new DirPos(pos.getX() + dir.offsetX * 4 + rot.offsetX, pos.getY() + 1, pos.getZ() + dir.offsetZ * 4 + rot.offsetZ, dir),
                new DirPos(pos.getX() + dir.offsetX * 4 - rot.offsetX, pos.getY() + 1, pos.getZ() + dir.offsetZ * 4 - rot.offsetZ, dir),
                new DirPos(pos.getX() + rot.offsetX * 4, pos.getY() + 1, pos.getZ() + rot.offsetZ * 4, rot),
                new DirPos(pos.getX() - rot.offsetX * 4, pos.getY() + 1, pos.getZ() - rot.offsetZ * 4, rot.getOpposite())
        };
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(enableDrill);
        buf.writeBoolean(enableCrusher);
        buf.writeBoolean(enableWalling);
        buf.writeBoolean(enableVeinMiner);
        buf.writeBoolean(enableSilkTouch);
        buf.writeBoolean(operational);
        buf.writeInt(targetDepth);
        buf.writeInt(chuteTimer);
        buf.writeLong(power);
        tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        enableDrill = buf.readBoolean();
        enableCrusher = buf.readBoolean();
        enableWalling = buf.readBoolean();
        enableVeinMiner = buf.readBoolean();
        enableSilkTouch = buf.readBoolean();
        operational = buf.readBoolean();
        targetDepth = buf.readInt();
        chuteTimer = buf.readInt();
        power = buf.readLong();
        tank.deserialize(buf);
    }

    protected int getY() {
        return pos.getY() - targetDepth - 4;
    }

    /**
     * Works outwards and tries to break a ring, returns true if all rings are broken (or ignorable) and the drill should extend.
     */
    private boolean tryDrill(int radius) {
        int y = getY();

        if (targetDepth == 0 || y == 0) {
            radius = 1;
        }
        for (int ring = 1; ring <= radius; ring++) {

            boolean ignoreAll = true;
            float combinedHardness = 0F;
            BlockPos bedrockOre = null;
            bedrockDrilling = false;

            for (int x = pos.getX() - ring; x <= pos.getX() + ring; x++) {
                for (int z = pos.getZ() - ring; z <= pos.getZ() + ring; z++) {

                    /* Process blocks either if we are in the inner ring (1 = 3x3) or if the target block is on the outer edge */
                    if (ring == 1 || (x == pos.getX() - ring || x == pos.getX() + ring || z == pos.getZ() - ring || z == pos.getZ() + ring)) {

                        BlockPos drillPos = new BlockPos(x, y, z);
                        IBlockState bState = world.getBlockState(drillPos);
                        Block b = bState.getBlock();

                        if (b == ModBlocks.ore_bedrock_block) {
                            combinedHardness = 5 * 60 * 20;
                            bedrockOre = new BlockPos(x, y, z);
                            bedrockDrilling = true;
                            enableCrusher = false;
                            ignoreAll = false;
                            break;
                        }

                        if (b instanceof BlockDepth) {
                            this.enableDrill = false;
                        }
                        if (shouldIgnoreBlock(bState, drillPos)) continue;
                        ignoreAll = false;

                        combinedHardness += bState.getBlockHardness(world, drillPos);
                    }
                }
            }

            if (!ignoreAll) {
                ticksWorked++;

                int ticksToWork = (int) Math.ceil(combinedHardness / this.speed);

                if (ticksWorked >= ticksToWork) {

                    if (bedrockOre == null) {
                        breakBlocks(ring);
                        buildWall(ring + 1, ring == radius && this.enableWalling);
                        if (ring == radius) mineOresFromWall(ring + 1);
                        tryCollect(radius + 1);
                    } else {
                        collectBedrock(bedrockOre);
                    }
                    ticksWorked = 0;
                }

                return false;
            } else {
                tryCollect(radius + 1);
            }
        }

        buildWall(radius + 1, this.enableWalling);
        ticksWorked = 0;
        return true;
    }

    /* breaks and drops all blocks in the specified ring */
    private void breakBlocks(int ring) {
        int y = getY();

        for (int x = pos.getX() - ring; x <= pos.getX() + ring; x++) {
            for (int z = pos.getZ() - ring; z <= pos.getZ() + ring; z++) {

                if (ring == 1 || (x == pos.getX() - ring || x == pos.getX() + ring || z == pos.getZ() - ring || z == pos.getZ() + ring)) {

                    BlockPos drillPos = new BlockPos(x, y, z);
                    IBlockState bState = world.getBlockState(drillPos);
                    if (!shouldIgnoreBlock(bState, drillPos)) {
                        tryMineAtLocation(bState, drillPos);
                    }
                }
            }
        }
    }

    private void tryMineAtLocation(IBlockState bState, BlockPos drillPos) {

        if (this.enableVeinMiner && this.getInstalledDrill().vein) {

            if (isOreDictOre(bState.getBlock())) {
                minX = drillPos.getX();
                minY = drillPos.getY();
                minZ = drillPos.getZ();
                maxX = drillPos.getX();
                maxY = drillPos.getY();
                maxZ = drillPos.getZ();
                breakRecursively(drillPos, 10);
                recursionBrake.clear();

                /* move all excavated items to the last drillable position which is also within collection range */
                List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1));
                for (EntityItem item : items)
                    item.setPosition(drillPos.getX() + 0.5, drillPos.getY() + 0.5, drillPos.getZ() + 0.5);

                return;
            }
        }
        breakSingleBlock(bState, drillPos);
    }

    private void breakRecursively(BlockPos drillPos, int depth) {

        if (depth < 0) return;
        if (recursionBrake.contains(drillPos)) return;
        recursionBrake.add(drillPos);

        IBlockState bState = world.getBlockState(drillPos);
        Block b = bState.getBlock();

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            BlockPos veinPos = drillPos.add(dir.offsetX, dir.offsetY, dir.offsetZ);
            if (world.getBlockState(veinPos).getBlock() == b) {
                breakRecursively(veinPos, depth - 1);
            }
        }

        breakSingleBlock(bState, drillPos);

        int x = drillPos.getX();
        int y = drillPos.getY();
        int z = drillPos.getZ();

        if (x < minX) minX = x;
        if (x > maxX) maxX = x;
        if (y < minY) minY = y;
        if (y > maxY) maxY = y;
        if (z < minZ) minZ = z;
        if (z > maxZ) maxZ = z;

        if (this.enableWalling) {
            world.setBlockState(drillPos, ModBlocks.sandbags.getDefaultState());
        }
    }

    private void breakSingleBlock(IBlockState bState, BlockPos drillPos) {
        Block b = bState.getBlock();
        NonNullList<ItemStack> items = NonNullList.create();
        b.getDrops(items, world, drillPos, bState, this.getFortuneLevel());

        if (b == ModBlocks.sandbags) {
            items.clear();
        } else {
            if (this.canSilkTouch()) {

                ItemStack result = new ItemStack(Item.getItemFromBlock(b), 1, b.getMetaFromState(bState));

                if (!result.isEmpty()) {
                    items.clear();
                    items.add(result.copy());
                }
            }

            if (this.enableCrusher) {

                NonNullList<ItemStack> list = NonNullList.create();

                for (ItemStack stack : items) {
                    ItemStack crushed = ShredderRecipes.getShredderResult(stack).copy();

                    if (crushed.getItem() == ModItems.scrap || crushed.getItem() == ModItems.dust) {
                        list.add(stack);
                    } else {
                        crushed.setCount(crushed.getCount() * stack.getCount());
                        list.add(crushed);
                    }
                }

                items = list;
            }

            if (this.hasNullifier) {

                NonNullList<ItemStack> goodList = NonNullList.create();

                for (ItemStack stack : items) {
                    if (!ItemMachineUpgrade.scrapItems.contains(stack.getItem())) {
                        goodList.add(stack);
                    }
                }

                items = goodList;
            }
        }

        for (ItemStack item : items) {
            world.spawnEntity(new EntityItem(world, drillPos.getX() + 0.5, drillPos.getY() + 0.5, drillPos.getZ() + 0.5, item));
        }

        world.destroyBlock(drillPos, false);
    }

    /* builds a wall along the specified ring, replacing fluid blocks. if wallEverything is set, it will also wall off replacable blocks like air or grass */
    private void buildWall(int ring, boolean wallEverything) {
        int y = getY();

        for (int x = pos.getX() - ring; x <= pos.getX() + ring; x++) {
            for (int z = pos.getZ() - ring; z <= pos.getZ() + ring; z++) {

                BlockPos wallPos = new BlockPos(x, y, z);
                IBlockState bState = world.getBlockState(wallPos);

                if (x == pos.getX() - ring || x == pos.getX() + ring || z == pos.getZ() - ring || z == pos.getZ() + ring) {

                    if (bState.getBlock().isReplaceable(world, wallPos) && (wallEverything || bState.getMaterial().isLiquid())) {
                        world.setBlockState(wallPos, ModBlocks.sandbags.getDefaultState());
                    }
                } else {

                    if (bState.getMaterial().isLiquid()) {
                        world.setBlockToAir(wallPos);
                    }
                }
            }
        }
    }

    // 1.7 mineOuterOres(int ring)
    private void mineOresFromWall(int ring) {
        int y = getY();

        for (int x = pos.getX() - ring; x <= pos.getX() + ring; x++) {
            for (int z = pos.getZ() - ring; z <= pos.getZ() + ring; z++) {

                if (ring == 1 || (x == pos.getX() - ring || x == pos.getX() + ring || z == pos.getZ() - ring || z == pos.getZ() + ring)) {

                    BlockPos drillPos = new BlockPos(x, y, z);
                    IBlockState bState = world.getBlockState(drillPos);
                    if (!shouldIgnoreBlock(bState, drillPos) && isOreDictOre(bState.getBlock())) {
                        tryMineAtLocation(bState, drillPos);
                    }
                }
            }
        }
    }

    private void tryEjectBuffer() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        BlockPos supplyPos = this.getPos().add(dir.offsetX * 4, -3, dir.offsetZ * 4);
        ForgeDirection supplyDir = dir.getOpposite();
        for (int i = 5; i <= 13; i++) {
            ItemStack stackInSlot = this.inventory.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;
            List<ItemStack> singleItemToEject = Collections.singletonList(stackInSlot.copy());
            List<ItemStack> leftovers = trySupply(supplyPos, singleItemToEject, supplyDir, false);
            ItemStack remainingStack = leftovers.isEmpty() ? ItemStack.EMPTY : leftovers.get(0);
            this.inventory.setStackInSlot(i, remainingStack);
        }
    }

    /**
     * Scans for nearby item entities, attempts to output them externally,
     * and then collects any remaining items into the internal buffer.
     *
     * @param radius The radius to scan for items.
     */
    private void tryCollect(int radius) {
        int yLevel = getY();
        AxisAlignedBB collectionArea = new AxisAlignedBB(pos.getX() - radius, yLevel - 1, pos.getZ() - radius, pos.getX() + radius + 1, yLevel + 2, pos.getZ() + radius + 1);
        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, collectionArea);
        if (items.isEmpty()) return;
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        BlockPos supplyPos = this.getPos().add(dir.offsetX * 4, -3, dir.offsetZ * 4);
        ForgeDirection supplyDir = dir.getOpposite();
        for (EntityItem entityItem : items) {
            if (entityItem.isDead) continue;
            ItemStack originalStack = entityItem.getItem();
            if (originalStack.isEmpty()) continue;
            List<ItemStack> itemToSupply = Collections.singletonList(originalStack.copy());
            List<ItemStack> leftovers = trySupply(supplyPos, itemToSupply, supplyDir, true);
            ItemStack remainingStack;
            if (leftovers.isEmpty()) {
                remainingStack = ItemStack.EMPTY;
            } else {
                remainingStack = leftovers.get(0);
            }
            if (remainingStack.getCount() < originalStack.getCount()) {
                chuteTimer = 40;
            }
            if (remainingStack.isEmpty()) {
                entityItem.setDead();
            } else {
                entityItem.setItem(remainingStack);
            }
        }
    }

    /* places all items into a connected container, if possible */
    private void supplyContainer(TileEntity te, List<ItemStack> items, ForgeDirection dir) {
        if (te == null || !te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.toEnumFacing())) {
            return;
        }
        IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.toEnumFacing());

        ListIterator<ItemStack> iterator = items.listIterator();
        while (iterator.hasNext()) {
            ItemStack originalStack = iterator.next();
            if (originalStack.isEmpty()) continue;
            ItemStack remainingStack = InventoryUtil.tryAddItemToInventory(inv, 0, inv.getSlots() - 1, originalStack);
            iterator.set(remainingStack);
            if (remainingStack.getCount() < originalStack.getCount()) {
                chuteTimer = 40;
            }
        }
    }

    /**
     * moves all items onto a connected conveyor belt
     */
    private void supplyConveyor(IConveyorBelt belt, List<ItemStack> items, BlockPos pos) {
        Random rand = world.rand;
        ListIterator<ItemStack> iterator = items.listIterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (item.isEmpty()) continue;
            Vec3d base = new Vec3d(pos.getX() + rand.nextDouble(), pos.getY() + 0.5, pos.getZ() + rand.nextDouble());
            Vec3d vec = belt.getClosestSnappingPosition(world, pos, base);
            EntityMovingItem moving = new EntityMovingItem(world);
            moving.setPosition(base.x, vec.y, base.z);
            moving.setItemStack(item.copy());
            if (world.spawnEntity(moving)) {
                iterator.set(ItemStack.EMPTY);
                chuteTimer = 40;
            }
        }
    }

    private void collectBedrock(BlockPos pos) {
        TileEntity oreTile = world.getTileEntity(pos);
        if (oreTile instanceof TileEntityBedrockOre ore) {

            if (ore.resource == null) return;
            if (ore.tier > this.getInstalledDrill().tier) return;
            if (ore.acidRequirement != null) {
                if (ore.acidRequirement.type != tank.getTankType() || ore.acidRequirement.fill > tank.getFill()) return;
                tank.setFill(tank.getFill() - ore.acidRequirement.fill);
            }

            ItemStack stack = ore.resource.copy();
            if (stack.getItem() == ModItems.bedrock_ore_base) {
                ItemBedrockOreBase.setOreAmount(stack, pos.getX(), pos.getZ());
            }

            List<ItemStack> stacksToSupply = Collections.singletonList(stack);

            ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
            BlockPos fillPos = this.getPos().add(dir.offsetX * 4, -3, dir.offsetZ * 4);
            trySupply(fillPos, stacksToSupply, dir.getOpposite(), true);
        }
    }

    public long getPowerConsumption() {
        return consumption;
    }

    private int getFortuneLevel() {
        EnumDrillType type = getInstalledDrill();

        if (type != null) return type.fortune;
        return 0;
    }

    private boolean shouldIgnoreBlock(IBlockState block, BlockPos pos) {
        Block b = block.getBlock();
        if (b == Blocks.AIR) return true;
        if (b == Blocks.BEDROCK) return true;
        if (b instanceof BlockGasBase) return true;
        float hardness = block.getBlockHardness(world, pos);
        if (hardness < 0 || hardness > 3_500_000) return true;
        return block.getMaterial().isLiquid();
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("drill")) this.enableDrill = !this.enableDrill;
        if (data.hasKey("walling")) this.enableWalling = !this.enableWalling;
        if (data.hasKey("veinminer")) this.enableVeinMiner = !this.enableVeinMiner;

        //cant have silk and crusher together
        if (data.hasKey("silktouch")) {
            if (!this.enableSilkTouch && this.enableCrusher) {
                this.enableCrusher = false;
            }
            this.enableSilkTouch = !this.enableSilkTouch;
        }
        if (data.hasKey("crusher")) {
            if (!this.enableCrusher && this.enableSilkTouch) {
                this.enableSilkTouch = false;
            }
            this.enableCrusher = !this.enableCrusher;
        }

        this.markDirty();
    }

    @Nullable
    public EnumDrillType getInstalledDrill() {
        ItemStack slotItem = inventory.getStackInSlot(4);
        if (!slotItem.isEmpty() && slotItem.getItem() instanceof ItemDrillbit) {
            return EnumUtil.grabEnumSafely(EnumDrillType.VALUES, slotItem.getItemDamage());
        }

        return null;
    }

    public boolean canVeinMine() {
        EnumDrillType type = getInstalledDrill();
        return this.enableVeinMiner && type != null && type.vein;
    }

    public boolean canSilkTouch() {
        EnumDrillType type = getInstalledDrill();
        return this.enableSilkTouch && type != null && type.silk;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.enableDrill = nbt.getBoolean("d");
        this.enableCrusher = nbt.getBoolean("c");
        this.enableWalling = nbt.getBoolean("w");
        this.enableVeinMiner = nbt.getBoolean("v");
        this.enableSilkTouch = nbt.getBoolean("s");
        this.targetDepth = nbt.getInteger("t");
        this.power = nbt.getLong("p");
        this.tank.readFromNBT(nbt, "tank");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("d", enableDrill);
        nbt.setBoolean("c", enableCrusher);
        nbt.setBoolean("w", enableWalling);
        nbt.setBoolean("v", enableVeinMiner);
        nbt.setBoolean("s", enableSilkTouch);
        nbt.setInteger("t", targetDepth);
        nbt.setLong("p", power);
        tank.writeToNBT(nbt, "tank");
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineExcavator(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineExcavator(player.inventory, this);
    }

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) {
            bb = new AxisAlignedBB(pos.add(-3, 0, -3), pos.add(4, 5, 4));
        }
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public long getPower() {
        return this.power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_excavator));
        if (type == UpgradeType.SPEED) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_DELAY, "-" + (100 - 200 / (level + 2)) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "+" + (level * 100) + "%"));
        }
        if (type == UpgradeType.POWER) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "-" + (100 - 100 / (level + 1)) + "%"));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.POWER, 3);
        upgrades.put(UpgradeType.EFFECT, 3);
        return upgrades;
    }

    /**
     * Attempts to supply a list of items to an external inventory or conveyor belt.
     * Can optionally try to place leftovers back into this machine's internal inventory.
     *
     * @param supplyPos     The block position to supply items to.
     * @param itemsToSupply The list of ItemStacks to be supplied. This list is not modified.
     * @param supplyDir     The direction from which the supply is occurring (used for IItemHandler).
     * @param fillBack      If true, attempts to place any leftovers into this machine's inventory (slots 5-13).
     * @return A new List containing any leftover ItemStacks that could not be supplied anywhere.
     */
    private @NotNull List<ItemStack> trySupply(BlockPos supplyPos, List<ItemStack> itemsToSupply, ForgeDirection supplyDir, boolean fillBack) {
        List<ItemStack> remainingItems = new ArrayList<>();
        for (ItemStack item : itemsToSupply) {
            if (!item.isEmpty()) {
                remainingItems.add(item.copy());
            }
        }

        if (remainingItems.isEmpty()) {
            return Collections.emptyList();
        }

        TileEntity tile = world.getTileEntity(supplyPos);
        supplyContainer(tile, remainingItems, supplyDir);
        remainingItems.removeIf(ItemStack::isEmpty);
        if (remainingItems.isEmpty()) {
            return Collections.emptyList();
        }

        Block b = world.getBlockState(supplyPos).getBlock();
        if (b instanceof IConveyorBelt belt) {
            supplyConveyor(belt, remainingItems, supplyPos);
        }
        if (remainingItems.isEmpty()) {
            return Collections.emptyList();
        }

        if (fillBack) {
            List<ItemStack> finalLeftovers = new ArrayList<>();
            for (ItemStack leftover : remainingItems) {
                ItemStack finalLeftover = InventoryUtil.tryAddItemToInventory(inventory, 5, 13, leftover);
                if (!finalLeftover.isEmpty()) {
                    finalLeftovers.add(finalLeftover);
                }
            }
            return finalLeftovers;
        }

        return remainingItems;
    }

    @Override
    public FluidTankNTM getTankToPaste() {
        return tank;
    }
}
