package com.hbm.tileentity.network;

import com.hbm.interfaces.NotableComments;
import com.hbm.util.NoteBuilder;
import com.hbm.util.NoteBuilder.Instrument;
import com.hbm.util.NoteBuilder.Note;
import com.hbm.util.NoteBuilder.Octave;
import com.hbm.util.Tuple.Pair;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map.Entry;

public class RTTYSystem {

	/** Public frequency band for reading purposes, delayed by one tick */
	public static HashMap<Pair<World, String>, RTTYChannel> broadcast = new HashMap();
	/** New message queue for writing, gets written into readable Map later on */
	public static HashMap<Pair<World, String>, Object> newMessages = new HashMap();

	/** Pushes a new signal to be used next tick. Only the last signal pushed will be used, unless both the existing and new signal parse as numbers, in which case they are summed. */
	public static void broadcast(World world, String channelName, Object signal) {
		Pair identifier = new Pair(world, channelName);

		if(NumberUtils.isNumber("" + signal) && newMessages.containsKey(identifier)) {
			Object existing = newMessages.get(identifier);
			if(NumberUtils.isNumber("" + existing)) {
				try {
					long first = Long.parseLong("" + signal);
					long second = Long.parseLong("" + existing);
					newMessages.put(identifier, "" + (first + second));
					return;
				} catch(Exception ex) { }
			}
		}

		newMessages.put(identifier, signal);
	}

	/** Returns the RTTY channel with that name, or null */
	public static RTTYChannel listen(World world, String channelName) {
		RTTYChannel channel = broadcast.get(new Pair(world, channelName));
		return channel;
	}

	/** Moves all new messages to the broadcast map, adding the appropriate timestamp and clearing the new message queue */
	public static void updateBroadcastQueue() {

		for(Entry<Pair<World, String>, Object> worldEntry : newMessages.entrySet()) {
			Pair<World, String> identifier = worldEntry.getKey();
			Object lastSignal = worldEntry.getValue();

			RTTYChannel channel = new RTTYChannel();
			channel.timeStamp = identifier.getKey().getTotalWorldTime();
			channel.signal = lastSignal;

			broadcast.put(identifier, channel);
		}

		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server != null) {
			for(WorldServer world : server.worlds) {
				long time = world.getTotalWorldTime();
				Object signal = TEST_SENDER_MELODY[(int) (time % TEST_SENDER_MELODY.length)];

				RTTYChannel chan = new RTTYChannel();
				chan.timeStamp = time;
				chan.signal = signal;
				broadcast.put(new Pair(world, "2012-08-06"), chan);
			}
		}

		newMessages.clear();
	}

	@NotableComments
	public static class RTTYChannel {
		public long timeStamp = -1; //the totalWorldTime at the time of publishing, happens in the server tick event's PRE-phase. the publishing timestamp is that same number minus one
		public Object signal; // a signal can be anything, a number, an encoded string, an entire blue whale, Steve from accounting, the concept of death, 7492 hot dogs, etc.
	}

	/* Special objects for signifying specific signals to be used with RTTY machines (or telex) */
	public static enum RTTYSpecialSignal {
		BEGIN_TTY,		//start a new message block
		STOP_TTY,		//end the message block
		PRINT_BUFFER	//print message, literally, it makes a paper printout
	}

	/* Song of Storms at 300 BPM — precomputed once, indexed by (worldTime % length). Unset slots default to "" so idle ticks still broadcast, matching upstream. */
	private static final Object[] TEST_SENDER_MELODY;
	static {
		int tempo = 4;
		TEST_SENDER_MELODY = new Object[tempo * 160];
		java.util.Arrays.fill(TEST_SENDER_MELODY, "");

		Instrument flute = Instrument.PIANO;
		Instrument accordion = Instrument.BASSGUITAR;

		TEST_SENDER_MELODY[tempo * 0] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 2] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 4] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

		TEST_SENDER_MELODY[tempo * 6] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 8] = NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

		TEST_SENDER_MELODY[tempo * 12] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 14] = NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 16] = NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 18] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 20] = NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

		TEST_SENDER_MELODY[tempo * 24] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 26] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 28] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

		TEST_SENDER_MELODY[tempo * 30] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 32] = NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

		TEST_SENDER_MELODY[tempo * 36] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 38] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 40] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

		TEST_SENDER_MELODY[tempo * 42] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 44] = NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

		TEST_SENDER_MELODY[tempo * 48] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 50] = NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 52] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(flute, Note.D, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 54] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW);

		TEST_SENDER_MELODY[tempo * 56] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 58] = NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 60] = NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).add(flute, Note.D, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 64] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(flute, Note.E, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 66] = NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 67] = NoteBuilder.start().add(flute, Note.F, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 68] = NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).add(flute, Note.E, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 69] = NoteBuilder.start().add(flute, Note.F, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 70] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.E, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 71] = NoteBuilder.start().add(flute, Note.B, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 72] = NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).add(flute, Note.A, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 76] = NoteBuilder.start().add(accordion, Note.G, Octave.LOW).add(flute, Note.A, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 78] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 80] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.F, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 81] = NoteBuilder.start().add(flute, Note.G, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 82] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(flute, Note.A, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 84] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

		TEST_SENDER_MELODY[tempo * 88] = NoteBuilder.start().add(accordion, Note.G, Octave.LOW).add(flute, Note.A, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 90] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 92] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.F, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 93] = NoteBuilder.start().add(accordion, Note.B, Octave.MID).add(flute, Note.G, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 94] = NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(flute, Note.E, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 96] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

		TEST_SENDER_MELODY[tempo * 100] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 101] = NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 102] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 104] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 106] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 107] = NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 108] = NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 112] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(flute, Note.E, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 114] = NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 115] = NoteBuilder.start().add(flute, Note.F, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 116] = NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).add(flute, Note.E, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 117] = NoteBuilder.start().add(flute, Note.F, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 118] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.E, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 119] = NoteBuilder.start().add(flute, Note.C, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 120] = NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.A, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 124] = NoteBuilder.start().add(accordion, Note.G, Octave.LOW).add(flute, Note.A, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 126] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).add(flute, Note.D, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 128] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).add(flute, Note.F, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 129] = NoteBuilder.start().add(flute, Note.G, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 130] = NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(flute, Note.A, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 132] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(accordion, Note.E, Octave.LOW).add(accordion, Note.A, Octave.MID).add(accordion, Note.G, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 134] = NoteBuilder.start().add(flute, Note.A, Octave.MID).end();

		TEST_SENDER_MELODY[tempo * 136] = NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
		TEST_SENDER_MELODY[tempo * 138] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).end();
		TEST_SENDER_MELODY[tempo * 140] = NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).end();
	}
}
