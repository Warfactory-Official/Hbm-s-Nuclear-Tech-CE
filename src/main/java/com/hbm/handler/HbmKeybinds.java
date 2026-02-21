package com.hbm.handler;

import com.hbm.capability.HbmCapability;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.gui.GUICalculator;
import com.hbm.items.IKeybindReceiver;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.lib.internal.MethodHandleHelper;
import com.hbm.main.MainRegistry;
import com.hbm.packet.KeybindPacket;
import com.hbm.packet.PacketDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.lang.invoke.MethodHandle;

public class HbmKeybinds {

	public static final String category = "key.categories.hbm";
    private static final MethodHandle hashHandle = MethodHandleHelper.findStaticGetter(KeyBinding.class, "HASH", "field_74514_b", KeyBindingMap.class);

    public static KeyBinding calculatorKey = new KeyBinding(category + ".calculator", Keyboard.KEY_N, category);
	public static KeyBinding jetpackKey = new KeyBinding(category + ".toggleBack", Keyboard.KEY_C, category);
	public static KeyBinding hudKey = new KeyBinding(category + ".toggleHUD", Keyboard.KEY_V, category);
	public static KeyBinding reloadKey = new KeyBinding(category + ".reload", Keyboard.KEY_R, category);
	public static KeyBinding dashKey = new KeyBinding(category + ".dash", Keyboard.KEY_LSHIFT, category);

	public static KeyBinding craneUpKey = new KeyBinding(category + ".craneMoveUp", Keyboard.KEY_UP, category);
	public static KeyBinding craneDownKey = new KeyBinding(category + ".craneMoveDown", Keyboard.KEY_DOWN, category);
	public static KeyBinding craneLeftKey = new KeyBinding(category + ".craneMoveLeft", Keyboard.KEY_LEFT, category);
	public static KeyBinding craneRightKey = new KeyBinding(category + ".craneMoveRight", Keyboard.KEY_RIGHT, category);
	public static KeyBinding craneLoadKey = new KeyBinding(category + ".craneLoad", Keyboard.KEY_RETURN, category);

	public static KeyBinding qmaw = new KeyBinding(category + ".qmaw", Keyboard.KEY_F1, category);

	public static KeyBinding abilityCycle = new KeyBinding(category + ".ability", -99, category);
	public static KeyBinding abilityAlt = new KeyBinding(category + ".abilityAlt", Keyboard.KEY_LMENU, category);
	public static KeyBinding copyToolAlt = new KeyBinding(category + ".copyToolAlt", Keyboard.KEY_LMENU, category);
	public static KeyBinding gunSecondaryKey = new KeyBinding(category + ".gunSecondary", -99, category);
	public static KeyBinding gunTertiaryKey = new KeyBinding(category + ".gunTertitary", -98, category);
	
	public static void register() {
        ClientRegistry.registerKeyBinding(calculatorKey);
		ClientRegistry.registerKeyBinding(jetpackKey);
		ClientRegistry.registerKeyBinding(hudKey);
		ClientRegistry.registerKeyBinding(reloadKey);
		ClientRegistry.registerKeyBinding(dashKey);

		ClientRegistry.registerKeyBinding(gunSecondaryKey);
		ClientRegistry.registerKeyBinding(gunTertiaryKey);

		ClientRegistry.registerKeyBinding(craneUpKey);
		ClientRegistry.registerKeyBinding(craneDownKey);
		ClientRegistry.registerKeyBinding(craneLeftKey);
		ClientRegistry.registerKeyBinding(craneRightKey);
		ClientRegistry.registerKeyBinding(craneLoadKey);
		ClientRegistry.registerKeyBinding(abilityCycle);
		ClientRegistry.registerKeyBinding(abilityAlt);
		ClientRegistry.registerKeyBinding(qmaw);
	}

	@SubscribeEvent
	public void keyEvent(KeyInputEvent event) {

		/// OVERLAP HANDLING ///
		handleOverlap(Keyboard.getEventKeyState(), Keyboard.getEventKey());

		/// KEYBIND PROPS ///
		handleProps(Keyboard.getEventKeyState(), Keyboard.getEventKey());

        /// CALCULATOR ///
        if(calculatorKey.isPressed()) {
            MainRegistry.proxy.me().closeScreen();
            FMLCommonHandler.instance().showGuiScreen(new GUICalculator());
        }
	}

