package in.adi.groovy.adidsl.impl.midi

import groovy.util.logging.Log4j2
import in.adi.groovy.adidsl.fw.DSLInit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import javax.sound.midi.Instrument
import javax.sound.midi.MidiChannel
import javax.sound.midi.MidiEvent
import javax.sound.midi.MidiSystem
import javax.sound.midi.Sequence
import javax.sound.midi.Sequencer
import javax.sound.midi.ShortMessage
import javax.sound.midi.Synthesizer
import javax.sound.midi.Track

class MusicDSL_Init implements DSLInit
{
	@Override
	void compilerConfiguration(CompilerConfiguration compilerConfiguration, ImportCustomizer imports)
	{
		imports.addStaticStars(SoundDSL.name)
		imports.addStaticStars(InstrumentName.name)
	}

	@Override
	void init()
	{

	}
}

class SoundDSL
{
	static void cplay(long oneNoteDuration, Note... notes)
	{
		cplay(InstrumentName.violin2, oneNoteDuration, notes)
	}
	
	static void cplay(Instrument instrument, long oneNoteDuration, Note... notes)
	{
		def ch = musicch()
		ch.instrument(instrument)
		ch.play(oneNoteDuration, notes)
	}
	
	static void cplay(Note... notes)
	{
		musicch().play(notes)	
	}
	
	static void tplay(long oneNoteDuration, Note... notes)
	{
		def track = musictrack()
		track.play(oneNoteDuration, notes)
		track.music.start()
	}
	
	static void tplay(Note... notes)
	{
		musictrack().play(notes)	
	}
	
	static SoundChannel musicch(int channelNo = 0)
	{
		return music().channel(channelNo)
	}
	
	static SoundTrack musictrack()
	{
		return music().track()
	}
	
	static Music music(Closure closure = {})
	{
		Music music = new Music()

		closure.delegate = music
		closure.resolveStrategy = Closure.DELEGATE_ONLY

		closure()

		return music
	}

	static class Music
	{
		static Synthesizer synthesizer
		Sequencer sequencer

		static 
		{
			synthesizer = MidiSystem.getSynthesizer()
			synthesizer.open()
		}
		
		Music()
		{
			sequencer = MidiSystem.getSequencer()
		}

		SoundChannel channel(int channelNo, Closure closure = {})
		{
			MidiChannel channel = synthesizer.getChannels()[channelNo]
			SoundChannel ch = new SoundChannel(channel, this)

			closure.delegate = ch
			closure.resolveStrategy = Closure.DELEGATE_ONLY

			closure()
			
			return ch
		}

		SoundTrack track(Closure closure = {})
		{
			def seq = new Sequence(Sequence.PPQ, 4)
			sequencer.setSequence(seq)
			sequencer.open()
			SoundTrack track = new SoundTrack(this, seq.createTrack())
			closure.delegate = seq
			closure.resolveStrategy = Closure.DELEGATE_ONLY
			closure()
			
			return track
		}
		
		void start()
		{
			if (sequencer == null)
				return
			
			println 'seq start'
			
			sequencer.start()
			sleep(10000)
			sequencer.stop()
		}
	}

	@Log4j2
	static class SoundTrack
	{
		private Music music
		private Track seqtrack
		private long oneNoteDuration = 1000

		SoundTrack(Music music, Track seqtrack)
		{
			this.music = music
			this.seqtrack = seqtrack
		}

		void play(long oneNoteDuration, Note... notes)
		{
			this.oneNoteDuration = oneNoteDuration
			play notes
		}
		
		void play(Note... notes)
		{
			notes.each {play it}
		}
		
		void play(Note note)
		{
			log.trace('adding note : {}', note)
			ShortMessage on = new ShortMessage(ShortMessage.NOTE_ON, note.val, 100)
			MidiEvent one = new MidiEvent(on, 1)
			seqtrack.add(one)

			ShortMessage off = new ShortMessage(ShortMessage.NOTE_OFF, note.val, 100)
			MidiEvent offe = new MidiEvent(off, 16)
			seqtrack.add(offe)
		}
	}

	@Log4j2
	static class SoundChannel
	{
		private MidiChannel channel

		private Music music

		private long oneNoteDuration = 1000
		
		SoundChannel(MidiChannel channel, Music music)
		{
			this.channel = channel
			this.music = music
		}

		void oneNoteDuration(long oneNoteDuration)
		{
			log.trace("Setting oneNoteDuration = {}", oneNoteDuration)
			this.oneNoteDuration = oneNoteDuration
		}
		
		void instrument(Instrument ins)
		{
			log.trace('Setting instrument : {}', ins)
			channel.programChange(ins.patch.bank, ins.patch.program)
		}

		void play(Instrument ins, long oneNoteDuration, Note... notes)
		{
			instrument(ins)
			play(oneNoteDuration, notes)
		}
		
		void play(long oneNoteDuration1, Note... notes)
		{
			oneNoteDuration(oneNoteDuration1)
			play notes
		}
		
		void play(Note... notes)
		{
			notes.each {play it}
		}
		
		void play(Note note)
		{
			play(note, oneNoteDuration)
		}
		
		void play(Note note, long thisOneNoteDuration)
		{
			log.trace('playing : {}', note)
			channel.noteOn(note.val, 10000)
			sleep(thisOneNoteDuration * note.length)
			channel.noteOff(note.val)
		}
	}
}

class InstrumentName
{
	static final Instrument[] instruments = SoundDSL.Music.synthesizer.getLoadedInstruments()
	static Instrument violin1 = instruments[40]
	static Instrument violin2 = instruments[144]
	static Instrument flute = instruments[73]
	static Instrument panflute = instruments[75]
	static Instrument mandolin = instruments[157]
	
	
	static Instrument tom808 = instruments[155]

	static Instrument drum = instruments[153]
	
	
	static Instrument ins(int number)
	{
		return instruments[number]
	}
}

class Note
{
	int val

	String label
	boolean lengthSet = false
	int length = 1

	Note(int val, String label)
	{
		this.val = val
		this.label = label
	}

	Note getAt(int times)
	{
		if (!lengthSet)
		{
			length = times
			lengthSet = true
		}
		else
		{
			length += times
		}

		return this
	}

	@Override
	String toString()
	{
		return label + 'x' + length
	}
}
