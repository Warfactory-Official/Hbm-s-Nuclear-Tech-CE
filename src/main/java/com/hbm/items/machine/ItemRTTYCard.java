package com.hbm.items.machine;

import com.hbm.items.ItemBakedBase;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.tileentity.network.RTTYSystem.RTTYChannel;
import com.hbm.util.I18nUtil;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.AbstractValue;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Tier 2 OpenComputers card for RTTY channels. OC types stay in the nested classes, so this one loads without OpenComputers. */
public class ItemRTTYCard extends ItemBakedBase {

    //torches and pagers only take 10 characters, this is just a sanity cap
    public static final int MAX_CHANNEL_LENGTH = 64;

    public ItemRTTYCard(String s) {
        super(s);
        setMaxStackSize(1);
    }

    //useless without OpenComputers, so hide it from creative and JEI
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (Loader.isModLoaded("opencomputers")) super.getSubItems(tab, items);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        list.add(I18nUtil.resolveKey(this.getTranslationKey() + ".desc"));

        if (stack.hasTagCompound()) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt.hasKey("oc:data")) {
                NBTTagCompound data = nbt.getCompoundTag("oc:data");
                if (data.hasKey("node")) {
                    NBTTagCompound node = data.getCompoundTag("node");
                    if (node.hasKey("address")) {
                        list.add(TextFormatting.DARK_GRAY + node.getString("address"));
                    }
                }
            }
        }

        list.add(TextFormatting.DARK_GRAY + "Tier 2");
    }

    //OC signals can only carry strings, numbers and booleans, anything else becomes a string
    static Object coerceSignal(Object signal) {
        if (signal == null || signal instanceof String || signal instanceof Number || signal instanceof Boolean) return signal;
        return String.valueOf(signal);
    }

    public static class Driver extends li.cil.oc.api.prefab.DriverItem {

        private static final int TIER_2 = 1;

        public Driver(Item item) {
            super(new ItemStack(item));
        }

        @Override
        public String slot(ItemStack stack) {
            return Slot.Card;
        }

        @Override
        public int tier(ItemStack stack) {
            return TIER_2;
        }

        @Override
        public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
            return new Environment(host);
        }
    }

    public static class Environment extends AbstractManagedEnvironment {

        private static final String NBT_EVENTS = "boundEvents";

        private final ConcurrentHashMap<String, Long> boundEvents = new ConcurrentHashMap<>();
        private final AtomicInteger eventCount = new AtomicInteger(0);
        private final EnvironmentHost host;

        public Environment(EnvironmentHost host) {
            this.host = host;

            setNode(Network.newNode(this, Visibility.Neighbors)
                    .withComponent("rtty")
                    .create());
        }

        public World world() {
            return this.host.world();
        }

        @Callback(direct = true, doc = "function(channel:string):handle -- Opens a handle to the given RTTY channel")
        public Object[] open(Context context, Arguments args) {
            String channel = parseChannel(args);
            String cardAddress = node().address();
            RTTYChanHandle handle = new RTTYChanHandle(cardAddress, channel);

            return new Object[]{handle};
        }

        @Callback(direct = true, doc = "function(channel:string) -- Binds an OS event to the given RTTY channel")
        public Object[] bindEvent(Context context, Arguments args) {
            String channel = parseChannel(args);

            if (this.boundEvents.put(channel, -1L) == null) {
                this.eventCount.incrementAndGet();
            }

            return new Object[0];
        }

        @Callback(direct = true, doc = "function(channel:string) -- Unbinds an OS event from the given RTTY channel")
        public Object[] unbindEvent(Context context, Arguments args) {
            String channel = parseChannel(args);

            if (this.boundEvents.remove(channel) != null) {
                this.eventCount.decrementAndGet();
            }

            return new Object[0];
        }

        @Override
        public void update() {
            if (this.eventCount.get() == 0) return;

            World world = world();

            for (Entry<String, Long> event : this.boundEvents.entrySet()) {
                RTTYChannel chan = RTTYSystem.listen(world, event.getKey());

                if (chan == null) continue;
                if (chan.timeStamp <= event.getValue()) continue;

                this.boundEvents.replace(event.getKey(), chan.timeStamp);
                node().sendToReachable("computer.signal", event.getKey(), coerceSignal(chan.signal));
            }
        }

        @Override
        public boolean canUpdate() {
            return true;
        }

        @Override
        public void save(NBTTagCompound nbt) {
            super.save(nbt);

            if (this.eventCount.get() == 0) return;

            NBTTagList eventsTag = new NBTTagList();
            for (String event : this.boundEvents.keySet()) {
                eventsTag.appendTag(new NBTTagString(event));
            }

            nbt.setTag(NBT_EVENTS, eventsTag);
        }

        @Override
        public void load(NBTTagCompound nbt) {
            super.load(nbt);

            if (!nbt.hasKey(NBT_EVENTS)) return;

            NBTTagList eventsTag = nbt.getTagList(NBT_EVENTS, 8);

            for (int i = 0; i < eventsTag.tagCount(); i++) {
                String event = eventsTag.getStringTagAt(i);

                if (this.boundEvents.put(event, -1L) == null) {
                    this.eventCount.incrementAndGet();
                }
            }
        }

        private static String parseChannel(Arguments args) {
            String channel = args.checkString(0);

            if (channel.isEmpty()) {
                throw new IllegalArgumentException("bad argument #1 (channel name expected, got no value)");
            }
            if (channel.length() > MAX_CHANNEL_LENGTH) {
                throw new IllegalArgumentException("bad argument #1 (channel name too long, max " + MAX_CHANNEL_LENGTH + ")");
            }

            return channel;
        }
    }

    public static class RTTYChanHandle extends AbstractValue {

        private static final String NBT_ADDRESS = "cardAddress";
        private static final String NBT_CHANNEL = "channel";

        private String cardAddress;
        private String channel;
        private long lastRead = -1;

        // Required for deserialization
        public RTTYChanHandle() {
        }

        public RTTYChanHandle(String cardAddress, String channel) {
            this.cardAddress = cardAddress;
            this.channel = channel;
        }

        @Callback(direct = true, doc = "function():string -- Returns the channel name this handle is bound to")
        public Object[] channel(Context context, Arguments args) {
            return new Object[]{this.channel};
        }

        @Callback(direct = false, doc = "function():any, boolean -- Reads the current signal from this handle's channel. Takes one minecraft tick to execute")
        public Object[] readSync(Context context, Arguments args) {
            return read(context, args);
        }

        @Callback(direct = false, doc = "function(signal:any) -- Writes the given signal to this handle's channel. Takes one minecraft tick to execute")
        public Object[] writeSync(Context context, Arguments args) {
            return write(context, args);
        }

        @Callback(direct = true, doc = "function():any, boolean -- Reads the current signal from this handle's channel")
        public Object[] read(Context context, Arguments args) {
            World world = getWorld(context);
            RTTYChannel chan = RTTYSystem.listen(world, this.channel);

            if (chan == null) return new Object[]{null, false};

            boolean isNew = chan.timeStamp > this.lastRead;
            this.lastRead = chan.timeStamp;

            return new Object[]{coerceSignal(chan.signal), isNew};
        }

        @Callback(direct = true, doc = "function(signal:any) -- Writes the given signal to this handle's channel")
        public Object[] write(Context context, Arguments args) {
            Object signal = args.isString(0) ? args.checkString(0) : args.checkAny(0);
            World world = getWorld(context);

            RTTYSystem.broadcast(world, this.channel, coerceSignal(signal));

            return new Object[0];
        }

        @Override
        public void save(NBTTagCompound nbt) {
            nbt.setString(NBT_ADDRESS, this.cardAddress);
            nbt.setString(NBT_CHANNEL, this.channel);
        }

        @Override
        public void load(NBTTagCompound nbt) {
            this.cardAddress = nbt.getString(NBT_ADDRESS);
            this.channel = nbt.getString(NBT_CHANNEL);
        }

        private World getWorld(Context context) {
            Node node = context.node().network().node(this.cardAddress);
            if (node == null) throw new IllegalArgumentException("no such component");

            return ((Environment) node.host()).world();
        }
    }
}
