package com.hbm.util;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IKeypadHandler;
import com.hbm.packet.threading.ThreadedPacket;
import com.hbm.packet.toclient.KeypadClientPacket;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Keypad {

	public TileEntity te;
	public Button[] buttons = new Button[12];
	public byte successColorTicks = 0;
	public byte failColorTicks = 0;
	//If it's active, that means it needs to be updating its buttons, using full rendering, and sending the data to the server to be sent to any other clients.
	public boolean isActive = true;
	//Should only be on the server, the actual passcode.
	public int storedCode = -1;
	//The code currently entered into the keypad
	public byte[] code = new byte[] { -1, -1, -1, -1, -1, -1 };
	//Decides if the code is currently being set, activated by the reset button on the bottom left.
	public boolean isSettingCode = true;

	public Keypad(TileEntity te) {
		this.te = te;
		for(int i = 0; i < buttons.length; i++) {
			buttons[i] = new Button();
		}
	}

	public boolean isActive() {
		return isActive;
	}

	public void update() {
		boolean active = false;
		for(Button b : buttons) {
			if(b.cooldown > 0) {
				b.cooldown--;
				active = true;
			}
		}
		if(successColorTicks > 0){
			successColorTicks --;
			active = true;
		}
		if(failColorTicks > 0){
			failColorTicks --;
			active = true;
		}
		if(isSettingCode){
			active = true;
		}
		if(!te.getWorld().isRemote) {
			byte[] data = new byte[12+1+6+2];
			for(int i = 0; i < 12; i++) {
				data[i] = buttons[i].cooldown;
			}
			data[12] = (byte) (isSettingCode ? 1 : 0);
			for(int i = 0; i < 6; i ++){
				data[13 + i] = code[i];
			}
			data[19] = successColorTicks;
			data[20] = failColorTicks;
            ThreadedPacket message = new KeypadClientPacket(te.getPos(), data);
            PacketThreading.createAllAroundThreadedPacket(message,
                    new TargetPoint(te.getWorld().provider.getDimension(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), 10));
        }
		isActive = active;
	}

	public void buttonClicked(int id) {
		if(buttons[id].cooldown == 0) {
			buttons[id].cooldown = 20;
			byte num;
			switch(id) {
			case 9:
				//Reset key
				int newCode = buildIntCode();
				if(storedCode == newCode){
					isSettingCode = true;
				}
				clearCode();
				return;
			case 10:
				//Number 0
				num = 0;
				break;
			case 11:
				//Return key
				newCode = buildIntCode();
				if(isSettingCode){
					storedCode = newCode;
					successColorTicks = 20;
					isSettingCode = false;
					if(te instanceof IKeypadHandler){
						((IKeypadHandler) te).passwordSet();
					}
				} else if(storedCode == newCode) {
					if(te instanceof IKeypadHandler){
						((IKeypadHandler) te).keypadActivated();
					}
					successColorTicks = 20;
				} else {
					failColorTicks = 20;
				}
				clearCode();
				return;
			default:
				//Numbers from 1 to 9
				num = (byte) (id + 1);
			}
			if(num < 10 && code[code.length - 1] < 0) {
				successColorTicks = 0;
				failColorTicks = 0;
				shiftCode();
				code[0] = num;
			}
		}
	}

	public void shiftCode() {
		for(int i = code.length - 1; i > 0; i--) {
			code[i] = code[i - 1];
			code[i - 1] = -1;
		}
	}
	
	public void clearCode(){
		for(int i = 0; i < code.length; i ++){
			code[i] = -1;
		}
	}

	public int buildIntCode() {
		if(code[0] < 0)
			return -1;
		int num = 0;
		for(int i = code.length-1; i >= 0; i--) {
			if(code[i] < 0){
				continue;
			}
			num = num * 10 + code[i];
		}
		return num;
	}
	
	public NBTTagCompound writeToNbt(NBTTagCompound tag){
		tag.setByteArray("code", code);
		tag.setInteger("currentPassword", storedCode);
		tag.setBoolean("isSettingCode", isSettingCode);
		tag.setByte("successColorTicks", successColorTicks);
		tag.setByte("failColorTicks", failColorTicks);
		
		byte[] cooldowns = new byte[12];
		for(int i = 0; i < 12; i ++){
			cooldowns[i] = buttons[i].cooldown;
		}
		tag.setByteArray("buttonCooldowns", cooldowns);
		return tag;
	}
	
	public void readFromNbt(NBTTagCompound tag){
		byte[] readCode = tag.getByteArray("code");
		if(readCode.length == 6)
			code = readCode;
		storedCode = tag.getInteger("currentPassword");
		isSettingCode = tag.getBoolean("isSettingCode");
		successColorTicks = tag.getByte("successColorTicks");
		failColorTicks = tag.getByte("failColorTicks");
		byte[] buttonCooldowns = tag.getByteArray("buttonCooldowns");
		if(buttonCooldowns.length == 12){
			for(int i = 0; i < 12; i ++){
				buttons[i].cooldown = buttonCooldowns[i];
			}
		}
		isActive = true;
	}

	@SideOnly(Side.CLIENT)
	public KeypadClient client() {
		return (KeypadClient) this;
	}

	//I thought I needed a class for this, but it turns out that all a button needs is a cooldown. Oh well.
	public static class Button {
		public byte cooldown = 0;
	}
}