	/**
	 * Shitty hack: Keybinds fire before minecraft checks right click on block, which means the tool cycle keybind would fire too.
	 * If cycle collides with right click and a block is being used, cancel the keybind.
	 * @param event
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void postClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase != TickEvent.Phase.END) return;
		EntityPlayer player = MainRegistry.proxy.me();
		if(player == null) return;
		if(player.world == null) return;

		HbmCapability.IHBMData props = HbmCapability.getData(player);

		// in theory, this should do the same keybind crap as the main one, but at the end of the client tick, fixing the issue
		// of detecting when a block is being interacted with
		// in practice, this shit doesn't fucking work. detection fails when the click is sub one tick long
		if(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode() == abilityCycle.getKeyCode()) {
			boolean last = props.getKeyPressed(EnumKeybind.ABILITY_CYCLE);
			boolean current = abilityCycle.pressed;

			if(last != current) {
				PacketDispatcher.wrapper.sendToServer(new KeybindPacket(EnumKeybind.ABILITY_CYCLE, current));
				props.setKeyPressed(EnumKeybind.ABILITY_CYCLE, current);
				onPressedClient(player, EnumKeybind.ABILITY_CYCLE, current);
			}
		}

		if(!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemGunBaseNT
				&& props.getKeyPressed(EnumKeybind.GUN_PRIMARY)) {
			Minecraft mc = Minecraft.getMinecraft();
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
			mc.leftClickCounter = 2;
			if(mc.playerController != null) {
				mc.playerController.resetBlockRemoving();
			}
		}
	}
	
	public static enum EnumKeybind {
		JETPACK,
		TOGGLE_JETPACK,
		TOGGLE_HEAD,
		RELOAD,
		DASH,
		CRANE_UP,
		CRANE_DOWN,
		CRANE_LEFT,
		CRANE_RIGHT,
		CRANE_LOAD,
		ABILITY_CYCLE,
		ABILITY_ALT,
		TOOL_ALT,
		GUN_PRIMARY,
		GUN_SECONDARY,
		GUN_TERTIARY;

        public static final EnumKeybind[] VALUES = values();
	}

	/** Handles keybind overlap. Make sure this runs first before referencing the keybinds set by the extprops */
	public static void handleOverlap(boolean state, int keyCode) {
		Minecraft mc = Minecraft.getMinecraft();
		if(GeneralConfig.enableKeybindOverlap && (mc.currentScreen == null || mc.currentScreen.allowUserInput)) {
            KeyBindingMap HASH;
            try {
                HASH = (KeyBindingMap) hashHandle.invokeExact();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

			//if anything errors here, run ./gradlew clean setupDecompWorkSpace
			for (KeyBinding key : KeyBinding.KEYBIND_ARRAY.values()) {
				if (key.getKeyCode() != keyCode || key.getKeyCode() == 0) continue;
				if (!key.getKeyConflictContext().isActive()) continue;

				KeyModifier mod = key.getKeyModifier();
				if (mod != KeyModifier.NONE && !mod.isActive()) continue;
				if (HASH.lookupActive(key.getKeyCode()) == key) continue;

				key.pressed = state;
				if (state && key.pressTime == 0) {
					key.pressTime = 1;
				}
			}

			/// GUN HANDLING ///
			boolean gunKey = keyCode == mc.gameSettings.keyBindAttack.getKeyCode() ||
					keyCode == HbmKeybinds.gunSecondaryKey.getKeyCode() ||
					keyCode == HbmKeybinds.gunTertiaryKey.getKeyCode() || keyCode == HbmKeybinds.reloadKey.getKeyCode();

			EntityPlayer player = mc.player;

			if(!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemGunBaseNT) {

				/* Shoot in favor of attacking */
				if(gunKey && keyCode == mc.gameSettings.keyBindAttack.getKeyCode()) {
					mc.gameSettings.keyBindAttack.pressed = false;
					mc.gameSettings.keyBindAttack.pressTime = 0;
				}

				/* Shoot in favor of interacting */
				/*if(gunKey && keyCode == mc.gameSettings.keyBindUseItem.getKeyCode()) {
					mc.gameSettings.keyBindUseItem.pressed = false;
					mc.gameSettings.keyBindUseItem.pressTime = 0;
				}*/

				/* Scope in favor of picking */
				if(gunKey && keyCode == mc.gameSettings.keyBindPickBlock.getKeyCode()) {
					mc.gameSettings.keyBindPickBlock.pressed = false;
					mc.gameSettings.keyBindPickBlock.pressTime = 0;
				}
			}
		}
	}

	public static void handleProps(boolean state, int keyCode) {

		/// KEYBIND PROPS ///
		EntityPlayer player = MainRegistry.proxy.me();
		HbmCapability.IHBMData props = HbmCapability.getData(player);

		for(EnumKeybind key : EnumKeybind.VALUES) {
			boolean last = props.getKeyPressed(key);
			boolean current = MainRegistry.proxy.getIsKeyPressed(key);

			if(last != current) {

				/// ABILITY HANDLING ///
				if(key == EnumKeybind.ABILITY_CYCLE && Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode() == abilityCycle.getKeyCode()) continue;

				props.setKeyPressed(key, current);
				PacketDispatcher.wrapper.sendToServer(new KeybindPacket(key, current));
				onPressedClient(player, key, current);
			}
		}
	}

	public static void onPressedClient(EntityPlayer player, EnumKeybind key, boolean state) {
		// ITEM HANDLING
		ItemStack held = player.getHeldItemMainhand();
		if(!held.isEmpty() && held.getItem() instanceof IKeybindReceiver rec) {
			if(rec.canHandleKeybind(player, held, key)) rec.handleKeybindClient(player, held, key, state);
		}
	}
}
