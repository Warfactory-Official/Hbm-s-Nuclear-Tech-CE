package com.hbm.items.special;

import java.util.List;

import com.hbm.blocks.rail.IRailNTM;
import com.hbm.blocks.rail.IRailNTM.MoveContext;
import com.hbm.blocks.rail.IRailNTM.RailCheckType;
import com.hbm.entity.train.EntityRailCarBase;
import com.hbm.entity.train.TrainCargoTram;
import com.hbm.entity.train.TrainCargoTramTrailer;
import com.hbm.items.ItemEnumMulti;
import com.hbm.util.EnumUtil;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemTrain extends ItemEnumMulti<ItemTrain.EnumTrainType> {

    public ItemTrain(String s) {
        super(s, EnumTrainType.VALUES, true, true);
        this.setCreativeTab(null);
        this.setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        EnumTrainType train = EnumUtil.grabEnumSafely(EnumTrainType.class, stack.getItemDamage());

        if(train.engine != null) tooltip.add(TextFormatting.GREEN + "Engine: " + TextFormatting.RESET + train.engine);
        tooltip.add(TextFormatting.GREEN + "Gauge: " + TextFormatting.RESET + train.gauge);
        if(train.maxSpeed != null) tooltip.add(TextFormatting.GREEN + "Max Speed: " + TextFormatting.RESET + train.maxSpeed);
        if(train.acceleration != null) tooltip.add(TextFormatting.GREEN + "Acceleration: " + TextFormatting.RESET + train.acceleration);
        if(train.brakeThreshold != null) tooltip.add(TextFormatting.GREEN + "Engine Brake Threshold: " + TextFormatting.RESET + train.brakeThreshold);
        if(train.parkingBrake != null) tooltip.add(TextFormatting.GREEN + "Parking Brake: " + TextFormatting.RESET + train.parkingBrake);
    }

    public enum EnumTrainType {

        CARGO_TRAM(TrainCargoTram.class,                "Electric",     "Standard Gauge",   "10m/s",    "0.2m/s²",  "<1m/s",    "Yes"),
        CARGO_TRAM_TRAILER(TrainCargoTramTrailer.class, null,           "Standard Gauge",   "Yes",      null,       null,       "No");

        public static final EnumTrainType[] VALUES = values();

        public Class<? extends EntityRailCarBase> train;
        public String engine;
        public String maxSpeed;
        public String acceleration;
        public String brakeThreshold;
        public String parkingBrake;
        public String gauge;

        EnumTrainType(Class<? extends EntityRailCarBase> train, String engine, String gauge, String maxSpeed, String acceleration, String brakeThreshold, String parkingBrake) {
            this.train = train;
            this.engine = engine;
            this.maxSpeed = maxSpeed;
            this.acceleration = acceleration;
            this.brakeThreshold = brakeThreshold;
            this.parkingBrake = parkingBrake;
            this.gauge = gauge;
        }
    }

    @Override
    public @NotNull EnumActionResult onItemUse(EntityPlayer player, World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float fx, float fy, float fz) {

        ItemStack stack = player.getHeldItem(hand);
        Block b = world.getBlockState(pos).getBlock();

        if(b instanceof IRailNTM) {

            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            EnumTrainType type = EnumUtil.grabEnumSafely(EnumTrainType.class, stack.getItemDamage());
            EntityRailCarBase train = null;
            try { train = type.train.getConstructor(World.class).newInstance(world); } catch(Exception e) { }

            if(train != null && train.getGauge() == ((IRailNTM) b).getGauge(world, x, y, z)) {

                train.setPosition(x + fx, y + fy, z + fz);
                BlockPos anchor = train.getCurrentAnchorPos();
                train.rotationYaw = player.rotationYaw;
                Vec3d corePos = train.getRelPosAlongRail(anchor, 0, new MoveContext(RailCheckType.CORE, 0));
                if(corePos != null) {
                    train.setPosition(corePos.x, corePos.y, corePos.z);
                    Vec3d frontPos = train.getRelPosAlongRail(anchor, train.getLengthSpan(), new MoveContext(RailCheckType.FRONT, train.getCollisionSpan() - train.getLengthSpan()));
                    Vec3d backPos = train.getRelPosAlongRail(anchor, -train.getLengthSpan(), new MoveContext(RailCheckType.BACK, train.getCollisionSpan() - train.getLengthSpan()));
                    if(frontPos != null && backPos != null) {
                        if(!world.isRemote) {
                            train.rotationYaw = EntityRailCarBase.generateYaw(frontPos, backPos);
                            world.spawnEntity(train);
                        }
                        stack.shrink(1);
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }

        return EnumActionResult.PASS;
    }
}
