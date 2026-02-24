package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.ModItems;
import com.hbm.items.armor.IPAMelee;
import com.hbm.items.armor.IPARanged;
import com.hbm.items.armor.IPAWeaponsProvider;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.misc.RenderScreenOverlay;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class XFactoryPA {

    public static void init() {

        ModItems.gun_pa_melee = new ItemGunPA(ItemGunBaseNT.WeaponQuality.UTILITY, "gun_pa_melee", new GunConfig()
                .draw(10).crosshair(RenderScreenOverlay.Crosshair.NONE)
                .rec(new Receiver(0))
                .pp(LAMBDA_CLICK_MELEE_PRIMARY).ps(LAMBDA_CLICK_MELEE_SENONDARY).decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                .anim(LAMBDA_MELEE_ANIMS).orchestra(ORCHESTRA)
        );

        ModItems.gun_pa_ranged = new ItemGunPA(ItemGunBaseNT.WeaponQuality.UTILITY, "gun_pa_ranged", new GunConfig()
                .draw(0).crosshair(RenderScreenOverlay.Crosshair.CROSS)
                .rec(new Receiver(0))
                .pp(LAMBDA_CLICK_RANGED_PRIMARY).ps(LAMBDA_CLICK_RANGED_SENONDARY).decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
        ).setFull3D();
    }

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> ORCHESTRA = (stack, ctx) -> {
        IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
        if(component != null) component.orchestra(stack, ctx);
    };

    public static BiFunction<ItemStack, HbmAnimationsSedna.GunAnimation, BusAnimationSedna> LAMBDA_MELEE_ANIMS = (stack, type) -> {
        IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
        if(component != null) return component.playAnim(stack, type);
        return null;
    };

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_CLICK_MELEE_PRIMARY = (stack, ctx) -> {
        IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
        if(component != null) component.clickPrimary(stack, ctx);
    };
    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_CLICK_MELEE_SENONDARY = (stack, ctx) -> {
        IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
        if(component != null) component.clickSecondary(stack, ctx);
    };

    public static void doSwing(ItemStack stack, ItemGunBaseNT.LambdaContext ctx, HbmAnimationsSedna.GunAnimation anim, int cooldown) {

        EntityPlayer player = ctx.getPlayer();
        int index = ctx.configIndex;
        ItemGunBaseNT.GunState state = ItemGunBaseNT.getState(stack, index);

        if(state == ItemGunBaseNT.GunState.IDLE) {
            ItemGunBaseNT.playAnimation(player, stack, anim, ctx.configIndex);
            ItemGunBaseNT.setState(stack, index, ItemGunBaseNT.GunState.COOLDOWN);
            ItemGunBaseNT.setTimer(stack, index, cooldown);
        }
    }

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_CLICK_RANGED_PRIMARY = (stack, ctx) -> {
        IPARanged component = IPAWeaponsProvider.getRangedComponentClient();
        if(component != null) component.clickPrimary(stack, ctx);
    };
    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_CLICK_RANGED_SENONDARY = (stack, ctx) -> {
        IPARanged component = IPAWeaponsProvider.getRangedComponentClient();
        if(component != null) component.clickSecondary(stack, ctx);
    };

    public static class ItemGunPA extends ItemGunBaseNT {

        public ItemGunPA(WeaponQuality quality, String s, GunConfig... cfg) {
            super(quality, s, cfg);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) { }
    }
}
