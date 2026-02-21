package com.hbm.entity.mob;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockTaint;
import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IRadiationImmune;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

@AutoRegister(name = "entity_tainted_creeper", trackingRange = 80, eggColors = {0x813b9b, 0xd71fdd})
public class EntityCreeperTainted extends EntityCreeper implements IRadiationImmune {

    public EntityCreeperTainted(World world) {
        super(world);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.isEntityAlive() && this.getHealth() < this.getMaxHealth() && this.ticksExisted % 10 == 0) {
            this.heal(1.0F);
        }
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    protected Item getDropItem() {
        return Item.getItemFromBlock(Blocks.TNT);
    }

    @Override
    protected void explode() {
        if (!this.world.isRemote) {
            boolean isPowered = this.getPowered();
            boolean griefing = ForgeEventFactory.getMobGriefingEvent(this.world, this);
            this.world.newExplosion(this, this.posX, this.posY, this.posZ, 5.0F, false, griefing);

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            if(griefing) {
                if (isPowered) {
                    for (int i = 0; i < 255; i++) {
                        int a = this.rand.nextInt(15) + (int) this.posX - 7;
                        int b = this.rand.nextInt(15) + (int) this.posY - 7;
                        int c = this.rand.nextInt(15) + (int) this.posZ - 7;
                        pos.setPos(a, b, c);
                        IBlockState state = this.world.getBlockState(pos);
                        if (state.isNormalCube() && !state.getBlock().isAir(state, world, pos)) {
                            if (GeneralConfig.enableHardcoreTaint)
                                this.world.setBlockState(pos, ModBlocks.taint.getDefaultState().withProperty(BlockTaint.TAINTAGE, this.rand.nextInt(3) + 5), 2);
                            else
                                this.world.setBlockState(pos, ModBlocks.taint.getDefaultState().withProperty(BlockTaint.TAINTAGE, this.rand.nextInt(3)), 2);
                        }
                    }
                } else {
                    for (int i = 0; i < 85; i++) {
                        int a = this.rand.nextInt(7) + (int) this.posX - 3;
                        int b = this.rand.nextInt(7) + (int) this.posY - 3;
                        int c = this.rand.nextInt(7) + (int) this.posZ - 3;
                        pos.setPos(a, b, c);
                        IBlockState state = this.world.getBlockState(pos);
                        if (state.isNormalCube() && !state.getBlock().isAir(state, world, pos)) {
                            if (GeneralConfig.enableHardcoreTaint)
                                this.world.setBlockState(pos, ModBlocks.taint.getDefaultState().withProperty(BlockTaint.TAINTAGE, this.rand.nextInt(6) + 10), 2);
                            else
                                this.world.setBlockState(pos, ModBlocks.taint.getDefaultState().withProperty(BlockTaint.TAINTAGE, this.rand.nextInt(3) + 4), 2);
                        }
                    }
                }
            }

            this.setDead();
        }
    }
}
