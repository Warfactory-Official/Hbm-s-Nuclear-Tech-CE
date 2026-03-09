package com.hbm.tileentity.network;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerRadioTorchSender;
import com.hbm.inventory.gui.GUIScreenRadioTorch;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.hbm.blocks.network.RadioTorchBase.LIT;

@AutoRegister
public class TileEntityRadioTorchSender extends TileEntityRadioTorchBase implements IGUIProvider {

    @Override
    public void update() {

        if (!world.isRemote) {
            EnumFacing facing = getTorchFacing();
            BlockPos inputPos = pos.offset(facing.getOpposite());
            IBlockState inputState = world.getBlockState(inputPos);

            int input = world.getRedstonePower(inputPos, facing);
            if (inputState.hasComparatorInputOverride()) {
                input = inputState.getComparatorInputOverride(world, inputPos);
            }
            input = MathHelper.clamp(input, 0, 15);

            boolean shouldSend = this.polling;

            if (input != this.lastState) {
                this.lastState = input;

                IBlockState state = world.getBlockState(pos);

                boolean oldLit = state.getValue(LIT);
                boolean newLit = this.lastState > 0;

                if (oldLit != newLit) {
                    world.setBlockState(pos, state.withProperty(LIT, newLit), 3);
                }

                this.markDirty();
                shouldSend = true;
            }

            if (shouldSend && !this.channel.isEmpty()) {
                RTTYSystem.broadcast(world, this.channel, this.customMap ? this.mapping[input] : (input + ""));
            }
        }

        super.update();
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerRadioTorchSender();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIScreenRadioTorch(this, true);
    }
}
