package com.hbm.blocks;

import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

/**
 * ABI note for mod interfaces that mirror inherited Minecraft members.
 * <p>
 * This interface exposes {@link Block#addInformation(ItemStack, World, List, ITooltipFlag)} only
 * as an override contract for blocks. In the dev environment the inherited block member and this
 * interface member coincide at the source level as {@code addInformation}, but in the
 * reobfuscated runtime the block override is renamed with Minecraft while the mod interface member
 * remains literal. Source-level aliasing here is therefore not a stable binary contract.
 * <p>
 * Consequences:
 * <ul>
 * <li>Never invoke {@code addInformation} through {@code ITooltipProvider}; preserve a static type
 * of {@link Block} or one of its subclasses so the call remains a virtual dispatch on the block
 * hierarchy rather than an {@code invokeinterface} site that can fail with
 * {@code AbstractMethodError}.</li>
 * <li>Mixins targeting this inherited Minecraft method must allow remapping. Marking such an
 * injector with {@code remap = false} freezes the dev name {@code addInformation} instead of the
 * runtime name inherited from {@link Block}, so target lookup succeeds in dev and fails against
 * the reobfuscated jar.</li>
 * <li>Do not rely on an interface default method to satisfy an abstract method declared in a
 * superclass.</li>
 * <li>Do not declare methods such as {@code getWorld()} or {@code getPos()} in a mod interface and
 * assume an inherited vanilla superclass implementation will satisfy them merely because the names
 * coincide in dev.</li>
 * </ul>
 *
 * Update: mixin is retarded. im removing these two interface methods.
 */
public interface ITooltipProvider {

    default void addStandardInfo(List<String> list) {

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            for (String s : I18nUtil.resolveKeyArray(((Block) this).getTranslationKey() + ".desc"))
                list.add(TextFormatting.YELLOW + s);
        } else {
            list.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "Hold <" +
                    TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "LSHIFT" +
                    TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "> to display more info");
        }
    }


    default EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.COMMON;
    }
}
