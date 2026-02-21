package com.hbm.lib;

import com.google.common.base.Predicates;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.api.energymk2.IEnergyConnectorBlock;
import com.hbm.api.energymk2.IEnergyConnectorMK2;
import com.hbm.api.fluidmk2.IFluidConnectorBlockMK2;
import com.hbm.api.fluidmk2.IFluidConnectorMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.HbmLivingCapability.EntityHbmPropsProvider;
import com.hbm.capability.HbmLivingCapability.IEntityHbmProps;
import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.entity.mob.EntityHunterChopper;
import com.hbm.entity.projectile.EntityChopperMine;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.MutableVec3d;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;

import static com.hbm.lib.internal.UnsafeHolder.BA_BASE;
import static com.hbm.lib.internal.UnsafeHolder.U;
import static net.minecraft.nbt.CompressedStreamTools.writeCompressed;

@Spaghetti("this whole class")
public class Library {
    private static final Runnable SPIN_WAITER;

    static {
        Runnable result;
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodHandle mh = lookup.findStatic(Thread.class, "onSpinWait", MethodType.methodType(void.class));
            CallSite cs = LambdaMetafactory.metafactory(lookup, "run", MethodType.methodType(Runnable.class), MethodType.methodType(void.class), mh, MethodType.methodType(void.class));
            result = (Runnable) cs.getTarget().invokeExact();
        } catch (Throwable t) {
            result = () -> {
            };
        }
        SPIN_WAITER = result;
    }

    private Library() {
    }

    static Random rand = new Random();
    public static final double DEG_TO_RAD = Math.PI / 180.0;

    public static final ForgeDirection POS_X = ForgeDirection.EAST;
    public static final ForgeDirection NEG_X = ForgeDirection.WEST;
    public static final ForgeDirection POS_Y = ForgeDirection.UP;
    public static final ForgeDirection NEG_Y = ForgeDirection.DOWN;
    public static final ForgeDirection POS_Z = ForgeDirection.SOUTH;
    public static final ForgeDirection NEG_Z = ForgeDirection.NORTH;

    public static final IBlockState AIR_DEFAULT_STATE = Blocks.AIR.getDefaultState();

    public static final int[] powersOfTen = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

    public static DecimalFormat numberformat = new DecimalFormat("0.00");

    public static String getColor(long a, long b) {
        float fraction = 100F * a / b;
        if (fraction > 75) return "§a";
        if (fraction > 25) return "§e";
        return "§c";
    }

    public static String getColoredMbPercent(long a, long b) {
        String color = getColor(a, b);
        return color + a + " §2/ " + b + " mB " + color + "(" + getPercentage(a / (double) b) + "%)";
    }

    public static String getColoredDurabilityPercent(long a, long b) {
        String color = getColor(a, b);
        return "Durability: " + color + a + " §2/ " + b + " " + color + "(" + getPercentage(a / (double) b) + "%)";
    }

    public static boolean checkForHeld(EntityPlayer player, Item item) {
        return player.getHeldItemMainhand().getItem() == item || player.getHeldItemOffhand().getItem() == item;
    }

    public static boolean isObstructed(World world, double x, double y, double z, double a, double b, double c) {
        RayTraceResult pos = rayTraceBlocks(world, new Vec3d(x, y, z), new Vec3d(a, b, c), false, true, true);
        return pos != null && pos.typeOfHit != Type.MISS;
    }

    public static int getColorProgress(double fraction) {
        int r = (int) (255 * Math.min(1, fraction * -2 + 2));
        int g = (int) (255 * Math.min(1, fraction * 2));
        return 65536 * r + 256 * g;
    }

    public static String getPercentage(double fraction) {
        return numberformat.format(roundFloat(fraction * 100D, 2));
    }

    public static String getShortNumber(long l) {
        return getShortNumber(new BigDecimal(l));
    }

    public static Map<Integer, String> numbersMap = null;


    public static void initNumbers() {
        numbersMap = new TreeMap<>();
        numbersMap.put(3, "k");
        numbersMap.put(6, "M");
        numbersMap.put(9, "G");
        numbersMap.put(12, "T");
        numbersMap.put(15, "P");
        numbersMap.put(18, "E");
        numbersMap.put(21, "Z");
        numbersMap.put(24, "Y");
        numbersMap.put(27, "R");
        numbersMap.put(30, "Q");
    }

    public static String getShortNumber(BigDecimal l) {
        if (numbersMap == null) initNumbers();

        boolean negative = l.signum() < 0;
        if (negative) {
            l = l.negate();
        }

        String result = l.toPlainString();
        BigDecimal c;
        for (Map.Entry<Integer, String> num : numbersMap.entrySet()) {
            c = new BigDecimal("1E" + num.getKey());
            if (l.compareTo(c) >= 0) {
                double res = l.divide(c).doubleValue();
                result = numberformat.format(roundFloat(res, 2)) + num.getValue();
            } else {
                break;
            }
        }

        if (negative) {
            result = "-" + result;
        }

        return result;
    }

    public static float roundFloat(float number, int decimal) {
        return (float) (Math.round(number * powersOfTen[decimal]) / (float) powersOfTen[decimal]);
    }

    public static float roundFloat(double number, int decimal) {
        return (float) (Math.round(number * powersOfTen[decimal]) / (float) powersOfTen[decimal]);
    }

    public static int getColorFromItemStack(ItemStack stack) {
        ResourceLocation path;
        ResourceLocation actualPath;
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(stack.getItem(), stack.getMetadata());
        if (sprite != null) {
            path = new ResourceLocation(sprite.getIconName() + ".png");
            actualPath = new ResourceLocation(path.getNamespace(), "textures/" + path.getPath());
        } else {
            path = new ResourceLocation(stack.getItem().getRegistryName() + ".png");
            actualPath = new ResourceLocation(path.getNamespace(), "textures/items/" + path.getPath());
        }
        return getColorFromResourceLocation(actualPath);
    }

    public static int getColorFromResourceLocation(ResourceLocation r) {
        if (r == null) {
            return 0;
        }
        try {
            BufferedImage image = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(r).getInputStream());
            return getRGBfromARGB(image.getRGB(image.getWidth() >> 1, image.getHeight() >> 1));
        } catch (Exception e) {
            MainRegistry.logger.warn("[NTM] Fluid Texture not found for {}", e.getMessage());
            return 0xFFFFFF;
        }
    }

    public static int getRGBfromARGB(int pixel) {
        return pixel & 0x00ffffff;
    }

    // Drillgon200: Just realized I copied the wrong method. God dang it.
    // It works though. Not sure why, but it works.
    // mlbv: refactored with brand new NTMBatteryCapabilityHandler helpers
    public static long chargeTEFromItems(IItemHandlerModifiable inventory, int index, long power, long maxPower) {
        ItemStack stack = inventory.getStackInSlot(index);
        if (stack.getItem() == ModItems.battery_creative || stack.getItem() == ModItems.fusion_core_infinite) {
            return maxPower;
        }
        long powerNeeded = maxPower - power;
        if (powerNeeded <= 0) return power;
        long heExtracted = dischargeBatteryIfValid(stack, powerNeeded, false);
        return power + heExtracted;
    }

    //not great either but certainly better
    // mlbv: a lot better now
    public static long chargeItemsFromTE(IItemHandlerModifiable inventory, int index, long power, long maxPower) {
        ItemStack stackToCharge = inventory.getStackInSlot(index);
        if (stackToCharge.isEmpty() || power <= 0) {
            return power;
        }
        long heCharged = chargeBatteryIfValid(stackToCharge, power, false);
        return power - heCharged;
    }

    public static EntityPlayer getClosestPlayerForSound(World world, double x, double y, double z, double radius) {
        if (world == null) return null;

        double d4 = -1.0D;
        EntityPlayer entity = null;

        if (radius >= 0) {
            AxisAlignedBB aabb = new AxisAlignedBB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
            List<EntityPlayer> list = world.getEntitiesWithinAABB(EntityPlayer.class, aabb);

            for (EntityPlayer player : list) {
                if (player.isEntityAlive()) {
                    double d5 = player.getDistanceSq(x, y, z);
                    if (d5 < radius * radius && (d4 == -1.0D || d5 < d4)) {
                        d4 = d5;
                        entity = player;
                    }
                }
            }
        } else {
            // use playerEntities instead of loadedEntityList for global player search
            for (EntityPlayer player : world.playerEntities) {
                if (player.isEntityAlive()) {
                    double d5 = player.getDistanceSq(x, y, z);
                    if (d4 == -1.0D || d5 < d4) {
                        d4 = d5;
                        entity = player;
                    }
                }
            }
        }

        return entity;
    }

    public static EntityHunterChopper getClosestChopperForSound(World world, double x, double y, double z, double radius) {
        if (world == null) return null;

        double d4 = -1.0D;
        EntityHunterChopper entity = null;

        if (radius >= 0) {
            AxisAlignedBB aabb = new AxisAlignedBB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
            List<EntityHunterChopper> list = world.getEntitiesWithinAABB(EntityHunterChopper.class, aabb);

            for (EntityHunterChopper chopper : list) {
                if (chopper.isEntityAlive()) {
                    double d5 = chopper.getDistanceSq(x, y, z);
                    if (d5 < radius * radius && (d4 == -1.0D || d5 < d4)) {
                        d4 = d5;
                        entity = chopper;
                    }
                }
            }
        } else {
            for (int i = 0; i < world.loadedEntityList.size(); ++i) {
                Entity e = (Entity) world.loadedEntityList.get(i);
                if (e.isEntityAlive() && e instanceof EntityHunterChopper) {
                    double d5 = e.getDistanceSq(x, y, z);
                    if (d4 == -1.0D || d5 < d4) {
                        d4 = d5;
                        entity = (EntityHunterChopper) e;
                    }
                }
            }
        }

        return entity;
    }

    public static EntityChopperMine getClosestMineForSound(World world, double x, double y, double z, double radius) {
        if (world == null) return null;

        double d4 = -1.0D;
        EntityChopperMine entity = null;

        if (radius >= 0) {
            AxisAlignedBB aabb = new AxisAlignedBB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
            List<EntityChopperMine> list = world.getEntitiesWithinAABB(EntityChopperMine.class, aabb);

            for (EntityChopperMine mine : list) {
                if (mine.isEntityAlive()) {
                    double d5 = mine.getDistanceSq(x, y, z);
                    if (d5 < radius * radius && (d4 == -1.0D || d5 < d4)) {
                        d4 = d5;
                        entity = mine;
                    }
                }
            }
        } else {
            for (int i = 0; i < world.loadedEntityList.size(); ++i) {
                Entity e = (Entity) world.loadedEntityList.get(i);
                if (e.isEntityAlive() && e instanceof EntityChopperMine) {
                    double d5 = e.getDistanceSq(x, y, z);
                    if (d4 == -1.0D || d5 < d4) {
                        d4 = d5;
                        entity = (EntityChopperMine) e;
                    }
                }
            }
        }

        return entity;
    }

    public static RayTraceResult rayTrace(EntityPlayer player, double length, float interpolation) {
        Vec3d vec3 = getPosition(interpolation, player);
        vec3 = vec3.add(0D, (double) player.eyeHeight, 0D);
        Vec3d vec31 = player.getLook(interpolation);
        Vec3d vec32 = vec3.add(vec31.x * length, vec31.y * length, vec31.z * length);
        return rayTraceBlocks(player.world, vec3, vec32, false, false, true);
    }

    public static RayTraceResult rayTrace(EntityPlayer player, double length, float interpolation, boolean b1, boolean b2, boolean b3) {
        Vec3d vec3 = getPosition(interpolation, player);
        vec3 = vec3.add(0D, (double) player.eyeHeight, 0D);
        Vec3d vec31 = player.getLook(interpolation);
        Vec3d vec32 = vec3.add(vec31.x * length, vec31.y * length, vec31.z * length);
        return rayTraceBlocks(player.world, vec3, vec32, b1, b2, b3);
    }

    public static AxisAlignedBB rotateAABB(AxisAlignedBB box, EnumFacing facing) {
        switch (facing) {
            case NORTH:
                return new AxisAlignedBB(box.minX, box.minY, 1 - box.minZ, box.maxX, box.maxY, 1 - box.maxZ);
            case SOUTH:
                return box;
            case EAST:
                return new AxisAlignedBB(box.minZ, box.minY, box.minX, box.maxZ, box.maxY, box.maxX);
            case WEST:
                return new AxisAlignedBB(1 - box.minZ, box.minY, box.minX, 1 - box.maxZ, box.maxY, box.maxX);
            default:
                return box;
        }
    }

    public static RayTraceResult rayTraceIncludeEntities(EntityPlayer player, double d, float f) {
        Vec3d vec3 = getPosition(f, player);
        vec3 = vec3.add(0D, (double) player.eyeHeight, 0D);
        Vec3d vec31 = player.getLook(f);
        Vec3d vec32 = vec3.add(vec31.x * d, vec31.y * d, vec31.z * d);
        return rayTraceIncludeEntities(player.world, vec3, vec32, player);
    }

    public static RayTraceResult rayTraceIncludeEntitiesCustomDirection(EntityPlayer player, Vec3d look, double d, float f) {
        Vec3d vec3 = getPosition(f, player);
        vec3 = vec3.add(0D, (double) player.eyeHeight, 0D);
        Vec3d vec32 = vec3.add(look.x * d, look.y * d, look.z * d);
        return rayTraceIncludeEntities(player.world, vec3, vec32, player);
    }

    public static Vec3d changeByAngle(Vec3d oldDir, float yaw, float pitch) {
        Vec3d dir = new Vec3d(0, 0, 1);
        dir = dir.rotatePitch((float) Math.toRadians(pitch)).rotateYaw((float) Math.toRadians(yaw));
        Vec3d angles = BobMathUtil.getEulerAngles(oldDir);
        return dir.rotatePitch((float) Math.toRadians(angles.y + 90)).rotateYaw((float) Math.toRadians(angles.x));
    }

    public static RayTraceResult rayTraceIncludeEntities(World w, Vec3d vec3, Vec3d vec32, @Nullable Entity excluded) {
        RayTraceResult blockHit = rayTraceBlocks(w, vec3, vec32, false, true, true);
        if (blockHit != null) {
            vec32 = blockHit.hitVec;
        }

        RayTraceResult entityHit = rayTraceEntities(w, excluded, vec3, vec32, 0.3D, entity -> entity instanceof EntityLivingBase && entity.isEntityAlive());

        if (entityHit != null) {
            if (blockHit == null) return entityHit;
            if (vec3.squareDistanceTo(blockHit.hitVec) > vec3.squareDistanceTo(entityHit.hitVec)) return entityHit;
        }

        return blockHit;
    }

    public static Pair<RayTraceResult, List<Entity>> rayTraceEntitiesOnLine(EntityPlayer player, double d, float f) {
        Vec3d vec3 = getPosition(f, player);
        vec3 = vec3.add(0D, (double) player.eyeHeight, 0D);
        Vec3d vec31 = player.getLook(f);
        Vec3d vec32 = vec3.add(vec31.x * d, vec31.y * d, vec31.z * d);
        RayTraceResult result = rayTraceBlocks(player.world, vec3, vec32, false, true, true);
        if (result != null) vec32 = result.hitVec;
        AxisAlignedBB box = new AxisAlignedBB(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z).grow(1D);
        List<Entity> ents = player.world.getEntitiesInAABBexcluding(player, box, Predicates.and(EntitySelectors.IS_ALIVE, entity -> entity instanceof EntityLiving && !(entity instanceof EntityPlayer p && p.isSpectator())));

        double sx = vec3.x;
        double sy = vec3.y;
        double sz = vec3.z;
        double ex = vec32.x;
        double ey = vec32.y;
        double ez = vec32.z;
        double ddx = ex - sx;
        double ddy = ey - sy;
        double ddz = ez - sz;
        boolean dx0 = ddx == 0.0D;
        boolean dy0 = ddy == 0.0D;
        boolean dz0 = ddz == 0.0D;
        double invDx = dx0 ? 0.0D : (1.0D / ddx);
        double invDy = dy0 ? 0.0D : (1.0D / ddy);
        double invDz = dz0 ? 0.0D : (1.0D / ddz);

        Iterator<Entity> itr = ents.iterator();
        while (itr.hasNext()) {
            Entity ent = itr.next();
            AxisAlignedBB entityBox = ent.getEntityBoundingBox();
            double t = intersectSegmentAabbHitTInv(sx, sy, sz, dx0, dy0, dz0, invDx, invDy, invDz, entityBox.minX - 0.1D, entityBox.minY - 0.1D, entityBox.minZ - 0.1D, entityBox.maxX + 0.1D, entityBox.maxY + 0.1D, entityBox.maxZ + 0.1D);
            if (Double.isNaN(t)) {
                itr.remove();
            }
        }
        return Pair.of(rayTraceIncludeEntities(player, d, f), ents);
    }

    public static RayTraceResult rayTraceEntitiesInCone(EntityPlayer player, double d, float f, float degrees) {
        World world = player.world;
        double angle = Math.min(Math.abs(degrees), 180.0D);
        double cosDegrees = Math.cos(Math.toRadians(angle));

        Vec3d start = getPosition(f, player);
        start = start.add(0D, (double) player.eyeHeight, 0D);
        Vec3d look = player.getLook(f);
        Vec3d end = start.add(look.x * d, look.y * d, look.z * d);

        RayTraceResult result = rayTraceBlocks(world, start, end, false, true, true);

        double sx = start.x;
        double sy = start.y;
        double sz = start.z;
        double ex = end.x;
        double ey = end.y;
        double ez = end.z;

        double segDx = ex - sx;
        double segDy = ey - sy;
        double segDz = ez - sz;
        double segLenSq = segDx * segDx + segDy * segDy + segDz * segDz;
        if (segLenSq < 1.0E-12D) {
            return result;
        }
        double segLen = Math.sqrt(segLenSq);

        double maxConeRadius = angle >= 90.0D ? segLen : (segLen * Math.sin(Math.toRadians(angle)));
        double pad = World.MAX_ENTITY_RADIUS + maxConeRadius;

        double minX = Math.min(sx, ex) - pad;
        double minY = Math.min(sy, ey) - pad;
        double minZ = Math.min(sz, ez) - pad;
        double maxX = Math.max(sx, ex) + pad;
        double maxY = Math.max(sy, ey) + pad;
        double maxZ = Math.max(sz, ez) + pad;

        int minSecYUnclamped = ((int) Math.floor(minY)) >> 4;
        int maxSecYUnclamped = ((int) Math.floor(maxY)) >> 4;

        int minSec = minSecYUnclamped;
        if (minSec < 0) minSec = 0;
        else if (minSec > 15) minSec = 15;

        int maxSec = maxSecYUnclamped;
        if (maxSec < 0) maxSec = 0;
        else if (maxSec > 15) maxSec = 15;

        IChunkProvider prov = world.getChunkProvider();

        int cx = ((int) Math.floor(sx)) >> 4;
        int cz = ((int) Math.floor(sz)) >> 4;
        int endCx = ((int) Math.floor(ex)) >> 4;
        int endCz = ((int) Math.floor(ez)) >> 4;

        int stepX = Integer.compare(endCx, cx);
        int stepZ = Integer.compare(endCz, cz);

        double tMaxX, tMaxZ;
        double tDeltaX, tDeltaZ;

        if (stepX == 0) {
            tMaxX = Double.POSITIVE_INFINITY;
            tDeltaX = Double.POSITIVE_INFINITY;
        } else {
            double nextBoundaryX = (stepX > 0) ? ((cx + 1) << 4) : (cx << 4);
            tMaxX = (nextBoundaryX - sx) / segDx;
            tDeltaX = 16.0D / Math.abs(segDx);
        }

        if (stepZ == 0) {
            tMaxZ = Double.POSITIVE_INFINITY;
            tDeltaZ = Double.POSITIVE_INFINITY;
        } else {
            double nextBoundaryZ = (stepZ > 0) ? ((cz + 1) << 4) : (cz << 4);
            tMaxZ = (nextBoundaryZ - sz) / segDz;
            tDeltaZ = 16.0D / Math.abs(segDz);
        }

        int rChunks = (pad <= 0.0D) ? 0 : (int) Math.ceil(pad * 0.0625D);

        LongOpenHashSet visited = TL_VISITED_CHUNKS.get();
        visited.clear();

        MutableVec3d closest = new MutableVec3d();
        Entity[] candidates = new Entity[32];
        double[] candidateDots = new double[32];
        int candidateCount = 0;

        int maxSteps = 2 + Math.abs(endCx - cx) + Math.abs(endCz - cz);
        for (int steps = 0; steps < maxSteps; steps++) {
            for (int ox = -rChunks; ox <= rChunks; ox++) {
                for (int oz = -rChunks; oz <= rChunks; oz++) {
                    int scx = cx + ox;
                    int scz = cz + oz;

                    long key = ChunkPos.asLong(scx, scz);
                    if (!visited.add(key)) continue;

                    Chunk chunk = prov.getLoadedChunk(scx, scz);
                    if (chunk == null) continue;

                    ClassInheritanceMultiMap<Entity>[] lists = chunk.entityLists;
                    for (int sec = minSec; sec <= maxSec; sec++) {
                        ClassInheritanceMultiMap<Entity> map = lists[sec];
                        if (map.isEmpty()) continue;

                        List<Entity> values = map.values;
                        for (int i = 0, n = values.size(); i < n; i++) {
                            Entity ent = values.get(i);
                            if (ent == player) continue;
                            if (!(ent instanceof EntityLiving)) continue;
                            if (!ent.isEntityAlive()) continue;
                            if (ent instanceof EntityPlayer p && p.isSpectator()) continue;
                            if (ent.noClip || !ent.canBeCollidedWith()) continue;

                            AxisAlignedBB eb = ent.getEntityBoundingBox();
                            if (aabbNonIntersect(minX, minY, minZ, maxX, maxY, maxZ, eb)) continue;

                            closestPointOnAabbToSegment(eb, sx, sy, sz, segDx, segDy, segDz, segLenSq, closest);
                            double rx = closest.x - sx;
                            double ry = closest.y - sy;
                            double rz = closest.z - sz;
                            double rLenSq = rx * rx + ry * ry + rz * rz;
                            if (rLenSq < 1.0E-12D) {
                                result = new RayTraceResult(ent);
                                result.hitVec = new Vec3d(ent.posX, ent.posY + ent.getEyeHeight() * 0.5D, ent.posZ);
                                return result;
                            }
                            if (rLenSq > segLenSq) continue;

                            double invRLen = 1.0D / Math.sqrt(rLenSq);
                            double dot = (rx * look.x + ry * look.y + rz * look.z) * invRLen;
                            if (dot <= cosDegrees) continue;

                            if (candidateCount == candidates.length) {
                                candidates = Arrays.copyOf(candidates, candidateCount + (candidateCount >> 1) + 16);
                                candidateDots = Arrays.copyOf(candidateDots, candidateCount + (candidateCount >> 1) + 16);
                            }
                            candidates[candidateCount] = ent;
                            candidateDots[candidateCount] = dot;
                            candidateCount++;
                        }
                    }
                }
            }

        if (cx == endCx && cz == endCz) break;
        if (tMaxX < tMaxZ) {
            cx += stepX;
            tMaxX += tDeltaX;
        } else {
            cz += stepZ;
            tMaxZ += tDeltaZ;
        }
    }

        if (candidateCount > 0) {
            sortCandidatesByDotDesc(candidates, candidateDots, candidateCount);
            for (int i = 0; i < candidateCount; i++) {
                Entity ent = candidates[i];
                if (!isObstructed(world, sx, sy, sz, ent.posX, ent.posY + ent.getEyeHeight() * 0.75D, ent.posZ)) {
                    result = new RayTraceResult(ent);
                    result.hitVec = new Vec3d(ent.posX, ent.posY + ent.getEyeHeight() * 0.5D, ent.posZ);
                    break;
                }
            }
        }

        return result;
    }

    private static void closestPointOnAabbToSegment(AxisAlignedBB box, double sx, double sy, double sz, double dx, double dy, double dz, double segLenSq, MutableVec3d out) {
        double bestX = 0.0D;
        double bestY = 0.0D;
        double bestZ = 0.0D;
        double bestDist = Double.POSITIVE_INFINITY;
        double bestStartDist = Double.POSITIVE_INFINITY;

        double t;
        double px;
        double py;
        double pz;
        double dist;
        double startDist;

        // X planes
        t = (Math.abs(dx) < 1.0E-12D) ? 0.0D : (box.minX - sx) / dx;
        if (t < 0.0D) t = 0.0D;
        else if (t > 1.0D) t = 1.0D;
        px = sx + dx * t;
        py = sy + dy * t;
        pz = sz + dz * t;
        px = clamp(px, box.minX, box.maxX);
        py = clamp(py, box.minY, box.maxY);
        pz = clamp(pz, box.minZ, box.maxZ);
        dist = distToSegmentSq(px, py, pz, sx, sy, sz, dx, dy, dz, segLenSq);
        startDist = distSq(px, py, pz, sx, sy, sz);
        if (isBetterCandidate(dist, startDist, bestDist, bestStartDist)) {
            bestDist = dist;
            bestStartDist = startDist;
            bestX = px;
            bestY = py;
            bestZ = pz;
        }

        t = (Math.abs(dx) < 1.0E-12D) ? 0.0D : (box.maxX - sx) / dx;
        if (t < 0.0D) t = 0.0D;
        else if (t > 1.0D) t = 1.0D;
        px = sx + dx * t;
        py = sy + dy * t;
        pz = sz + dz * t;
        px = clamp(px, box.minX, box.maxX);
        py = clamp(py, box.minY, box.maxY);
        pz = clamp(pz, box.minZ, box.maxZ);
        dist = distToSegmentSq(px, py, pz, sx, sy, sz, dx, dy, dz, segLenSq);
        startDist = distSq(px, py, pz, sx, sy, sz);
        if (isBetterCandidate(dist, startDist, bestDist, bestStartDist)) {
            bestDist = dist;
            bestStartDist = startDist;
            bestX = px;
            bestY = py;
            bestZ = pz;
        }

        // Y planes
        t = (Math.abs(dy) < 1.0E-12D) ? 0.0D : (box.minY - sy) / dy;
        if (t < 0.0D) t = 0.0D;
        else if (t > 1.0D) t = 1.0D;
        px = sx + dx * t;
        py = sy + dy * t;
        pz = sz + dz * t;
        px = clamp(px, box.minX, box.maxX);
        py = clamp(py, box.minY, box.maxY);
        pz = clamp(pz, box.minZ, box.maxZ);
        dist = distToSegmentSq(px, py, pz, sx, sy, sz, dx, dy, dz, segLenSq);
        startDist = distSq(px, py, pz, sx, sy, sz);
        if (isBetterCandidate(dist, startDist, bestDist, bestStartDist)) {
            bestDist = dist;
            bestStartDist = startDist;
            bestX = px;
            bestY = py;
            bestZ = pz;
        }

        t = (Math.abs(dy) < 1.0E-12D) ? 0.0D : (box.maxY - sy) / dy;
        if (t < 0.0D) t = 0.0D;
        else if (t > 1.0D) t = 1.0D;
        px = sx + dx * t;
        py = sy + dy * t;
        pz = sz + dz * t;
        px = clamp(px, box.minX, box.maxX);
        py = clamp(py, box.minY, box.maxY);
        pz = clamp(pz, box.minZ, box.maxZ);
        dist = distToSegmentSq(px, py, pz, sx, sy, sz, dx, dy, dz, segLenSq);
        startDist = distSq(px, py, pz, sx, sy, sz);
        if (isBetterCandidate(dist, startDist, bestDist, bestStartDist)) {
            bestDist = dist;
            bestStartDist = startDist;
            bestX = px;
            bestY = py;
            bestZ = pz;
        }

        // Z planes
        t = (Math.abs(dz) < 1.0E-12D) ? 0.0D : (box.minZ - sz) / dz;
        if (t < 0.0D) t = 0.0D;
        else if (t > 1.0D) t = 1.0D;
        px = sx + dx * t;
        py = sy + dy * t;
        pz = sz + dz * t;
        px = clamp(px, box.minX, box.maxX);
        py = clamp(py, box.minY, box.maxY);
        pz = clamp(pz, box.minZ, box.maxZ);
        dist = distToSegmentSq(px, py, pz, sx, sy, sz, dx, dy, dz, segLenSq);
        startDist = distSq(px, py, pz, sx, sy, sz);
        if (isBetterCandidate(dist, startDist, bestDist, bestStartDist)) {
            bestDist = dist;
            bestStartDist = startDist;
            bestX = px;
            bestY = py;
            bestZ = pz;
        }

        t = (Math.abs(dz) < 1.0E-12D) ? 0.0D : (box.maxZ - sz) / dz;
        if (t < 0.0D) t = 0.0D;
        else if (t > 1.0D) t = 1.0D;
        px = sx + dx * t;
        py = sy + dy * t;
        pz = sz + dz * t;
        px = clamp(px, box.minX, box.maxX);
        py = clamp(py, box.minY, box.maxY);
        pz = clamp(pz, box.minZ, box.maxZ);
        dist = distToSegmentSq(px, py, pz, sx, sy, sz, dx, dy, dz, segLenSq);
        startDist = distSq(px, py, pz, sx, sy, sz);
        if (isBetterCandidate(dist, startDist, bestDist, bestStartDist)) {
            bestDist = dist;
            bestStartDist = startDist;
            bestX = px;
            bestY = py;
            bestZ = pz;
        }

        out.set(bestX, bestY, bestZ);
    }

    private static double distToSegmentSq(double px, double py, double pz, double sx, double sy, double sz, double dx, double dy, double dz, double segLenSq) {
        if (segLenSq < 1.0E-12D) return distSq(px, py, pz, sx, sy, sz);
        double t = ((px - sx) * dx + (py - sy) * dy + (pz - sz) * dz) / segLenSq;
        if (t < 0.0D) t = 0.0D;
        else if (t > 1.0D) t = 1.0D;
        double cx = sx + dx * t;
        double cy = sy + dy * t;
        double cz = sz + dz * t;
        return distSq(px, py, pz, cx, cy, cz);
    }

    private static double distSq(double ax, double ay, double az, double bx, double by, double bz) {
        double rx = ax - bx;
        double ry = ay - by;
        double rz = az - bz;
        return rx * rx + ry * ry + rz * rz;
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        return Math.min(v, max);
    }

    private static boolean isBetterCandidate(double dist, double startDist, double bestDist, double bestStartDist) {
        if (dist < bestDist - 1.0E-12D) return true;
        return Math.abs(dist - bestDist) < 0.01D && startDist < bestStartDist;
    }

    private static void sortCandidatesByDotDesc(Entity[] candidates, double[] dots, int count) {
        sortCandidatesByDotDesc(candidates, dots, 0, count - 1);
    }

    private static void sortCandidatesByDotDesc(Entity[] candidates, double[] dots, int left, int right) {
        int i = left;
        int j = right;
        double pivot = dots[(left + right) >>> 1];
        while (i <= j) {
            while (dots[i] > pivot) i++;
            while (dots[j] < pivot) j--;
            if (i <= j) {
                double d = dots[i];
                dots[i] = dots[j];
                dots[j] = d;
                Entity e = candidates[i];
                candidates[i] = candidates[j];
                candidates[j] = e;
                i++;
                j--;
            }
        }
        if (left < j) sortCandidatesByDotDesc(candidates, dots, left, j);
        if (i < right) sortCandidatesByDotDesc(candidates, dots, i, right);
    }

    //Drillgon200: Turns out the closest point on a bounding box to a line is a pretty good method for determine if a cone and an AABB intersect.
    //Actually that was a pretty garbage method. Changing it out for a slightly less efficient sphere culling algorithm that only gives false positives.
    //https://bartwronski.com/2017/04/13/cull-that-cone/
    //Idea is that we find the closest point on the cone to the center of the sphere and check if it's inside the sphere.
    public static boolean isBoxCollidingCone(AxisAlignedBB box, Vec3d coneStart, Vec3d coneEnd, float degrees) {
        Vec3d center = box.getCenter();
        double radius = center.distanceTo(new Vec3d(box.maxX, box.maxY, box.maxZ));
        Vec3d V = center.subtract(coneStart);
        double VlenSq = V.lengthSquared();
        Vec3d direction = coneEnd.subtract(coneStart);
        double size = direction.length();
        double V1len = V.dotProduct(direction.normalize());
        double angRad = Math.toRadians(degrees);
        double distanceClosestPoint = Math.cos(angRad) * Math.sqrt(VlenSq - V1len * V1len) - V1len * Math.sin(angRad);

        boolean angleCull = distanceClosestPoint > radius;
        boolean frontCull = V1len > radius + size;
        boolean backCull = V1len < -radius;
        return !(angleCull || frontCull || backCull);
    }

    public static Vec3d getEuler(Vec3d vec) {
        double yaw = Math.toDegrees(Math.atan2(vec.x, vec.z));
        double sqrt = MathHelper.sqrt(vec.x * vec.x + vec.z * vec.z);
        double pitch = Math.toDegrees(Math.atan2(vec.y, sqrt));
        return new Vec3d(yaw, pitch, 0);
    }

    //Drillgon200: https://thebookofshaders.com/glossary/?search=smoothstep
    public static double smoothstep(double t, double edge0, double edge1) {
        t = MathHelper.clamp((t - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    public static float smoothstep(float t, float edge0, float edge1) {
        t = MathHelper.clamp((t - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    public static Vec3d getPosition(float interpolation, EntityPlayer player) {
        if (interpolation == 1.0F) {
            return new Vec3d(player.posX, player.posY + (player.getEyeHeight() - player.getDefaultEyeHeight()), player.posZ);
        } else {
            double d0 = player.prevPosX + (player.posX - player.prevPosX) * interpolation;
            double d1 = player.prevPosY + (player.posY - player.prevPosY) * interpolation + (player.getEyeHeight() - player.getDefaultEyeHeight());
            double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * interpolation;
            return new Vec3d(d0, d1, d2);
        }
    }

    public static boolean canConnect(IBlockAccess world, BlockPos pos, ForgeDirection dir /* cable's connecting side */) {

        if (world instanceof World) {
            if (((World) world).isOutsideBuildHeight(pos)) return false;
        } else {
            if (pos.getY() < 0 || pos.getY() > 255) return false;
        }

        Block b = world.getBlockState(pos).getBlock();

        if (b instanceof IEnergyConnectorBlock) {
            IEnergyConnectorBlock con = (IEnergyConnectorBlock) b;

            if (con.canConnect(world, pos, dir.getOpposite() /* machine's connecting side */)) return true;
        }

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof IEnergyConnectorMK2) {
            IEnergyConnectorMK2 con = (IEnergyConnectorMK2) te;

            if (con.canConnect(dir.getOpposite() /* machine's connecting side */)) return true;
        }

        return false;
    }

    //Alcater: Finally this shit is no more

    //TODO: jesus christ
    // Flut-Füll gesteuerter Energieübertragungsalgorithmus
    // Flood fill controlled energy transmission algorithm
    // public static void ffgeua(MutableBlockPos pos, boolean newTact, ISource that, World worldObj) {

    // 	/*
    // 	 * This here smoldering crater is all that remains from the old energy system.
    // 	 * In loving memory, 2019-2023.
    // 	 * You won't be missed.
    // 	 */
    // }

    //Th3_Sl1ze: Sincerely I hate deprecated interfaces but couldn't figure out how to make mechs work without them. Will let them live for now

    /**
     * dir is the direction along the fluid duct entering the block
     */
    public static boolean canConnectFluid(IBlockAccess world, BlockPos pos, ForgeDirection dir /* duct's connecting side */, FluidType type) {
        return canConnectFluid(world, pos.getX(), pos.getY(), pos.getZ(), dir, type);
    }

    public static boolean canConnectFluid(IBlockAccess world, int x, int y, int z, ForgeDirection dir /* duct's connecting side */, FluidType type) {

        if (y > 255 || y < 0) return false;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        Block b = world.getBlockState(pos).getBlock();

        if (b instanceof IFluidConnectorBlockMK2 con) {

            if (con.canConnect(type, world, x, y, z, dir.getOpposite() /* machine's connecting side */)) return true;
        }

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof IFluidConnectorMK2 con) {

            return con.canConnect(type, dir.getOpposite() /* machine's connecting side */);
        }

        return false;
    }

    public static boolean hasInventoryItem(InventoryPlayer inventory, Item ammo) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.getItem() == ammo) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasInventoryOreDict(InventoryPlayer inventory, String name) {
        int oreId = OreDictionary.getOreID(name);
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            int[] ids = OreDictionary.getOreIDs(stack);
            for (int id : ids) {
                if (id == oreId) return true;
            }
        }
        return false;
    }

    public static int countInventoryItem(InventoryPlayer inventory, Item ammo) {
        int count = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.getItem() == ammo) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static void consumeInventoryItem(InventoryPlayer inventory, Item ammo) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.getItem() == ammo && !stack.isEmpty()) {
                stack.shrink(1);
                inventory.setInventorySlotContents(i, stack.copy());
                return;
            }
        }
    }

    //////  //////  //////  //////  //////  ////        //////  //////  //////
    //      //  //  //        //    //      //  //      //      //      //
    ////    //////  /////     //    ////    ////        ////    //  //  //  //
    //      //  //     //     //    //      //  //      //      //  //  //  //
    /// ///  //  //  /////     //    //////  //  //      //////  //////  //////
    //Alcater: Huh thats interesing... You can hide from the chopper as long as you are outside 80% of its radius??
    public static EntityLivingBase getClosestEntityForChopper(World world, double x, double y, double z, double radius) {
        double d4 = -1.0D;
        EntityLivingBase entityplayer = null;

        for (int i = 0; i < world.loadedEntityList.size(); ++i) {
            if (world.loadedEntityList.get(i) instanceof EntityLivingBase && !(world.loadedEntityList.get(i) instanceof EntityHunterChopper)) {
                EntityLivingBase entityplayer1 = (EntityLivingBase) world.loadedEntityList.get(i);

                if (entityplayer1.isEntityAlive() && !(entityplayer1 instanceof EntityPlayer && ((EntityPlayer) entityplayer1).capabilities.disableDamage)) {
                    double d5 = entityplayer1.getDistanceSq(x, y, z);
                    double d6 = radius;

                    if (entityplayer1.isSneaking()) {
                        d6 = radius * 0.800000011920929D;
                    }

                    if ((radius < 0.0D || d5 < d6 * d6) && (d4 == -1.0D || d4 > d5)) {
                        d4 = d5;
                        entityplayer = entityplayer1;
                    }
                }
            }
        }

        return entityplayer;
    }

    //Drillgon200: Loot tables? I don't have time for that!
    //mlbv: technical debt moment
    public static void generateChestContents(Random random, WeightedRandomChestContentFrom1710[] lootTable, ICapabilityProvider chest, int rolls) {
        if (chest.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler test = chest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (test instanceof IItemHandlerModifiable inventory) {
                for (int j = 0; j < rolls; ++j) {
                    ItemStack[] stacks = WeightedRandom.getRandomItem(random, Arrays.asList(lootTable)).generateChestContent(random, inventory);
                    for (ItemStack item : stacks) {
                        inventory.setStackInSlot(random.nextInt(inventory.getSlots()), item);
                    }
                }
            }
        }

    }

    @Deprecated(forRemoval = true, since = "1.5.2.1")
    public static Block getRandomConcrete() {
        int i = rand.nextInt(100);

        if (i < 5) return ModBlocks.brick_concrete_broken;
        if (i < 20) return ModBlocks.brick_concrete_cracked;
        if (i < 50) return ModBlocks.brick_concrete_mossy;

        return ModBlocks.brick_concrete;
    }

    /**
     * Deterministic variant for worldgen: callers must pass their seeded RNG.
     */
    public static Block getRandomConcrete(Random random) {
        int i = random.nextInt(100);

        if (i < 5) return ModBlocks.brick_concrete_broken;
        if (i < 20) return ModBlocks.brick_concrete_cracked;
        if (i < 50) return ModBlocks.brick_concrete_mossy;

        return ModBlocks.brick_concrete;
    }


    public static void placeDoorWithoutCheck(World worldIn, BlockPos pos, EnumFacing facing, Block door, boolean isRightHinge) {
        BlockPos blockpos2 = pos.up();
        boolean flag2 = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(blockpos2);
        IBlockState iblockstate = door.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HINGE, isRightHinge ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT).withProperty(BlockDoor.POWERED, Boolean.valueOf(flag2)).withProperty(BlockDoor.OPEN, Boolean.valueOf(flag2));
        worldIn.setBlockState(pos, iblockstate.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER), 2);
        worldIn.setBlockState(blockpos2, iblockstate.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), 2);
        worldIn.notifyNeighborsOfStateChange(pos, door, false);
        worldIn.notifyNeighborsOfStateChange(blockpos2, door, false);
    }

    /**
     * Same as ItemStack.areItemStacksEqual, except the second one's tag only has to contain all the first one's tag, rather than being exactly equal.
     */
    public static boolean areItemStacksCompatible(ItemStack base, ItemStack toTest, boolean shouldCompareSize) {
        if (base.isEmpty() && toTest.isEmpty()) {
            return true;
        } else {
            if (!base.isEmpty() && !toTest.isEmpty()) {

                if (shouldCompareSize && base.getCount() != toTest.getCount()) {
                    return false;
                } else if (base.getItem() != toTest.getItem()) {
                    return false;
                } else if (base.getMetadata() != toTest.getMetadata() && !(base.getMetadata() == OreDictionary.WILDCARD_VALUE)) {
                    return false;
                } else if (base.getTagCompound() == null && toTest.getTagCompound() != null) {
                    return false;
                } else {
                    return (base.getTagCompound() == null || tagContainsOther(base.getTagCompound(), toTest.getTagCompound())) && base.areCapsCompatible(toTest);
                }
            }
        }
        return false;
    }

    public static boolean areItemStacksCompatible(ItemStack base, ItemStack toTest) {
        return areItemStacksCompatible(base, toTest, true);
    }

    /**
     * Returns true if the second compound contains all the tags and values of the first one, but it can have more. This helps with intermod compatibility
     */
    public static boolean tagContainsOther(NBTTagCompound tester, NBTTagCompound container) {
        if (tester == null && container == null) {
            return true;
        } else if (tester == null && container != null) {
            return true;
        } else if (tester != null && container == null) {
        } else {
            for (String s : tester.getKeySet()) {
                if (!container.hasKey(s)) {
                    return false;
                } else {
                    NBTBase nbt1 = tester.getTag(s);
                    NBTBase nbt2 = container.getTag(s);
                    if (nbt1 instanceof NBTTagCompound && nbt2 instanceof NBTTagCompound) {
                        if (!tagContainsOther((NBTTagCompound) nbt1, (NBTTagCompound) nbt2)) return false;
                    } else {
                        if (!nbt1.equals(nbt2)) return false;
                    }
                }
            }
        }
        return true;
    }

    public static List<int[]> getBlockPosInPath(BlockPos pos, int length, Vec3d vec0) {
        List<int[]> list = new ArrayList<>();

        for (int i = 0; i <= length; i++) {
            list.add(new int[]{(int) (pos.getX() + (vec0.x * i)), pos.getY(), (int) (pos.getZ() + (vec0.z * i)), i});
        }

        return list;
    }

    public static List<ItemStack> copyItemStackList(List<ItemStack> inputs) {
        List<ItemStack> list = new ArrayList<>(inputs.size());
        for (ItemStack stack : inputs) {
            list.add(stack.copy());
        }
        return list;
    }

    public static List<List<ItemStack>> copyItemStackListList(List<List<ItemStack>> inputs) {
        List<List<ItemStack>> list = new ArrayList<>(inputs.size());
        for (List<ItemStack> list2 : inputs) {
            List<ItemStack> newList = new ArrayList<>(list2.size());
            for (ItemStack stack : list2) {
                newList.add(stack.copy());
            }
            list.add(newList);
        }
        return list;
    }

    public static IEntityHbmProps getEntRadCap(Entity e) {
        IEntityHbmProps cap = e.getCapability(EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null);
        return cap != null ? cap : EntityHbmPropsProvider.DUMMY;
    }

    public static void addToInventoryOrDrop(EntityPlayer player, ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.dropItem(stack, false);
        }
    }

    public static Vec3d normalFromRayTrace(RayTraceResult r) {
        Vec3i n = r.sideHit.getDirectionVec();
        return new Vec3d(n.getX(), n.getY(), n.getZ());
    }

    @Nullable
    private static IEnergyStorage getFE(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (!stack.hasCapability(CapabilityEnergy.ENERGY, null)) return null;
        return stack.getCapability(CapabilityEnergy.ENERGY, null);
    }

    /**
     * @return true if is instance of IBatteryItem or has FE capability
     */
    public static boolean isBattery(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof IBatteryItem || getFE(stack) != null;
    }

    public static boolean isDischargeableBattery(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof IBatteryItem battery) {
            return battery.getCharge(stack) > 0 && battery.getDischargeRate(stack) > 0;
        }
        IEnergyStorage cap = getFE(stack);
        return cap != null && cap.getEnergyStored() > 0 && cap.canExtract();
    }

    public static boolean isChargeableBattery(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof IBatteryItem battery) {
            return battery.getMaxCharge(stack) > battery.getCharge(stack) && battery.getChargeRate(stack) > 0;
        }
        IEnergyStorage cap = getFE(stack);
        return cap != null && cap.getMaxEnergyStored() > cap.getEnergyStored() && cap.canReceive();
    }

    public static boolean isEmptyBattery(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof IBatteryItem battery) {
            return battery.getCharge(stack) <= 0;
        }
        IEnergyStorage cap = getFE(stack);
        return cap != null && cap.getEnergyStored() <= 0;
    }

    public static boolean isFullBattery(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof IBatteryItem battery) {
            long max = battery.getMaxCharge(stack);
            long cur = battery.getCharge(stack);
            return cur >= max;
        }
        IEnergyStorage cap = getFE(stack);
        return cap != null && cap.getEnergyStored() >= cap.getMaxEnergyStored();
    }

    @Contract(pure = true)
    public static boolean isStackDrainableForTank(@NotNull ItemStack stack, @NotNull FluidTankNTM tank) {
        Item item = stack.getItem();
        if (tank.getFill() >= tank.getMaxFill()) return false;

        if (NTMFluidCapabilityHandler.isNtmFluidContainer(item)) {
            if (!NTMFluidCapabilityHandler.isFullNtmFluidContainer(item)) return false;
            if (tank.getTankType() != Fluids.NONE && tank.getTankType() != FluidContainerRegistry.getFluidType(stack))
                return false;
            return tank.getFill() + FluidContainerRegistry.getFluidContent(stack) <= tank.getMaxFill();
        } else if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            FluidStack test = handler.drain(Integer.MAX_VALUE, false);
            if (test == null) return false;
            return tank.fill(test, false) > 0;
        } else return false;
    }

    @Contract(pure = true)
    public static boolean isStackFillableForTank(@NotNull ItemStack stack, @NotNull FluidTankNTM tank) {
        Item item = stack.getItem();
        if (tank.getTankType() == Fluids.NONE) return false;
        if (NTMFluidCapabilityHandler.isNtmFluidContainer(item)) {
            if (!NTMFluidCapabilityHandler.isEmptyNtmFluidContainer(item)) return false;
            return FluidContainerRegistry.getFillRecipe(stack, tank.getTankType()) != null;
        } else if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            return handler.fill(new FluidStack(tank.getTankTypeFF(), Integer.MAX_VALUE), false) > 0;
        } else return false;
    }

    public static boolean isMachineUpgrade(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemMachineUpgrade;
    }

    private static int clampFeRequest(long feLong) {
        if (feLong <= 0) return 0;
        return feLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) feLong;
    }

    /**
     * Charges the item if valid.
     *
     * @return actual energy charged (in HE).
     * @throws IllegalArgumentException if chargeAmountHE <= 0.
     */
    public static long chargeBatteryIfValid(@NotNull ItemStack stack, long chargeAmountHE, boolean instant) {
        if (stack.isEmpty()) return 0;
        if (chargeAmountHE <= 0) throw new IllegalArgumentException("chargeAmountHE must be > 0");
        if (stack.getItem() instanceof IBatteryItem battery) {
            long max = Math.max(0L, battery.getMaxCharge(stack));
            long cur = Math.max(0L, Math.min(max, battery.getCharge(stack)));
            long room = Math.max(0L, max - cur);
            long rate = Math.max(0L, battery.getChargeRate(stack));
            long req = instant ? chargeAmountHE : Math.min(chargeAmountHE, rate);
            long added = Math.min(req, room);
            if (added > 0) battery.chargeBattery(stack, added);
            return added;
        }
        IEnergyStorage cap = getFE(stack);
        double rate = GeneralConfig.conversionRateHeToRF;
        if (cap == null || rate <= 0d) return 0;
        long feReqLong = Math.round(chargeAmountHE * rate);
        int feReq = clampFeRequest(feReqLong);
        if (feReq <= 0) return 0;
        int canReceive = cap.receiveEnergy(feReq, true);
        if (canReceive <= 0) return 0;
        int feReceived = cap.receiveEnergy(canReceive, false);
        long heAdded = (long) Math.floor(feReceived / rate);
        return Math.max(0L, heAdded);
    }

    /**
     * Discharges the item if valid.
     *
     * @return actual energy extracted (in HE).
     * @throws IllegalArgumentException if dischargeAmountHE <= 0.
     */
    public static long dischargeBatteryIfValid(@NotNull ItemStack stack, long dischargeAmountHE, boolean instant) {
        if (stack.isEmpty()) return 0;
        if (dischargeAmountHE <= 0) throw new IllegalArgumentException("dischargeAmountHE must be > 0");
        if (stack.getItem() instanceof IBatteryItem battery) {
            long cur = Math.max(0L, battery.getCharge(stack));
            long rate = Math.max(0L, battery.getDischargeRate(stack));
            long req = instant ? dischargeAmountHE : Math.min(dischargeAmountHE, rate);
            long take = Math.min(req, cur);
            if (take > 0) battery.dischargeBattery(stack, take);
            return take;
        }
        IEnergyStorage cap = getFE(stack);
        double rate = GeneralConfig.conversionRateHeToRF;
        if (cap == null || rate <= 0d) return 0;
        long feReqLong = Math.round(dischargeAmountHE * rate);
        int feReq = clampFeRequest(feReqLong);
        if (feReq <= 0) return 0;
        int canExtract = cap.extractEnergy(feReq, true);
        if (canExtract <= 0) return 0;
        int feExtracted = cap.extractEnergy(canExtract, false);
        long heExtracted = (long) Math.floor(feExtracted / rate);
        return Math.max(0L, heExtracted);
    }

    public static long getCompressedNbtSize(NBTTagCompound compound) {
        try {
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            writeCompressed(compound, bytearrayoutputstream);
            return bytearrayoutputstream.size();
        } catch (IOException ignored) {
            return -1;
        }
    }

    public static float getTENbtPercentage(TileEntity te, float limitByteSize) {
        NBTTagCompound compound = new NBTTagCompound();
        compound = te.writeToNBT(compound);
        float percent = 0.0f;
        if (limitByteSize > 0) {
            percent = (float) Library.getCompressedNbtSize(compound) / limitByteSize;
        }
        return percent;
    }

    private static final ThreadLocal<LongOpenHashSet> TL_VISITED_CHUNKS = ThreadLocal.withInitial(() -> new LongOpenHashSet(512));

    @Nullable
    public static RayTraceResult rayTrace(@NotNull World world, @NotNull Vec3d startPos, @NotNull Vec3d directionVec, double maxLength, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, boolean stopOnEntity) {

        if (Double.isNaN(startPos.x) || Double.isNaN(startPos.y) || Double.isNaN(startPos.z) || Double.isNaN(directionVec.x) || Double.isNaN(directionVec.y) || Double.isNaN(directionVec.z)) {
            return null;
        }

        double dirLenSq = directionVec.x * directionVec.x + directionVec.y * directionVec.y + directionVec.z * directionVec.z;
        if (dirLenSq < 1.0E-7D) {
            return null;
        }

        double invLen = maxLength / Math.sqrt(dirLenSq);
        Vec3d endPos = new Vec3d(startPos.x + directionVec.x * invLen, startPos.y + directionVec.y * invLen, startPos.z + directionVec.z * invLen);

        RayTraceResult blockHitResult = rayTraceBlocksInternal(world, startPos, endPos, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock, 200);

        if (stopOnEntity) {
            Vec3d entityEnd = (blockHitResult != null && blockHitResult.typeOfHit == RayTraceResult.Type.BLOCK) ? blockHitResult.hitVec : endPos;

            RayTraceResult entityHit = rayTraceEntities(world, null, startPos, entityEnd, 0.3D, null);
            if (entityHit != null) {
                return entityHit;
            }
        }

        return blockHitResult;
    }

    @Nullable
    public static RayTraceResult rayTraceBlocks(@NotNull World world, @NotNull Vec3d startVec, @NotNull Vec3d endVec, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        return rayTraceBlocksInternal(world, startVec, endVec, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock, 200);
    }

    @Nullable
    public static RayTraceResult rayTraceBlocks(@NotNull World world, @NotNull Vec3d startVec, @NotNull Vec3d endVec, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, int maxSteps) {
        return rayTraceBlocksInternal(world, startVec, endVec, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock, maxSteps);
    }

    @Nullable
    private static Chunk getChunkForBlockTrace(@NotNull World world, int cx, int cz) {
        IChunkProvider prov = world.getChunkProvider();
        return world.isRemote ? prov.getLoadedChunk(cx, cz) : prov.provideChunk(cx, cz);
    }

    @Nullable
    private static RayTraceResult rayTraceBlocksInternal(@NotNull World world, @NotNull Vec3d startVec, @NotNull Vec3d endVec, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, int maxSteps) {
        if (Double.isNaN(startVec.x) || Double.isNaN(startVec.y) || Double.isNaN(startVec.z) || Double.isNaN(endVec.x) || Double.isNaN(endVec.y) || Double.isNaN(endVec.z)) {
            return null;
        }

        int endX = (int) Math.floor(endVec.x);
        int endY = (int) Math.floor(endVec.y);
        int endZ = (int) Math.floor(endVec.z);

        int x = (int) Math.floor(startVec.x);
        int y = (int) Math.floor(startVec.y);
        int z = (int) Math.floor(startVec.z);

        double startX = startVec.x;
        double startY = startVec.y;
        double startZ = startVec.z;

        double dx = endVec.x - startX;
        double dy = endVec.y - startY;
        double dz = endVec.z - startZ;

        int stepX = Integer.compare(endX, x);
        int stepY = Integer.compare(endY, y);
        int stepZ = Integer.compare(endZ, z);

        byte faceX = (byte) (5 - ((stepX + 1) >>> 1));
        byte faceY = (byte) (1 - ((stepY + 1) >>> 1));
        byte faceZ = (byte) (3 - ((stepZ + 1) >>> 1));

        double tDeltaX = (stepX == 0) ? Double.POSITIVE_INFINITY : (1.0D / Math.abs(dx));
        double tDeltaY = (stepY == 0) ? Double.POSITIVE_INFINITY : (1.0D / Math.abs(dy));
        double tDeltaZ = (stepZ == 0) ? Double.POSITIVE_INFINITY : (1.0D / Math.abs(dz));

        double tMaxX = (stepX == 0) ? Double.POSITIVE_INFINITY : (((stepX > 0) ? ((x + 1.0D) - startX) : (startX - (double) x)) * tDeltaX);
        double tMaxY = (stepY == 0) ? Double.POSITIVE_INFINITY : (((stepY > 0) ? ((y + 1.0D) - startY) : (startY - (double) y)) * tDeltaY);
        double tMaxZ = (stepZ == 0) ? Double.POSITIVE_INFINITY : (((stepZ > 0) ? ((z + 1.0D) - startZ) : (startZ - (double) z)) * tDeltaZ);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);

        int cachedChunkX = Integer.MIN_VALUE;
        int cachedChunkZ = Integer.MIN_VALUE;
        ExtendedBlockStorage[] cachedEbsArr = null;

        int cachedSecY = Integer.MIN_VALUE;
        ExtendedBlockStorage cachedEbs = null;
        boolean cachedEbsEmpty = true;

        IBlockState startState;
        if ((y & ~255) != 0) {
            startState = AIR_DEFAULT_STATE;
        } else {
            int cx = x >> 4;
            int cz = z >> 4;

            cachedChunkX = cx;
            cachedChunkZ = cz;

            Chunk chunk = getChunkForBlockTrace(world, cx, cz);
            cachedEbsArr = (chunk != null) ? chunk.getBlockStorageArray() : null;

            int secY = y >> 4;
            cachedSecY = secY;

            if (cachedEbsArr == null) {
                startState = AIR_DEFAULT_STATE;
            } else {
                cachedEbs = cachedEbsArr[secY];
                cachedEbsEmpty = (cachedEbs == null) || cachedEbs.isEmpty();
                startState = cachedEbsEmpty ? AIR_DEFAULT_STATE : cachedEbs.get(x & 15, y & 15, z & 15);
            }
        }

        if (startState != AIR_DEFAULT_STATE) {
            if ((!ignoreBlockWithoutBoundingBox || startState.getCollisionBoundingBox(world, pos) != Block.NULL_AABB) && startState.getBlock().canCollideCheck(startState, stopOnLiquid)) {
                RayTraceResult hit = startState.collisionRayTrace(world, pos, startVec, endVec);
                //noinspection ConstantValue
                if (hit != null) {
                    return hit;
                }
            }
        }

        boolean trackAirMiss = returnLastUncollidableBlock && !ignoreBlockWithoutBoundingBox;

        boolean hasLastMiss = false;
        int lastMissBx = 0, lastMissBy = 0, lastMissBz = 0;
        byte lastMissSide = 0;
        double lastMissHx = 0.0D, lastMissHy = 0.0D, lastMissHz = 0.0D;

        double curX;
        double curY;
        double curZ;
        MutableVec3d stepStart = new MutableVec3d();
        int stepsLeft = Math.max(0, maxSteps);

        while (stepsLeft-- >= 0) {
            if (x == endX && y == endY && z == endZ) {
                if (!returnLastUncollidableBlock || !hasLastMiss) return null;
                return new RayTraceResult(RayTraceResult.Type.MISS, new Vec3d(lastMissHx, lastMissHy, lastMissHz), EnumFacing.VALUES[lastMissSide], new BlockPos(lastMissBx, lastMissBy, lastMissBz));
            }

            byte sideHit;
            double tNext;

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    sideHit = faceX;
                    tNext = tMaxX;
                    tMaxX += tDeltaX;

                    x += stepX;
                    curX = (stepX > 0) ? (double) x : (double) (x + 1);
                    curY = startY + dy * tNext;
                    curZ = startZ + dz * tNext;
                } else {
                    sideHit = faceZ;
                    tNext = tMaxZ;
                    tMaxZ += tDeltaZ;

                    z += stepZ;
                    curX = startX + dx * tNext;
                    curY = startY + dy * tNext;
                    curZ = (stepZ > 0) ? (double) z : (double) (z + 1);
                }
            } else {
                if (tMaxY < tMaxZ) {
                    sideHit = faceY;
                    tNext = tMaxY;
                    tMaxY += tDeltaY;

                    y += stepY;
                    curX = startX + dx * tNext;
                    curY = (stepY > 0) ? (double) y : (double) (y + 1);
                    curZ = startZ + dz * tNext;
                } else {
                    sideHit = faceZ;
                    tNext = tMaxZ;
                    tMaxZ += tDeltaZ;

                    z += stepZ;
                    curX = startX + dx * tNext;
                    curY = startY + dy * tNext;
                    curZ = (stepZ > 0) ? (double) z : (double) (z + 1);
                }
            }

            pos.setPos(x, y, z);

            IBlockState state;
            if ((y & ~255) != 0) {
                state = AIR_DEFAULT_STATE;
            } else {
                int cx = x >> 4;
                int cz = z >> 4;

                if (cx != cachedChunkX || cz != cachedChunkZ) {
                    cachedChunkX = cx;
                    cachedChunkZ = cz;
                    Chunk chunk = getChunkForBlockTrace(world, cx, cz);
                    cachedEbsArr = (chunk != null) ? chunk.getBlockStorageArray() : null;
                    cachedSecY = Integer.MIN_VALUE;
                    cachedEbs = null;
                    cachedEbsEmpty = true;
                }

                int secY = y >> 4;
                if (secY != cachedSecY) {
                    cachedSecY = secY;
                    if (cachedEbsArr == null) {
                        cachedEbs = null;
                        cachedEbsEmpty = true;
                    } else {
                        cachedEbs = cachedEbsArr[secY];
                        cachedEbsEmpty = (cachedEbs == null) || cachedEbs.isEmpty();
                    }
                }

                state = cachedEbsEmpty ? AIR_DEFAULT_STATE : cachedEbs.get(x & 15, y & 15, z & 15);
            }

            if (state == AIR_DEFAULT_STATE) {
                if (trackAirMiss) {
                    hasLastMiss = true;
                    lastMissSide = sideHit;
                    lastMissBx = x;
                    lastMissBy = y;
                    lastMissBz = z;
                    lastMissHx = curX;
                    lastMissHy = curY;
                    lastMissHz = curZ;
                }
                continue;
            }

            if (!ignoreBlockWithoutBoundingBox || state.getMaterial() == Material.PORTAL || state.getCollisionBoundingBox(world, pos) != Block.NULL_AABB) {
                Block block = state.getBlock();

                if (block.canCollideCheck(state, stopOnLiquid)) {
                    stepStart.set(curX, curY, curZ);
                    RayTraceResult hit = state.collisionRayTrace(world, pos, stepStart, endVec);
                    //noinspection ConstantValue
                    if (hit != null) {
                        return hit;
                    }
                } else if (returnLastUncollidableBlock) {
                    hasLastMiss = true;
                    lastMissSide = sideHit;
                    lastMissBx = x;
                    lastMissBy = y;
                    lastMissBz = z;
                    lastMissHx = curX;
                    lastMissHy = curY;
                    lastMissHz = curZ;
                }
            }
        }

        if (!returnLastUncollidableBlock || !hasLastMiss) return null;
        return new RayTraceResult(RayTraceResult.Type.MISS, new Vec3d(lastMissHx, lastMissHy, lastMissHz), EnumFacing.VALUES[lastMissSide], new BlockPos(lastMissBx, lastMissBy, lastMissBz));
    }

    public static @Nullable RayTraceResult rayTraceEntities(@NotNull World world, @Nullable Entity exclude, @NotNull Vec3d start, @NotNull Vec3d end, double inflate, @Nullable Predicate<? super Entity> extraFilter) {
        double sx = start.x;
        double sy = start.y;
        double sz = start.z;
        double ex = end.x;
        double ey = end.y;
        double ez = end.z;
        if (Double.isNaN(sx) || Double.isNaN(sy) || Double.isNaN(sz) || Double.isNaN(ex) || Double.isNaN(ey) || Double.isNaN(ez)) {
            return null;
        }

        double ddx = ex - sx;
        double ddy = ey - sy;
        double ddz = ez - sz;
        double segLenSq = ddx * ddx + ddy * ddy + ddz * ddz;
        if (segLenSq < 1.0E-12D) {
            return null;
        }

        boolean dx0 = ddx == 0.0D;
        boolean dy0 = ddy == 0.0D;
        boolean dz0 = ddz == 0.0D;
        double invDx = dx0 ? 0.0D : (1.0D / ddx);
        double invDy = dy0 ? 0.0D : (1.0D / ddy);
        double invDz = dz0 ? 0.0D : (1.0D / ddz);

        boolean hasFilter = extraFilter != null;

        IChunkProvider prov = world.getChunkProvider();
        double pad = World.MAX_ENTITY_RADIUS + inflate;

        double minX = Math.min(sx, ex) - pad;
        double minY = Math.min(sy, ey) - pad;
        double minZ = Math.min(sz, ez) - pad;
        double maxX = Math.max(sx, ex) + pad;
        double maxY = Math.max(sy, ey) + pad;
        double maxZ = Math.max(sz, ez) + pad;

        int minSecYUnclamped = ((int) Math.floor(minY)) >> 4;
        int maxSecYUnclamped = ((int) Math.floor(maxY)) >> 4;

        int minSec = minSecYUnclamped;
        if (minSec < 0) minSec = 0;
        else if (minSec > 15) minSec = 15;

        int maxSec = maxSecYUnclamped;
        if (maxSec < 0) maxSec = 0;
        else if (maxSec > 15) maxSec = 15;

        int cx = ((int) Math.floor(sx)) >> 4;
        int cz = ((int) Math.floor(sz)) >> 4;
        int endCx = ((int) Math.floor(ex)) >> 4;
        int endCz = ((int) Math.floor(ez)) >> 4;

        int stepX = Integer.compare(endCx, cx);
        int stepZ = Integer.compare(endCz, cz);

        double tMaxX, tMaxZ;
        double tDeltaX, tDeltaZ;

        if (stepX == 0) {
            tMaxX = Double.POSITIVE_INFINITY;
            tDeltaX = Double.POSITIVE_INFINITY;
        } else {
            double nextBoundaryX = (stepX > 0) ? ((cx + 1) << 4) : (cx << 4);
            tMaxX = (nextBoundaryX - sx) / ddx;
            tDeltaX = 16.0D / Math.abs(ddx);
        }

        if (stepZ == 0) {
            tMaxZ = Double.POSITIVE_INFINITY;
            tDeltaZ = Double.POSITIVE_INFINITY;
        } else {
            double nextBoundaryZ = (stepZ > 0) ? ((cz + 1) << 4) : (cz << 4);
            tMaxZ = (nextBoundaryZ - sz) / ddz;
            tDeltaZ = 16.0D / Math.abs(ddz);
        }

        int rChunks = (pad <= 0.0D) ? 0 : (int) Math.ceil(pad * 0.0625D);

        LongOpenHashSet visited = TL_VISITED_CHUNKS.get();
        visited.clear();

        Entity bestEntity = null;
        double bestT = Double.POSITIVE_INFINITY;
        double bestHitX = 0.0D, bestHitY = 0.0D, bestHitZ = 0.0D;

        double tPrev = 0.0D;
        int maxSteps = 2 + Math.abs(endCx - cx) + Math.abs(endCz - cz);

        for (int steps = 0; steps < maxSteps; steps++) {
            for (int ox = -rChunks; ox <= rChunks; ox++) {
                for (int oz = -rChunks; oz <= rChunks; oz++) {
                    int scx = cx + ox;
                    int scz = cz + oz;

                    long key = ChunkPos.asLong(scx, scz);
                    if (!visited.add(key)) {
                        continue;
                    }

                    Chunk chunk = prov.getLoadedChunk(scx, scz);
                    if (chunk == null) {
                        continue;
                    }

                    ClassInheritanceMultiMap<Entity>[] lists = chunk.entityLists;

                    for (int sec = minSec; sec <= maxSec; sec++) {
                        ClassInheritanceMultiMap<Entity> map = lists[sec];
                        if (map.isEmpty()) continue;

                        List<Entity> values = map.values;
                        //noinspection ForLoopReplaceableByForEach
                        for (int i = 0, n = values.size(); i < n; i++) {
                            Entity e = values.get(i);
                            if (e == exclude) continue;
                            if (e instanceof EntityPlayer p && p.isSpectator()) continue;
                            if (hasFilter && !extraFilter.test(e)) continue;
                            if (e.noClip || !e.canBeCollidedWith()) continue;

                            AxisAlignedBB eb = e.getEntityBoundingBox();
                            if (aabbNonIntersect(minX, minY, minZ, maxX, maxY, maxZ, eb)) continue;

                            double t = intersectSegmentAabbHitTInv(sx, sy, sz, dx0, dy0, dz0, invDx, invDy, invDz, eb.minX - inflate, eb.minY - inflate, eb.minZ - inflate, eb.maxX + inflate, eb.maxY + inflate, eb.maxZ + inflate);
                            if (!Double.isNaN(t) && t < bestT) {
                                bestT = t;
                                bestEntity = e;
                                if (t == 0.0D) {
                                    return new RayTraceResult(e, start);
                                }
                                bestHitX = sx + ddx * t;
                                bestHitY = sy + ddy * t;
                                bestHitZ = sz + ddz * t;
                            }

                            Entity[] parts = e.getParts();
                            if (parts != null) {
                                for (Entity part : parts) {
                                    if (part == exclude) continue;
                                    if (part instanceof EntityPlayer pp && pp.isSpectator()) continue;
                                    if (hasFilter && !extraFilter.test(part)) continue;
                                    if (part.noClip || !part.canBeCollidedWith()) continue;

                                    AxisAlignedBB pb = part.getEntityBoundingBox();
                                    if (aabbNonIntersect(minX, minY, minZ, maxX, maxY, maxZ, pb)) continue;

                                    double tp = intersectSegmentAabbHitTInv(sx, sy, sz, dx0, dy0, dz0, invDx, invDy, invDz, pb.minX - inflate, pb.minY - inflate, pb.minZ - inflate, pb.maxX + inflate, pb.maxY + inflate, pb.maxZ + inflate);
                                    if (!Double.isNaN(tp) && tp < bestT) {
                                        bestT = tp;
                                        bestEntity = part;
                                        if (tp == 0.0D) {
                                            return new RayTraceResult(part, start);
                                        }
                                        bestHitX = sx + ddx * tp;
                                        bestHitY = sy + ddy * tp;
                                        bestHitZ = sz + ddz * tp;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (bestT <= tPrev) {
                break;
            }

            if (cx == endCx && cz == endCz) {
                break;
            }

            // 2D DDA step, Z on tie
            double tStep;
            if (tMaxX < tMaxZ) {
                tStep = tMaxX;
                cx += stepX;
                tMaxX += tDeltaX;
            } else {
                tStep = tMaxZ;
                cz += stepZ;
                tMaxZ += tDeltaZ;
            }

            tPrev = tStep;
        }

        return (bestEntity != null) ? new RayTraceResult(bestEntity, new Vec3d(bestHitX, bestHitY, bestHitZ)) : null;
    }

    private static boolean aabbNonIntersect(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, AxisAlignedBB b) {
        return !(maxX > b.minX) || !(minX < b.maxX) || !(maxY > b.minY) || !(minY < b.maxY) || !(maxZ > b.minZ) || !(minZ < b.maxZ);
    }

    /**
     * Slab intersection on segment P(t)=S + D*t with t in [0,1], using precomputed inverse components.
     * Returns:
     * - entry t if starting outside the box
     * - exit t if starting inside (closer to AxisAlignedBB#calculateIntercept)
     * NaN if no hit.
     */
    private static double intersectSegmentAabbHitTInv(double sx, double sy, double sz, boolean dx0, boolean dy0, boolean dz0, double invDx, double invDy, double invDz, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double tEnter = 0.0D;
        double tExit = 1.0D;

        if (dx0) {
            if (sx < minX || sx > maxX) return Double.NaN;
        } else {
            double t1 = (minX - sx) * invDx;
            double t2 = (maxX - sx) * invDx;
            double lo = Math.min(t1, t2);
            double hi = Math.max(t1, t2);
            tEnter = Math.max(tEnter, lo);
            tExit = Math.min(tExit, hi);
            if (tExit < tEnter) return Double.NaN;
        }

        if (dy0) {
            if (sy < minY || sy > maxY) return Double.NaN;
        } else {
            double t1 = (minY - sy) * invDy;
            double t2 = (maxY - sy) * invDy;
            double lo = Math.min(t1, t2);
            double hi = Math.max(t1, t2);
            tEnter = Math.max(tEnter, lo);
            tExit = Math.min(tExit, hi);
            if (tExit < tEnter) return Double.NaN;
        }

        if (dz0) {
            if (sz < minZ || sz > maxZ) return Double.NaN;
        } else {
            double t1 = (minZ - sz) * invDz;
            double t2 = (maxZ - sz) * invDz;
            double lo = Math.min(t1, t2);
            double hi = Math.max(t1, t2);
            tEnter = Math.max(tEnter, lo);
            tExit = Math.min(tExit, hi);
            if (tExit < tEnter) return Double.NaN;
        }

        boolean inside = sx >= minX && sx <= maxX && sy >= minY && sy <= maxY && sz >= minZ && sz <= maxZ;
        double tHit = inside ? tExit : tEnter;

        if (tHit < 0.0D || tHit > 1.0D) return Double.NaN;
        return tHit;
    }

    /**
     * Attempts to export a list of items to an external inventory or conveyor belt at a given position.
     * It first tries to insert into an IItemHandler (chest, etc.), then tries to place on an IConveyorBelt.
     *
     * @param world         The world object.
     * @param exportToPos   The block position of the target inventory/conveyor.
     * @param accessSide    The direction from which the target block is being accessed.
     * @param itemsToExport A list of ItemStacks to be exported. This list will not be modified.
     * @return A new list containing any leftover ItemStacks that could not be fully exported. Returns an empty list on full success.
     */
    public static @NotNull List<ItemStack> popProducts(@NotNull World world, @NotNull BlockPos exportToPos, @NotNull ForgeDirection accessSide, @NotNull List<ItemStack> itemsToExport) {
        return popProducts(world, exportToPos, Objects.requireNonNull(accessSide.toEnumFacing()), itemsToExport);
    }

    /**
     * Attempts to export a list of items to an external inventory or conveyor belt at a given position.
     * It first tries to insert into an IItemHandler (chest, etc.), then tries to place on an IConveyorBelt.
     *
     * @param world         The world object.
     * @param exportToPos   The block position of the target inventory/conveyor.
     * @param accessSide    The direction from which the target block is being accessed.
     * @param itemsToExport A list of ItemStacks to be exported. This list will not be modified.
     * @return A new list containing any leftover ItemStacks that could not be fully exported. Returns an empty list on full success.
     */
    public static @NotNull List<ItemStack> popProducts(@NotNull World world, @NotNull BlockPos exportToPos, @NotNull EnumFacing accessSide, @NotNull List<ItemStack> itemsToExport) {
        if (itemsToExport.isEmpty()) return Collections.emptyList();
        List<ItemStack> remainingItems = new ArrayList<>();

        TileEntity tile = world.getTileEntity(exportToPos);
        if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide)) {
            IItemHandler inv = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide);
            if (inv != null) {
                for (ItemStack stack : itemsToExport) {
                    ItemStack remainder = ItemHandlerHelper.insertItemStacked(inv, stack, false);
                    if (!remainder.isEmpty()) remainingItems.add(remainder);
                }
            }
        }

        if (remainingItems.isEmpty()) {
            return Collections.emptyList();
        }

        Block block = world.getBlockState(exportToPos).getBlock();
        if (block instanceof IConveyorBelt belt) {
            Vec3d base = new Vec3d(exportToPos.getX() + 0.5, exportToPos.getY() + 0.5, exportToPos.getZ() + 0.5);
            ListIterator<ItemStack> iterator = remainingItems.listIterator();
            while (iterator.hasNext()) {
                ItemStack item = iterator.next();
                Vec3d vec = belt.getClosestSnappingPosition(world, exportToPos, base);
                EntityMovingItem moving = new EntityMovingItem(world);
                moving.setPosition(base.x, vec.y, base.z);
                moving.setItemStack(item.copy());
                if (world.spawnEntity(moving)) {
                    iterator.set(ItemStack.EMPTY);
                }
            }
            remainingItems.removeIf(ItemStack::isEmpty);
        }

        return remainingItems;
    }


    /**
     * Attempts to export items from a source inventory slot range [from, to] to an external
     * inventory or conveyor belt at a given position. It first tries to insert into an
     * IItemHandler (chest, etc.), then tries to place on an IConveyorBelt.
     * <p>
     * All modifications happen in-place on the provided {@code inventory}. This method returns void.
     *
     * @param world       The world object.
     * @param exportToPos The block position of the target inventory/conveyor.
     * @param accessSide  The direction from which the target block is being accessed.
     * @param inventory   The source inventory to export from.
     * @param from        Inclusive start slot index in the source inventory.
     * @param to          Inclusive end slot index in the source inventory.
     */
    public static void popProducts(@NotNull World world, @NotNull BlockPos exportToPos, @NotNull ForgeDirection accessSide, @NotNull IItemHandler inventory, int from, int to) {
        popProducts(world, exportToPos, Objects.requireNonNull(accessSide.toEnumFacing()), inventory, from, to);
    }

    /**
     * Attempts to export items from a source inventory slot range [from, to] to an external
     * inventory or conveyor belt at a given position. It first tries to insert into an
     * IItemHandler (chest, etc.), then tries to place on an IConveyorBelt.
     * <p>
     * All modifications happen in-place on the provided {@code inventory}. This method returns void.
     *
     * @param world       The world object.
     * @param exportToPos The block position of the target inventory/conveyor.
     * @param accessSide  The direction from which the target block is being accessed.
     * @param inventory   The source inventory to export from.
     * @param from        Inclusive start slot index in the source inventory, inclusive.
     * @param to          Inclusive end slot index in the source inventory, inclusive.
     */
    public static void popProducts(@NotNull World world, @NotNull BlockPos exportToPos, @NotNull EnumFacing accessSide, @NotNull IItemHandler inventory, int from, int to) {
        int slots = inventory.getSlots();
        if (slots <= 0) return;

        int start = Math.max(0, from);
        int end = Math.min(to, slots - 1);
        if (start > end) return;
        TileEntity tile = world.getTileEntity(exportToPos);
        IItemHandler target = null;
        if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide)) {
            target = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide);
        }

        if (target != null) {
            for (int slot = start; slot <= end; slot++) {
                while (true) {
                    ItemStack toMoveSim = inventory.extractItem(slot, Integer.MAX_VALUE, true);
                    if (toMoveSim.isEmpty()) break;
                    ItemStack leftover = ItemHandlerHelper.insertItemStacked(target, toMoveSim, false);
                    int inserted = toMoveSim.getCount() - (leftover.isEmpty() ? 0 : leftover.getCount());
                    if (inserted <= 0) break;
                    inventory.extractItem(slot, inserted, false);
                }
            }
        }

        Block block = world.getBlockState(exportToPos).getBlock();
        if (block instanceof IConveyorBelt belt) {
            if (world.isRemote) return;
            Vec3d base = new Vec3d(exportToPos.getX() + 0.5, exportToPos.getY() + 0.5, exportToPos.getZ() + 0.5);
            for (int slot = start; slot <= end; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                Vec3d vec = belt.getClosestSnappingPosition(world, exportToPos, base);
                EntityMovingItem moving = new EntityMovingItem(world);
                moving.setPosition(base.x, vec.y, base.z);
                moving.setItemStack(stack.copy());
                if (world.spawnEntity(moving)) {
                    inventory.extractItem(slot, stack.getCount(), false);
                }
            }
        }
    }


    /**
     * Attempts to pull items for a given recipe from a source inventory into a destination inventory.
     *
     * @param sourceContainer      The IItemHandler of the inventory to pull from.
     * @param sourceSlots          The specific slots in the source inventory that can be accessed.
     * @param destinationInventory The IItemHandlerModifiable of the machine's inventory to pull into.
     * @param recipeIngredients    A list of AStacks representing the required ingredients.
     * @param sourceTE             The TileEntity of the source inventory, used for canExtractItem checks. Can be null.
     * @param destStartSlot        The starting slot index (inclusive) of the ingredient area in the destination inventory.
     * @param destEndSlot          The ending slot index (inclusive) of the ingredient area in the destination inventory.
     * @param finalizeBy           A Runnable that is executed if at least one item is successfully pulled. Can be null.
     * @return true if any items were successfully pulled, false otherwise.
     */
    @CanIgnoreReturnValue
    public static boolean pullItemsForRecipe(@NotNull IItemHandler sourceContainer, int @NotNull [] sourceSlots, @NotNull IItemHandlerModifiable destinationInventory, @NotNull List<AStack> recipeIngredients, @Nullable TileEntityMachineBase sourceTE, int destStartSlot, int destEndSlot, @Nullable Runnable finalizeBy) {
        if (recipeIngredients.isEmpty() || sourceSlots.length == 0) return false;
        boolean itemsPulled = false;

        int[] srcSlotIdx = new int[sourceSlots.length];
        ItemStack[] srcMatch = new ItemStack[sourceSlots.length];
        int[] srcRemaining = new int[sourceSlots.length];
        int srcCount = 0;

        for (int slot : sourceSlots) {
            ItemStack stack = sourceContainer.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            int count = stack.getCount();
            if (count <= 0) continue;

            ItemStack match = stack.copy();
            match.setCount(1);

            srcSlotIdx[srcCount] = slot;
            srcMatch[srcCount] = match;
            srcRemaining[srcCount] = count;
            srcCount++;
        }

        if (srcCount == 0) return false;

        for (AStack recipeIngredient : recipeIngredients) {
            if (recipeIngredient == null || recipeIngredient.count() <= 0) {
                continue;
            }

            AStack singleIngredient = recipeIngredient.copy().singulize();
            int maxStackSize = singleIngredient.getStack().getMaxStackSize();
            if (maxStackSize <= 0) maxStackSize = 1;
            int slotsNeeded = (int) Math.ceil((double) recipeIngredient.count() / maxStackSize);

            IntArrayList partialSlots = new IntArrayList();
            IntArrayList fullSlots = new IntArrayList();
            IntArrayList emptySlots = new IntArrayList();

            for (int i = destStartSlot; i <= destEndSlot; i++) {
                ItemStack destStack = destinationInventory.getStackInSlot(i);
                if (destStack.isEmpty()) {
                    emptySlots.add(i);
                    continue;
                }

                ItemStack compareStack;
                if (destStack.getCount() == 1) {
                    compareStack = destStack;
                } else {
                    compareStack = destStack.copy();
                    compareStack.setCount(1);
                }

                if (singleIngredient.isApplicable(compareStack)) {
                    if (destStack.getCount() < destStack.getMaxStackSize()) {
                        partialSlots.add(i);
                    } else {
                        fullSlots.add(i);
                    }
                }
            }
            int slotsOccupied = partialSlots.size() + fullSlots.size();

            for (int destSlot : partialSlots) {
                ItemStack destStack = destinationInventory.getStackInSlot(destSlot);
                int amountToPull = destStack.getMaxStackSize() - destStack.getCount();
                if (amountToPull <= 0) continue;
                itemsPulled = tryPull(sourceContainer, destinationInventory, sourceTE, itemsPulled, srcSlotIdx, srcMatch, srcRemaining, srcCount, singleIngredient, destSlot, amountToPull);
            }

            int newSlotsToFill = slotsNeeded - slotsOccupied;
            if (newSlotsToFill > 0) {
                for (int i = 0; i < newSlotsToFill && i < emptySlots.size(); i++) {
                    int destSlot = emptySlots.getInt(i);
                    itemsPulled = tryPull(sourceContainer, destinationInventory, sourceTE, itemsPulled, srcSlotIdx, srcMatch, srcRemaining, srcCount, singleIngredient, destSlot, maxStackSize);
                }
            }
        }

        if (itemsPulled && finalizeBy != null) {
            finalizeBy.run();
        }

        return itemsPulled;
    }

    private static boolean tryPull(@NotNull IItemHandler sourceContainer, @NotNull IItemHandlerModifiable destinationInventory, @Nullable TileEntityMachineBase sourceTE, boolean itemsPulled, int[] srcSlotIdx, ItemStack[] srcMatch, int[] srcRemaining, int srcCount, AStack singleIngredient, int destSlot, int amountToPull) {
        for (int i = 0; i < srcCount && amountToPull > 0; i++) {
            int remaining = srcRemaining[i];
            if (remaining <= 0) continue;
            if (!singleIngredient.isApplicable(srcMatch[i])) continue;
            int sourceSlot = srcSlotIdx[i];
            int pullThisTime = Math.min(amountToPull, remaining);
            if (sourceTE != null) {
                ItemStack live = sourceContainer.getStackInSlot(sourceSlot);
                if (live.isEmpty()) {
                    srcRemaining[i] = 0;
                    continue;
                }
                if (live.getCount() < pullThisTime) pullThisTime = live.getCount();
                if (pullThisTime <= 0) {
                    srcRemaining[i] = 0;
                    continue;
                }
                if (!sourceTE.canExtractItem(sourceSlot, live, pullThisTime)) continue;
            }

            ItemStack extracted = sourceContainer.extractItem(sourceSlot, pullThisTime, false);
            if (extracted.isEmpty()) continue;

            int got = extracted.getCount();
            if (got <= 0) continue;

            srcRemaining[i] = remaining - got;

            ItemStack destStack = destinationInventory.getStackInSlot(destSlot);
            if (destStack.isEmpty()) {
                destinationInventory.setStackInSlot(destSlot, extracted);
            } else {
                destStack.grow(got);
                destinationInventory.setStackInSlot(destSlot, destStack);
            }

            amountToPull -= got;
            itemsPulled = true;
        }

        return itemsPulled;
    }

    // ----------------- Vanilla Encoding -----------------

    /**
     * Identical to {@link BlockPos#toLong()}
     */
    public static long blockPosToLong(int x, int y, int z) {
        return ((long) x & 0x03FF_FFFF) << 38 | ((long) y & 0x0000_0FFF) << 26 | ((long) z & 0x03FF_FFFF);
    }

    public static int getBlockPosX(long serialized) {
        return (int) (serialized >> 38);
    }

    public static int getBlockPosY(long serialized) {
        return (int) (serialized << 26 >> 52);
    }

    public static int getBlockPosZ(long serialized) {
        return (int) (serialized << 38 >> 38);
    }

    public static long shiftBlockPos(long serialized, int dx, int dy, int dz) {
        return (serialized + (((long) dx) << 38)) & 0xFFFF_FFC0_0000_0000L | (serialized + (((long) dy) << 26)) & 0x0000_003F_FC00_0000L | (serialized + (long) dz) & 0x0000_0000_03FF_FFFFL;
    }

    public static long shiftBlockPos(long serialized, EnumFacing e) {
        return shiftBlockPos(serialized, e.getXOffset(), e.getYOffset(), e.getZOffset());
    }

    public static long shiftBlockPos(long serialized, EnumFacing e, int n) {
        return shiftBlockPos(serialized, e.getXOffset() * n, e.getYOffset() * n, e.getZOffset() * n);
    }

    @Contract(mutates = "param1")
    public static BlockPos.@NotNull MutableBlockPos fromLong(@NotNull BlockPos.MutableBlockPos pos, long serialized) {
        pos.setPos(getBlockPosX(serialized), getBlockPosY(serialized), getBlockPosZ(serialized));
        return pos;
    }

    public static long blockPosToChunkLong(long serialized) {
        return ((serialized >> 42) & 0xFFFFFFFFL) | ((serialized << 38 >> 42) << 32);
    }

    public static int getChunkPosX(long ck) {
        return (int) ck;
    }

    public static int getChunkPosZ(long ck) {
        return (int) (ck >>> 32);
    }

    public static long shiftChunkPos(long ck, int dx, int dz) {
        return ((ck + ((dz & 0xFFFF_FFFFL) << 32)) & 0xFFFF_FFFF_0000_0000L) | ((ck +  (dx & 0xFFFF_FFFFL))     & 0xFFFF_FFFFL);
    }

    public static long shiftChunkPosX(long ck, int dx) {
        return (ck & 0xFFFF_FFFF_0000_0000L)
                | ((ck + (dx & 0xFFFF_FFFFL)) & 0xFFFF_FFFFL);
    }

    public static long shiftChunkPosZ(long ck, int dz) {
        return ((ck + ((dz & 0xFFFF_FFFFL) << 32)) & 0xFFFF_FFFF_0000_0000L)
                |  (ck & 0xFFFF_FFFFL);
    }

    /**
     * Identical to {@link net.minecraft.world.chunk.BlockStateContainer#getIndex(int, int, int)}
     */
    public static int packLocal(int localX, int localY, int localZ) {
        return (localY << 8) | (localZ << 4) | localX;
    }

    public static int blockPosToLocal(int x, int y, int z) {
        return ((y & 0xF) << 8) | ((z & 0xF) << 4) | (x & 0xF);
    }

    public static int blockPosToLocal(BlockPos pos) {
        return blockPosToLocal(pos.getX(), pos.getY(), pos.getZ());
    }

    public static int blockPosToLocal(long serialized) {
        return ((int) (serialized >> 18) & 0xF00) | ((int) (serialized << 4) & 0xF0) | ((int) (serialized >> 38) & 0xF);
    }

    public static int getLocalX(int packed) {
        return packed & 0xF;
    }

    public static int getLocalY(int packed) {
        return (packed >>> 8) & 0xF;
    }

    public static int getLocalZ(int packed) {
        return (packed >>> 4) & 0xF;
    }

    public static int setLocalX(int packed, int newX) {
        return (packed & ~0xF) | (newX & 0xF);
    }

    public static int setLocalY(int packed, int newY) {
        return (packed & ~0xF00) | ((newY & 0xF) << 8);
    }

    public static int setLocalZ(int packed, int newZ) {
        return (packed & ~0xF0) | ((newZ & 0xF) << 4);
    }

    // ----------------- Custom Encoding -----------------

    /**
     * chunkX, chunkZ ∈ [-2_097_152, 2_097_151] (±33.5M blocks)
     * subY ∈ [-524_288, 524_287] (±8.3M blocks)
     * Its codec is X22 | Z22 | Y20, which has poor avalanche if using HashCommon#mix for hashcode
     * or any other finalizer with only one multiplication. SectionKeyHash#hash is recommended.
     */
    public static long sectionToLong(int chunkX, @Range(from = -524_288, to = 524_287) int subY, int chunkZ) {
        return ((((long) chunkX) & 0x3FFFFFL) << 42) | ((((long) chunkZ) & 0x3FFFFFL) << 20) | (((long) subY) & 0xFFFFFL);
    }

    public static long sectionToLong(long ck, @Range(from = -524_288, to = 524_287) int subY) {
        return (ck << 42) | ((ck >>> 12) & 0x0000_03FF_FFF0_0000L) | (((long) subY) & 0xFFFFFL);
    }

    public static long sectionToLong(ChunkPos pos, @Range(from = -524_288, to = 524_287) int subY) {
        return sectionToLong(pos.x, subY, pos.z);
    }

    public static int getSectionX(long key) {
        return (int) (key >> 42);
    }

    public static int getSectionY(long key) {
        return (int) (key << 44 >> 44);
    }

    public static int getSectionZ(long key) {
        return (int) (key << 22 >> 42);
    }

    public static long setSectionX(long key, int chunkX) {
        return (key & ~(0x3FFFFFL << 42)) | ((((long) chunkX) & 0x3FFFFFL) << 42);
    }

    public static long setSectionY(long key, @Range(from = -524_288, to = 524_287) int subY) {
        return (key & ~0xFFFFFL) | (((long) subY) & 0xFFFFFL);
    }

    public static long setSectionZ(long key, int chunkZ) {
        return (key & ~(0x3FFFFFL << 20)) | ((((long) chunkZ) & 0x3FFFFFL) << 20);
    }

    public static long shiftSection(long key, int dx, int dy, int dz) {
        return ((((key >>> 42) + (long) dx) & 0x3FFFFFL) << 42)
                | ((((((key >>> 20) & 0x3FFFFFL) + (long) dz) & 0x3FFFFFL) << 20))
                | ((((key & 0xFFFFFL) + ((long) dy & 0xFFFFFL)) & 0xFFFFFL));
    }

    public static long shiftSection(long key, long delta) {
        return ((((key >>> 42) + (delta >>> 42)) & 0x3FFFFFL) << 42)
                | ((((((key >>> 20) & 0x3FFFFFL) + ((delta >>> 20) & 0x3FFFFFL)) & 0x3FFFFFL) << 20))
                | ((((key & 0xFFFFFL) + (delta & 0xFFFFFL)) & 0xFFFFFL));
    }

    public static long shiftSectionX(long key, int dx) {
        return (key & ~(0x3FFFFFL << 42)) | (((key >>> 42) + dx & 0x3FFFFFL) << 42);
    }

    public static long shiftSectionY(long key, int dy) {
        return (key & ~0xFFFFFL) | (key & 0xFFFFFL) + dy & 0xFFFFFL;
    }

    public static long shiftSectionZ(long key, int dz) {
        return (key & ~(0x3FFFFFL << 20)) | ((((key >>> 20) & 0x3FFFFFL) + dz & 0x3FFFFFL) << 20);
    }

    public static long sectionToChunkLong(long sck) {
        return (((sck << 22) >> 10) & 0xFFFF_FFFF_0000_0000L) | ((sck >> 42) & 0xFFFF_FFFFL);
    }

    public static long blockPosToSectionLong(int x, int y, int z) {
        return sectionToLong(x >> 4, y >> 4, z >> 4);
    }

    public static long blockPosToSectionLong(long serialized) {
        return (((serialized >>> 42) & 0x3FFFFFL) << 42) | (((serialized >>> 4) & 0x3FFFFFL) << 20) | (serialized << 26 >> 56) & 0xFFFFFL;
    }

    public static long blockPosToSectionLong(BlockPos pos) {
        return sectionToLong(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
    }

    @Nullable
    public static <T extends Comparable<T>> IBlockState changeBlockState(Block trueState, Block falseState, IBlockState state, IProperty<T> preservingProperty, boolean flag) {
        if (!state.getPropertyKeys().contains(preservingProperty)) return null;
        Block current = state.getBlock();
        if (current != trueState && current != falseState) return null;
        T value = state.getValue(preservingProperty);
        IBlockState newState = flag ? trueState.getDefaultState() : falseState.getDefaultState();
        if (newState.getBlock() == current) return null;
        return newState.withProperty(preservingProperty, value);
    }

    @Nullable
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static IBlockState changeBlockState(Block trueState, Block falseState, IBlockState state, boolean flag, IProperty<?>... preserveProps) {
        Block current = state.getBlock();
        if (current != trueState && current != falseState) return null;
        IBlockState newState = flag ? trueState.getDefaultState() : falseState.getDefaultState();
        if (newState.getBlock() == current) return null;

        if (preserveProps != null) {
            for (IProperty<?> p : preserveProps) {
                if (p == null) continue;
                if (state.getPropertyKeys().contains(p) && newState.getPropertyKeys().contains(p)) {
                    Comparable val = state.getValue((IProperty) p);
                    newState = newState.withProperty((IProperty) p, val);
                }
            }
        }
        return newState;
    }

    public static boolean isSwappingBetweenVariants(IBlockState state1, IBlockState state2, Block validBlock1, Block validBlock2) {
        return (state1.getBlock() == validBlock1 || state1.getBlock() == validBlock2) && (state2.getBlock() == validBlock1 || state2.getBlock() == validBlock2);
    }

    // mlbv: remove and replace it with Thread.onSpinWait() if we ever migrate to Java 9+
    public static void onSpinWait() {
        SPIN_WAITER.run();
    }

    public static int nextIntDeterministic(long seed, int chunkX, int chunkZ, @Range(from = 1, to = Integer.MAX_VALUE) int bound) {
        long state = seed ^ ChunkPos.asLong(chunkX, chunkZ);
        final long threshold = Integer.remainderUnsigned(-bound, bound) & 0xffff_ffffL;
        while (true) {
            state += 0x9E3779B97F4A7C15L;
            long z = HashCommon.murmurHash3(state);
            long r = z >>> 32;
            long m = r * (long) bound;
            if ((m & 0xffff_ffffL) >= threshold) {
                return (int) (m >>> 32);
            }
        }
    }

    public static long fnv1a64(ByteBuf buf) {
        long hash = 0xcbf29ce484222325L;
        int len = buf.readableBytes();
        if (buf.hasMemoryAddress()) {
            long addr = buf.memoryAddress() + buf.readerIndex();
            long end = addr + len;
            for (; addr < end; addr++) {
                hash ^= (U.getByte(addr) & 0xffL);
                hash *= 0x100000001b3L;
            }
        } else if (buf.hasArray()) {
            byte[] arr = buf.array();
            long offset = BA_BASE + buf.arrayOffset() + buf.readerIndex();
            long end = offset + len;
            for (; offset < end; offset++) {
                hash ^= (U.getByte(arr, offset) & 0xffL);
                hash *= 0x100000001b3L;
            }
        } else {
            int start = buf.readerIndex();
            for (int i = 0; i < len; i++) {
                hash ^= (buf.getByte(start + i) & 0xffL);
                hash *= 0x100000001b3L;
            }
        }
        return hash;
    }

    public static long fnv1a64(String s) {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        long hash = 0xcbf29ce484222325L;
        for (byte b : data) {
            hash ^= (b & 0xFFL);
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
